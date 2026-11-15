package com.svetomir.spendly.data.repository

import com.svetomir.spendly.data.remote.RatesResponse

interface CurrencyRepository {
    suspend fun getRates(): Result<RatesResponse>
}
