package com.crc.sensorplatform.base

class Constants {
    companion object {
        var strDeviceName = "Not Connected"
        var strDeviceAddress = "Not Connected"

        var strReceivedData = ""

        var dLastAccel : Double = 0.0

        var nSelUser = 0
        var nSelDB = 0

        var latitude : Double = 0.0
        var longitude : Double = 0.0
        var oldLatitude : Double = 0.0
        var oldLongitude : Double = 0.0

        var strBodyTemp : String                                = "0"
        var strTemp : String                                    = "0"
        var strHumi : String                                    = "0"
        var strStep : Int                                       = 0
        var strDistance : Double                                = 0.0
        var strSpo2 : String                                    = "0"
        var strHbA1c : String                                   = "0"
        var strHeartRate : String                               = "0"

        val INDEX_BODYTEMPERATURE : Int                         = 27
        val INDEX_TEMPERATURE : Int                             = 8
        val INDEX_HUMIDITY : Int                                = 9
        val INDEX_STEP : Int                                    = 28
        val INDEX_DISTANCE : Int                                = 29
        val INDEX_SPO2 : Int                                    = 12
        val INDEX_HBA1C : Int                                   = 26
        val INDEX_HEARTRATE : Int                               = 2

        // Address
        val MODULE_ADDRESS_OXIMETRY0                             = "00:00:00:00:00:00"
        val MODULE_ADDRESS_OXIMETRY1                             = "50:02:91:A3:53:C6"
        val MODULE_ADDRESS_OXIMETRY2                             = "50:02:91:A3:53:BE"
        val MODULE_ADDRESS_OXIMETRY3                             = "50:02:91:A3:53:D2"
        val MODULE_ADDRESS_OXIMETRY4                             = "50:02:91:A3:53:BE"

        val MODULE_ADDRESS_CHESTPOD0                             = "00:00:00:00:00:00"
        val MODULE_ADDRESS_CHESTPOD1                             = "D8:A0:1D:5C:71:D2"
        val MODULE_ADDRESS_CHESTPOD2                             = "D8:A0:1D:5C:0B:DA"
        val MODULE_ADDRESS_CHESTPOD3                             = "D8:A0:1D:5C:0C:0A"
        val MODULE_ADDRESS_CHESTPOD4                             = "D8:A0:1D:5A:BE:12"





        var strOximetryAddress                                  = MODULE_ADDRESS_OXIMETRY1
        var strChestPodAddress                                  = MODULE_ADDRESS_CHESTPOD1


        val MODULE_SERVICE_UUID_CHESTPOD                                = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
        val MODULE_CHARACTERISTIC_UUID_CHESTPOD                         = "beb5483e-36e1-4688-b7f5-ea07361b26a8"

        val SHARED_PREF_SEUPDATA : String                               = "setupData"
        var PREF_CHESTPOD_INDEX : String                                = "prefChestPodIndex"
        var PREF_SPO2_INDEX : String                                    = "prefSpO2Index"
        var PREF_USER_INDEX : String                                    = "prefUserIndex"
        var PREF_DB_INDEX : String                                    = "prefDBIndex"

        var nSelChestPod                                                = 0
        var nSelSpO2                                                    = 0

        val TIMEZONE = "Asia/Seoul"

        var OLD_CURRENT_TIME  : Long                                          = 0
        var NEW_CURRENT_TIME : Long                                            = 0
        val STANDARD_ONE_MINUTE : Long                                  = 1000 * 60
        val STANDARD_TEN_SECONDS : Long                                  = 1000 * 10

        var fAccelX : Float                                             = 0.0f
        var fAccelY : Float                                             = 0.0f
        var fAccelZ : Float                                             = 0.0f

    }
}