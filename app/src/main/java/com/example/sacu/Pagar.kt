package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView

class Pagar : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pagar)

        //BOTONES MENU
        botonesMenu()

        //VARIABLES
        val numPedido = findViewById<TextView>(R.id.txtNumPedido)
        val tarjetaPred = findViewById<TextView>(R.id.textNumTarjeta)
        val total = findViewById<TextView>(R.id.txtTotal)
        val fecha = findViewById<TextView>(R.id.txtFecha)

        //BOTONES
        val btnComprar = findViewById<Button>(R.id.btnComprar)
        val btncambiarMetodo = findViewById<ImageButton>(R.id.btncambiar)


        //RECYCLE VIEW
        val rvProductos = findViewById<RecyclerView>(R.id.rvProductos)

        //FUNCIONES BOTONES
        btnComprar.setOnClickListener {
            intent = Intent(this, PagoProcesado::class.java)
            startActivity(intent)
        }

        btncambiarMetodo.setOnClickListener {
            intent = Intent(this, CambiarMetodo::class.java)
            startActivity(intent)
        }


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
            intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        btnPerfil.setOnClickListener {
            // Lógica para el botón de inicio de sesión
            intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }

        btnCarrito.setOnClickListener {
            // Lógica para el botón de inicio de sesión
            intent = Intent(this, Carrito::class.java)
            startActivity(intent)
        }

        btnNotif.setOnClickListener {
            // Lógica para el botón de inicio de sesión
            intent = Intent(this, Notificaciones::class.java)
            startActivity(intent)
        }
    }

}