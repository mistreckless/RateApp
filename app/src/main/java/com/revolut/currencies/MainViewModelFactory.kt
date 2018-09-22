package com.revolut.currencies

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.revolut.core.repository.RateRepository
import javax.inject.Inject

class MainViewModelFactory @Inject constructor(private val repository: RateRepository) :
        ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) MainViewModel(
            repository) as T else throw IllegalArgumentException(
            "view model not found")
}