package com.example.invenzo_10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ResumenStockAdapter(
    private val lista: MutableList<Producto>
) : RecyclerView.Adapter<ResumenStockAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtProducto: TextView = view.findViewById(R.id.txtStockProducto)
        val txtCategoria: TextView = view.findViewById(R.id.txtStockCategoria)
        val txtStock: TextView = view.findViewById(R.id.txtStockActual)
        val txtEstado: TextView = view.findViewById(R.id.txtStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock_summary, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = lista[position]
        val context = holder.itemView.context

        holder.txtProducto.text = producto.nombre
        holder.txtCategoria.text = "Categoría: ${producto.categoria.nombre}"
        holder.txtStock.text = "Stock: ${producto.cantidad}"

        when {
            producto.cantidad <= 0 -> {
                holder.txtEstado.text = "Sin Stock"
                holder.txtEstado.setTextColor(ContextCompat.getColor(context, R.color.dangerColor))
                holder.txtEstado.setBackgroundResource(R.drawable.bg_red_icon)
                holder.txtEstado.backgroundTintList = ContextCompat.getColorStateList(context, R.color.dangerLight)
            }
            producto.cantidad <= producto.stockMinimo -> {
                holder.txtEstado.text = "Bajo Stock"
                holder.txtEstado.setTextColor(ContextCompat.getColor(context, R.color.warningColor))
                holder.txtEstado.setBackgroundResource(R.drawable.bg_green_icon) // Usamos el mismo shape
                holder.txtEstado.backgroundTintList = ContextCompat.getColorStateList(context, R.color.warningLight)
            }
            else -> {
                holder.txtEstado.text = "Disponible"
                holder.txtEstado.setTextColor(ContextCompat.getColor(context, R.color.successColor))
                holder.txtEstado.setBackgroundResource(R.drawable.bg_green_icon)
                holder.txtEstado.backgroundTintList = ContextCompat.getColorStateList(context, R.color.successLight)
            }
        }
    }

    fun actualizar(datos: List<Producto>) {
        lista.clear()
        lista.addAll(datos)
        notifyDataSetChanged()
    }
}