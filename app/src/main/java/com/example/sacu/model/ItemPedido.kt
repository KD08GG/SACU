package com.example.sacu.model

data class ItemPedido(
    val id: String = "",
    val producto_id: String = "",
    val nombre: String = "",
    val cantidad: Int = 0,
    val precio_unitario: Double = 0.0
)