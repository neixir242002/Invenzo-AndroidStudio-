package com.example.invenzo_10

import com.google.gson.JsonElement
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

data class UserUpdateResponse(
    val message: String?,
    val status: String?,
    val user: UserData?
)

data class ProfileUpdateRequest(
    val nombre: String,
    val email: String,
    val rol: String? = null
)

data class UserData(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String?, 
    @SerializedName("foto") val foto: String?,
    val empresa: EmpresaData?
)

data class EmpresaData(
    val id: Int,
    val nombre: String
)

data class RegisterRequest(
    @SerializedName("nombre") val nombre: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
    val empresa: String,
    val rol: String
)

data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
    @SerializedName("token") val token: String = "" 
)

data class UserCreateRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val rol: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class Categoria(
    val id: Int,
    val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("productos_count") val productosCount: Int?, 
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName(value = "activo", alternate = ["activa"]) var activo: Any?
)

data class CategoriaRequest(
    val nombre: String,
    val descripcion: String?,
    @SerializedName("activo") val activo: Int = 1,
    @SerializedName("activa") val activa: Int = 1
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
    @SerializedName("categoria") val categoria: Categoria?
)

data class EditarProductoRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("codigo") val codigo: String,
    @SerializedName("categoria_id") val categoria_id: Int,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("stock_minimo") val stock_minimo: Int,
    @SerializedName("precio") val precio: Double
)

data class EstadoProductoRequest(
    @SerializedName("activo") val activo: Int
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
    val producto: Producto,
    val usuario: UsuarioAuditoria?
)

data class Auditoria(
    val id: Int?,
    @SerializedName("accion") val accion: String?,
    @SerializedName("modulo") val modulo: String?,
    @SerializedName("created_at") val fecha: String?,
    @SerializedName("usuario") val usuario: UsuarioAuditoria?
)

data class UsuarioAuditoria(
    @SerializedName("nombre") val nombre: String?
)

data class AuditoriaRequest(
    @SerializedName("accion") val accion: String,
    @SerializedName("modulo") val modulo: String
)

data class Notificacion(
    val id: String,
    val data: JsonElement?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("read_at") val readAt: String?,
    @SerializedName("tipo") val tipoRoot: String?
) {
    val tipo: String get() = (tipoRoot ?: getStringFromData("tipo") ?: getStringFromData("type") ?: "SISTEMA").toString().uppercase()

    val titulo: String get() {
        if (tipo == "MOVIMIENTO" || tipo == "STOCK") return "Control"
        val t = getStringFromData("titulo") ?: getStringFromData("producto") ?: 
                getStringFromData("nombre") ?: getStringFromData("title") ?: getStringFromData("producto_nombre")
        return t ?: "Inventario"
    }

    val mensaje: String get() {
        val m = getStringFromData("mensaje") ?: getStringFromData("message") ?: getStringFromData("body")
        if (!m.isNullOrBlank()) return m!!
        return when (tipo) {
            "MOVIMIENTO" -> {
                val mov = (getStringFromData("tipo_movimiento") ?: getStringFromData("tipo") ?: "movimiento").toString().lowercase()
                val cant = (getStringFromData("cantidad") ?: "0")
                "$mov de $cant unidades"
            }
            "STOCK" -> {
                val t = titulo
                if (t == "Control") "Control tiene stock bajo" else "$t tiene stock bajo"
            }
            "NUEVO_PRODUCTO" -> "Nuevo producto registrado"
            else -> "Toca para ver detalles"
        }
    }

    val leida: Boolean get() = readAt != null

    private fun getStringFromData(key: String): String? {
        return try {
            if (data == null || data.isJsonNull || !data.isJsonObject) return null
            val element = data.asJsonObject.get(key) ?: data.asJsonObject.get(key.lowercase())
            if (element != null && !element.isJsonNull) {
                return if (element.isJsonPrimitive) element.asString else element.toString().replace("\"", "")
            }
            null
        } catch (e: Exception) { null }
    }
}

data class NotificacionRequest(
    val titulo: String,
    val title: String,
    val mensaje: String,
    val message: String,
    val tipo: String,
    val type: String
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
    val dia: String,
    val entradas: Any,
    val salidas: Any
)

data class CategoriaGrafica(
    val categoria: String,
    val total: Float
)

data class DashboardAndroid(
    val productos_total: Int,
    val categorias_total: Int,
    val movimientos_total: Int,
    val valor_total: String,
    val stock_normal: Int,
    val stock_bajo: Int,
    val sin_stock: Int
)
