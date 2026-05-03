package com.example.sacu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.sacu.repository.FirestoreRepository
import com.google.firebase.firestore.ListenerRegistration

class PagoProcesado : AppCompatActivity() {

    private val repository = FirestoreRepository()
    private var estadoListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pago_procesado)

        val txtNumPedido = findViewById<TextView>(R.id.txtNumPedido)
        val txtTiempo    = findViewById<TextView>(R.id.txtTiempo)
        val btnAceptar   = findViewById<Button>(R.id.btnHome)

        val numeroPedido = intent.getIntExtra("numero_pedido", 0)
        txtNumPedido.text = if (numeroPedido > 0) numeroPedido.toString() else "---"

        estadoListener = repository.escucharEstadoGlobal(
            onUpdate = { tiempoEspera, _ ->
                txtTiempo.text = getString(R.string.wait_time_format, tiempoEspera)
            },
            onError = { txtTiempo.text = "--:--" }
        )

        btnAceptar.setOnClickListener {
            startActivity(
                Intent(this, Home::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        estadoListener?.remove()
    }
}
