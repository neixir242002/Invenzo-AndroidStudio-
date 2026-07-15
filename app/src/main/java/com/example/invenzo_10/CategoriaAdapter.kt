package com.example.invenzo_10

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CategoriaAdapter(
    private var lista: List<Categoria>,
    private val onItemClick: (Categoria) -> Unit
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
        val fechaTexto = item.createdAt?.split("T")?.get(0) ?: "N/A"
        holder.fecha.text = "Creado: $fechaTexto"
        holder.cantidad.text = "${item.productosCount ?: 0} Productos"
        
        // Lógica de estado ultra-robusta
        val isActive = when (val value = item.activo) {
            is Number -> value.toInt() == 1
            is Boolean -> value
            is String -> value == "1" || value.equals("active", true) || value.equals("activo", true)
            else -> true // Por defecto activo si es nulo
        }

        if (isActive) {
            holder.estado.text = "Activo"
            holder.estado.setBackgroundResource(R.drawable.bg_green_icon)
            holder.estado.setTextColor(Color.parseColor("#059669"))
            holder.estado.backgroundTintList = null 
        } else {
            holder.estado.text = "Inactivo"
            holder.estado.setBackgroundResource(R.drawable.bg_user_pill)
            holder.estado.setTextColor(Color.parseColor("#64748B"))
            holder.estado.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E2E8F0"))
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    fun updateData(newList: List<Categoria>) {
        lista = newList
        notifyDataSetChanged()
    }

    override fun getItemCount() = lista.size
}
