package com.example.sacu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sacu.adapter.NotificacionAdapter
import com.example.sacu.model.Notificacion
import com.example.sacu.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class Notificaciones : AppCompatActivity() {
    
    private val repository = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: NotificacionAdapter
    private val listaNotificaciones = mutableListOf<Notificacion>()
    private var notifListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notificaciones)

        botonesMenu()

        val rvNotif = findViewById<RecyclerView>(R.id.rvNotif)
        rvNotif.layoutManager = LinearLayoutManager(this)
        
        adapter = NotificacionAdapter(listaNotificaciones) { notif ->
            val intent = Intent(this, Detalles::class.java)
            intent.putExtra("pedido_id", notif.pedido_id)
            startActivity(intent)
        }
        rvNotif.adapter = adapter

        escucharNotificacionesReales()
    }

    private fun escucharNotificacionesReales() {
        val uid = auth.currentUser?.uid ?: return
        notifListener = repository.escucharNotificaciones(uid, { notificaciones ->
            listaNotificaciones.clear()
            listaNotificaciones.addAll(notificaciones)
            adapter.notifyDataSetChanged()
            Log.d("Notificaciones", "Se cargaron ${notificaciones.size} alertas")
        }, { Log.e("Notif", "Error: ${it.message}") })
    }

    private fun botonesMenu() {
        findViewById<ImageButton>(R.id.btnHome).setOnClickListener { startActivity(Intent(this, Home::class.java)) }
        findViewById<ImageButton>(R.id.btnPerfil).setOnClickListener { startActivity(Intent(this, Perfil::class.java)) }
        findViewById<ImageButton>(R.id.btnCarrito).setOnClickListener { startActivity(Intent(this, Carrito::class.java)) }
    }

    override fun onDestroy() {
        super.onDestroy()
        notifListener?.remove()
    }
}