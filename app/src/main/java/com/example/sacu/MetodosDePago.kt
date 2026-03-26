package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView

class MetodosDePago : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_metodos_de_pago)

        //BOTONES MENU
        botonesMenu()

        //VARIABLES
        val tarjetaPred = findViewById<TextView>(R.id.textNumTarjeta)

        //BOTONES
        val btnBorrar = findViewById<ImageButton>(R.id.btnBorrar)
        val btnAgregarTarjetas = findViewById<ImageButton>(R.id.btnMasTarjetas)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        val btnBorrarTarjeta = findViewById<Button>(R.id.btnBorrarTarjeta)

        //RECYCLE VIEW
        val rvTarjetas = findViewById<RecyclerView>(R.id.rvProductos)

        //FRAMES LAYOUT
        val frameBorrar = findViewById<FrameLayout>(R.id.borrarNotif)

        //FUNCIONES BOTONES
        btnAgregarTarjetas.setOnClickListener {
            intent = Intent(this, AgregarTarjeta::class.java)
            startActivity(intent)
        }

        btnBorrar.setOnClickListener {
            frameBorrar.visibility = FrameLayout.VISIBLE
        }

        btnCancelar.setOnClickListener {
            frameBorrar.visibility = FrameLayout.INVISIBLE
        }

        btnBorrarTarjeta.setOnClickListener {
            frameBorrar.visibility = FrameLayout.INVISIBLE
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