package com.example.sacu

class OrderStateManager(private val notificationManager: OrderNotificationManager) {

    fun onOrderStatusChanged(orderId: String, userId: String, newStatus: OrderStatus, numeroPedido: Int = 0) {
        when (newStatus) {
            is OrderStatus.Pendiente -> {
                // Mostrar notificación "en preparación"
                notificationManager.showNotification(orderId, newStatus, userId, numeroPedido)
            }
            is OrderStatus.Listo -> {
                // Actualizar notificación a "pedido listo, pasa a recogerlo"
                notificationManager.showNotification(orderId, newStatus, userId, numeroPedido)
            }
            is OrderStatus.Recogido -> {
                // Actualizar historial y quitar de notificaciones push
                notificationManager.dismissNotification(orderId)
            }
            is OrderStatus.Terminado -> {
                // Ya no mostrar notificación push, marcar en historial
                notificationManager.dismissNotification(orderId)
            }
            is OrderStatus.Cancelado -> {
                // Mostrar notificación con motivo y acción dismiss
                notificationManager.showNotification(orderId, newStatus, userId, numeroPedido)
            }
        }
    }
}