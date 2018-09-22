package com.revolut.data

import com.google.gson.annotations.SerializedName
import java.util.*

data class ExchangeRate(@SerializedName("date") val date: String = "",
                        @SerializedName("rates") val rates: Rates = Collections.emptyMap(),
                        @SerializedName("base") val base: String = "")

typealias Rates = MutableMap<String, Double>
typealias Rate = Pair<String, Double>