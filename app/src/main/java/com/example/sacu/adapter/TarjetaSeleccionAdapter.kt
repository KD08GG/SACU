package com.example.sacu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sacu.R

class TarjetaSeleccionAdapter(
    private val tarjetas: List<Tarjeta>,
    private var selectedPosition: Int = 0     // índice de la tarjeta predeterminada actual
) : RecyclerView.Adapter<TarjetaSeleccionAdapter.TarjetaSeleccionViewHolder>() {

    inner class TarjetaSeleccionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView      = itemView.findViewById(R.id.txtNombreTarjetaSel)
        val txtNumero: TextView      = itemView.findViewById(R.id.txtNumeroTarjetaSel)
        val btnEstrella: ImageButton = itemView.findViewById(R.id.btnEstrellaTarjeta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarjetaSeleccionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarjeta_seleccion, parent, false)
        return TarjetaSeleccionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TarjetaSeleccionViewHolder, position: Int) {
        val tarjeta = tarjetas[position]

        holder.txtNombre.text = tarjeta.nombre
        holder.txtNumero.text = tarjeta.numero

        // Estrella amarilla si es la seleccionada, gris si no
        if (position == selectedPosition) {
            holder.btnEstrella.setImageResource(android.R.drawable.star_big_on)   // ★ amarilla
            holder.btnEstrella.setColorFilter(
                android.graphics.Color.parseColor("#FFC107"),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else {
            holder.btnEstrella.setImageResource(android.R.drawable.star_big_off)  // ☆ gris
            holder.btnEstrella.setColorFilter(
                android.graphics.Color.parseColor("#AAAAAA"),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }

        // Al tocar la estrella, se actualiza la selección
        holder.btnEstrella.setOnClickListener {
            val anterior = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(anterior)          // repinta la estrella anterior → gris
            notifyItemChanged(selectedPosition)  // repinta la nueva → amarilla
        }
    }

    override fun getItemCount(): Int = tarjetas.size

    /** Devuelve la tarjeta actualmente marcada como predeterminada */
    fun getTarjetaSeleccionada(): Tarjeta? =
        tarjetas.getOrNull(selectedPosition)
}