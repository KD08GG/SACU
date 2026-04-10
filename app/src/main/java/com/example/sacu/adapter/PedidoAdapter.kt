package com.example.sacu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sacu.R
import com.example.sacu.model.Pedido
import java.text.SimpleDateFormat
import java.util.Locale

class PedidoAdapter(
    private val pedidos: List<Pedido>,
    private val onPedidoClick: (Pedido) -> Unit
) : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    inner class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProducto: ImageView  = itemView.findViewById(R.id.imgPedido)
        val txtNombre: TextView     = itemView.findViewById(R.id.txtNombrePedido)
        val txtFecha: TextView      = itemView.findViewById(R.id.txtFechaPedido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]

        // Nombre del pedido (se puede cambiar por el nombre real del producto si
        // amplías el modelo Pedido con un campo 'nombre_producto')
        holder.txtNombre.text = "Pedido #${pedido.numero_fila}"

        holder.txtFecha.text = pedido.fecha?.toDate()?.let { sdf.format(it) } ?: "—"

        holder.imgProducto.setImageResource(R.drawable.icono_nota)

        holder.itemView.setOnClickListener { onPedidoClick(pedido) }
    }

    override fun getItemCount(): Int = pedidos.size
}
