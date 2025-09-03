package com.example.himaikfinance.data.model

data class IncomeData(
    val createdAt: String,
    val createdBy: String,
    val id: Int,
    val name: String,
    val nominal: String,
    val transactionId: Int,
    val transfer_date: String
)