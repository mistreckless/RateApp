package com.revolut.view

import android.support.v7.util.DiffUtil
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.revolut.data.Rates
import io.reactivex.Observable
import io.reactivex.disposables.Disposables

fun EditText.toObservable(): Observable<String> = Observable.create<String> { e ->
    val listener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            e.onNext(s.toString())
        }
    }
    e.setDisposable(Disposables.fromAction { removeTextChangedListener(listener) })

    addTextChangedListener(listener)
}

fun List<String>.calculateCurrencyDiff(newItems: List<String>): DiffUtil.Callback = object : DiffUtil.Callback(){
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        this@calculateCurrencyDiff[oldItemPosition] == newItems[newItemPosition]

    override fun getOldListSize(): Int = this@calculateCurrencyDiff.size

    override fun getNewListSize(): Int = newItems.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            this@calculateCurrencyDiff[oldItemPosition] == newItems[newItemPosition]

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}