package com.revolut.app

import android.app.Application
import com.revolut.MainActivity
import com.revolut.MainActivityModule
import com.revolut.PerActivity
import com.revolut.data.RateRepository
import com.revolut.data.RevolutApiService
import com.revolut.data.RevolutRateRepository
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, ActivityBuilder::class, NetworkModule::class, RepositoryModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: RevolutApp)
}

@Module
abstract class ActivityBuilder {

    @PerActivity
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun bindMainActivity(): MainActivity
}

@Module
class RepositoryModule{

    @Singleton
    @Provides
    fun provideRatesRepository(apiService: RevolutApiService): RateRepository = RevolutRateRepository(apiService)
}

@Module
class NetworkModule{
    @Singleton
    @Provides
    fun provideRevolutApiService(): RevolutApiService = RevolutApiService.create()
}