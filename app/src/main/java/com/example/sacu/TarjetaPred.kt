package com.example.sacu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sacu.adapter.TarjetaSeleccionAdapter
import com.example.sacu.model.Tarjeta
import com.example.sacu.utils.UserSession

class TarjetaPred : AppCompatActivity() {
    
    private lateinit var userSession: UserSession
    private lateinit var adapter: TarjetaSeleccionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tarjeta_pred)
        userSession = UserSession(this)

        botonesMenu()

        val rvTarjetas = findViewById<RecyclerView>(R.id.rvTarjetas)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        rvTarjetas.layoutManager = LinearLayoutManager(this)
        
        val tarjetas = userSession.obtenerTarjetas()
        
        adapter = TarjetaSeleccionAdapter(tarjetas) { tarjetaSeleccionada ->
            // Al seleccionar una estrella, marcamos esta tarjeta como predeterminada y las demás no
            tarjetas.forEach { it.esPredeterminada = (it.id == tarjetaSeleccionada.id) }
        }
        rvTarjetas.adapter = adapter

        btnGuardar.setOnClickListener {
            // Guardar el estado de todas las tarjetas (una será predeterminada ahora)
            tarjetas.forEach { userSession.guardarTarjeta(it) }
            Toast.makeText(this, "Tarjeta predeterminada actualizada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun botonesMenu() {
        findViewById<ImageButton>(R.id.btnHome).setOnClickListener { startActivity(Intent(this, Home::class.java)) }
        findViewById<ImageButton>(R.id.btnPerfil).setOnClickListener { startActivity(Intent(this, Perfil::class.java)) }
        findViewById<ImageButton>(R.id.btnCarrito).setOnClickListener { startActivity(Intent(this, Carrito::class.java)) }
        findViewById<ImageButton>(R.id.btnNotif).setOnClickListener { startActivity(Intent(this, Notificaciones::class.java)) }
    }
}