package com.example.sacu.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sacu.Carrito
import com.example.sacu.R
import com.example.sacu.model.ItemPedido
import com.example.sacu.model.Producto
import com.example.sacu.repository.carritoTotal

class ItemCarritoAdapter(
    private val items: MutableList<ItemPedido>,
    private val onCarritoActualizado: () -> Unit
) : RecyclerView.Adapter<ItemCarritoAdapter.CarritoViewHolder>() {

    inner class CarritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProducto: ImageView  = itemView.findViewById(R.id.imgProductoCarrito)
        val txtNombre: TextView     = itemView.findViewById(R.id.txtNombreCarrito)
        val txtPrecio: TextView     = itemView.findViewById(R.id.txtPrecioCarrito)
        val txtCantidad: TextView   = itemView.findViewById(R.id.txtCantidadCarrito)

        val btnAgregar: TextView = itemView.findViewById(R.id.btnSumarProducto)

        val btnRestar: TextView = itemView.findViewById(R.id.btnRestarProducto)

        val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminar)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val item = items[position]

        holder.txtNombre.text = item.nombre
        holder.txtPrecio.text = "$${item.precio_unitario.toInt()}"
        holder.txtCantidad.text = item.cantidad.toString()

        holder.btnAgregar.setOnClickListener {
            val pos = holder.bindingAdapterPosition

            if (pos != RecyclerView.NO_POSITION) {
                items[pos].cantidad++

                holder.txtCantidad.text = items[pos].cantidad.toString()

                onCarritoActualizado()
            }
        }

        holder.btnRestar.setOnClickListener {
            val pos = holder.bindingAdapterPosition

            if (pos != RecyclerView.NO_POSITION) {
                val currentItem = items[pos]

                if (currentItem.cantidad > 1) {
                    currentItem.cantidad--
                    holder.txtCantidad.text = currentItem.cantidad.toString()
                } else {
                    items.removeAt(pos)
                    notifyItemRemoved(pos)
                }

                onCarritoActualizado()
            }
        }

        holder.btnEliminar.setOnClickListener {
            val pos = holder.bindingAdapterPosition

            if (pos != RecyclerView.NO_POSITION) {
                items.removeAt(pos)
                notifyItemRemoved(pos)
                onCarritoActualizado()
            }
        }
    }


    override fun getItemCount(): Int = items.size
}
