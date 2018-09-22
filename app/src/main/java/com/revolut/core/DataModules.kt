package com.revolut.core

import com.revolut.BuildConfig
import com.revolut.core.repository.RateRepository
import com.revolut.core.repository.RevolutRateRepository
import com.revolut.core.service.RevolutApiService
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
object RepositoryModule {

    @Singleton
    @Provides
    @JvmStatic
    fun provideRatesRepository(apiService: RevolutApiService): RateRepository =
        RevolutRateRepository(apiService)
}

@Module
object NetworkModule {
    @Singleton
    @Provides
    @JvmStatic
    fun provideRevolutApiService(client: OkHttpClient): RevolutApiService =
        Retrofit.Builder()
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.EXCHANGE_BASE_URL)
            .build()
            .create(RevolutApiService::class.java)

    @Singleton
    @Provides
    @JvmStatic
    fun provideOkHttpClient(): OkHttpClient {
        val client = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            client.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
        return client.build()
    }
}