package com.revolut.core.service

import com.revolut.core.data.ExchangeRate
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface RevolutApiService {
    @GET("/latest")
    fun getCurrencies(@Query("base") baseCurrency: String): Single<ExchangeRate>
}