package com.example.invenzo_10

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import java.io.File

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

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val producto = lista[position]

        holder.nombre.text = producto.nombre
        holder.codigo.text = producto.codigo
        holder.categoria.text = producto.categoria
        holder.stock.text = "Stock: ${producto.stock}"
        holder.precio.text = "$ ${producto.precio}"

        val archivo = File(producto.rutaImagen)

        if (archivo.exists()) {
            holder.imagen.setImageBitmap(
                BitmapFactory.decodeFile(
                    archivo.absolutePath
                )
            )
        }

        //==============================
        // Estado del Stock
        //==============================

        when {

            producto.stock == 0 -> {

                holder.estado.text = "Sin stock"
                holder.estado.setTextColor(Color.RED)

            }

            producto.stock <= producto.stockmini -> {

                holder.estado.text = "Stock bajo"
                holder.estado.setTextColor(
                    Color.parseColor("#FF9800")
                )

            }

            else -> {

                holder.estado.text = "Disponible"
                holder.estado.setTextColor(
                    Color.parseColor("#4CAF50")
                )

            }

        }

        //==============================
        // Estado del producto
        //==============================

        if (producto.activo) {

            holder.accion.text = "Activo"
            holder.accion.setTextColor(
                Color.parseColor("#4CAF50")
            )

        } else {

            holder.accion.text = "Inactivo"
            holder.accion.setTextColor(Color.RED)

        }
        holder.imagen.setOnClickListener {

            AlertDialog.Builder(holder.itemView.context)
                .setTitle(producto.nombre)
                .setMessage("¿Qué desea hacer?")
                .setPositiveButton("Editar producto") { _, _ ->

                    onEditar(producto, position)

                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

    }
}