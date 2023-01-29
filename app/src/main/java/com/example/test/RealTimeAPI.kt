package com.example.test

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RealTimeAPI {
    @GET("/{latitude}/{longitude}")
    suspend fun getResultsByLocation(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double
    ): Response<Array<RealTimeResult>>
}
