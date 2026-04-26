package com.example.sacu.model

data class Tarjeta(
    val id: String = "",
    val nombreTitular: String = "",
    val numero: String = "",
    val fechaExpiracion: String = "",
    val cvv: String = "",
    var esPredeterminada: Boolean = false
)