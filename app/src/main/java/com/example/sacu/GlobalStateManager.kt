package com.example.sacu

import com.google.firebase.firestore.FirebaseFirestore

data class GlobalState(
    val tiempoEspera: Int = 0,
    val turnoActual: Int = 0,
    val turnoSiguiente: Int = 1
)

class GlobalStateManager {
    private val db = FirebaseFirestore.getInstance()

    fun getGlobalState(onSuccess: (GlobalState) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("global_state").document("current").get()
            .addOnSuccessListener { document ->
                val tiempoEspera = document.getLong("tiempo_espera")?.toInt() ?: 0
                val turnoActual = document.getLong("turno_actual")?.toInt() ?: 0
                val turnoSiguiente = document.getLong("turno_siguiente")?.toInt() ?: 1
                onSuccess(GlobalState(tiempoEspera, turnoActual, turnoSiguiente))
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun updateTiempoEspera(nuevoTiempo: Int, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val updates = mapOf("tiempo_espera" to nuevoTiempo)
        db.collection("global_state").document("current").update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateTurnos(turnoActual: Int, turnoSiguiente: Int, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val updates = mapOf(
            "turno_actual" to turnoActual,
            "turno_siguiente" to turnoSiguiente
        )
        db.collection("global_state").document("current").update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}