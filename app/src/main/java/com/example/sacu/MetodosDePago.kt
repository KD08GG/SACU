package com.example.sacu

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sacu.adapter.TarjetaAdapter
import com.example.sacu.model.Tarjeta
import com.example.sacu.utils.UserSession

class MetodosDePago : AppCompatActivity() {

    private lateinit var userSession: UserSession
    private lateinit var tarjetaAdapter: TarjetaAdapter
    private var tarjetaParaBorrar: Tarjeta? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_metodos_de_pago)
        userSession = UserSession(this)

        botonesMenu()

        // RecyclerView de otras tarjetas
        val rvTarjetas = findViewById<RecyclerView>(R.id.rvProductos)
        rvTarjetas.layoutManager = LinearLayoutManager(this)
        
        tarjetaAdapter = TarjetaAdapter(emptyList()) { tarjeta ->
            mostrarDialogoBorrar(tarjeta)
        }
        rvTarjetas.adapter = tarjetaAdapter

        // Botones y Diálogos
        val btnAgregarTarjetas = findViewById<ImageButton>(R.id.btnMasTarjetas)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        val btnBorrarConfirmar = findViewById<Button>(R.id.btnBorrarTarjeta)
        val frameBorrar = findViewById<FrameLayout>(R.id.borrarNotif)

        btnAgregarTarjetas.setOnClickListener {
            startActivity(Intent(this, AgregarTarjeta::class.java))
        }

        btnCancelar.setOnClickListener {
            frameBorrar.visibility = FrameLayout.INVISIBLE
        }

        btnBorrarConfirmar.setOnClickListener {
            tarjetaParaBorrar?.let {
                userSession.eliminarTarjeta(it.id)
                Toast.makeText(this, "Tarjeta eliminada", Toast.LENGTH_SHORT).show()
                actualizarInterfaz()
            }
            frameBorrar.visibility = FrameLayout.INVISIBLE
        }
        
        findViewById<ImageButton>(R.id.btnBorrar).setOnClickListener {
            userSession.obtenerTarjetaPredeterminada()?.let { mostrarDialogoBorrar(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarInterfaz()
    }

    private fun actualizarInterfaz() {
        val todasLasTarjetas = userSession.obtenerTarjetas()
        val tarjetaPred = todasLasTarjetas.find { it.esPredeterminada }
        val otrasTarjetas = todasLasTarjetas.filter { !it.esPredeterminada }

        // Actualizar Tarjeta Predeterminada
        val textTitulo = findViewById<TextView>(R.id.textTarjetaPred)
        val textNumero = findViewById<TextView>(R.id.textNumTarjeta)
        val btnBorrarIcon = findViewById<ImageButton>(R.id.btnBorrar)

        if (tarjetaPred != null) {
            textTitulo.text = tarjetaPred.nombreTitular
            textNumero.text = "**** **** **** ${tarjetaPred.numero.takeLast(4)}"
            btnBorrarIcon.visibility = View.VISIBLE
        } else {
            textTitulo.text = "Sin tarjeta"
            textNumero.text = "Agrega una para pagar"
            btnBorrarIcon.visibility = View.GONE
        }

        // Actualizar Lista (RecyclerView)
        tarjetaAdapter.actualizarLista(otrasTarjetas)
    }

    private fun mostrarDialogoBorrar(tarjeta: Tarjeta) {
        tarjetaParaBorrar = tarjeta
        val frameBorrar = findViewById<FrameLayout>(R.id.borrarNotif)
        
        // Buscamos el TextView que muestra el número en el diálogo
        // Dado que el XML no tiene IDs específicos para esos TextViews dentro de la estructura anidada,
        // vamos a buscar el segundo TextView dentro del LinearLayout del diálogo.
        val container = frameBorrar.getChildAt(2) as? FrameLayout
        val linearLayout = container?.getChildAt(0) as? android.widget.LinearLayout
        val txtNumeroDialogo = linearLayout?.getChildAt(1) as? TextView
        
        txtNumeroDialogo?.text = "**** **** **** ${tarjeta.numero.takeLast(4)}"
        
        frameBorrar.visibility = View.VISIBLE
    }

    private fun botonesMenu() {
        findViewById<ImageButton>(R.id.btnHome).setOnClickListener { startActivity(Intent(this, Home::class.java)) }
        findViewById<ImageButton>(R.id.btnPerfil).setOnClickListener { startActivity(Intent(this, Perfil::class.java)) }
        findViewById<ImageButton>(R.id.btnCarrito).setOnClickListener { startActivity(Intent(this, Carrito::class.java)) }
        findViewById<ImageButton>(R.id.btnNotif).setOnClickListener { startActivity(Intent(this, Notificaciones::class.java)) }
    }
}