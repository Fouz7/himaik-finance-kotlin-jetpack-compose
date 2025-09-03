package com.example.himaikfinance.data.model

data class GetIncomeResponse(
    val `data`: List<IncomeData>,
    val pagination: Pagination,
    val statusCode: Int,
    val success: Boolean
)