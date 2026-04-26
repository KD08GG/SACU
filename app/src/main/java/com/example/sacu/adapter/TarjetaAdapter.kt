package com.example.sacu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sacu.R
import com.example.sacu.model.Tarjeta

class TarjetaAdapter(
    private var tarjetas: List<Tarjeta>,
    private val onEliminarClick: (Tarjeta) -> Unit
) : RecyclerView.Adapter<TarjetaAdapter.TarjetaViewHolder>() {

    inner class TarjetaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombreTarjeta)
        val txtNumero: TextView = itemView.findViewById(R.id.txtNumeroTarjeta)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminarTarjeta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarjetaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarjeta, parent, false)
        return TarjetaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TarjetaViewHolder, position: Int) {
        val tarjeta = tarjetas[position]

        holder.txtNombre.text = tarjeta.nombreTitular
        holder.txtNumero.text = "**** **** **** ${tarjeta.numero.takeLast(4)}"

        holder.btnEliminar.setOnClickListener {
            onEliminarClick(tarjeta)
        }
    }

    override fun getItemCount(): Int = tarjetas.size

    fun actualizarLista(nuevaLista: List<Tarjeta>) {
        this.tarjetas = nuevaLista
        notifyDataSetChanged()
    }
}