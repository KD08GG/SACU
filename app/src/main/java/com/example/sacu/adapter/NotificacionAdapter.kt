package com.example.sacu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sacu.R
import com.example.sacu.model.Notificacion

class NotificacionAdapter(
    private val notificaciones: List<Notificacion>,
    private val onMasDetallesClick: (Notificacion) -> Unit
) : RecyclerView.Adapter<NotificacionAdapter.NotificacionViewHolder>() {

    class NotificacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtPedidoLabel: TextView    = itemView.findViewById(R.id.txtPedidoLabel)
        val txtNumeroPedido: TextView   = itemView.findViewById(R.id.txtNumeroPedido)
        val txtEstado: TextView         = itemView.findViewById(R.id.txtEstadoNotif)
        val btnMasDetalles: Button      = itemView.findViewById(R.id.btnMasDetalles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacion, parent, false)
        return NotificacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        val notif = notificaciones[position]
        val context = holder.itemView.context

        holder.txtPedidoLabel.text  = context.getString(R.string.notification_label)
        holder.txtNumeroPedido.text = ""
        holder.txtEstado.text       = notif.mensaje

        holder.btnMasDetalles.setOnClickListener { onMasDetallesClick(notif) }
    }

    override fun getItemCount(): Int = notificaciones.size
}
