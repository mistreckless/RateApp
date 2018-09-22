package com.revolut.data

import android.util.Log
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

interface RateRepository{

    fun getRates(): Single<Rates>
}

class RevolutRateRepository(private val apiService: RevolutApiService): RateRepository{
    override fun getRates(): Single<Rates> = apiService.getCurrencies()
        .map {
            it.rates[BASE_CURRENCY] = 1.0
            it.rates
        }
        .subscribeOn(Schedulers.io())

}

const val BASE_CURRENCY = "EUR"