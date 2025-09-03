package com.example.himaikfinance.data.model

data class Pagination(
    val limit: Int,
    val page: Int,
    val totalItems: Int,
    val totalPages: Int
)