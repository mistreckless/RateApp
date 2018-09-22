package com.revolut.view

import android.view.LayoutInflater
import android.view.ViewGroup
import com.revolut.MainViewModel
import com.revolut.R
import com.revolut.data.Rate
import kotlinx.android.synthetic.main.current_exchange_item.view.*

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
            .subscribe {
                viewModel.putAmount(if (it.isNullOrEmpty()) 0.0 else it.toDouble())
            })
    }

}