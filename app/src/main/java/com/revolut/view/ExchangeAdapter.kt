package com.revolut.view

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.revolut.MainViewModel
import dagger.Lazy
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ExchangeAdapter(private val factory: ExchangeViewFactory,
                      private val lifecycle: Lifecycle,
                      private val viewModel: MainViewModel) :
        RecyclerView.Adapter<BaseExchangeViewHolder>(), LifecycleObserver {
    private val holders = mutableListOf<BaseExchangeViewHolder>()

    init {
        lifecycle.addObserver(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseExchangeViewHolder =
        when (viewType) {
            CURRENT_EXCHANGE_TYPE -> factory.provideCurrentExchangeViewHolder(parent)
            else                  -> factory.provideExchangeViewHolder(parent)
        }

    override fun getItemCount(): Int = viewModel.items.size

    override fun onBindViewHolder(holder: BaseExchangeViewHolder, position: Int) {
        holder.bind(viewModel.items[position], position)
    }

    override fun onViewAttachedToWindow(holder: BaseExchangeViewHolder) {
        holders.add(holder)
        holder.onAttach()
    }

    override fun onViewDetachedFromWindow(holder: BaseExchangeViewHolder) {
        holder.onDetach()
        holders.remove(holder)
    }

    override fun getItemViewType(position: Int): Int = when (position) {
        0    -> CURRENT_EXCHANGE_TYPE
        else -> EXCHANGE_TYPE
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() = lifecycle.removeObserver(this)

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() = holders.forEach { it.onAttach() }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() = holders.forEach { it.onDetach() }

    companion object {
        const val EXCHANGE_TYPE = 0
        const val CURRENT_EXCHANGE_TYPE = 1
    }
}

abstract class BaseExchangeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    protected val disposables by lazy { CompositeDisposable() }

    abstract fun bind(currency: String, position: Int)
    abstract fun onAttach()
    open fun onDetach() {
        disposables.clear()
    }
}


class ExchangeViewFactory @Inject constructor(private val viewModel: Lazy<MainViewModel>) {
    fun provideExchangeViewHolder(parent: ViewGroup) =
        ExchangeViewHolder(parent, viewModel.get())
    fun provideCurrentExchangeViewHolder(parent: ViewGroup) =
        CurrentExchangeViewHolder(parent, viewModel.get())
}