package com.example.sacu.repository

import android.util.Log
import com.example.sacu.model.Notificacion
import com.example.sacu.model.Pedido
import com.example.sacu.model.Producto
import com.example.sacu.model.Usuario
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.Calendar

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    // USUARIOS
    fun verificarMatricula(matricula: String, onSuccess: (Boolean) -> Unit, onError: (Exception) -> Unit) {
        db.collection("usuarios_autorizados").document(matricula).get()
             .addOnSuccessListener { document -> onSuccess(document.exists()) }
            .addOnFailureListener { onError(it) }
    }

    fun obtenerUsuarioAutorizado(matricula: String, onSuccess: (Map<String, Any>?) -> Unit, onError: (Exception) -> Unit) {
        db.collection("usuarios_autorizados").document(matricula).get()
            .addOnSuccessListener { document -> onSuccess(document.data) }
            .addOnFailureListener { onError(it) }
    }

    fun guardarUsuario(usuario: Usuario, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        db.collection("usuarios").document(usuario.uid).set(usuario)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun obtenerUsuario(uid: String, onSuccess: (Usuario?) -> Unit, onError: (Exception) -> Unit) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document -> 
                val user = document.toObject(Usuario::class.java)
                onSuccess(user) 
            }
            .addOnFailureListener { onError(it) }
    }

    // PRODUCTOS
    fun obtenerProductosPorCategoria(categoria: String, onSuccess: (List<Producto>) -> Unit, onError: (Exception) -> Unit) {
        db.collection("productos")
            .whereEqualTo("categoria", categoria)
            .get()
            .addOnSuccessListener { result ->
                val productos = result.documents.mapNotNull { doc ->
                    doc.toObject(Producto::class.java)?.copy(id = doc.id)
                }
                onSuccess(productos)
            }
            .addOnFailureListener { onError(it) }
    }

    // PEDIDOS
    fun obtenerSiguienteNumeroPedido(onResult: (Int) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        db.collection("pedidos")
            .whereGreaterThanOrEqualTo("fecha", Timestamp(startOfDay))
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onResult(1)
                } else {
                    val ultimoPedido = snapshot.documents[0].toObject(Pedido::class.java)
                    val ultimoNumero = ultimoPedido?.numero_fila ?: 0
                    onResult(ultimoNumero + 1)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error turno: ${e.message}")
                onResult(1) 
            }
    }

    fun crearPedido(pedido: Pedido, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        db.collection("pedidos").add(pedido)
            .addOnSuccessListener { onSuccess(it.id) }
            .addOnFailureListener { onError(it) }
    }

    fun escucharPedidoActivo(usuarioId: String, onUpdate: (Pedido?) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        return db.collection("pedidos")
            .whereEqualTo("usuario_id", usuarioId)
            .whereIn("estado", listOf("PENDIENTE", "EN_PREPARACION", "LISTO", "RECOGIDO", "TERMINADO"))
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                
                val pedidos = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Pedido::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                // BUSCAMOS EL SIGUIENTE PEDIDO EN PREPARACIÓN (CASCADA)
                // Siempre priorizamos mostrar el que se está cocinando.
                val enPreparacion = pedidos.find { it.estado == "PENDIENTE" || it.estado == "EN_PREPARACION" }
                
                if (enPreparacion != null) {
                    onUpdate(enPreparacion)
                } else {
                    // Si no hay ninguno en preparación, enviamos el más reciente LISTO (para notificar y cerrar)
                    onUpdate(pedidos.find { it.estado == "LISTO" })
                }
            }
    }

    fun escucharTodosLosPedidosDelUsuario(usuarioId: String, onUpdate: (List<Pedido>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        return db.collection("pedidos")
            .whereEqualTo("usuario_id", usuarioId)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val pedidos = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Pedido::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                onUpdate(pedidos)
            }
    }

    fun escucharPedidosEnFila(onUpdate: (Int) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        return db.collection("pedidos")
            .whereIn("estado", listOf("PENDIENTE", "EN_PREPARACION"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                onUpdate(snapshot?.size() ?: 0)
            }
    }

    fun escucharEstadoGlobal(onUpdate: (tiempoEspera: Int, turnoActual: Int) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        return db.collection("global_state").document("current")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val tiempoEspera = snapshot?.getLong("tiempo_espera")?.toInt() ?: 0
                val turnoActual  = snapshot?.getLong("turno_actual")?.toInt()  ?: 0
                onUpdate(tiempoEspera, turnoActual)
            }
    }

    // PAGOS

    // ADMIN

    fun obtenerSaldoTarjeta(numero: String, onResult: (saldo: Double, activa: Boolean) -> Unit) {
        val id = numero.replace(" ", "").replace("-", "")
        db.collection("tarjetas_virtuales").document(id).get()
            .addOnSuccessListener { snap ->
                onResult(snap.getDouble("saldo") ?: 0.0, snap.getBoolean("activa") ?: false)
            }
            .addOnFailureListener { onResult(0.0, false) }
    }

    fun actualizarSaldoTarjeta(numero: String, nuevoSaldo: Double, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val id = numero.replace(" ", "").replace("-", "")
        db.collection("tarjetas_virtuales").document(id)
            .update("saldo", nuevoSaldo)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun resetearTarjetasVirtuales(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val col = db.collection("tarjetas_virtuales")
        val batch = db.batch()
        batch.set(col.document("4242424242424242"), mapOf("saldo" to 1000.0, "activa" to true))
        batch.set(col.document("4000000000000002"), mapOf("saldo" to 0.0,    "activa" to true))
        batch.set(col.document("4000000000009995"), mapOf("saldo" to 500.0,  "activa" to false))
        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    /** Crea las tarjetas de prueba en Firestore si aún no existen. Idempotente. */
    fun inicializarTarjetasVirtuales() {
        val tarjetasPrueba = listOf(
            Triple("4242424242424242", 1000.0, true),   // aprueba siempre
            Triple("4000000000000002", 0.0,    true),   // sin fondos
            Triple("4000000000009995", 500.0,  false)   // declinada
        )
        val coleccion = db.collection("tarjetas_virtuales")
        for ((id, saldo, activa) in tarjetasPrueba) {
            coleccion.document(id).get().addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    coleccion.document(id).set(mapOf("saldo" to saldo, "activa" to activa))
                }
            }
        }
    }

    /**
     * Verifica la tarjeta con un cargo temporal de $10.
     * Si la tarjeta existe, está activa y tiene fondos: descuenta los $10 y llama onSuccess.
     * El caller es responsable de llamar a devolverVerificacion() después del delay simulado.
     */
    fun verificarYCobrarTemporal(
        numero: String,
        usuarioId: String,
        onSuccess: (terminacion: String) -> Unit,
        onError: (PaymentErrorReason) -> Unit
    ) {
        val hash = hashNumero(numero)
        val terminacion = numero.replace(" ", "").replace("-", "").takeLast(4)
        val tarjetaRef = db.collection("tarjetas_virtuales").document(hash)

        db.runTransaction { tx ->
            val snap = tx.get(tarjetaRef)
            if (!snap.exists()) throw PaymentException(PaymentErrorReason.TARJETA_INVALIDA)
            if (snap.getBoolean("activa") != true) throw PaymentException(PaymentErrorReason.TARJETA_DECLINADA)
            val saldo = snap.getDouble("saldo") ?: 0.0
            if (saldo < 10.0) throw PaymentException(PaymentErrorReason.FONDOS_INSUFICIENTES)
            tx.update(tarjetaRef, "saldo", saldo - 10.0)
        }.addOnSuccessListener {
            registrarTransaccion(usuarioId, terminacion, 10.0, "VERIFICACION", "APROBADO", null)
            onSuccess(terminacion)
        }.addOnFailureListener { e ->
            val reason = extraerError(e)
            registrarTransaccion(usuarioId, terminacion, 10.0, "VERIFICACION", "RECHAZADO", reason.name)
            onError(reason)
        }
    }

    /** Devuelve los $10 del cargo temporal después de la verificación exitosa. */
    fun devolverVerificacion(
        numero: String,
        usuarioId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val hash = hashNumero(numero)
        val terminacion = numero.replace(" ", "").replace("-", "").takeLast(4)
        val tarjetaRef = db.collection("tarjetas_virtuales").document(hash)

        db.runTransaction { tx ->
            val saldo = tx.get(tarjetaRef).getDouble("saldo") ?: 0.0
            tx.update(tarjetaRef, "saldo", saldo + 10.0)
        }.addOnSuccessListener {
            registrarTransaccion(usuarioId, terminacion, 10.0, "DEVOLUCION", "APROBADO", null)
            onSuccess()
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error devolviendo verificación: ${e.message}")
            onError(e)
        }
    }

    /**
     * Procesa el cobro real de un pedido.
     * Verifica saldo, descuenta atómicamente y registra la transacción.
     */
    fun procesarPago(
        numero: String,
        monto: Double,
        usuarioId: String,
        onSuccess: (terminacion: String) -> Unit,
        onError: (PaymentErrorReason) -> Unit
    ) {
        val hash = hashNumero(numero)
        val terminacion = numero.replace(" ", "").replace("-", "").takeLast(4)
        val tarjetaRef = db.collection("tarjetas_virtuales").document(hash)

        db.runTransaction { tx ->
            val snap = tx.get(tarjetaRef)
            if (!snap.exists()) throw PaymentException(PaymentErrorReason.TARJETA_INVALIDA)
            if (snap.getBoolean("activa") != true) throw PaymentException(PaymentErrorReason.TARJETA_DECLINADA)
            val saldo = snap.getDouble("saldo") ?: 0.0
            if (saldo < monto) throw PaymentException(PaymentErrorReason.FONDOS_INSUFICIENTES)
            tx.update(tarjetaRef, "saldo", saldo - monto)
        }.addOnSuccessListener {
            registrarTransaccion(usuarioId, terminacion, monto, "COBRO", "APROBADO", null)
            onSuccess(terminacion)
        }.addOnFailureListener { e ->
            val reason = extraerError(e)
            registrarTransaccion(usuarioId, terminacion, monto, "COBRO", "RECHAZADO", reason.name)
            onError(reason)
        }
    }

    private fun registrarTransaccion(
        usuarioId: String,
        terminacion: String,
        monto: Double,
        tipo: String,
        estado: String,
        motivo: String?
    ) {
        val data = hashMapOf<String, Any?>(
            "usuario_id"  to usuarioId,
            "terminacion" to terminacion,
            "monto"       to monto,
            "tipo"        to tipo,
            "estado"      to estado,
            "motivo"      to motivo,
            "timestamp"   to Timestamp.now()
        )
        db.collection("transacciones").add(data)
            .addOnFailureListener { e -> Log.e("Firestore", "Error registrando transacción: ${e.message}") }
    }

    private fun hashNumero(numero: String): String {
        return numero.replace(" ", "").replace("-", "")
    }

    private fun extraerError(e: Exception): PaymentErrorReason = when {
        e is PaymentException -> e.reason
        e.cause is PaymentException -> (e.cause as PaymentException).reason
        else -> PaymentErrorReason.ERROR_RED
    }

    fun escucharNotificaciones(usuarioId: String, onUpdate: (List<Notificacion>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        return db.collection("notificaciones")
            .whereEqualTo("usuario_id", usuarioId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val notificaciones = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Notificacion::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                onUpdate(notificaciones)
            }
    }
}