package com.revolut

import android.arch.lifecycle.ViewModel
import android.support.v7.util.DiffUtil
import com.revolut.data.BASE_CURRENCY
import com.revolut.data.Rates
import com.revolut.view.calculateCurrencyDiff
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.*

class MainViewModel : ViewModel() {
    private val ratesState by lazy {
        BehaviorSubject
            .createDefault(Exchange(Collections.emptyMap(), 0.0, BASE_CURRENCY, BASE_CURRENCY))
    }

    private val itemsState by lazy {
        BehaviorSubject.createDefault<ItemsState>(StateDefault)
    }

    val items by lazy { mutableListOf<String>() }

    fun putRates(rates: Rates) = with(ratesState.value!!) {
        checkItems(rates, this.rates)
        ratesState.onNext(Exchange(rates, amount, base, first))
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

    fun observeItems(): Observable<ItemsState> = itemsState

    private fun countExchange(key: String, exchange: Exchange): Double {
        val rate = exchange.rates[key]!!
        return rate * exchange.amount
    }

    private fun countCustomExchange(key: String, exchange: Exchange): Double {
        val currentRate = exchange.rates[key]!!
        val firstRate = exchange.rates[exchange.first]!!
        return when (key) {
            BASE_CURRENCY  -> {
                exchange.amount / firstRate
            }
            exchange.first -> {
                exchange.amount
            }
            else           -> {
                exchange.amount / firstRate * currentRate
            }
        }
    }

    private fun checkItems(newRates: Rates, oldRates: Rates) {
        if (oldRates.isEmpty()) {
            val newItems = newRates.keys.reversed()
            items.addAll(newItems)
            itemsState.onNext(StateLoaded)
        } else if (newRates.keys != oldRates.keys){
            itemsState.onNext(StateChanged(oldRates.keys.reversed().calculateCurrencyDiff(oldRates.keys.reversed())))
        }
    }

    private fun swap(oldPosition: Int, newPosition: Int) {
        val item = items[newPosition]
        items[newPosition] = items[oldPosition]
        items[oldPosition] = item
        itemsState.onNext(StateSwapped(oldPosition, newPosition))
    }
}


data class Exchange(val rates: Rates, val amount: Double, val base: String, val first: String)

sealed class ItemsState
object StateDefault : ItemsState()
object StateLoaded : ItemsState()
data class StateSwapped(val oldPosition: Int, val newPosition: Int) : ItemsState()
data class StateChanged(val diffUtilCallback: DiffUtil.Callback): ItemsState()


