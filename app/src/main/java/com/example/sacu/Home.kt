package com.example.sacu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sacu.adapter.ProductoAdapter
import com.example.sacu.model.Producto
import com.example.sacu.repository.Compra
import com.example.sacu.repository.FirestoreRepository

class Home : AppCompatActivity() {

    private val repository = FirestoreRepository()
    private val compra = Compra()


    // Adaptadores para los RecyclerViews
    private lateinit var desayunosAdapter: ProductoAdapter
    private lateinit var comidasAdapter: ProductoAdapter

    // Listas de productos
    private var listaDesayunos = mutableListOf<Producto>()
    private var listaComidas = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Imprimir todos los productos de Firestore
        repository.obtenerTodosLosProductos(
            onSuccess = { productos ->
                Log.d("SACU_TEST", "========== TODOS LOS PRODUCTOS ==========")
                Log.d("SACU_TEST", "Total de productos: ${productos.size}")

                productos.forEachIndexed { index, producto ->
                    Log.d("SACU_TEST", "Producto ${index + 1}:")
                    Log.d("SACU_TEST", "  Nombre: ${producto.nombre}")
                    Log.d("SACU_TEST", "  Categoría: ${producto.categoria}")
                    Log.d("SACU_TEST", "  Precio: $${producto.precio}")
                    Log.d("SACU_TEST", "  Imagen URL: ${producto.imagen_url}")
                    Log.d("SACU_TEST", "  Disp: ${producto.disponible}")
                    Log.d("SACU_TEST", "  ---")
                }
                Log.d("SACU_TEST", "==========================================")
            },
            onError = { error ->
                Log.e("SACU_TEST", "Error al obtener productos: ${error.message}")
            }
        )

        // BOTONES MENU
        botonesMenu()

        // VARIABLES
        val nombre = findViewById<TextView>(R.id.txtNombre)
        val id = findViewById<TextView>(R.id.txtID)
        val totalPedidos = findViewById<TextView>(R.id.TotalPedidos)
        val tiempoEspera = findViewById<TextView>(R.id.TiempoEspera)
        val numPedido = findViewById<TextView>(R.id.NumPedido)

        // BOTONES DE LA PANTALLA
        val btnDesayunos = findViewById<Button>(R.id.btnDesayunos)
        val btnComidas = findViewById<Button>(R.id.btnComidas)

        // RECYCLE VIEW
        val rvDesayunos = findViewById<RecyclerView>(R.id.rvDesayunos)
        val rvComidas = findViewById<RecyclerView>(R.id.rvComidas)

        // Configurar RecyclerViews
        setupRecyclerViews(rvDesayunos, rvComidas)

        // FRAMES LAYOUT
        val framePedido = findViewById<FrameLayout>(R.id.framePedido)
        // framePedido.visibility = FrameLayout.VISIBLE

        // FUNCIONES BOTONES
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

        // Cargar datos desde Firestore
        cargarDesayunos()
        cargarComidas()
    }

    private fun setupRecyclerViews(rvDesayunos: RecyclerView, rvComidas: RecyclerView) {
        // Configurar layout managers (horizontal)
        val layoutManagerDesayunos = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val layoutManagerComidas = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rvDesayunos.layoutManager = layoutManagerDesayunos
        rvComidas.layoutManager = layoutManagerComidas

        // Inicializar adaptadores
        desayunosAdapter = ProductoAdapter(listaDesayunos) { producto ->
            onAgregarProductoClick(producto)
        }

        comidasAdapter = ProductoAdapter(listaComidas) { producto ->
            onAgregarProductoClick(producto)
        }

        rvDesayunos.adapter = desayunosAdapter
        rvComidas.adapter = comidasAdapter
    }

    private fun cargarDesayunos() {
        repository.obtenerProductosPorCategoria(
            categoria = "Desayunos",
            onSuccess = { productos ->
                listaDesayunos.clear()
                listaDesayunos.addAll(productos)
                desayunosAdapter.notifyDataSetChanged()

                Log.d("SACU_HOME", "Desayunos cargados: ${productos.size}")
            },
            onError = { error ->
                Log.e("SACU_HOME", "Error cargando desayunos: ${error.message}")
            }
        )
    }

    private fun cargarComidas() {
        Log.d("SACU_HOME", "Iniciando carga de comidas...")

        repository.obtenerProductosPorCategoria(
            categoria = "Comidas",
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

    private fun onAgregarProductoClick(producto: Producto) {
        // Por ahora solo mostramos en log que se agregó
        Log.d("SACU_HOME", "Agregar al carrito: ${producto.nombre}")
        compra.agregarProducto(producto)

    }

    private fun botonesMenu() {
        // MENU DE BOTONES DE NAVEGACION
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnPerfil = findViewById<ImageButton>(R.id.btnPerfil)
        val btnCarrito = findViewById<ImageButton>(R.id.btnCarrito)
        val btnNotif = findViewById<ImageButton>(R.id.btnNotif)

        btnHome.setOnClickListener {
            // Ya estamos en Home
        }

        btnPerfil.setOnClickListener {
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }

        btnCarrito.setOnClickListener {
            val intent = Intent(this, Carrito::class.java)
            startActivity(intent)
        }

        btnNotif.setOnClickListener {
            val intent = Intent(this, Notificaciones::class.java)
            startActivity(intent)
        }
    }
}

