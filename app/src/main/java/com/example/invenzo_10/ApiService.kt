package com.example.invenzo_10

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @Headers("Accept: application/json")
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @Headers("Accept: application/json")
    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<GenericResponse>

    // --- PRODUCTOS ---
    @Headers("Accept: application/json")
    @GET("api/productos")
    suspend fun getProductos(@Header("Authorization") token: String): Response<List<Producto>>

    @Headers("Accept: application/json")
    @DELETE("api/productos/{id}")
    suspend fun eliminarProducto(@Header("Authorization") token: String, @Path("id") id: Int): Response<GenericResponse>

    // Cambiado a POST: Muchos servidores locales rechazan PATCH/PUT en rutas de acción personalizadas
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @PUT("api/productos/{id}")
    suspend fun toggleStatusProducto(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: EstadoProductoRequest
    ): Response<GenericResponse>
    // Regresamos a PUT: Si decía "guardado" pero no cambiaba nada, el servidor lo recibía pero quizás ignoraba el body
    @Headers("Content-Type: application/json", "Accept: application/json")
    @PUT("api/productos/{id}")
    suspend fun actualizarProducto(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: EditarProductoRequest
    ): Response<GenericResponse>

    @Multipart
    @POST("api/productos")
    suspend fun agregarProducto(
        @Header("Authorization") token: String,
        @Part("nombre") nombre: RequestBody,
        @Part("codigo") codigo: RequestBody,
        @Part("categoria_id") categoriaId: RequestBody,
        @Part("precio") precio: RequestBody,
        @Part("cantidad") cantidad: RequestBody,
        @Part("stock_minimo") stockMinimo: RequestBody,
        @Part foto: MultipartBody.Part
    ): Response<GenericResponse>

    // --- CATEGORIAS ---
    @Headers("Accept: application/json")
    @GET("api/categorias")
    suspend fun getCategorias(@Header("Authorization") token: String): Response<List<Categoria>>

    @Headers("Accept: application/json")
    @POST("api/categorias")
    suspend fun agregarCategoria(
        @Header("Authorization") token: String,
        @Body request: CategoriaRequest
    ): Response<GenericResponse>

    // --- MOVIMIENTOS ---
    @Headers("Accept: application/json")
    @POST("api/movimientos")
    suspend fun registrarMovimiento(
        @Header("Authorization") token: String,
        @Body request: MovimientoRequest
    ): Response<MovimientoResponse>

    @Headers("Accept: application/json")
    @GET("api/movimientos")
    suspend fun getMovimientos(
        @Header("Authorization") token: String
    ): Response<List<Movimiento>>

    //================ REPORTES DASHBOARD =================//

    @Headers("Accept: application/json")
    @GET("api/reportes/general")
    suspend fun getReporteGeneral(
        @Header("Authorization") token: String
    ): Response<ReporteGeneral>

    @Headers("Accept: application/json")
    @GET("api/reportes/estadisticas-mensuales")
    suspend fun getEstadisticasMensuales(
        @Header("Authorization") token: String
    ): Response<List<EstadisticaMensual>>

    @Headers("Accept: application/json")
    @GET("api/reportes/movimientos-semanales")
    suspend fun getMovimientosSemanales(
        @Header("Authorization") token: String
    ): Response<List<MovimientoSemanal>>

    @Headers("Accept: application/json")
    @GET("api/reportes/categorias")
    suspend fun getCategoriasGrafica(
        @Header("Authorization") token: String
    ): Response<List<CategoriaGrafica>>

    @GET("api/reportes/dashboard-android")
    suspend fun dashboardAndroid(
        @Header("Authorization") token: String
    ): Response<DashboardAndroid>
}