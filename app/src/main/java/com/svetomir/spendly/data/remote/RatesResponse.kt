package com.svetomir.spendly.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class RatesResponse(
    val base: String,
    val date: String,
    val rates: Map<String, Double>,
)