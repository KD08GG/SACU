package com.example.sacu.model

import com.google.firebase.Timestamp

data class Pedido(
    val id: String = "",
    val usuario_id: String = "",
    val estado: String = "",
    val total: Double = 0.0,
    val numero_fila: Int = 0,
    val tiempo_estimado: Int = 0,
    val fecha: Timestamp? = null
)