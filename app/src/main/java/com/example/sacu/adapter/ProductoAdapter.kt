package com.example.sacu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sacu.R
import com.example.sacu.model.ItemPedido
import com.example.sacu.model.Producto
import com.example.sacu.repository.carritoTotal

class ProductoAdapter(
    private val productos: List<Producto>,
    private val onAgregarClick: (Producto) -> Unit,
    private val onRestarClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProducto: ImageView     = itemView.findViewById(R.id.imgProducto)
        val txtNombre: TextView        = itemView.findViewById(R.id.txtNombreProducto)
        val txtPrecio: TextView        = itemView.findViewById(R.id.txtPrecioProducto)
        val btnAgregar: TextView = itemView.findViewById(R.id.btnSumarProducto)
        val btnRestar: TextView = itemView.findViewById(R.id.btnRestarProducto)
        val btnEmp: ImageButton = itemView.findViewById(R.id.empProd)

        val btnsCantidad: LinearLayout = itemView.findViewById(R.id.btnCantidadProducto)

        val txtCantidad: TextView = itemView.findViewById(R.id.txtCantidadProducto)


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
                .placeholder(R.drawable.icono_mas)
                .into(holder.imgProducto)
        }

        // Buscar producto dentro del carrito
        val itemCarrito = carritoTotal.find { it.producto_id == producto.id }
        val cantidad = itemCarrito?.cantidad ?: 0

        // Mostrar cantidad actual
        holder.txtCantidad.text = cantidad.toString()

        // Mostrar u ocultar controles
        if (cantidad > 0) {
            holder.btnsCantidad.visibility = View.VISIBLE
            holder.btnEmp.visibility = View.GONE
        } else {
            holder.btnsCantidad.visibility = View.GONE
            holder.btnEmp.visibility = View.VISIBLE
        }

        holder.btnAgregar.setOnClickListener {
            onAgregarClick(producto)
            notifyItemChanged(holder.bindingAdapterPosition)
        }

        holder.btnRestar.setOnClickListener {
            onRestarClick(producto)
            notifyItemChanged(holder.bindingAdapterPosition)
        }

        holder.btnEmp.setOnClickListener {
            onAgregarClick(producto)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = productos.size
}