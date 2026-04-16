package com.example.sacu.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.sacu.model.Usuario

class UserSession(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("SACU_SESSION", Context.MODE_PRIVATE)

    fun guardarUsuario(usuario: Usuario) {
        prefs.edit().apply {
            putString("uid", usuario.uid)
            putString("nombre", usuario.nombre)
            putString("matricula", usuario.matricula)
            putString("tipo", usuario.tipo)
            apply()
        }
    }

    fun obtenerUsuario(): Usuario? {
        val uid = prefs.getString("uid", null) ?: return null
        return Usuario(
            uid = uid,
            nombre = prefs.getString("nombre", "") ?: "",
            matricula = prefs.getString("matricula", "") ?: "",
            tipo = prefs.getString("tipo", "") ?: ""
        )
    }

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}