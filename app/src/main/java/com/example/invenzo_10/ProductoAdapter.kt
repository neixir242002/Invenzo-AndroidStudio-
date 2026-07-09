package com.example.invenzo_10

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProductoAdapter(
    private val lista: MutableList<Producto>,
    private val onAction: (Producto, Int, String) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen: ImageView = itemView.findViewById(R.id.imgProducto)
        val nombre: TextView = itemView.findViewById(R.id.txtNombre)
        val codigo: TextView = itemView.findViewById(R.id.txtCodigo)
        val categoria: TextView = itemView.findViewById(R.id.txtCategoria)
        val stock: TextView = itemView.findViewById(R.id.txtStock)
        val precio: TextView = itemView.findViewById(R.id.txtPrecio)
        val estado: TextView = itemView.findViewById(R.id.txtEstado)
        val accion: TextView = itemView.findViewById(R.id.txtAcciones)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = lista[position]
        val context = holder.itemView.context

        holder.nombre.text = producto.nombre
        holder.codigo.text = "#${producto.codigo}"
        holder.categoria.text = producto.categoria.nombre
        holder.stock.text = "Stock: ${producto.cantidad}"
        holder.precio.text = "$${producto.precio}"

        val urlImagen = construirUrlImagen(producto.foto)
        
        Glide.with(context)
            .load(urlImagen)
            .placeholder(R.drawable.ic_launcher_background) 
            .error(R.drawable.ic_launcher_background)
            .diskCacheStrategy(DiskCacheStrategy.ALL) 
            .transform(CenterCrop(), RoundedCorners(24))
            .into(holder.imagen)

        when {
            producto.cantidad == 0 -> {
                holder.estado.text = "Sin stock"
                holder.estado.setBackgroundResource(R.drawable.bg_red_icon)
                holder.estado.setTextColor(ContextCompat.getColor(context, R.color.dangerColor))
            }
            producto.cantidad <= producto.stockMinimo -> {
                holder.estado.text = "Stock bajo"
                holder.estado.setBackgroundResource(R.drawable.bg_orange_icon)
                holder.estado.setTextColor(Color.parseColor("#F97316"))
            }
            else -> {
                holder.estado.text = "Disponible"
                holder.estado.setBackgroundResource(R.drawable.bg_green_icon)
                holder.estado.setTextColor(ContextCompat.getColor(context, R.color.successColor))
            }
        }

        if (producto.activo == 1) {
            holder.accion.text = "Activo"
            holder.accion.setTextColor(ContextCompat.getColor(context, R.color.successColor))
        } else {
            holder.accion.text = "Inactivo"
            holder.accion.setTextColor(ContextCompat.getColor(context, R.color.dangerColor))
        }

        // --- MENÚ DE ACCIONES AL TOCAR EL PRODUCTO ---
        holder.itemView.setOnClickListener {
            val labelToggle = if (producto.activo == 1) "Desactivar" else "Activar"
            val opciones = arrayOf("Editar producto", labelToggle, "Eliminar definitivamente")

            MaterialAlertDialogBuilder(context)
                .setTitle(producto.nombre)
                .setItems(opciones) { _, which ->
                    when (which) {
                        0 -> onAction(producto, position, "EDIT")
                        1 -> onAction(producto, position, "TOGGLE")
                        2 -> {
                            // Confirmación extra para eliminar
                            MaterialAlertDialogBuilder(context)
                                .setTitle("¿Eliminar producto?")
                                .setMessage("Esta acción borrará el producto de forma permanente.")
                                .setPositiveButton("Eliminar") { _, _ -> onAction(producto, position, "DELETE") }
                                .setNegativeButton("Cancelar", null)
                                .show()
                        }
                    }
                }
                .setNegativeButton("Cerrar", null)
                .show()
        }
    }

    private fun construirUrlImagen(foto: String?): String? {
        if (foto.isNullOrEmpty()) return null
        if (foto.startsWith("http")) return foto
        val baseUrl = RetrofitClient.BASE_URL.trimEnd('/')
        val cleanPath = foto.trimStart('/')
        return if (cleanPath.startsWith("storage/")) "$baseUrl/$cleanPath" else "$baseUrl/storage/$cleanPath"
    }
}
