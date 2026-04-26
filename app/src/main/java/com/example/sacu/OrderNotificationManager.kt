package com.example.sacu

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore

class OrderNotificationManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "order_notifications"
    private val db = FirebaseFirestore.getInstance()

    private data class NotificationContent(
        val title: String,
        val message: String,
        val actions: List<NotificationCompat.Action> = emptyList(),
        val canDismiss: Boolean = false
    )

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Order Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for order status updates"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(orderId: String, status: OrderStatus, userId: String, numeroPedido: Int = 0) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val content = when (status) {
            is OrderStatus.Pendiente -> NotificationContent("Pedido Realizado", "En preparación")
            is OrderStatus.Listo -> NotificationContent("Pedido Listo", "Pasa a recogerlo")
            is OrderStatus.Recogido -> return // No mostrar notificación
            is OrderStatus.Terminado -> return // No mostrar notificación
            is OrderStatus.Cancelado -> NotificationContent(
                "Pedido Cancelado",
                status.reason,
                listOf(
                    NotificationCompat.Action.Builder(
                        0, "Dismiss", PendingIntent.getBroadcast(
                            context, 0, Intent("DISMISS_NOTIFICATION").apply { putExtra("orderId", orderId) },
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    ).build()
                ),
                true
            )
        }

        // Show push notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(content.title)
            .setContentText(content.message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .apply {
                content.actions.forEach { addAction(it) }
            }
            .build()

        notificationManager.notify(orderId.hashCode(), notification)

        // Save to Firestore
        val notificationData = hashMapOf(
            "usuario_id"    to userId,
            "pedido_id"     to orderId,
            "numero_pedido" to numeroPedido,
            "mensaje"       to content.message,
            "leida"         to false,
            "fecha"         to com.google.firebase.Timestamp.now()
        )

        db.collection("notificaciones").document(orderId).set(notificationData)
    }

    fun dismissNotification(orderId: String) {
        notificationManager.cancel(orderId.hashCode())
        // Mark as read in Firestore
        db.collection("notificaciones").document(orderId).update("leida", true)
    }
}
