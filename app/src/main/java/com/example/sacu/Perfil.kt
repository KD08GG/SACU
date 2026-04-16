package com.example.sacu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sacu.adapter.PedidoAdapter
import com.example.sacu.model.Pedido
import com.example.sacu.repository.FirestoreRepository
import com.example.sacu.utils.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class Perfil : AppCompatActivity() {
    private val repository = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var userSession: UserSession
    private lateinit var adapter: PedidoAdapter
    private val listaPedidos = mutableListOf<Pedido>()
    private var pedidosListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)
        userSession = UserSession(this)

        val txtNombre = findViewById<TextView>(R.id.txtNombre)
        val txtID = findViewById<TextView>(R.id.txtID)
        val rvUltimosPedidos = findViewById<RecyclerView>(R.id.rvComidas)

        botonesMenu()
        cargarInformacionUsuario(txtNombre, txtID)

        rvUltimosPedidos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = PedidoAdapter(listaPedidos) { pedido ->
            val intent = Intent(this, Detalles::class.java).apply { putExtra("pedido_id", pedido.id) }
            startActivity(intent)
        }
        rvUltimosPedidos.adapter = adapter

        cargarUltimosPedidos()
        setupAcciones()
    }

    private fun cargarInformacionUsuario(txtNombre: TextView, txtID: TextView) {
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
        }, { Log.e("Perfil", "Error de red: ${it.message}") })
    }

    private fun cargarUltimosPedidos() {
        val uid = auth.currentUser?.uid ?: return
        // Se agregaron los argumentos faltantes (onUpdate y onError) para coincidir con el repositorio
        pedidosListener = repository.escucharTodosLosPedidosDelUsuario(uid, { pedidos ->
            listaPedidos.clear()
            listaPedidos.addAll(pedidos)
            adapter.notifyDataSetChanged()
        }, { error ->
            Log.e("Perfil", "Error cargando pedidos: ${error.message}")
        })
    }

    private fun setupAcciones() {
        findViewById<ImageButton>(R.id.btnEditar).setOnClickListener { startActivity(Intent(this, TarjetaPred::class.java)) }
        findViewById<ImageButton>(R.id.btnEditar2).setOnClickListener { startActivity(Intent(this, TarjetaPred::class.java)) }
        findViewById<ImageButton>(R.id.btnMasTarjetas).setOnClickListener { startActivity(Intent(this, AgregarTarjeta::class.java)) }
        findViewById<Button>(R.id.btnMetodos).setOnClickListener { startActivity(Intent(this, MetodosDePago::class.java)) }
        findViewById<Button>(R.id.btnComidas).setOnClickListener { startActivity(Intent(this, Notificaciones::class.java)) }
        findViewById<ImageButton>(R.id.btnLogOut).setOnClickListener { cerrarSesion() }
    }

    private fun cerrarSesion() {
        auth.signOut()
        userSession.cerrarSesion()
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun botonesMenu() {
        findViewById<ImageButton>(R.id.btnHome).setOnClickListener { startActivity(Intent(this, Home::class.java)) }
        findViewById<ImageButton>(R.id.btnCarrito).setOnClickListener { startActivity(Intent(this, Carrito::class.java)) }
        findViewById<ImageButton>(R.id.btnNotif).setOnClickListener { startActivity(Intent(this, Notificaciones::class.java)) }
    }

    override fun onDestroy() {
        super.onDestroy()
        pedidosListener?.remove()
    }
}