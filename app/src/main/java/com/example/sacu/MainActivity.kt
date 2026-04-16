package com.example.sacu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sacu.model.Usuario
import com.example.sacu.repository.FirestoreRepository
import com.example.sacu.utils.UserSession
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

        etMatricula = findViewById(R.id.etMatricula)
        etPassword = findViewById(R.id.etPassword)
        btnIngresar = findViewById(R.id.btnIngresar)

        if (auth.currentUser != null) {
            irAHome()
            return
        }

        btnIngresar.setOnClickListener {
            val matricula = etMatricula.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (matricula.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnIngresar.isEnabled = false
            btnIngresar.text = "Verificando..."

            repository.verificarMatricula(matricula, { existe ->
                if (!existe) {
                    mostrarError("Matrícula no autorizada")
                    return@verificarMatricula
                }
                iniciarSesion(matricula, password)
            }, { mostrarError("Sin conexión") })
        }
    }

    private fun iniciarSesion(matricula: String, password: String) {
        val email = "$matricula@sacu.udlap.mx"
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                repository.obtenerUsuario(uid, { usuario ->
                    if (usuario != null) {
                        UserSession(this).guardarUsuario(usuario)
                        irAHome()
                    } else {
                        crearPerfilFirestore(uid, matricula)
                    }
                }, { irAHome() })
            }
            .addOnFailureListener {
                registrarYCrearPerfil(email, password, matricula)
            }
    }

    private fun registrarYCrearPerfil(email: String, password: String, matricula: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                crearPerfilFirestore(result.user?.uid ?: "", matricula)
            }
            .addOnFailureListener { mostrarError("Credenciales incorrectas") }
    }

    private fun crearPerfilFirestore(uid: String, matricula: String) {
        repository.obtenerUsuarioAutorizado(matricula, { datos ->
            val nuevoUsuario = Usuario(
                uid = uid,
                nombre = datos?.get("nombre") as? String ?: "Usuario SACU",
                matricula = matricula,
                tipo = datos?.get("tipo") as? String ?: "estudiante"
            )
            UserSession(this).guardarUsuario(nuevoUsuario)
            repository.guardarUsuario(nuevoUsuario, { irAHome() }, { irAHome() })
        }, { irAHome() })
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