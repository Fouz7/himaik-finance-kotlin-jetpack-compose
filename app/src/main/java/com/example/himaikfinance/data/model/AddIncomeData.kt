package com.example.himaikfinance.data.model

import com.google.gson.annotations.SerializedName

data class AddIncomeData(
    val createdAt: String,
    val createdBy: String,
    val id: Int,
    val name: String,
    val nominal: String,
    val transactionId: Any,
    @SerializedName("transfer_date")
    val transferDate: String
)