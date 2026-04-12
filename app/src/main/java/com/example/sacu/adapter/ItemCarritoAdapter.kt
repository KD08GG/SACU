package com.example.sacu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sacu.R
import com.example.sacu.model.ItemPedido

class ItemCarritoAdapter(
    private val items: List<ItemPedido>
) : RecyclerView.Adapter<ItemCarritoAdapter.CarritoViewHolder>() {

    inner class CarritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProducto: ImageView  = itemView.findViewById(R.id.imgProductoCarrito)
        val txtNombre: TextView     = itemView.findViewById(R.id.txtNombreCarrito)
        val txtPrecio: TextView     = itemView.findViewById(R.id.txtPrecioCarrito)
        val txtCantidad: TextView   = itemView.findViewById(R.id.txtCantidadCarrito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val item = items[position]

        holder.txtNombre.text   = item.nombre
        holder.txtPrecio.text   = "$${item.precio_unitario.toInt()}"
        holder.txtCantidad.text = item.cantidad.toString()

    }

    override fun getItemCount(): Int = items.size
}
