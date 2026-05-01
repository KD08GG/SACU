package com.example.sacu

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class PaymentNotificationManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "payment_notifications"
    private val notifId = 9001

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones de Pago",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Verificaciones y cobros de tarjeta"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun mostrarVerificando(terminacion: String) =
        mostrar("Verificando tarjeta...", "Aplicando cargo temporal de \$10 a terminación $terminacion")

    fun mostrarCargoTemporalAplicado(terminacion: String) =
        mostrar("Cargo temporal aplicado", "Se cargaron \$10 a tu cuenta terminación $terminacion. Verificando...")

    fun mostrarTarjetaVerificada(terminacion: String) =
        mostrar("✓ Tarjeta verificada", "Cargo de \$10 revertido. Tarjeta terminación $terminacion agregada correctamente.")

    fun mostrarErrorTarjeta(motivo: String) =
        mostrar("✗ Tarjeta no agregada", motivo)

    fun mostrarProcesandoPago(terminacion: String, monto: Double) =
        mostrar("Procesando pago...", "Realizando cobro de \$${"%.2f".format(monto)} a tarjeta terminación $terminacion")

    fun mostrarPagoExitoso(terminacion: String, monto: Double) =
        mostrar("✓ Pago aprobado", "Se han descontado \$${"%.2f".format(monto)} de tu cuenta con terminación $terminacion.")

    fun mostrarPagoRechazado(terminacion: String, motivo: String) =
        mostrar("✗ Pago rechazado", "$motivo en tarjeta terminación $terminacion.")

    private fun mostrar(titulo: String, mensaje: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notifId, notification)
    }
}
