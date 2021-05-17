package com.brightdrop.technical.assessment.ble.ble_mailbox_peripheral

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import java.util.*

object EP1MailboxProfile {
    val LOCKER_SERVICE_UUID: UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb")
    val CLIENT_CONFIG_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    val CHARACTERISTIC_LOCKER_UUID: UUID = UUID.fromString("31517c58-66bf-470c-b662-e352a6c80cba")

    fun createTimeService(): BluetoothGattService {
        val service = BluetoothGattService(
                LOCKER_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val locker = BluetoothGattCharacteristic(
                CHARACTERISTIC_LOCKER_UUID,
                PROPERTY_WRITE or PROPERTY_READ or PROPERTY_NOTIFY,
                PERMISSION_READ or PERMISSION_WRITE)

        val configDescriptor = BluetoothGattDescriptor(
                CLIENT_CONFIG_UUID,
                BluetoothGattDescriptor.PERMISSION_WRITE or BluetoothGattDescriptor.PERMISSION_READ)

        locker.addDescriptor(configDescriptor)

        service.addCharacteristic(locker)

        return service
    }
}
