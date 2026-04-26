package com.example.sacu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sacu.R
import com.example.sacu.model.ItemPedido

class ItemPedidoDisplayAdapter(
    private val items: List<ItemPedido>
) : RecyclerView.Adapter<ItemPedidoDisplayAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProducto: ImageView = itemView.findViewById(R.id.imgProducto)
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombreProducto)
        val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecioProducto)
        val txtCantidad: TextView = itemView.findViewById(R.id.txtCantidadProducto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_display, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.txtNombre.text = item.nombre
        holder.txtPrecio.text = "$${item.precio_unitario.toInt()}"
        holder.txtCantidad.text = "x${item.cantidad}"
        
        // Aquí podrías usar Glide si tuvieras la URL de la imagen en ItemPedido
        // Glide.with(holder.itemView.context).load(item.imagenUrl).into(holder.imgProducto)
    }

    override fun getItemCount(): Int = items.size
}