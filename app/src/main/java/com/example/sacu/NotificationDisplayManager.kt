package com.example.sacu

import com.google.firebase.firestore.FirebaseFirestore

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val canDismiss: Boolean = false
)

class NotificationDisplayManager(
    private val orderStateManager: OrderStateManager,
    private val globalStateManager: GlobalStateManager
) {
    private val db = FirebaseFirestore.getInstance()

    fun getNotificationsToDisplay(userId: String, onSuccess: (List<NotificationItem>) -> Unit, onFailure: (Exception) -> Unit) {
        val notifications = mutableListOf<NotificationItem>()

        // Add global time estimate notification
        globalStateManager.getGlobalState(
            onSuccess = { globalState ->
                val timeNotification = NotificationItem(
                    id = "time_estimate",
                    title = "Tiempo de espera aproximado",
                    message = "${globalState.tiempoEspera} minutos",
                    timestamp = System.currentTimeMillis()
                )
                notifications.add(timeNotification)

                // Query active notifications for user
                db.collection("notificaciones")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isActive", true)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            val notification = NotificationItem(
                                id = document.id,
                                title = document.getString("title") ?: "",
                                message = document.getString("message") ?: "",
                                timestamp = document.getLong("timestamp") ?: 0L,
                                canDismiss = document.getBoolean("canDismiss") ?: false
                            )
                            notifications.add(notification)
                        }
                        onSuccess(notifications)
                    }
                    .addOnFailureListener { onFailure(it) }
            },
            onFailure = onFailure
        )
    }

    fun dismissNotification(notificationId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("notificaciones").document(notificationId).update("isActive", false)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}