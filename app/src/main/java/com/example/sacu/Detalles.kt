package com.example.sacu

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.sacu.model.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class Detalles : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalles)

        botonesMenu()

        val pedidoId = intent.getStringExtra("pedido_id") ?: return

        // Vincular vistas
        val numPedido = findViewById<TextView>(R.id.txtNumPedido)
        val estado = findViewById<TextView>(R.id.txtEstado)
        val fecha = findViewById<TextView>(R.id.txtFecha)
        val hora = findViewById<TextView>(R.id.txtHora)
        val pago = findViewById<TextView>(R.id.txtMetodo)
        val folio = findViewById<TextView>(R.id.txtFolio)

        // Cargar información real desde Firestore
        db.collection("pedidos").document(pedidoId).get()
            .addOnSuccessListener { document ->
                val pedido = document.toObject(Pedido::class.java)
                pedido?.let {
                    numPedido.text = it.numero_fila.toString()
                    estado.text = translateEstado(it.estado)
                    
                    it.fecha?.toDate()?.let { date ->
                        fecha.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                        hora.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
                    }
                    
                    folio.text = document.id.takeLast(8).uppercase()
                    pago.text = "Tarjeta Predeterminada" // O el método usado
                }
            }
    }

    private fun translateEstado(estado: String): String {
        return when (estado) {
            "PENDIENTE" -> "En fila"
            "EN_PREPARACION" -> "Cocinando"
            "LISTO" -> "¡Listo!"
            "RECOGIDO" -> "Recogido"
            else -> estado
        }
    }

    private fun botonesMenu() {
        findViewById<ImageButton>(R.id.btnHome).setOnClickListener { startActivity(Intent(this, Home::class.java)) }
        findViewById<ImageButton>(R.id.btnPerfil).setOnClickListener { startActivity(Intent(this, Perfil::class.java)) }
        findViewById<ImageButton>(R.id.btnCarrito).setOnClickListener { startActivity(Intent(this, Carrito::class.java)) }
        findViewById<ImageButton>(R.id.btnNotif).setOnClickListener { startActivity(Intent(this, Notificaciones::class.java)) }
    }
}