package com.example.invenzo_10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ReporteProductoAdapter(private var productos: List<Producto>) :
    RecyclerView.Adapter<ReporteProductoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombre: TextView = view.findViewById(R.id.txtNombre)
        val txtCategoria: TextView = view.findViewById(R.id.txtCategoria)
        val txtStock: TextView = view.findViewById(R.id.txtStock)
        val txtNivel: TextView = view.findViewById(R.id.txtNivel)
        val imgStatusIcon: ImageView = view.findViewById(R.id.imgStatusIcon)
        val iconContainer: View = view.findViewById(R.id.iconContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reporte_producto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = productos[position]
        val context = holder.itemView.context

        holder.txtNombre.text = p.nombre
        holder.txtCategoria.text = p.categoria.nombre
        holder.txtStock.text = "Stock: ${p.cantidad}"

        val stockLevel = when {
            p.cantidad == 0 -> "Crítico"
            p.cantidad <= p.stockMinimo -> "Bajo"
            else -> "Normal"
        }

        holder.txtNivel.text = stockLevel

        when (stockLevel) {
            "Crítico" -> {
                holder.txtNivel.backgroundTintList = ContextCompat.getColorStateList(context, R.color.dangerLight)
                holder.txtNivel.setTextColor(ContextCompat.getColor(context, R.color.dangerColor))

                holder.iconContainer.backgroundTintList = ContextCompat.getColorStateList(context, R.color.dangerLight)
                holder.imgStatusIcon.setImageResource(R.drawable.icons8_alerta_24)
                holder.imgStatusIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.dangerColor)
            }
            "Bajo" -> {
                holder.txtNivel.backgroundTintList = ContextCompat.getColorStateList(context, R.color.warningLight)
                holder.txtNivel.setTextColor(ContextCompat.getColor(context, R.color.warningColor))

                holder.iconContainer.backgroundTintList = ContextCompat.getColorStateList(context, R.color.warningLight)
                holder.imgStatusIcon.setImageResource(R.drawable.icons8_caja_llena_24)
                holder.imgStatusIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.warningColor)
            }
            else -> {
                holder.txtNivel.backgroundTintList = ContextCompat.getColorStateList(context, R.color.successLight)
                holder.txtNivel.setTextColor(ContextCompat.getColor(context, R.color.successColor))

                holder.iconContainer.backgroundTintList = ContextCompat.getColorStateList(context, R.color.successLight)
                holder.imgStatusIcon.setImageResource(R.drawable.ic_check)
                holder.imgStatusIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.successColor)
            }
        }
    }

    override fun getItemCount() = productos.size

    fun updateData(nuevaLista: List<Producto>) {
        this.productos = nuevaLista
        notifyDataSetChanged()
    }
}
