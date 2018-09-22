package com.revolut.core.repository

import com.revolut.core.data.ExchangeRate
import com.revolut.core.service.RevolutApiService
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RateRepositoryTest {

    @Mock
    private lateinit var apiService: RevolutApiService

    private lateinit var rateRepository: RateRepository

    @Before
    fun setUp() {
        rateRepository = RevolutRateRepository(apiService)
    }

    @Test
    fun `test getRates when a rates are successfully loaded`() {
        val expectedPredict = RatesLoaded(mutableMapOf(BASE_CURRENCY to 1.0))

        BDDMockito.given(apiService.getCurrencies(BASE_CURRENCY))
            .willReturn(Single.just(ExchangeRate()))

        val observer = rateRepository.getRates(BASE_CURRENCY).test()

        observer.awaitCount(1).assertValue(expectedPredict)
    }

    @Test
    fun `test getRates when an error occurs`() {
        BDDMockito.given(apiService.getCurrencies(BASE_CURRENCY))
            .willReturn(Single.error(Exception("")))

        val observer = rateRepository.getRates(BASE_CURRENCY).test()

        observer.awaitCount(1).assertValue { it is RatesError }
    }

    companion object {
        const val BASE_CURRENCY = "EUR"
    }
}