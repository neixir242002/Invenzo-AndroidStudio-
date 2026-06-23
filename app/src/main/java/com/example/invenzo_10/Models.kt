//package com.example.invenzo_10
//
//import com.google.gson.annotations.SerializedName
//
//// --- RESPUESTAS DEL SERVIDOR ---
//
//data class LoginResponse(
//    @SerializedName("access_token") val accessToken: String?,
//    @SerializedName("token_type") val tokenType: String?,
//    val user: UserData?
//)
//
//data class GenericResponse(
//    val message: String?,
//    val status: String?
//)
//
//data class UserData(
//    val id: Int,
//    val name: String,
//    val email: String
//)
//
//// --- PETICIONES AL SERVIDOR ---
//
//data class RegisterRequest(
//    val nombre: String,
//    val email: String,
//    val password: String,
//    @SerializedName("password_confirmation") val passwordConfirmation: String,
//    val empresa: String
//)
//
//data class LoginRequest(
//    val email: String,
//    val password: String
//)
//
//
//// Registrar Categorria
//
