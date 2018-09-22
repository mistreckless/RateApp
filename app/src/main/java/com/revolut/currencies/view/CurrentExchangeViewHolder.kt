package com.revolut.currencies.view

import android.view.LayoutInflater
import android.view.ViewGroup
import com.revolut.R
import com.revolut.currencies.MainViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.current_exchange_item.view.*
import java.util.concurrent.TimeUnit

class CurrentExchangeViewHolder(parent: ViewGroup, private val viewModel: MainViewModel) :
        BaseExchangeViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.current_exchange_item, parent, false)) {

    private lateinit var currency: String

    override fun bind(currency: String, position: Int) {
        this.currency = currency
        with(itemView) {
            tvCurrency.text = currency
            edAmount.setText(viewModel.observeExchange(currency).blockingFirst().toString())
        }
    }

    override fun onAttach() {
        disposables.add(itemView.edAmount.toObservable()
            .distinctUntilChanged()
            .debounce(100, TimeUnit.MILLISECONDS)
            .map(String::parseToDouble)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(viewModel::putAmount)
            .subscribe())
    }

}