package com.example.sacu.model

import com.google.firebase.Timestamp

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val matricula: String = "",
    val tipo: String = "",
    val fecha_creacion: Timestamp? = null
)