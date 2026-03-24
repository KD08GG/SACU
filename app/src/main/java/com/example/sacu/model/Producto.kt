package com.example.sacu.model

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val categoria: String = "",
    val disponible: Boolean = true,
    val imagen_url: String = ""
)