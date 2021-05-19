package com.brightdrop.technical.assessment.ble.ble_mailbox_peripheral

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.brightdrop.technical.assessment.ble.common.Constants.CHARACTERISTIC_AUTH_LOCKER_UUID
import com.brightdrop.technical.assessment.ble.common.Constants.CHARACTERISTIC_LOCKER_UUID
import com.brightdrop.technical.assessment.ble.common.Constants.CLIENT_CONFIG_UUID
import com.brightdrop.technical.assessment.ble.common.Constants.LOCKER_SERVICE_UUID

object EP1MailboxProfile {
    fun createTimeService(): BluetoothGattService {
        val service = BluetoothGattService(
                LOCKER_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val configDescriptor = BluetoothGattDescriptor(
            CLIENT_CONFIG_UUID,
            BluetoothGattDescriptor.PERMISSION_WRITE or BluetoothGattDescriptor.PERMISSION_READ)

        val locker = BluetoothGattCharacteristic(
                CHARACTERISTIC_LOCKER_UUID,
                PROPERTY_WRITE or PROPERTY_READ or PROPERTY_NOTIFY,
                PERMISSION_READ or PERMISSION_WRITE)
        locker.addDescriptor(configDescriptor)

        val auth = BluetoothGattCharacteristic(
            CHARACTERISTIC_AUTH_LOCKER_UUID,
            PROPERTY_WRITE or PROPERTY_READ or PROPERTY_NOTIFY,
            PERMISSION_READ or PERMISSION_WRITE)
        auth.addDescriptor(configDescriptor)

        service.addCharacteristic(locker)
        service.addCharacteristic(auth)

        return service
    }
}
