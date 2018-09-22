package com.revolut.core.data

import com.google.gson.annotations.SerializedName

data class ExchangeRate(@SerializedName("date") val date: String = "",
                        @SerializedName("rates") val rates: Rates = mutableMapOf(),
                        @SerializedName("base") val base: String = "")

typealias Rates = MutableMap<String, Double>