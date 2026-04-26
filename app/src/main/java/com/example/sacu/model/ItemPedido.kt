package com.example.sacu.model

data class ItemPedido(
    val id: String = "",
    val producto_id: String = "",
    val nombre: String = "",
    var cantidad: Int = 0,
    val precio_unitario: Double = 0.0,
    val imagen_url: String = ""
)