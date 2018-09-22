package com.revolut.core.repository

import com.revolut.core.data.Rates
import com.revolut.core.service.RevolutApiService
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

interface RateRepository {

    fun getRates(base: String): Single<RatesResult>
}

class RevolutRateRepository(private val apiService: RevolutApiService) :
        RateRepository {
    override fun getRates(base: String): Single<RatesResult> = apiService.getCurrencies(base)
        .map<RatesResult> {
            it.rates[base] = 1.0
            RatesLoaded(it.rates)
        }
        .onErrorReturn(::RatesError)
        .subscribeOn(Schedulers.io())

}

sealed class RatesResult
data class RatesLoaded(val rates: Rates) : RatesResult()
data class RatesError(val e: Throwable) : RatesResult()

