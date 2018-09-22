package com.revolut

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import dagger.Module
import dagger.Provides

@Module
class MainActivityModule{

    @PerActivity
    @Provides
    fun provideViewModel(activity: MainActivity): MainViewModel = ViewModelProviders.of(activity as AppCompatActivity)[MainViewModel::class.java]
}