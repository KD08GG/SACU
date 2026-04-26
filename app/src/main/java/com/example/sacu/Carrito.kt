package com.example.sacu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sacu.adapter.ItemCarritoAdapter
import com.example.sacu.adapter.ProductoAdapter
import com.example.sacu.model.ItemPedido
import com.example.sacu.model.Producto
import com.example.sacu.repository.Compra
import com.example.sacu.repository.carritoTotal

private var carritoTotal = carritoTotal


class Carrito : AppCompatActivity() {
    private lateinit var comidasAdapter: ItemCarritoAdapter
    private var listaComidas = carritoTotal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_carrito)

        //carrito = compra.carrito
        Log.d("SACU_CARRITO", "Carrito: $listaComidas")


        //BOTONES MENU
        botonesMenu()

        //BOTONES DE LA PANTALLA
        val btnComprar = findViewById<Button>(R.id.btnComprar)
        val btnExplorarMenu = findViewById<Button>(R.id.btnExplorarMenu)
        val tvCarritoVacio = findViewById<TextView>(R.id.tvCarritoVacio)

        //RECYCLE VIEW
        val rvProductos = findViewById<RecyclerView>(R.id.rvComidas)

        // Actualizar visibilidad basado en si el carrito está vacío
        updateCartVisibility(btnComprar, btnExplorarMenu, tvCarritoVacio)

        // FUNCION BOTON COMPRAR
        btnComprar.setOnClickListener {
            val intent = Intent(this, Pagar::class.java)
            startActivity(intent)
        }

        // FUNCION BOTON EXPLORAR MENU
        btnExplorarMenu.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        setupRecyclerViews(rvProductos)



    }

    private fun setupRecyclerViews(rvComidas: RecyclerView) {
        val layoutManagerComidas = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        rvComidas.layoutManager = layoutManagerComidas

        comidasAdapter = ItemCarritoAdapter(listaComidas)

        rvComidas.adapter = comidasAdapter
    }

    private fun updateCartVisibility(btnComprar: Button, btnExplorarMenu: Button, tvCarritoVacio: TextView) {
        if (listaComidas.isEmpty()) {
            btnComprar.isVisible = false
            btnExplorarMenu.isVisible = true
            tvCarritoVacio.isVisible = true
        } else {
            btnComprar.isVisible = true
            btnExplorarMenu.isVisible = false
            tvCarritoVacio.isVisible = false
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