package com.crc.sensorplatform.base

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("sensor-platform-api-gw-dev") // replace with your API endpoint
    fun getData(@Body workout: WorkOut): Call<DataResponse>
}