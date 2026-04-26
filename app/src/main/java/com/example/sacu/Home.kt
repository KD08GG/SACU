package com.example.sacu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.example.sacu.repository.carritoTotal
import com.example.sacu.utils.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class Home : AppCompatActivity() {

    private val repository = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()
    private val compra = Compra()
    private lateinit var userSession: UserSession
    private lateinit var orderNotificationManager: OrderNotificationManager
    private lateinit var orderStateManager: OrderStateManager
    private var lastKnownStatus: String? = null

    private lateinit var txtNombre: TextView
    private lateinit var txtID: TextView
    private lateinit var frameEnFila: FrameLayout
    private lateinit var frameTiempo: FrameLayout
    private lateinit var framePedido: FrameLayout
    private lateinit var frameTurnoActual: FrameLayout

    private lateinit var totalPedidos: TextView
    private lateinit var tiempoEspera: TextView
    private lateinit var numPedido: TextView
    private lateinit var turnoActual: TextView

    private lateinit var desayunosAdapter: ProductoAdapter
    private lateinit var comidasAdapter: ProductoAdapter

    private var listaDesayunos = mutableListOf<Producto>()
    private var listaComidas = mutableListOf<Producto>()

    private var listaCarrito = carritoTotal

    private var filaListener: ListenerRegistration? = null
    private var pedidoActivoListener: ListenerRegistration? = null
    private var globalStateListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        userSession = UserSession(this)
        orderNotificationManager = OrderNotificationManager(this)
        orderStateManager = OrderStateManager(orderNotificationManager)

        // Vincular vistas
        txtNombre = findViewById(R.id.txtNombre)
        txtID = findViewById(R.id.txtID)
        totalPedidos = findViewById(R.id.TotalPedidos)
        tiempoEspera = findViewById(R.id.TiempoEspera)
        numPedido = findViewById(R.id.NumPedido)
        turnoActual = findViewById(R.id.TurnoActual)
        frameEnFila = findViewById(R.id.frameEnFila)
        frameTiempo = findViewById(R.id.frameTiempo)
        framePedido = findViewById(R.id.framePedido)
        frameTurnoActual = findViewById(R.id.frameTurnoActual)

        setupRecyclerViews()
        setupListeners()
        cargarInformacionUsuario()
        cargarProductos()
        
        setupNavegacion()
    }

    private fun cargarInformacionUsuario() {
        val localUser = userSession.obtenerUsuario()
        if (localUser != null && localUser.nombre.isNotEmpty()) {
            txtNombre.text = localUser.nombre
            txtID.text = localUser.matricula
        }

        val uid = auth.currentUser?.uid ?: return
        repository.obtenerUsuario(uid, { cloudUser ->
            cloudUser?.let {
                userSession.guardarUsuario(it)
                txtNombre.text = it.nombre
                txtID.text = it.matricula
            }
        }, { Log.e("Home", "Error sincronizando: ${it.message}") })
    }

    private fun setupListeners() {
        val uid = auth.currentUser?.uid ?: return
        
        // Listener de pedidos en fila
        filaListener = repository.escucharPedidosEnFila({ cantidad ->
            totalPedidos.text = cantidad.toString()
        }, { error ->
            Log.e("Home", "Error en fila: ${error.message}")
        })

        // Listener de estado global
        globalStateListener = repository.escucharEstadoGlobal({ tiempo, turno ->
            tiempoEspera.text = getString(R.string.wait_time_format, tiempo)
            turnoActual.text = turno.toString()
        }, { error ->
            Log.e("Home", "Error estado global: ${error.message}")
        })

        // Listener de mi pedido actual con lógica de cascada
        pedidoActivoListener = repository.escucharPedidoActivo(uid, { pedido ->
            if (pedido != null) {
                // Notificaciones: Detectar cambios SIEMPRE (incluyendo LISTO)
                val stateKey = "${pedido.id}_${pedido.estado}"
                if (stateKey != lastKnownStatus) {
                    lastKnownStatus = stateKey
                    val orderStatus: OrderStatus? = when (pedido.estado) {
                        "PENDIENTE" -> OrderStatus.Pendiente
                        "EN_PREPARACION" -> OrderStatus.Pendiente
                        "LISTO" -> OrderStatus.Listo
                        "RECOGIDO" -> OrderStatus.Recogido
                        "TERMINADO" -> OrderStatus.Terminado
                        else -> null
                    }
                    orderStatus?.let {
                        orderStateManager.onOrderStatusChanged(pedido.id, uid, it, pedido.numero_fila)
                    }
                }

                // UI: Mostrar el widget SOLO si el pedido actual está en preparación
                if (pedido.estado == "PENDIENTE" || pedido.estado == "EN_PREPARACION") {
                    numPedido.text = pedido.numero_fila.toString()
                    framePedido.visibility = View.VISIBLE
                    frameTurnoActual.visibility = View.VISIBLE
                } else {
                    // Si el pedido prioritario está LISTO, el widget se oculta.
                    // El repository nos dará el siguiente PENDIENTE si existe, disparando este listener de nuevo.
                    framePedido.visibility = View.GONE
                    frameTurnoActual.visibility = View.GONE
                }
            } else {
                framePedido.visibility = View.GONE
                frameTurnoActual.visibility = View.GONE
                lastKnownStatus = null
            }
        }, { error ->
            Log.e("Home", "Error pedido activo: ${error.message}")
        })
    }

    private fun setupRecyclerViews() {
        val rvDesayunos = findViewById<RecyclerView>(R.id.rvDesayunos)
        val rvComidas = findViewById<RecyclerView>(R.id.rvComidas)

        rvDesayunos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvComidas.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        //desayunosAdapter = ProductoAdapter(listaDesayunos) { producto, _ -> compra.agregarProducto(producto) }
        //comidasAdapter = ProductoAdapter(listaComidas) { producto, _ -> compra.agregarProducto(producto) }

        desayunosAdapter = ProductoAdapter(
            productos = listaComidas,
            onAgregarClick = { producto ->
                onAgregarProductoClick(producto)
            },
            onRestarClick = { producto ->
                onRestarProductoClick(producto)
            }
        )

        comidasAdapter = ProductoAdapter(
            productos = listaComidas,
            onAgregarClick = { producto ->
                onAgregarProductoClick(producto)
            },
            onRestarClick = { producto ->
                onRestarProductoClick(producto)
            }
        )

        rvDesayunos.adapter = desayunosAdapter
        rvComidas.adapter = comidasAdapter
    }

    private fun cargarProductos() {
        repository.obtenerProductosPorCategoria("Desayunos", { 
            listaDesayunos.clear()
            listaDesayunos.addAll(it)
            desayunosAdapter.notifyDataSetChanged() 
        }, {})
        repository.obtenerProductosPorCategoria("Comidas", { 
            listaComidas.clear()
            listaComidas.addAll(it)
            comidasAdapter.notifyDataSetChanged() 
        }, {})
    }

    private fun irATodoComida(tipo: String) {
        startActivity(Intent(this, TodoComida::class.java).apply { putExtra("tipo", tipo) })
    }

    private fun setupNavegacion() {
        findViewById<TextView>(R.id.btnDesayunos).setOnClickListener { irATodoComida("Desayunos") }
        findViewById<TextView>(R.id.btnComidas).setOnClickListener { irATodoComida("Comidas") }
        findViewById<ImageButton>(R.id.btnPerfil).setOnClickListener { startActivity(Intent(this, Perfil::class.java)) }
        findViewById<ImageButton>(R.id.btnCarrito).setOnClickListener { startActivity(Intent(this, Carrito::class.java)) }
        findViewById<ImageButton>(R.id.btnNotif).setOnClickListener { startActivity(Intent(this, Notificaciones::class.java)) }
    }

    override fun onDestroy() {
        super.onDestroy()
        filaListener?.remove()
        pedidoActivoListener?.remove()
        globalStateListener?.remove()
    }

    private fun onAgregarProductoClick(producto: Producto) {
        // Por ahora solo mostramos en log que se agregó
        Log.d("SACU_HOME", "Agregar al carrito: ${producto.nombre}")
        compra.agregarProducto(producto)
    }

    private fun onRestarProductoClick(producto: Producto) {
        // Por ahora solo mostramos en log que se agregó
        Log.d("SACU_HOME", "Quitar al carrito: ${producto.nombre}")
        compra.quitarProducto(producto)
    }
}