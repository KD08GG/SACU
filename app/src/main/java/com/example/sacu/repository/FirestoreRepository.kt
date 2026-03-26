package com.example.sacu.repository

import com.example.sacu.model.Notificacion
import com.example.sacu.model.Pedido
import com.example.sacu.model.Producto
import com.example.sacu.model.Transaccion
import com.example.sacu.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FirestoreRepository {

    // Conexión a Firestore y Auth
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ─────────────────────────────────────────
    // USUARIOS
    // ─────────────────────────────────────────

    // Verifica si una matrícula existe en la lista blanca
    fun verificarMatricula(
        matricula: String,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("usuarios_autorizados")
            .document(matricula)
            .get()
            .addOnSuccessListener { document ->
                onSuccess(document.exists())
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    // Obtiene los datos del documento en usuarios_autorizados
    fun obtenerUsuarioAutorizado(
        matricula: String,
        onSuccess: (Map<String, Any>?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("usuarios_autorizados")
            .document(matricula)
            .get()
            .addOnSuccessListener { document ->
                onSuccess(document.data)
            }
            .addOnFailureListener { onError(it) }
    }

    // Guarda el perfil del usuario en Firestore después del registro
    fun guardarUsuario(
        usuario: Usuario,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("usuarios")
            .document(usuario.uid)
            .set(usuario)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    // Obtiene los datos del usuario actual
    fun obtenerUsuario(
        uid: String,
        onSuccess: (Usuario?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val usuario = document.toObject(Usuario::class.java)
                onSuccess(usuario)
            }
            .addOnFailureListener { onError(it) }
    }

    // ─────────────────────────────────────────
    // PRODUCTOS
    // ─────────────────────────────────────────

    // Obtiene productos filtrados por categoría (ej: "Desayunos", "Comidas")
    fun obtenerProductosPorCategoria(
        categoria: String,
        onSuccess: (List<Producto>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("productos")
            .whereEqualTo("categoria", categoria)
            .whereEqualTo("disponible", true)
            .get()
            .addOnSuccessListener { result ->
                val productos = result.documents.map { doc ->
                    val producto = doc.toObject(Producto::class.java)!!
                    producto.copy(id = doc.id)
                }
                onSuccess(productos)
            }
            .addOnFailureListener { onError(it) }
    }

    // ─────────────────────────────────────────
    // PEDIDOS
    // ─────────────────────────────────────────

    // Crea un nuevo pedido en Firestore
    fun crearPedido(
        pedido: Pedido,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("pedidos")
            .add(pedido)
            .addOnSuccessListener { documentRef ->
                onSuccess(documentRef.id)
            }
            .addOnFailureListener { onError(it) }
    }

    // Escucha en tiempo real el pedido activo del usuario
    // Retorna un ListenerRegistration para poder cancelarlo cuando la Activity se cierre
    fun escucharPedidoActivo(
        usuarioId: String,
        onUpdate: (Pedido?) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection("pedidos")
            .whereEqualTo("usuario_id", usuarioId)
            .whereIn("estado", listOf("PENDIENTE", "EN_PREPARACION", "LISTO"))
            .limit(1)
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

    // Actualiza el estado de un pedido
    fun actualizarEstadoPedido(
        pedidoId: String,
        nuevoEstado: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("pedidos")
            .document(pedidoId)
            .update("estado", nuevoEstado)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    // ─────────────────────────────────────────
    // NOTIFICACIONES
    // ─────────────────────────────────────────

    // Escucha en tiempo real las notificaciones no leídas del usuario
    fun escucharNotificaciones(
        usuarioId: String,
        onUpdate: (List<Notificacion>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection("notificaciones")
            .whereEqualTo("usuario_id", usuarioId)
            .whereEqualTo("leida", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val notificaciones = snapshot?.documents?.map { doc ->
                    doc.toObject(Notificacion::class.java)!!.copy(id = doc.id)
                } ?: emptyList()
                onUpdate(notificaciones)
            }
    }

    // Marca una notificación como leída
    fun marcarNotificacionLeida(
        notificacionId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("notificaciones")
            .document(notificacionId)
            .update("leida", true)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    // ─────────────────────────────────────────
    // TRANSACCIONES
    // ─────────────────────────────────────────

    // Crea una transacción antes de enviar al usuario a pagar
    fun crearTransaccion(
        transaccion: Transaccion,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("transacciones")
            .add(transaccion)
            .addOnSuccessListener { ref -> onSuccess(ref.id) }
            .addOnFailureListener { onError(it) }
    }
}