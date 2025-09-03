package com.example.himaikfinance.data.model

data class GetTransactionResponse(
    val `data`: List<TransactionData>,
    val pagination: Pagination,
    val statusCode: Int,
    val success: Boolean
)