package com.example.himaikfinance.data.model

import com.google.gson.annotations.SerializedName

data class AddIncomeRequest(
    val name: String,
    val nominal: Int,
    @SerializedName("transfer_date")
    val transferDate: String
)