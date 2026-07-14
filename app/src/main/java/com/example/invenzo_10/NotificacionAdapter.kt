package com.example.invenzo_10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificacionAdapter(private val lista: List<Notificacion>) :
    RecyclerView.Adapter<NotificacionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitulo: TextView = view.findViewById(R.id.txtTituloNotif)
        val txtMensaje: TextView = view.findViewById(R.id.txtMensajeNotif)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.txtTitulo.text = item.titulo
        holder.txtMensaje.text = item.mensaje
    }

    override fun getItemCount(): Int = lista.size
}
