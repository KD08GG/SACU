package com.example.sacu.repository

enum class PaymentErrorReason(val mensaje: String) {
    TARJETA_INVALIDA("Tarjeta no reconocida"),
    TARJETA_DECLINADA("Tarjeta declinada por el emisor"),
    FONDOS_INSUFICIENTES("Fondos insuficientes"),
    ERROR_RED("Error de conexión, intenta de nuevo")
}

class PaymentException(val reason: PaymentErrorReason) : Exception(reason.name)
