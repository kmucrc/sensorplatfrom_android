package com.crc.sensorplatform.base

class Constants {
    companion object {
        var strDeviceName = "Not Connected"
        var strDeviceAddress = "Not Connected"

        val REQUEST_ENABLE_BT = 1

        // Address
        val MODULE_ADDRESS_OXIMETRY                             = "50:02:91:A3:53:BE"


        val MODULE_SERVICE_UUID_CHESTPOD                                = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
        val MODULE_CHARACTERISTIC_UUID_CHESTPOD                         = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
    }
}