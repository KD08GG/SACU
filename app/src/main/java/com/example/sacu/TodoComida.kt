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
import com.example.sacu.adapter.ProductoAdapter
import com.example.sacu.model.Producto
import com.example.sacu.repository.Compra
import com.example.sacu.repository.FirestoreRepository

class TodoComida : AppCompatActivity() {
    private val repository = FirestoreRepository()
    private val compra = Compra()
    private lateinit var comidasAdapter: ProductoAdapter
    private var listaComidas = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_todo_comida)

        //BOTONES MENU
        botonesMenu()

        //VARIABLES
        val tipoRecibido = intent.getStringExtra("tipo")
        var nombre  = findViewById<TextView>(R.id.txtComida)
        nombre.text = tipoRecibido


        //BOTONES
        val btnComprar = findViewById<Button>(R.id.btnComprar)

        //RECYCLE VIEW
        val rvComidas = findViewById<RecyclerView>(R.id.rvComidas)

        //FUNCIONES BOTONES
        btnComprar.setOnClickListener {
            val intent = Intent(this, Pagar::class.java)
            startActivity(intent)
        }

        setupRecyclerViews(rvComidas)
        cargarComidas(tipoRecibido!!)

    }


    private fun setupRecyclerViews(rvComidas: RecyclerView) {
        // Grid con 2 columnas
        val layoutManagerComidas = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)

        rvComidas.layoutManager = layoutManagerComidas

        comidasAdapter = ProductoAdapter(listaComidas) { producto ->
            onAgregarProductoClick(producto)
        }
        rvComidas.adapter = comidasAdapter
    }

    private fun onAgregarProductoClick(producto: Producto) {
        // Por ahora solo mostramos en log que se agregó
        Log.d("SACU_HOME", "Agregar al carrito: ${producto.nombre}")
        compra.agregarProducto(producto)
    }

    private fun cargarComidas(tipo: String) {
        Log.d("SACU_HOME", "Iniciando carga de comidas...")

        repository.obtenerProductosPorCategoria(
            categoria = tipo,
            onSuccess = { productos ->
                Log.d("SACU_HOME", "Comidas encontradas: ${productos.size}")

                // Imprime cada producto para depurar
                productos.forEach { producto ->
                    Log.d("SACU_HOME", "Comida: ${producto.nombre} - Categoría: ${producto.categoria}")
                }

                listaComidas.clear()
                listaComidas.addAll(productos)
                comidasAdapter.notifyDataSetChanged()

                // Verifica si el adapter tiene los datos
                Log.d("SACU_HOME", "Adapter de comidas tiene: ${comidasAdapter.itemCount} items")
            },
            onError = { error ->
                Log.e("SACU_HOME", "Error cargando comidas: ${error.message}")
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