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

        //FRAME QUE DICE EL TURNO ACTUAL
        //frameTurnoActual
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

        // Listener de estado global: tiempo de espera real y turno actual desde panel cocina
        globalStateListener = repository.escucharEstadoGlobal({ tiempo, turno ->
            tiempoEspera.text = getString(R.string.wait_time_format, tiempo)
            turnoActual.text = turno.toString()
        }, { error ->
            Log.e("Home", "Error estado global: ${error.message}")
        })

        // Listener de mi pedido actual
        // frameEnFila y frameTiempo siempre son visibles (info global).
        // framePedido y frameTurnoActual aparecen solo cuando hay un pedido activo.
        pedidoActivoListener = repository.escucharPedidoActivo(uid, { pedido ->
            if (pedido != null) {
                numPedido.text = pedido.numero_fila.toString()
                framePedido.visibility = View.VISIBLE
                frameTurnoActual.visibility = View.VISIBLE

                // Dispatch notifications only when the status actually changes
                val newStatus = pedido.estado
                if (newStatus != lastKnownStatus) {
                    lastKnownStatus = newStatus
                    val orderStatus: OrderStatus? = when (newStatus) {
                        "PENDIENTE" -> OrderStatus.Pendiente
                        "LISTO" -> OrderStatus.Listo
                        "RECOGIDO" -> OrderStatus.Recogido
                        "TERMINADO" -> OrderStatus.Terminado
                        else -> null
                    }
                    orderStatus?.let {
                        orderStateManager.onOrderStatusChanged(pedido.id, uid, it, pedido.numero_fila)
                    }
                }
            } else {
                lastKnownStatus = null
                framePedido.visibility = View.GONE
                frameTurnoActual.visibility = View.GONE
                // frameEnFila and frameTiempo remain visible — they show global queue info
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
        
        desayunosAdapter = ProductoAdapter(listaDesayunos) { compra.agregarProducto(it) }
        comidasAdapter = ProductoAdapter(listaComidas) { compra.agregarProducto(it) }
        
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
}