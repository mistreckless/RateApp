package com.revolut

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.revolut.data.Rate
import com.revolut.view.ExchangeAdapter
import com.revolut.view.ExchangeViewFactory
import dagger.Lazy
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import javax.inject.Provider

class MainActivity : MvpAppCompatActivity(), MainActivityView {

    @InjectPresenter
    lateinit var presenter: MainActivityPresenter

    @Inject
    lateinit var presenterProvider: Provider<MainActivityPresenter>

    @Inject
    lateinit var exchangeViewFactory: ExchangeViewFactory

    @Inject
    lateinit var viewModel: Lazy<MainViewModel>

    @ProvidePresenter
    fun providePresenter(): MainActivityPresenter = presenterProvider.get()

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val exAdapter = ExchangeAdapter(exchangeViewFactory, lifecycle, viewModel.get())
        with(recView){
            layoutManager = LinearLayoutManager(context)
            adapter = exAdapter
        }

        viewModel.get().observeItems().subscribe {itemsState ->
            when(itemsState){
                StateDefault -> {}
                StateLoaded -> exAdapter.notifyDataSetChanged()
                is StateSwapped -> {
                    exAdapter.onStop()
                    recView.smoothScrollToPosition(itemsState.newPosition)
                    exAdapter.notifyItemChanged(itemsState.oldPosition)
                    exAdapter.notifyItemChanged(itemsState.newPosition)
                    exAdapter.onStart()
                }
                is StateChanged ->{
                    Log.e("state", "changed")
                    val diffResult = DiffUtil.calculateDiff(itemsState.diffUtilCallback, false)
                    diffResult.dispatchUpdatesTo(exAdapter)
                }
            }
        }
    }

}

interface MainActivityView: MvpView{
}