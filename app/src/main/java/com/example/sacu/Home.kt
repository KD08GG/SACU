package com.example.sacu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sacu.repository.FirestoreRepository

class Home : AppCompatActivity() {

    private val repository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //BOTONES MENU
        botonesMenu()

        //VARIABLES
        val nombre = findViewById<TextView>(R.id.txtNombre)
        val id = findViewById<TextView>(R.id.txtID)
        val totalPedidos = findViewById<TextView>(R.id.TotalPedidos)
        val tiempoEspera = findViewById<TextView>(R.id.TiempoEspera)
        val numPedido = findViewById<TextView>(R.id.NumPedido)


        //BOTONES DE LA PANTALLA
        val btnDesayunos = findViewById<Button>(R.id.btnDesayunos)
        val btnComidas = findViewById<Button>(R.id.btnComidas)

        //RECYCLE VIEW
        val rvDesayunos = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvDesayunos)
        val rvComidas = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvComidas)


        // FRAMES LAYOUT
        val framePedido = findViewById<FrameLayout>(R.id.framePedido) //este frame muestra la parte donde dice el número de pedido, se puede hacer invisible si no hay pedido realizado
        //framePedido.visibility = FrameLayout.VISIBLE


        //FUNCIONES BOTONES
        btnDesayunos.setOnClickListener {
            val intent = Intent(this, TodoComida::class.java)
            intent.putExtra("tipo", "Desayunos")
            startActivity(intent)
        }

        btnComidas.setOnClickListener {
            val intent = Intent(this, TodoComida::class.java)
            intent.putExtra("tipo", "Comidas")
            startActivity(intent)
        }


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

    private fun botonesMenu () {
        //MENU DE BOTONES DE NAVEGACION
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnPerfil = findViewById< ImageButton>(R.id.btnPerfil)
        val btnCarrito = findViewById< ImageButton>(R.id.btnCarrito)
        val btnNotif = findViewById< ImageButton>(R.id.btnNotif)

        //FUNCIONES BOTONES DE MENU
        btnHome.setOnClickListener {
            // Lógica para el botón de inicio de sesión
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        btnPerfil.setOnClickListener {
            // Lógica para el botón de inicio de sesión
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }

        btnCarrito.setOnClickListener {
            // Lógica para el botón de inicio de sesión
            val intent = Intent(this, Carrito::class.java)
            startActivity(intent)
        }

        btnNotif.setOnClickListener {
            // Lógica para el botón de inicio de sesión
            val intent = Intent(this, Notificaciones::class.java)
            startActivity(intent)
        }

    }

}