package com.example.invenzo_10

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MovimientoAdapter(
    private var lista: MutableList<Movimiento>
) : RecyclerView.Adapter<MovimientoAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtProducto: TextView = itemView.findViewById(R.id.txtProducto)
        val txtFecha: TextView = itemView.findViewById(R.id.txtFecha)
        val txtTipo: TextView = itemView.findViewById(R.id.txtTipo)
        val txtStock: TextView = itemView.findViewById(R.id.txtStock)
        val imgTipoIcon: ImageView = itemView.findViewById(R.id.imgTipoIcon)
        val iconContainer: View = itemView.findViewById(R.id.iconContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movimiento_control, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movimiento = lista[position]
        val context = holder.itemView.context

        holder.txtProducto.text = movimiento.producto.nombre
        holder.txtFecha.text = formatearFecha(movimiento.createdAt)
        
        val esEntrada = movimiento.tipo.lowercase() == "entrada"
        
        holder.txtTipo.text = movimiento.tipo.replaceFirstChar { it.uppercase() }
        holder.txtStock.text = "${if (esEntrada) "+" else "-"} ${movimiento.cantidad}"

        if (esEntrada) {
            // Estilo Entrada (Verde)
            holder.txtTipo.backgroundTintList = ContextCompat.getColorStateList(context, R.color.successLight)
            holder.txtTipo.setTextColor(ContextCompat.getColor(context, R.color.successColor))
            
            holder.iconContainer.backgroundTintList = ContextCompat.getColorStateList(context, R.color.successLight)
            holder.imgTipoIcon.setImageResource(R.drawable.ic_arrow)
            holder.imgTipoIcon.rotation = 0f // Hacia arriba
            holder.imgTipoIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.successColor)
            
            holder.txtStock.setTextColor(ContextCompat.getColor(context, R.color.successColor))
        } else {
            // Estilo Salida (Rojo)
            holder.txtTipo.backgroundTintList = ContextCompat.getColorStateList(context, R.color.dangerLight)
            holder.txtTipo.setTextColor(ContextCompat.getColor(context, R.color.dangerColor))
            
            holder.iconContainer.backgroundTintList = ContextCompat.getColorStateList(context, R.color.dangerLight)
            holder.imgTipoIcon.setImageResource(R.drawable.ic_arrow)
            holder.imgTipoIcon.rotation = 180f // Hacia abajo
            holder.imgTipoIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.dangerColor)
            
            holder.txtStock.setTextColor(ContextCompat.getColor(context, R.color.dangerColor))
        }
    }

    private fun formatearFecha(fechaStr: String?): String {
        if (fechaStr.isNullOrEmpty()) return "Fecha no disponible"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(fechaStr)
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            date?.let { formatter.format(it) } ?: fechaStr
        } catch (e: Exception) {
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = parser.parse(fechaStr)
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                date?.let { formatter.format(it) } ?: fechaStr
            } catch (e2: Exception) {
                fechaStr
            }
        }
    }

    fun actualizar(nuevaLista: List<Movimiento>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}