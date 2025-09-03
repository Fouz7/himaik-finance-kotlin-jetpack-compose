package com.example.himaikfinance.data.model

data class TransactionData(
    val balance: String,
    val createdAt: String,
    val createdBy: String,
    val credit: String,
    val debit: String,
    val notes: String,
    val transactionId: Int
)