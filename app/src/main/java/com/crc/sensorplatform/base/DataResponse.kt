package com.crc.sensorplatform.base

import org.json.JSONObject

data class DataResponse (
    val statusCode: String,
//    val responseMessage: JSONObject
    val body: Map<String, String>
)