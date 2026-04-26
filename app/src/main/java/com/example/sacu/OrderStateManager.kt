package com.example.sacu

class OrderStateManager(private val notificationManager: OrderNotificationManager) {

    fun onOrderStatusChanged(orderId: String, userId: String, newStatus: OrderStatus) {
        when (newStatus) {
            is OrderStatus.Pendiente -> {
                // Mostrar notificación "en preparación"
                notificationManager.showNotification(orderId, newStatus, userId)
            }
            is OrderStatus.Listo -> {
                // Actualizar notificación a "pedido listo, pasa a recogerlo"
                notificationManager.showNotification(orderId, newStatus, userId)
            }
            is OrderStatus.Recogido -> {
                // No mostrar notificación, mostrar en historial
                notificationManager.dismissNotification(orderId)
                // Aquí iría la lógica para agregar al historial
            }
            is OrderStatus.Terminado -> {
                // Ya no mostrar notificación
                notificationManager.dismissNotification(orderId)
            }
            is OrderStatus.Cancelado -> {
                // Mostrar notificación con motivo y acción dismiss
                notificationManager.showNotification(orderId, newStatus, userId)
            }
        }
    }
}