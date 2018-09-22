package com.revolut.data

import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface RevolutApiService {
    @GET("/latest")
    fun getCurrencies(@Query("base") baseCurrency: String = "EUR"): Single<ExchangeRate>

    companion object Factory {
        private const val EXCHANGE_BASE_URL = "https://revolut.duckdns.org"

        fun create(): RevolutApiService {
            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
            val retrofit = Retrofit.Builder()
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(EXCHANGE_BASE_URL)
                .build()

            return retrofit.create(RevolutApiService::class.java)
        }
    }
}