package com.revolut.currencies

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.revolut.R
import com.revolut.currencies.view.ExchangeAdapter
import dagger.Lazy
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModel: Lazy<MainViewModel>

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val exAdapter = ExchangeAdapter(lifecycle, viewModel.get())
        with(recView) {
            layoutManager = LinearLayoutManager(context)
            adapter = exAdapter
        }

        viewModel.get().observeItemsState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { itemsState ->
                when (itemsState) {
                    StateDefault    -> {
                        //do nothing
                    }
                    StateLoaded     -> {
                        tvError.visibility = View.GONE
                        recView.visibility = View.VISIBLE
                        exAdapter.notifyDataSetChanged()
                    }
                    is StateSwapped -> {
                        exAdapter.onStop()
                        recView.smoothScrollToPosition(itemsState.newPosition)
                        exAdapter.notifyItemChanged(itemsState.oldPosition)
                        exAdapter.notifyItemChanged(itemsState.newPosition)
                        exAdapter.onStart()
                    }
                    is StateChanged -> {
                        tvError.visibility = View.GONE
                        recView.visibility = View.VISIBLE
                        val diffResult = DiffUtil.calculateDiff(itemsState.diffUtilCallback, false)
                        diffResult.dispatchUpdatesTo(exAdapter)
                    }
                    is StateError   -> {
                        tvError.visibility = View.VISIBLE
                        recView.visibility = View.GONE
                        tvError.text = itemsState.reason
                    }
                }
            }
    }

    override fun onStart() {
        super.onStart()
        viewModel.get().startListen()
    }

    override fun onStop() {
        viewModel.get().stopListen()
        super.onStop()
    }

}