package com.revolut.currencies

import android.arch.lifecycle.ViewModelProviders
import com.revolut.core.PerActivity
import dagger.Module
import dagger.Provides

@Module
object MainActivityModule {

    @PerActivity
    @Provides
    @JvmStatic
    fun provideViewModel(activity: MainActivity,
                         mainViewModelFactory: MainViewModelFactory): MainViewModel =
        ViewModelProviders.of(activity, mainViewModelFactory)[MainViewModel::class.java]
}