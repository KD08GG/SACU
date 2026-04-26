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
                // No mostrar notificación, mostrar en historial
                notificationManager.dismissNotification(orderId)
                        // lógica para un historial de pedidos pendiente
            }
            is OrderStatus.Terminado -> {
                // Ya no mostrar notificación
                notificationManager.dismissNotification(orderId)
            }
            is OrderStatus.Cancelado -> {
                // Mostrar notificación con motivo y acción dismiss
                notificationManager.showNotification(orderId, newStatus, userId, numeroPedido)
            }
        }
    }
}