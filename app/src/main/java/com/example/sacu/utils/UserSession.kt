package com.example.sacu.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.sacu.model.Usuario

class UserSession(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("SACU_SESSION", Context.MODE_PRIVATE)

    fun guardarUsuario(usuario: Usuario) {
        prefs.edit {
            putString("uid", usuario.uid)
            putString("nombre", usuario.nombre)
            putString("matricula", usuario.matricula)
            putString("tipo", usuario.tipo)
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
        prefs.edit { clear() }
    }
}