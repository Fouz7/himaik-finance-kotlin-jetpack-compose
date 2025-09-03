package com.example.himaikfinance.data.model

data class LoginResponse(
    val message: String,
    val data: LoginData
) {
    data class LoginData(
        val user: UserDto,
        val token: String
    )
}
