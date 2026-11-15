package com.svetomir.spendly.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface FrankfurterApi {

    @GET("v1/latest")
    suspend fun getLatestRates(@Query("base") base: String = "EUR"): RatesResponse
}
