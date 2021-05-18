package com.brightdrop.technical.assessment.ble.ble_mailbox_central

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.brightdrop.technical.assessment.ble.ble_mailbox_central.ble.CHARACTERISTIC_AUTH_LOCKER_UUID
import com.brightdrop.technical.assessment.ble.ble_mailbox_central.ble.CHARACTERISTIC_LOCKER_UUID
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.brightdrop.technical.assessment.ble.ble_mailbox_central.ble.ConnectionEventListener
import com.brightdrop.technical.assessment.ble.ble_mailbox_central.ble.ConnectionManager
import kotlinx.android.synthetic.main.activity_ble_operations.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.alert
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


class BleOperationsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var device: BluetoothDevice
    private val dateFormatter = SimpleDateFormat("MMM d, HH:mm:ss", Locale.US)
    private var notifyingCharacteristics = mutableListOf<UUID>()
    private val authenticationFormat = "Authentication: %s"

    override fun onCreate(savedInstanceState: Bundle?) {
        ConnectionManager.registerListener(connectionEventListener)
        super.onCreate(savedInstanceState)
        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            ?: error("Missing BluetoothDevice from MainActivity!")

        setContentView(R.layout.activity_ble_operations)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            title = getString(R.string.ble_playground)
        }

        toggle_btn.setOnClickListener {
            CoroutineScope(IO).launch {
                if(ConnectionManager.authState) {
                    if(ConnectionManager.lockerState == getString(R.string.mailbox_locked)) {
                        ConnectionManager.writeLockerCharacteristic(device, "UNLOCKED")
                    } else {
                        ConnectionManager.writeLockerCharacteristic(device, "LOCKED")
                    }
                    delay(500)
                    ConnectionManager.readLockerCharacteristic(device)
                } else {
                    log("You are not auth!!!")
                }
            }
        }

        auth.setOnClickListener {
            val li = LayoutInflater.from(this@BleOperationsActivity) as LayoutInflater
            val promptsView : View  = li.inflate(R.layout.auth_dialog, null)
            val alertDialogBuilder = AlertDialog.Builder(
                this@BleOperationsActivity,
                R.style.AppTheme
            )
            alertDialogBuilder.setView(promptsView)

            val userInput = promptsView
                .findViewById<View>(R.id.editTextDialogUserInput) as EditText

            alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, id ->
                    ConnectionManager.writeAuthCharacteristic(device,
                        authenticationFormat.format(userInput.text.toString()))

                    CoroutineScope(IO).launch {
                        delay(500)
                        ConnectionManager.readAuthCharacteristic(device)
                    }

                    dialog.dismiss()
                }
                .setNegativeButton(
                    "Cancel"
                ) { dialog, id -> dialog.cancel() }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        val gpsTracker = GpsTracker(this@BleOperationsActivity)
        if (gpsTracker.canGetLocation()) {
            ConnectionManager.lon = gpsTracker.getLongitude()
            ConnectionManager.lat = gpsTracker.getLatitude()
        } else {
            gpsTracker.showSettingsAlert()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment_view) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onDestroy() {
        ConnectionManager.unregisterListener(connectionEventListener)
        ConnectionManager.teardownConnection(device)
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun log(message: String) {
        val formattedMessage = String.format("%s: %s", dateFormatter.format(Date()), message)
        runOnUiThread {
            val currentLogText = if (log_text_view.text.isEmpty()) {
                "Beginning of log."
            } else {
                log_text_view.text
            }
            log_text_view.text = "$currentLogText\n$formattedMessage"
            log_scroll_view.post { log_scroll_view.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onDisconnect = {
                runOnUiThread {
                    alert {
                        title = "Disconnected"
                        message = "Disconnected from device."
                        positiveButton("OK") { onBackPressed() }
                    }.show()
                }
            }

            onCharacteristicRead = { _, characteristic ->
                log("Read from ${characteristic.uuid}: ${String(characteristic.value)}")
                if( characteristic.uuid ==  CHARACTERISTIC_LOCKER_UUID ) {
                    ConnectionManager.lockerState = ConnectionManager.getLockerStatusButtonText(String(characteristic.value))
                    CoroutineScope(Main).launch {
                        toggle_btn.text = ConnectionManager.lockerState
                    }
                } else if( characteristic.uuid ==  CHARACTERISTIC_AUTH_LOCKER_UUID ) {
                    ConnectionManager.authState = ConnectionManager.getAuthStatus(String(characteristic.value))
                    CoroutineScope(Main).launch {
                        auth.visibility = View.GONE
                        toggle_btn.isEnabled = true
                    }
                }
            }

            onCharacteristicWrite = { _, characteristic ->
                log("Wrote to ${characteristic.uuid}: ${String(characteristic.value)}")
            }

            onMtuChanged = { _, mtu ->
                log("MTU updated to $mtu")
            }

            onCharacteristicChanged = { _, characteristic ->
                log("Value changed on ${characteristic.uuid}: ${String(characteristic.value)}")
            }

            onNotificationsEnabled = { _, characteristic ->
                log("Enabled notifications on ${characteristic.uuid}")
                notifyingCharacteristics.add(characteristic.uuid)
            }

            onNotificationsDisabled = { _, characteristic ->
                log("Disabled notifications on ${characteristic.uuid}")
                notifyingCharacteristics.remove(characteristic.uuid)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.addMarker(
            MarkerOptions()
                .position(LatLng(ConnectionManager.lat, ConnectionManager.lon)))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(ConnectionManager.lat, ConnectionManager.lon), 15f))
        googleMap?.uiSettings?.isScrollGesturesEnabled = false
    }
}