package com.brightdrop.technical.assessment.ble.common

import java.util.*

object Constants {
    /** UUID of the Client Characteristic Configuration Descriptor (0x2902). */
    const val CCC_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805F9B34FB"
    val CLIENT_CONFIG_UUID: UUID = UUID.fromString(CCC_DESCRIPTOR_UUID)
    val LOCKER_SERVICE_UUID: UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb")
    val CHARACTERISTIC_LOCKER_UUID: UUID = UUID.fromString("31517c58-66bf-470c-b662-e352a6c80cba")
    val CHARACTERISTIC_AUTH_LOCKER_UUID: UUID = UUID.fromString("31517c59-66bf-470c-b662-e352a6c80cba")

    // states
    const val UNLOCKED = "UNLOCKED"
    const val LOCKED = "LOCKED"
    const val ACCESS_DENIED = "ACCESS DENIED"
    const val ACCESS_GRANTED = "ACCESS GRANTED"
    const val MAILBOX_LOCKED =  "MAILBOX LOCKED"
    const val MAILBOX_UNLOCKED =  "MAILBOX UNLOCKED"
}