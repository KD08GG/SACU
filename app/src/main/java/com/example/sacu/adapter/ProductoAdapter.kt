package com.example.sacu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sacu.R
import com.example.sacu.model.Producto
import com.example.sacu.repository.Compra

class ProductoAdapter(
    private val productos: List<Producto>,
    private val compra: Compra
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    private val cantidades = mutableMapOf<Int, Int>()

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProducto: ImageView = itemView.findViewById(R.id.imgProducto)
        val txtNombre: TextView    = itemView.findViewById(R.id.txtNombreProducto)
        val txtPrecio: TextView    = itemView.findViewById(R.id.txtPrecioProducto)
        val txtCantidad: TextView  = itemView.findViewById(R.id.txtCantidadProducto)
        val btnRestar: TextView    = itemView.findViewById(R.id.btnRestarProducto)
        val btnSumar: TextView     = itemView.findViewById(R.id.btnSumarProducto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]

        holder.txtNombre.text = producto.nombre
        holder.txtPrecio.text = "$${producto.precio.toInt()}"

        if (producto.imagen_url.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(producto.imagen_url)
                .placeholder(R.drawable.logito)
                .into(holder.imgProducto)
        }

        holder.txtCantidad.text = cantidades.getOrDefault(position, 0).toString()

        holder.btnSumar.setOnClickListener {
            val nueva = cantidades.getOrDefault(position, 0) + 1
            cantidades[position] = nueva
            holder.txtCantidad.text = nueva.toString()
            compra.agregarProducto(producto)
        }


        holder.btnRestar.setOnClickListener {
            val actual = cantidades.getOrDefault(position, 0)
            if (actual > 0) {
                val nueva = actual - 1
                cantidades[position] = nueva
                holder.txtCantidad.text = nueva.toString()
                compra.quitarProducto(producto)   // sincroniza con el carrito
            }
        }
    }

    override fun getItemCount(): Int = productos.size
}
