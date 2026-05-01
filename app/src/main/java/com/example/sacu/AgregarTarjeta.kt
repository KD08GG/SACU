package com.example.sacu

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.sacu.model.Tarjeta
import com.example.sacu.repository.FirestoreRepository
import com.example.sacu.utils.UserSession
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

class AgregarTarjeta : AppCompatActivity() {

    private lateinit var userSession: UserSession
    private val repository = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var paymentNotifManager: PaymentNotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agregar_tarjeta)
        userSession = UserSession(this)
        paymentNotifManager = PaymentNotificationManager(this)

        botonesMenu()

        val etApodo   = findViewById<EditText>(R.id.txtApodo)
        val etNombre  = findViewById<EditText>(R.id.txtNombre)
        val etDigitos = findViewById<EditText>(R.id.txtDigitos)
        val etFecha   = findViewById<EditText>(R.id.txtFecha)
        val etCvv     = findViewById<EditText>(R.id.txtCVV)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        btnGuardar.setOnClickListener {
            val apodo  = etApodo.text.toString().trim()
            val nombre = etNombre.text.toString().trim()
            val numero = etDigitos.text.toString().trim()
            val fecha  = etFecha.text.toString().trim()
            val cvv    = etCvv.text.toString().trim()

            if (nombre.isEmpty() || numero.length < 16 || fecha.isEmpty() || cvv.length < 3) {
                Toast.makeText(this, "Por favor completa correctamente los datos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = auth.currentUser?.uid ?: run {
                Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnGuardar.isEnabled = false
            btnGuardar.text = "Verificando..."

            paymentNotifManager.mostrarVerificando(numero.replace(" ", "").takeLast(4))

            repository.verificarYCobrarTemporal(numero, uid,
                onSuccess = { terminacion ->
                    paymentNotifManager.mostrarCargoTemporalAplicado(terminacion)

                    // Simulamos el tiempo de procesamiento de la pasarela
                    Handler(Looper.getMainLooper()).postDelayed({
                        repository.devolverVerificacion(numero, uid,
                            onSuccess = {
                                paymentNotifManager.mostrarTarjetaVerificada(terminacion)

                                val nuevaTarjeta = Tarjeta(
                                    id = UUID.randomUUID().toString(),
                                    nombreTitular = if (apodo.isNotEmpty()) apodo else nombre,
                                    numero = numero,
                                    fechaExpiracion = fecha,
                                    cvv = cvv,
                                    esPredeterminada = userSession.obtenerTarjetas().isEmpty()
                                )
                                userSession.guardarTarjeta(nuevaTarjeta)
                                Toast.makeText(this, "Tarjeta agregada correctamente", Toast.LENGTH_SHORT).show()
                                finish()
                            },
                            onError = {
                                btnGuardar.isEnabled = true
                                btnGuardar.text = "Agregar tarjeta"
                                Toast.makeText(this, "Error al completar la verificación", Toast.LENGTH_LONG).show()
                            }
                        )
                    }, 2000L)
                },
                onError = { reason ->
                    btnGuardar.isEnabled = true
                    btnGuardar.text = "Agregar tarjeta"
                    paymentNotifManager.mostrarErrorTarjeta(reason.mensaje)
                    Toast.makeText(this, reason.mensaje, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun botonesMenu() {
        findViewById<ImageButton>(R.id.btnHome).setOnClickListener { startActivity(Intent(this, Home::class.java)) }
        findViewById<ImageButton>(R.id.btnPerfil).setOnClickListener { startActivity(Intent(this, Perfil::class.java)) }
        findViewById<ImageButton>(R.id.btnCarrito).setOnClickListener { startActivity(Intent(this, Carrito::class.java)) }
        findViewById<ImageButton>(R.id.btnNotif).setOnClickListener { startActivity(Intent(this, Notificaciones::class.java)) }
    }
}
