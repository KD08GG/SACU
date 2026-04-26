package com.example.sacu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sacu.R
import com.example.sacu.model.Pedido
import com.example.sacu.model.Producto
import com.example.sacu.repository.Compra
import java.text.SimpleDateFormat
import java.util.Locale

class PedidoAdapter(
    private val pedidos: List<Pedido>,
    private val onPedidoClick: (Pedido) -> Unit
) : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val compra = Compra()

    inner class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombrePedido)
        val txtFecha: TextView = itemView.findViewById(R.id.txtFechaPedido)
        val btnRebuy: View = itemView.findViewById(R.id.my_image_button)
        
        // ImageViews del Grid
        val img1: ImageView = itemView.findViewById(R.id.imgProducto1)
        val img2: ImageView = itemView.findViewById(R.id.imgProducto2)
        val img3: ImageView = itemView.findViewById(R.id.imgProducto3)
        val img4: ImageView = itemView.findViewById(R.id.imgMasProductos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]

        holder.txtNombre.text = "Pedido #${pedido.numero_fila}"
        holder.txtFecha.text = pedido.fecha?.toDate()?.let { sdf.format(it) } ?: "—"

        configurarImagenes(holder, pedido)

        holder.btnRebuy.setOnClickListener {
            pedido.productos.forEach { item ->
                val producto = Producto(
                    id = item.producto_id,
                    nombre = item.nombre,
                    precio = item.precio_unitario,
                    imagen_url = item.imagen_url
                )
                repeat(item.cantidad) {
                    compra.agregarProducto(producto)
                }
            }
            Toast.makeText(holder.itemView.context, "Productos agregados al carrito", Toast.LENGTH_SHORT).show()
        }

        holder.itemView.setOnClickListener { onPedidoClick(pedido) }
    }

    private fun configurarImagenes(holder: PedidoViewHolder, pedido: Pedido) {
        val productos = pedido.productos
        val total = productos.size
        val imageViews = listOf(holder.img1, holder.img2, holder.img3, holder.img4)

        // Limpiar todas las imágenes primero
        imageViews.forEach { 
            it.visibility = View.GONE 
            it.setImageResource(0)
        }

        when {
            total == 1 -> {
                holder.img1.visibility = View.VISIBLE
                cargarImagen(holder.img1, productos[0].imagen_url)
            }
            total == 2 -> {
                holder.img1.visibility = View.VISIBLE
                holder.img2.visibility = View.VISIBLE
                cargarImagen(holder.img1, productos[0].imagen_url)
                cargarImagen(holder.img2, productos[1].imagen_url)
            }
            total == 3 -> {
                holder.img1.visibility = View.VISIBLE
                holder.img2.visibility = View.VISIBLE
                holder.img3.visibility = View.VISIBLE
                cargarImagen(holder.img1, productos[0].imagen_url)
                cargarImagen(holder.img2, productos[1].imagen_url)
                cargarImagen(holder.img3, productos[2].imagen_url)
            }
            total == 4 -> {
                imageViews.forEach { it.visibility = View.VISIBLE }
                cargarImagen(holder.img1, productos[0].imagen_url)
                cargarImagen(holder.img2, productos[1].imagen_url)
                cargarImagen(holder.img3, productos[2].imagen_url)
                cargarImagen(holder.img4, productos[3].imagen_url)
            }
            total > 4 -> {
                imageViews.forEach { it.visibility = View.VISIBLE }
                cargarImagen(holder.img1, productos[0].imagen_url)
                cargarImagen(holder.img2, productos[1].imagen_url)
                cargarImagen(holder.img3, productos[2].imagen_url)
                holder.img4.setImageResource(R.drawable.moreicon_outline)
                holder.img4.scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
        }
    }

    private fun cargarImagen(view: ImageView, url: String) {
        if (url.isNotEmpty()) {
            Glide.with(view.context)
                .load(url)
                .placeholder(R.drawable.icono_nota)
                .error(R.drawable.icono_nota)
                .centerCrop()
                .into(view)
        } else {
            view.setImageResource(R.drawable.icono_nota)
        }
    }

    override fun getItemCount(): Int = pedidos.size
}