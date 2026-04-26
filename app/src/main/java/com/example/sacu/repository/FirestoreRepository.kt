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
            .whereIn("estado", listOf("PENDIENTE", "EN_PREPARACION", "LISTO"))
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val pedido = snapshot?.documents?.firstOrNull()?.let { doc ->
                    doc.toObject(Pedido::class.java)?.copy(id = doc.id)
                }
                onUpdate(pedido)
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