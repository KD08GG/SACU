package com.example.sacu

sealed class OrderStatus {
    object Pendiente : OrderStatus()
    object Listo : OrderStatus()
    object Recogido : OrderStatus()
    object Terminado : OrderStatus()
    data class Cancelado(val reason: String) : OrderStatus()
}