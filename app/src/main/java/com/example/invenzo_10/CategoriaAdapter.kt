package com.example.invenzo_10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CategoriaAdapter(
    private var lista: List<Categoria>
) : RecyclerView.Adapter<CategoriaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txtNombreCategoria)
        val fecha: TextView = view.findViewById(R.id.txtFechaCreacion)
        val cantidad: TextView = view.findViewById(R.id.txtCantidadProductos)
        val estado: TextView = view.findViewById(R.id.txtEstadoCategoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        holder.nombre.text = item.nombre
        
        // Formatear fecha: mostrar solo la parte de la fecha si viene con hora
        val fechaTexto = item.createdAt?.split("T")?.get(0) ?: "N/A"
        holder.fecha.text = "Creado: $fechaTexto"
        
        // Cantidad de productos con valor por defecto
        val count = item.productosCount ?: 0
        holder.cantidad.text = "$count Productos"
        
        // Lógica flexible para el estado (soporta Int, String o Boolean)
        val isActive = when (item.activo) {
            is Number -> item.activo.toInt() == 1
            is String -> item.activo == "1" ||
                    item.activo.equals("active", true) ||
                    item.activo.equals("activo", true)
            is Boolean -> item.activo
            null -> true // Si no viene el campo, asumir Activo
            else -> true
        }

        if (isActive) {
            holder.estado.text = "Activo"
            holder.estado.setBackgroundResource(R.drawable.bg_green_icon)
            holder.estado.backgroundTintList = null // Resetear por si fue usado antes
        } else {
            holder.estado.text = "Inactivo"
            holder.estado.setBackgroundResource(R.drawable.bg_user_pill)
            holder.estado.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context, android.R.color.darker_gray)
        }
    }

    fun updateData(newList: List<Categoria>) {
        lista = newList
        notifyDataSetChanged()
    }

    override fun getItemCount() = lista.size
}
