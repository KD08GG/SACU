package com.example.sacu

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sacu.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private val repository = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var etMatricula: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnIngresar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vincular vistas
        etMatricula = findViewById(R.id.etMatricula)
        etPassword = findViewById(R.id.etPassword)
        btnIngresar = findViewById(R.id.btnIngresar)

        // Si el usuario ya tiene sesión activa, ir directo a Home
        if (auth.currentUser != null) {
            irAHome()
            return
        }

        btnIngresar.setOnClickListener {
            val matricula = etMatricula.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validaciones básicas
            if (matricula.isEmpty()) {
                etMatricula.error = "Ingresa tu matrícula"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Ingresa tu contraseña"
                return@setOnClickListener
            }

            btnIngresar.isEnabled = false
            btnIngresar.text = "Verificando..."

            // Paso 1: verificar que la matrícula esté autorizada
            repository.verificarMatricula(
                matricula = matricula,
                onSuccess = { existe ->
                    if (!existe) {
                        mostrarError("Matrícula no registrada en el sistema")
                        return@verificarMatricula
                    }
                    // Paso 2: iniciar sesión con Firebase Auth
                    // El email que usamos es matricula@sacu.udlap.mx
                    val email = "$matricula@sacu.udlap.mx"
                    iniciarSesion(email, password)
                },
                onError = { error ->
                    mostrarError("Error de conexión: ${error.message}")
                }
            )
        }
    }

    private fun iniciarSesion(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // Login exitoso → ir a Home
                irAHome()
            }
            .addOnFailureListener { error ->
                // Si falla el login puede ser porque no tiene cuenta aún
                // En el MVP el admin crea las cuentas, pero para pruebas
                // intentamos registrar si el login falla
                val matricula = email.replace("@sacu.udlap.mx", "")
                registrarUsuario(email, password, matricula)
            }
    }

    private fun registrarUsuario(email: String, password: String, matricula: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                // Guardar perfil en Firestore
                repository.obtenerUsuarioAutorizado(
                    matricula = matricula,
                    onSuccess = { datos ->
                        val usuario = com.example.sacu.model.Usuario(
                            uid = uid,
                            nombre = datos?.get("nombre") as? String ?: "",
                            matricula = matricula,
                            tipo = datos?.get("tipo") as? String ?: "estudiante"
                        )
                        repository.guardarUsuario(
                            usuario = usuario,
                            onSuccess = { irAHome() },
                            onError = { irAHome() }
                        )
                    },
                    onError = { irAHome() }
                )
            }
            .addOnFailureListener { error ->
                mostrarError("Credenciales incorrectas")
            }
    }

    private fun irAHome() {
        startActivity(Intent(this, Home::class.java))
        finish()
    }

    private fun mostrarError(mensaje: String) {
        runOnUiThread {
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            btnIngresar.isEnabled = true
            btnIngresar.text = "Ingresar"
        }
    }
}