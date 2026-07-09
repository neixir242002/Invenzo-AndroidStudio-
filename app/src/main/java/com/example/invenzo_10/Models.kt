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
    @SerializedName("nombre") val nombre: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
    val empresa: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class Categoria(
    val id: Int,
    val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    
    @SerializedName("productos_count", alternate = ["cantidad_productos", "productosCount", "cantidad"]) 
    val productosCount: Int?, 
    
    @SerializedName("created_at", alternate = ["fecha_creacion", "fecha"]) 
    val createdAt: String?,
    
    @SerializedName("activo", alternate = ["status", "estado", "is_active"]) 
    val activo: Any? // Usamos Any por si viene como String "1", Int 1 o Boolean true
)

data class CategoriaRequest(
    val nombre: String,
    val descripcion: String?
)

data class ProductoRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("codigo") val codigo: String,
    @SerializedName("categoria_id") val categoria_id: Int,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("stock_minimo") val stock_minimo: Int,
    @SerializedName("precio") val precio: Double,
    @SerializedName("activo") val activo: Int = 1
)

data class Producto(
    val id: Int,
    val nombre: String,
    val codigo: String,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("stock_minimo") val stockMinimo: Int,
    @SerializedName("activo") val activo: Int,
    @SerializedName("precio") val precio: String,
    @SerializedName("foto") val foto: String?,
    @SerializedName("categoria") val categoria: Categoria
)
data class EditarProductoRequest(
    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("codigo")
    val codigo: String,

    @SerializedName("categoria_id")
    val categoria_id: Int,

    @SerializedName("cantidad")
    val cantidad: Int,

    @SerializedName("stock_minimo")
    val stock_minimo: Int,

    @SerializedName("precio")
    val precio: Double
)
data class EstadoProductoRequest(
    @SerializedName("activo")
    val activo: Int
)

data class MovimientoRequest(
    val producto_id: Int,
    val tipo: String,
    val cantidad: Int,
    val observacion: String?
)

data class MovimientoResponse(
    val message: String?,
    val movimiento: Movimiento?
)

data class Movimiento(
    val id: Int,
    val tipo: String,
    val cantidad: Int,
    val observacion: String?,
    @SerializedName("created_at") val createdAt: String?,
    val producto: Producto
)

data class ReporteGeneral(
    val productos_total: Int,
    val categorias_total: Int,
    val usuarios_total: Int,
    val stock_bajo: Int,
    val entradas_total: Int,
    val salidas_total: Int
)
data class EstadisticaMensual(
    val mes: String,
    val valor: Float
)
data class MovimientoSemanal(
    val dia:String,
    val entradas:Int,
    val salidas:Int
)
data class CategoriaGrafica(
    val categoria: String,
    val total: Float
)

data class DashboardAndroid(
    val productos_total: Int,
    val categorias_total: Int,
    val movimientos_total: Int,
    val valor_total: Double,

    val stock_normal: Int,
    val stock_bajo: Int,
    val sin_stock: Int
)