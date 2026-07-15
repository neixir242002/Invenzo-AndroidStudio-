package com.example.invenzo_10

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners


class ProductoAdapter(
    private val lista: MutableList<Producto>,
    private val onEditar: (Producto, Int) -> Unit
): RecyclerView.Adapter<ProductoAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val imagen: ImageView =
            itemView.findViewById(R.id.imgProducto)

        val nombre: TextView =
            itemView.findViewById(R.id.txtNombre)

        val codigo: TextView =
            itemView.findViewById(R.id.txtCodigo)

        val categoria: TextView =
            itemView.findViewById(R.id.txtCategoria)

        val stock: TextView =
            itemView.findViewById(R.id.txtStock)

        val precio: TextView =
            itemView.findViewById(R.id.txtPrecio)

        val estado: TextView =
            itemView.findViewById(R.id.txtEstado)
        val accion: TextView =
            itemView.findViewById(R.id.txtAcciones)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_producto,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val producto = lista[position]

        holder.nombre.text = producto.nombre

        holder.codigo.text = "#${producto.codigo}"
        holder.categoria.text = producto.categoria?.nombre ?: "Sin categoría"
        holder.stock.text = "Stock: ${producto.cantidad}"
        holder.precio.text = "$${producto.precio}"

        // USAMOS LA FUNCIÓN REALTIME PARA EVITAR CACHÉ COMPLETAMENTE
        val urlImagen = RetrofitClient.obtenerUrlRealtime(producto.foto)
        
        Glide.with(context)
            .load(urlImagen)
            .placeholder(R.drawable.ic_launcher_background) 
            .error(R.drawable.ic_launcher_background)
            .diskCacheStrategy(DiskCacheStrategy.NONE) 
            .skipMemoryCache(true)
            .transform(CenterCrop(), RoundedCorners(24))
            .into(holder.imagen)

        // --- ESTADO DE STOCK ---
        when {

            producto.cantidad == 0 -> {

                holder.estado.text = "Sin stock"
                holder.estado.setBackgroundResource(R.drawable.bg_red_icon)
                holder.estado.setTextColor(ContextCompat.getColor(context, R.color.dangerColor))
                holder.estado.backgroundTintList = null

            }

            producto.cantidad <= producto.stockMinimo -> {

                holder.estado.text = "Stock bajo"
                holder.estado.setBackgroundResource(R.drawable.bg_orange_icon)
                holder.estado.setTextColor(Color.parseColor("#F97316"))
                holder.estado.backgroundTintList = null

            }

            else -> {

                holder.estado.text = "Disponible"
                holder.estado.setBackgroundResource(R.drawable.bg_green_icon)
                holder.estado.setTextColor(ContextCompat.getColor(context, R.color.successColor))
                holder.estado.backgroundTintList = null

            }
        }

        //==============================
        // Estado del producto
        //==============================

        if (producto.activo == 1) {

            holder.accion.text = "Activo"
            holder.accion.setBackgroundResource(R.drawable.bg_green_icon)
            holder.accion.setTextColor(Color.parseColor("#059669"))
            holder.accion.backgroundTintList = null

        } else {

            holder.accion.text = "Inactivo"
            holder.accion.setBackgroundResource(R.drawable.bg_user_pill)
            holder.accion.setTextColor(Color.parseColor("#64748B"))
            holder.accion.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E2E8F0"))
        }

        holder.itemView.setOnClickListener {
            onAction(producto, position, "SHOW_OPTIONS")
        }
    }
}

