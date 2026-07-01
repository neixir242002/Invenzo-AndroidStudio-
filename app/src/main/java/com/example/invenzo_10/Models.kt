package com.example.invenzo_10

import com.google.gson.annotations.SerializedName


data class LoginResponse(
    val message: String?,
    val token: String?,
    val user: UserData?
)


data class GenericResponse(
    val message: String?,
    val status: String?
)


data class UserData(
    val id: Int,
    val nombre: String,
    val email: String
)

data class RegisterRequest(

    @SerializedName("nombre")
    val nombre: String,

    val email: String,

    val password: String,

    @SerializedName("password_confirmation")
    val passwordConfirmation: String,

    val empresa: String
)


data class LoginRequest(
    val email: String,
    val password: String
)