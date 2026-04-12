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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sacu.adapter.ItemCarritoAdapter
import com.example.sacu.adapter.PedidoAdapter
import com.example.sacu.model.ItemPedido
import com.example.sacu.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth


class Perfil : AppCompatActivity() {
    private lateinit var comidasAdapter: PedidoAdapter
    private val repository = FirestoreRepository()
    private var listaComidas = mutableListOf<ItemPedido>()
    val userId = FirebaseAuth.getInstance().currentUser?.uid


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        val user = FirebaseAuth.getInstance().currentUser


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
            val intent = Intent(this, TarjetaPred::class.java)
            startActivity(intent)
        }

        btnEditar2.setOnClickListener {
            val intent = Intent(this, TarjetaPred::class.java)
            startActivity(intent)
        }

        btnMasTarjetas.setOnClickListener {
            val intent = Intent(this, AgregarTarjeta::class.java)
            startActivity(intent)
        }

        btnMetodos.setOnClickListener {
            val intent = Intent(this, MetodosDePago::class.java)
            startActivity(intent)
        }

        btnUltimos.setOnClickListener {
            val intent = Intent(this, Notificaciones::class.java)
            startActivity(intent)
        }

        btnLogOut.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        setupRecyclerViews(rvUltimosPedidos)


    }

    private fun setupRecyclerViews(rvComidas: RecyclerView) {
        val layoutManagerComidas = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rvComidas.layoutManager = layoutManagerComidas

        //comidasAdapter = PedidoAdapter(listaComidas)

        //rvComidas.adapter = comidasAdapter
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