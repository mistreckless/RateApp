package com.revolut

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.revolut.data.Rate
import com.revolut.data.RateRepository
import com.revolut.data.Rates
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@PerActivity
@InjectViewState
class MainActivityPresenter @Inject constructor(
    private val rateRepository: RateRepository,
    private val viewModel: Lazy<MainViewModel>
) : MvpPresenter<MainActivityView>() {

    private val disposables by lazy { CompositeDisposable() }

    override fun onFirstViewAttach() {
        disposables.add(
            Observable.interval(1000, TimeUnit.MILLISECONDS)
                .flatMapSingle { rateRepository.getRates() }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(viewModel.get()::putRates)
                .subscribe(
                    {}, Throwable::printStackTrace)
        )
    }

    override fun onDestroy() {
        disposables.clear()
    }
}

fun Rates.toList(): List<Rate> {
    val res = mutableListOf<Rate>()
    keys.forEach {
        res.add(it to this@toList[it]!!)
    }
    res.reverse()
    return res
}