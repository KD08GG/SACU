package com.example.sacu.model

import com.google.firebase.Timestamp

data class Notificacion(
    val id: String = "",
    val usuario_id: String = "",
    val pedido_id: String = "",
    val mensaje: String = "",
    val leida: Boolean = false,
    val fecha: Timestamp? = null
)