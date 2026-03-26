package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PagoProcesado : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pago_procesado)

        //VARIABLES
        val numPedido = findViewById<TextView>(R.id.txtNumPedido)
        val tiempoEspera = findViewById<TextView>(R.id.txtTiempo)


        // BOTONES PANTALLA
        val btnAceptar = findViewById<Button>(R.id.btnHome)

        //FUNCIONES BOTONES
        btnAceptar.setOnClickListener {
            intent = Intent(this, Home::class.java)
            startActivity(intent)
         }

    }
}