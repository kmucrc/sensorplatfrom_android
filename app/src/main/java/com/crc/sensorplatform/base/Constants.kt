package com.crc.sensorplatform.base

class Constants {
    companion object {
        var strDeviceName = "Not Connected"
        var strDeviceAddress = "Not Connected"

        var strReceivedData = ""

        var nSelUser = 0


        var latitude : Double = 0.0
        var longitude : Double = 0.0

        // Address
        val MODULE_ADDRESS_OXIMETRY0                             = "00:00:00:00:00:00"
        val MODULE_ADDRESS_OXIMETRY1                             = "D8:A0:1D:5C:71:D2"
        val MODULE_ADDRESS_OXIMETRY2                             = "D8:A0:1D:5C:0B:DA"
        val MODULE_ADDRESS_OXIMETRY3                             = "D8:A0:1D:5C:0C:0A"
        val MODULE_ADDRESS_OXIMETRY4                             = "D8:A0:1D:5A:BE:12"

        val MODULE_ADDRESS_CHESTPOD0                             = "00:00:00:00:00:00"
        val MODULE_ADDRESS_CHESTPOD1                             = "50:02:91:A3:53:C6"
        val MODULE_ADDRESS_CHESTPOD2                             = "50:02:91:A3:53:BE"
        val MODULE_ADDRESS_CHESTPOD3                             = "50:02:91:A3:53:D2"
        val MODULE_ADDRESS_CHESTPOD4                             = "50:02:91:A3:53:BE"

        var strOximetryAddress                                  = MODULE_ADDRESS_OXIMETRY1
        var strChestPodAddress                                  = MODULE_ADDRESS_CHESTPOD1


        val MODULE_SERVICE_UUID_CHESTPOD                                = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
        val MODULE_CHARACTERISTIC_UUID_CHESTPOD                         = "beb5483e-36e1-4688-b7f5-ea07361b26a8"

        val SHARED_PREF_SEUPDATA : String                               = "setupData"
        var PREF_CHESTPOD_INDEX : String                                = "prefChestPodIndex"
        var PREF_SPO2_INDEX : String                                    = "prefSpO2Index"
        var PREF_USER_INDEX : String                                    = "prefUserIndex"

        var nSelChestPod                                                = 0
        var nSelSpO2                                                    = 0

        val TIMEZONE = "Asia/Seoul"
    }
}