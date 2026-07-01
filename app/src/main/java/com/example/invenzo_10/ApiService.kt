package com.example.invenzo_10

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @Headers("Accept: application/json")
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @Headers("Accept: application/json")
    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<GenericResponse>

    // Ejemplo de ruta protegida por Sanctum
    @Headers("Accept: application/json")
    @GET("api/productos")
    suspend fun getProductos(
        @Header("Authorization") token: String
    ): Response<List<Producto>>
}
