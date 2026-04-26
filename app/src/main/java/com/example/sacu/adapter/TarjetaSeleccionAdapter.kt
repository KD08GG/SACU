package com.example.sacu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sacu.R
import com.example.sacu.model.Tarjeta

class TarjetaSeleccionAdapter(
    private var tarjetas: List<Tarjeta>,
    private val onTarjetaSeleccionada: (Tarjeta) -> Unit
) : RecyclerView.Adapter<TarjetaSeleccionAdapter.TarjetaSeleccionViewHolder>() {

    private var selectedPosition: Int = tarjetas.indexOfFirst { it.esPredeterminada }

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

        holder.txtNombre.text = tarjeta.nombreTitular
        holder.txtNumero.text = "**** **** **** ${tarjeta.numero.takeLast(4)}"

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
            notifyItemChanged(anterior)
            notifyItemChanged(selectedPosition)
            onTarjetaSeleccionada(tarjeta)
        }
    }

    override fun getItemCount(): Int = tarjetas.size

    fun actualizarLista(nuevaLista: List<Tarjeta>) {
        this.tarjetas = nuevaLista
        this.selectedPosition = nuevaLista.indexOfFirst { it.esPredeterminada }
        notifyDataSetChanged()
    }
}