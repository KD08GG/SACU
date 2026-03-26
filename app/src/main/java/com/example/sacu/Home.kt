package com.example.sacu

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.sacu.repository.FirestoreRepository

class Home : AppCompatActivity() {

    private val repository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Prueba: leer productos de Firestore
        repository.obtenerProductosPorCategoria(
            categoria = "Desayunos",
            onSuccess = { productos ->
                // Si llegamos aquí, Firestore está funcionando
                productos.forEach { producto ->
                    Log.d("SACU_TEST", "Producto: ${producto.nombre} - $${producto.precio}")
                }
                Log.d("SACU_TEST", "Total desayunos encontrados: ${productos.size}")
            },
            onError = { error ->
                Log.e("SACU_TEST", "Error al leer Firestore: ${error.message}")
            }
        )
    }
}