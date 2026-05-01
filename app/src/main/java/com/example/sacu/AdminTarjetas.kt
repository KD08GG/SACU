package com.example.sacu

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sacu.repository.FirestoreRepository

class AdminTarjetas : AppCompatActivity() {

    private val repository = FirestoreRepository()

    private val numeros = listOf(
        "4242424242424242",
        "4000000000000002",
        "4000000000009995"
    )

    private lateinit var txtSaldos: List<TextView>
    private lateinit var etSaldos: List<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_tarjetas)

        txtSaldos = listOf(
            findViewById(R.id.txtSaldo1),
            findViewById(R.id.txtSaldo2),
            findViewById(R.id.txtSaldo3)
        )
        etSaldos = listOf(
            findViewById(R.id.etSaldo1),
            findViewById(R.id.etSaldo2),
            findViewById(R.id.etSaldo3)
        )

        cargarSaldos()

        findViewById<Button>(R.id.btnGuardarAdmin).setOnClickListener { guardarCambios() }
        findViewById<Button>(R.id.btnResetar).setOnClickListener { resetear() }
    }

    private fun cargarSaldos() {
        numeros.forEachIndexed { i, numero ->
            repository.obtenerSaldoTarjeta(numero) { saldo, _ ->
                txtSaldos[i].text = "$${"%.2f".format(saldo)}"
                etSaldos[i].setText("%.2f".format(saldo))
            }
        }
    }

    private fun guardarCambios() {
        var pendientes = numeros.size
        var huboError = false

        numeros.forEachIndexed { i, numero ->
            val nuevoSaldo = etSaldos[i].text.toString().toDoubleOrNull()
            if (nuevoSaldo == null) {
                Toast.makeText(this, "Saldo inválido en tarjeta ${i + 1}", Toast.LENGTH_SHORT).show()
                return
            }

            repository.actualizarSaldoTarjeta(numero, nuevoSaldo,
                onSuccess = {
                    pendientes--
                    if (pendientes == 0 && !huboError) {
                        Toast.makeText(this, "Saldos actualizados", Toast.LENGTH_SHORT).show()
                        cargarSaldos()
                    }
                },
                onError = {
                    huboError = true
                    Toast.makeText(this, "Error al guardar tarjeta ${i + 1}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun resetear() {
        repository.resetearTarjetasVirtuales(
            onSuccess = {
                Toast.makeText(this, "Valores reseteados a los originales", Toast.LENGTH_SHORT).show()
                cargarSaldos()
            },
            onError = {
                Toast.makeText(this, "Error al resetear: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
