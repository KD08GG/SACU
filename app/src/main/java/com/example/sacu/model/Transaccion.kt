package com.example.sacu.model

import com.google.firebase.Timestamp

data class Transaccion(
    val id: String = "",
    val usuario_id: String = "",
    val pedido_id: String = "",
    val monto: Double = 0.0,
    val estado: String = "",
    val mp_payment_id: String = "",
    val fecha_creacion: Timestamp? = null,
    val fecha_confirmacion: Timestamp? = null
)