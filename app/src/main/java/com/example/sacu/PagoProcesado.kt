package com.example.sacu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class PagoProcesado : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pago_procesado)

        val txtNumPedido = findViewById<TextView>(R.id.txtNumPedido)
        val btnAceptar = findViewById<Button>(R.id.btnHome)

        // MOSTRAR EL NÚMERO DE TURNO (numero_pedido) EN LUGAR DEL ID
        val numeroPedido = intent.getIntExtra("numero_pedido", 0)
        txtNumPedido.text = if (numeroPedido > 0) numeroPedido.toString() else "---"

        btnAceptar.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}