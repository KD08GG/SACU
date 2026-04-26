package com.example.sacu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sacu.adapter.ItemCarritoAdapter
import com.example.sacu.model.Pedido
import com.example.sacu.repository.carritoTotal
import com.example.sacu.repository.Compra
import com.example.sacu.repository.FirestoreRepository
import com.example.sacu.utils.UserSession
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Pagar : AppCompatActivity() {

    private lateinit var comidasAdapter: ItemCarritoAdapter
    private var listaComidas = carritoTotal

    private val compra = Compra()
    private val repository = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var userSession: UserSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pagar)
        userSession = UserSession(this)

        botonesMenu()

        val total = findViewById<TextView>(R.id.txtTotal)
        val btnComprar = findViewById<Button>(R.id.btnComprar)
        val rvProductos = findViewById<RecyclerView>(R.id.rvProductos)
        val txtFecha = findViewById<TextView>(R.id.txtFecha)
        val txtNumPedido = findViewById<TextView>(R.id.txtNumPedido)
        val btnCambiar = findViewById<ImageButton>(R.id.btncambiar)

        // Configurar fecha actual
        txtFecha.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        // Configurar siguiente número de pedido
        repository.obtenerSiguienteNumeroPedido { numero ->
            txtNumPedido.text = numero.toString()
        }

        // Configurar información de tarjeta
        actualizarInfoTarjeta()

        setupRecyclerViews(rvProductos)

        val totalAmount = compra.totalAPagar()
        total.text = getString(R.string.total_label, totalAmount.toString())

        btnComprar.setOnClickListener {
            val tarjeta = userSession.obtenerTarjetaPredeterminada()
            if (tarjeta == null) {
                Toast.makeText(this, "No tienes seleccionada ningún método de pago", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            procesarPedido(totalAmount)
        }

        btnCambiar.setOnClickListener {
            val intent = Intent(this, MetodosDePago::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarInfoTarjeta()
    }

    private fun actualizarInfoTarjeta() {
        val tarjeta = userSession.obtenerTarjetaPredeterminada()
        val textTitulo = findViewById<TextView>(R.id.textTarjetaPred)
        val textNumero = findViewById<TextView>(R.id.textNumTarjeta)
        
        if (tarjeta != null) {
            textTitulo.text = tarjeta.nombreTitular
            textNumero.text = "**** **** **** ${tarjeta.numero.takeLast(4)}"
        } else {
            textTitulo.text = "Sin tarjeta"
            textNumero.text = "Agrega una para pagar"
        }
    }

    private fun procesarPedido(totalAmount: Double) {
        val uid = auth.currentUser?.uid ?: return
        btnComprarUI(getString(R.string.processing))

        repository.obtenerSiguienteNumeroPedido { siguienteNumero ->
            val nuevoPedido = Pedido(
                usuario_id = uid,
                estado = "PENDIENTE",
                total = totalAmount,
                numero_fila = siguienteNumero,
                fecha = Timestamp(Date()),
                productos = ArrayList(carritoTotal)
            )
            
            repository.crearPedido(nuevoPedido, 
                onSuccess = { pedidoId ->
                    compra.limpiarCarrito()
                    val intent = Intent(this, PagoProcesado::class.java).apply {
                        putExtra("pedido_id", pedidoId)
                        putExtra("numero_pedido", siguienteNumero)
                    }
                    startActivity(intent)
                    finish()
                },
                onError = { error ->
                    btnComprarUI("Comprar")
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun btnComprarUI(texto: String) {
        findViewById<Button>(R.id.btnComprar).apply {
            text = texto
            isEnabled = (texto != getString(R.string.processing))
        }
    }

    private fun setupRecyclerViews(rvComidas: RecyclerView) {
        rvComidas.layoutManager = LinearLayoutManager(this)
        comidasAdapter = ItemCarritoAdapter(listaComidas)
        rvComidas.adapter = comidasAdapter
    }

    private fun botonesMenu() {
        findViewById<ImageButton>(R.id.btnHome).setOnClickListener { startActivity(Intent(this, Home::class.java)) }
        findViewById<ImageButton>(R.id.btnPerfil).setOnClickListener { startActivity(Intent(this, Perfil::class.java)) }
        findViewById<ImageButton>(R.id.btnCarrito).setOnClickListener { startActivity(Intent(this, Carrito::class.java)) }
        findViewById<ImageButton>(R.id.btnNotif).setOnClickListener { startActivity(Intent(this, Notificaciones::class.java)) }
    }
}