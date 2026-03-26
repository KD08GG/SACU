package com.example.sacu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Perfil : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        //BOTONES MENU
        botonesMenu()

        //VARIABLES
        val nombre = findViewById<TextView>(R.id.txtNombre)
        val id = findViewById<TextView>(R.id.txtID)
        val tarjeta = findViewById<TextView>(R.id.textTarjetaPred)

        //BOTONES DE LA PANTALLA
        val btnEditar = findViewById<ImageButton>(R.id.btnEditar)
        val btnEditar2 = findViewById<ImageButton>(R.id.btnEditar2)
        val btnMasTarjetas = findViewById<ImageButton>(R.id.btnMasTarjetas)
        val btnMetodos = findViewById<Button>(R.id.btnMetodos)
        val btnUltimos = findViewById<Button>(R.id.btnComidas)
        val btnLogOut = findViewById<ImageButton>(R.id.btnLogOut)

        //RECYCLE VIEW
        val rvUltimosPedidos = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvComidas)

        //FUNCIONES BOTONES
        btnEditar.setOnClickListener {
            intent = Intent(this, TarjetaPred::class.java)
            startActivity(intent)
        }

        btnEditar2.setOnClickListener {
            intent = Intent(this, TarjetaPred::class.java)
            startActivity(intent)
        }

        btnMasTarjetas.setOnClickListener {
            intent = Intent(this, AgregarTarjeta::class.java)
            startActivity(intent)
        }

        btnMetodos.setOnClickListener {
            intent = Intent(this, MetodosDePago::class.java)
            startActivity(intent)
        }

        btnUltimos.setOnClickListener {
            intent = Intent(this, Notificaciones::class.java)
            startActivity(intent)
        }

        btnLogOut.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
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