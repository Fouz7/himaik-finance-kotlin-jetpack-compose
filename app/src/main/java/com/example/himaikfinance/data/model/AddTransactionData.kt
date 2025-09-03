package com.example.himaikfinance.data.model

data class AddTransactionData(
    val balance: String,
    val createdAt: String,
    val createdBy: String,
    val credit: String,
    val debit: String,
    val notes: String,
    val transactionId: Int
)