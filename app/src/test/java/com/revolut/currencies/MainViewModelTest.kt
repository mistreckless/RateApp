package com.revolut.currencies

import com.revolut.core.repository.RateRepository
import com.revolut.core.repository.RatesError
import com.revolut.core.repository.RatesLoaded
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @Mock
    private lateinit var ratesRepository: RateRepository

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        viewModel = MainViewModel(ratesRepository)
        viewModel.startListen()
    }

    @After
    fun clear() {
        viewModel.stopListen()
    }

    @Test
    fun `test observeItemsState when an items are successfully loaded`() {
        val expectedPredict = StateLoaded

        BDDMockito.given(ratesRepository.getRates(MainViewModel.BASE_CURRENCY))
            .willReturn(Single.just(RatesLoaded(rates)))

        val observer = viewModel.observeItemsState().skip(1).take(1).test()

        observer.awaitCount(1).assertValue(expectedPredict)
    }

    @Test
    fun `test observeItemsState when an error occurs`() {
        val expectedPredict = StateError("")

        BDDMockito.given(ratesRepository.getRates(MainViewModel.BASE_CURRENCY))
            .willReturn(Single.just(RatesError(Throwable(""))))

        val observer = viewModel.observeItemsState().skip(1).take(1).test()

        observer.awaitCount(1).assertValue(expectedPredict)
    }

    @Test
    fun `test observeItemsState when an items currencies are changed`() {

        BDDMockito.given(ratesRepository.getRates(MainViewModel.BASE_CURRENCY))
            .willReturn(Single.just(RatesLoaded(anotherRates)), Single.just(RatesLoaded(rates)))

        val observer = viewModel.observeItemsState().skip(2).take(1).test()
        observer.awaitCount(1).assertValue { it is StateChanged }
    }

    @Test
    fun `test observeItemsState when an items are swapped`() {
        val expectedPredict = StateSwapped(oldPosition, newPosition)

        BDDMockito.given(ratesRepository.getRates(MainViewModel.BASE_CURRENCY))
            .willReturn(Single.just(RatesLoaded(rates)))

        val observer = viewModel.observeItemsState().skip(2).take(1).test()

        Thread.sleep(MainViewModel.INTERVAL_SECONDS * 2000)

        viewModel.changeCurrent("RUB", oldPosition, 0.0)

        observer.awaitCount(1).assertValue(expectedPredict)
    }

    @Test
    fun `test observeItemsState when an items are successfully loaded after an error occurs`() {
        val expectedPredicts = arrayOf(StateError(""), StateLoaded)
        BDDMockito.given(ratesRepository.getRates(MainViewModel.BASE_CURRENCY))
            .willReturn(Single.just(RatesError(Throwable(""))), Single.just(RatesLoaded(rates)))

        val observer = viewModel.observeItemsState().skip(1).take(2).test()

        observer.awaitCount(2).assertValues(*expectedPredicts)
    }

    @Test
    fun `test observeExchange when a currency doesn't exist`() {
        val expectedPredict = 0.0

        BDDMockito.given(ratesRepository.getRates(MainViewModel.BASE_CURRENCY))
            .willReturn(Single.just(RatesLoaded(rates)))

        Thread.sleep(MainViewModel.INTERVAL_SECONDS * 2000)

        val observer = viewModel.observeExchange(fakeCurrency).take(1).test()

        observer.awaitCount(1).assertValue(expectedPredict)
    }

    @Test
    fun `test observeExchange when a first currency is a base currency`() {
        val expectedPredict = rubRate * amount

        BDDMockito.given(ratesRepository.getRates(MainViewModel.BASE_CURRENCY))
            .willReturn(Single.just(RatesLoaded(rates)))

        Thread.sleep(MainViewModel.INTERVAL_SECONDS * 2000)

        viewModel.putAmount(amount)

        val observer = viewModel.observeExchange(rubCurrency).take(1).test()

        observer.awaitCount(1).assertValue(expectedPredict)
    }

    @Test
    fun `test observeExchange when a first currency is not a base currency and we want to exchange not the base currency`() {
        val expectedPredict = amount / usdRate * rubRate

        BDDMockito.given(ratesRepository.getRates(MainViewModel.BASE_CURRENCY))
            .willReturn(Single.just(RatesLoaded(rates)))

        Thread.sleep(MainViewModel.INTERVAL_SECONDS * 2000)

        viewModel.changeCurrent(usdCurrency, oldPosition, amount)

        val observer = viewModel.observeExchange(rubCurrency).take(1).test()

        observer.awaitCount(1).assertValue(expectedPredict)
    }

    @Test
    fun `test observeExchange when a first currency is not a base currency and we want to exchange the base currency`() {
        val expectedPredict = amount / usdRate

        BDDMockito.given(ratesRepository.getRates(MainViewModel.BASE_CURRENCY))
            .willReturn(Single.just(RatesLoaded(rates)))

        Thread.sleep(MainViewModel.INTERVAL_SECONDS * 2000)

        viewModel.changeCurrent(usdCurrency, oldPosition, amount)

        val observer = viewModel.observeExchange(MainViewModel.BASE_CURRENCY).take(1).test()

        observer.awaitCount(1).assertValue(expectedPredict)
    }

    @Test
    fun `test observeExchange when amount are changed`() {
        val expectedPredict = usdRate * amount

        BDDMockito.given(ratesRepository.getRates(MainViewModel.BASE_CURRENCY))
            .willReturn(Single.just(RatesLoaded(rates)))

        Thread.sleep(MainViewModel.INTERVAL_SECONDS * 2000)

        viewModel.putAmount(amount)

        val observer = viewModel.observeExchange(usdCurrency).take(1).test()

        observer.awaitCount(1).assertValue(expectedPredict)

    }

    @Test
    fun `test observeExchange when a rates are changed`() {
        val expectedPredict = newUsdRate * amount

        BDDMockito.given(ratesRepository.getRates(MainViewModel.BASE_CURRENCY))
            .willReturn(Single.just(RatesLoaded(rates)), Single.just(RatesLoaded(newRates)))

        Thread.sleep(MainViewModel.INTERVAL_SECONDS * 2000)

        viewModel.putAmount(amount)

        val observer = viewModel.observeExchange(usdCurrency).skip(1).take(1).test()

        observer.awaitCount(1).assertValue(expectedPredict)
    }

    companion object {
        val amount = 1.0
        val rubRate = 80.0
        val eurRate = 1.0
        val usdRate = 1.5
        val newUsdRate = 2.0
        val rubCurrency = "RUB"
        val usdCurrency = "USD"
        val rates = mutableMapOf(rubCurrency to rubRate,
            usdCurrency to usdRate,
            MainViewModel.BASE_CURRENCY to eurRate)
        val newRates = mutableMapOf(rubCurrency to rubRate,
            usdCurrency to newUsdRate,
            MainViewModel.BASE_CURRENCY to eurRate)
        val anotherRates = mutableMapOf(MainViewModel.BASE_CURRENCY to eurRate)
        val fakeCurrency = "ZAR"
        val oldPosition = 1
        val newPosition = 0
    }

}