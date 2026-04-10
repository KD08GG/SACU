package com.example.sacu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sacu.R
import com.example.sacu.model.Producto

class ProductoAdapter(
    private val productos: List<Producto>,
    private val onAgregarClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProducto: ImageView     = itemView.findViewById(R.id.imgProducto)
        val txtNombre: TextView        = itemView.findViewById(R.id.txtNombreProducto)
        val txtPrecio: TextView        = itemView.findViewById(R.id.txtPrecioProducto)
        val btnAgregar: ImageButton    = itemView.findViewById(R.id.btnAgregarProducto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]

        holder.txtNombre.text  = producto.nombre
        holder.txtPrecio.text  = "$${producto.precio.toInt()}"

        // Carga la imagen desde URL con Glide; si no hay URL muestra un placeholder
        if (producto.imagen_url.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(producto.imagen_url)
                .placeholder(R.drawable.icono_mas)
                .into(holder.imgProducto)
        }

        holder.btnAgregar.setOnClickListener { onAgregarClick(producto) }
    }

    override fun getItemCount(): Int = productos.size
}
