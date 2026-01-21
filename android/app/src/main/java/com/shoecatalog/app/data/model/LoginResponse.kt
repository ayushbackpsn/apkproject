package com.shoecatalog.app.data.model

data class LoginResponse(
    val token: String,
    val user: User
)

data class User(
    val id: String,
    val username: String
)

