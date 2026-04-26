package com.example.sacu

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore

class OrderNotificationManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "order_notifications"
    private val db = FirebaseFirestore.getInstance()

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Order Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for order status updates"
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showNotification(orderId: String, status: OrderStatus, userId: String) {
        val (title, message, actions, canDismiss) = when (status) {
            is OrderStatus.Pendiente -> "Pedido Realizado" to "En preparación" to emptyList() to false
            is OrderStatus.Listo -> "Pedido Listo" to "Pasa a recogerlo" to emptyList() to false
            is OrderStatus.Recogido -> return // No mostrar notificación
            is OrderStatus.Terminado -> return // No mostrar notificación
            is OrderStatus.Cancelado -> "Pedido Cancelado" to status.reason to listOf(
                NotificationCompat.Action.Builder(
                    0, "Dismiss", PendingIntent.getBroadcast(
                        context, 0, Intent("DISMISS_NOTIFICATION").apply { putExtra("orderId", orderId) },
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                ).build()
            ) to true
        }

        // Show push notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .apply {
                actions.forEach { addAction(it) }
            }
            .build()

        notificationManager.notify(orderId.hashCode(), notification)

        // Save to Firestore
        val notificationData = hashMapOf(
            "orderId" to orderId,
            "userId" to userId,
            "title" to title,
            "message" to message,
            "timestamp" to System.currentTimeMillis(),
            "canDismiss" to canDismiss,
            "status" to status::class.simpleName,
            "isActive" to true
        )

        db.collection("notificaciones").document(orderId).set(notificationData)
    }

    fun dismissNotification(orderId: String) {
        notificationManager.cancel(orderId.hashCode())
        // Mark as inactive in Firestore
        db.collection("notificaciones").document(orderId).update("isActive", false)
    }
}