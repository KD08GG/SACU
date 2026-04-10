package com.example.sacu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sacu.R


data class Tarjeta(
    val id: String = "",
    val nombre: String = "",
    val numero: String = ""   // número enmascarado, ej: "522 ****** ***** 993"
)

class TarjetaAdapter(
    private val tarjetas: MutableList<Tarjeta>,
    private val onEliminarClick: (Tarjeta, Int) -> Unit
) : RecyclerView.Adapter<TarjetaAdapter.TarjetaViewHolder>() {

    inner class TarjetaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView     = itemView.findViewById(R.id.txtNombreTarjeta)
        val txtNumero: TextView     = itemView.findViewById(R.id.txtNumeroTarjeta)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminarTarjeta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarjetaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarjeta, parent, false)
        return TarjetaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TarjetaViewHolder, position: Int) {
        val tarjeta = tarjetas[position]

        holder.txtNombre.text = tarjeta.nombre
        holder.txtNumero.text = tarjeta.numero

        holder.btnEliminar.setOnClickListener {
            onEliminarClick(tarjeta, position)
        }
    }

    override fun getItemCount(): Int = tarjetas.size

    /** Elimina un item de la lista y notifica al RecyclerView */
    fun eliminarItem(position: Int) {
        tarjetas.removeAt(position)
        notifyItemRemoved(position)
    }
}
