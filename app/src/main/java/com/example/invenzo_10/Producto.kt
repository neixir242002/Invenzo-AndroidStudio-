package com.example.invenzo_10

data class Producto(
    val nombre: String,
    val codigo: String,
    val categoria: String,
    val stock: Int,
    val stockmini: Int,
    var activo: Boolean,
    val precio: Double,
    val rutaImagen: String
)