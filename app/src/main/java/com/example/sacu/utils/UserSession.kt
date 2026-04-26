package com.example.sacu.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.sacu.model.Tarjeta
import com.example.sacu.model.Usuario
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserSession(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("SACU_SESSION", Context.MODE_PRIVATE)
    private val gson = Gson()

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

    // GESTIÓN DE TARJETAS LOCALES
    fun guardarTarjeta(tarjeta: Tarjeta) {
        val tarjetas = obtenerTarjetas().toMutableList()
        val index = tarjetas.indexOfFirst { it.id == tarjeta.id }
        
        if (index != -1) {
            tarjetas[index] = tarjeta
        } else {
            tarjetas.add(tarjeta)
        }
        
        if (tarjeta.esPredeterminada) {
            tarjetas.forEach { if (it.id != tarjeta.id) it.esPredeterminada = false }
        } else if (tarjetas.size == 1) {
            tarjetas[0].esPredeterminada = true
        }

        prefs.edit().putString("lista_tarjetas", gson.toJson(tarjetas)).apply()
    }

    fun obtenerTarjetas(): List<Tarjeta> {
        val json = prefs.getString("lista_tarjetas", null) ?: return emptyList()
        val type = object : TypeToken<List<Tarjeta>>() {}.type
        return gson.fromJson(json, type)
    }

    fun obtenerTarjetaPredeterminada(): Tarjeta? {
        return obtenerTarjetas().find { it.esPredeterminada }
    }

    fun eliminarTarjeta(id: String) {
        val tarjetas = obtenerTarjetas().toMutableList()
        tarjetas.removeAll { it.id == id }
        if (tarjetas.isNotEmpty() && tarjetas.none { it.esPredeterminada }) {
            tarjetas[0].esPredeterminada = true
        }
        prefs.edit().putString("lista_tarjetas", gson.toJson(tarjetas)).apply()
    }

    fun cerrarSesion() {
        prefs.edit { clear() }
    }
}