package com.revolut.currencies

import android.arch.lifecycle.ViewModel
import android.support.v7.util.DiffUtil
import com.revolut.core.data.Rates
import com.revolut.core.repository.RateRepository
import com.revolut.core.repository.RatesError
import com.revolut.core.repository.RatesLoaded
import com.revolut.core.repository.RatesResult
import com.revolut.currencies.view.calculateCurrencyDiff
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import java.util.concurrent.TimeUnit

class MainViewModel(private val repository: RateRepository) : ViewModel() {
    private val ratesState by lazy {
        BehaviorSubject
            .createDefault(Exchange(Collections.emptyMap(),
                0.0,
                BASE_CURRENCY,
                BASE_CURRENCY))
    }

    private val itemsState by lazy {
        BehaviorSubject.createDefault<ItemsState>(StateDefault)
    }

    private val _items = mutableListOf<String>()
    val items: List<String>
        get() = _items.toList()

    private val disposables by lazy {
        CompositeDisposable()
    }

    fun startListen() {
        disposables.add(Observable.interval(INTERVAL_SECONDS, TimeUnit.SECONDS)
            .flatMapSingle { repository.getRates(BASE_CURRENCY) }
            .doOnNext(this::processRatesResult)
            .subscribe({}, Throwable::printStackTrace)
        )
    }

    fun stopListen() {
        disposables.clear()
    }

    fun putAmount(amount: Double) = with(ratesState.value!!) {
        ratesState.onNext(Exchange(rates, amount, base, first))
    }

    fun changeCurrent(current: String, position: Int, amount: Double) = with(ratesState.value!!) {
        swap(position, 0)
        ratesState.onNext(Exchange(rates, amount, base, current))
    }

    fun observeExchange(key: String): Observable<Double> = ratesState
        .map {
            if (it.first == it.base) countExchange(key, it)
            else countCustomExchange(key, ratesState.value!!)
        }

    fun observeItemsState(): Observable<ItemsState> = itemsState

    private fun processRatesResult(resultState: RatesResult) {
        when (resultState) {
            is RatesError  -> {
                itemsState.onNext(StateError(resultState.e.message.toString()))
            }
            is RatesLoaded -> {
                val exchange = ratesState.value!!
                val oldRates = exchange.rates
                val newRates = resultState.rates
                when {
                    oldRates.isEmpty()             -> {
                        val newItems = newRates.keys.reversed()
                        _items.addAll(newItems)
                        itemsState.onNext(StateLoaded)
                    }
                    newRates.keys != oldRates.keys -> {
                        itemsState
                            .onNext(StateChanged(oldRates.keys.toList().calculateCurrencyDiff(
                                newRates.keys.reversed())))
                    }
                    itemsState.value is StateError -> {
                        itemsState.onNext(StateLoaded)
                    }
                }
                ratesState.onNext(Exchange(newRates,
                    exchange.amount,
                    exchange.base,
                    exchange.first))
            }
        }
    }

    private fun countExchange(key: String, exchange: Exchange): Double {
        val rate = exchange.rates[key] ?: 0.0
        return rate * exchange.amount
    }

    private fun countCustomExchange(key: String, exchange: Exchange): Double {
        val currentRate = exchange.rates[key] ?: 0.0
        val firstRate = exchange.rates[exchange.first] ?: 0.0
        return when (key) {
            BASE_CURRENCY  -> {
                if (firstRate != 0.0) exchange.amount / firstRate else 0.0
            }
            exchange.first -> {
                exchange.amount
            }
            else           -> {
                if (firstRate != 0.0) exchange.amount / firstRate * currentRate else 0.0
            }
        }
    }

    private fun swap(oldPosition: Int, newPosition: Int) {
        val item = _items[newPosition]
        _items[newPosition] = _items[oldPosition]
        _items[oldPosition] = item
        itemsState.onNext(StateSwapped(oldPosition, newPosition))
    }

    companion object {
        const val BASE_CURRENCY = "EUR"
        const val INTERVAL_SECONDS = 1L
    }
}


data class Exchange(val rates: Rates, val amount: Double, val base: String, val first: String)

sealed class ItemsState
object StateDefault : ItemsState()
object StateLoaded : ItemsState()
data class StateSwapped(val oldPosition: Int, val newPosition: Int) : ItemsState()
data class StateChanged(val diffUtilCallback: DiffUtil.Callback) : ItemsState()
data class StateError(val reason: String) : ItemsState()


