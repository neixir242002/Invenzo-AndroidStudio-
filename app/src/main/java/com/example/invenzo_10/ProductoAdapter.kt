package com.example.invenzo_10

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
<<<<<<< HEAD
=======
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getColor
>>>>>>> 9f9f11d (Styles Producto)
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import kotlin.random.Random

class ProductoAdapter(
    private val lista: MutableList<Producto>
) : RecyclerView.Adapter<ProductoAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val imagen: ImageView =
            itemView.findViewById(R.id.imgProducto)

        val nombre: TextView =
            itemView.findViewById(R.id.txtNombre)

        val categoria: TextView =
            itemView.findViewById(R.id.txtCategoria)

        val cantidad: TextView =
            itemView.findViewById(R.id.txtCantidad)

        val precio: TextView =
            itemView.findViewById(R.id.txtPrecio)
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

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val producto = lista[position]

        holder.nombre.text = producto.nombre
        holder.categoria.text = producto.categoria
        holder.cantidad.text = "Cantidad: ${producto.cantidad}"
        holder.precio.text = "$ ${producto.precio}"

        val archivo = File(producto.rutaImagen)

        if (archivo.exists()) {

            val bitmap =
                BitmapFactory.decodeFile(
                    archivo.absolutePath
                )

            holder.imagen.setImageBitmap(bitmap)
        }
<<<<<<< HEAD
=======

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

            val dialog = AlertDialog.Builder(holder.itemView.context)
                .setTitle(producto.nombre)
                .setMessage("¿Qué desea hacer?")
                .setPositiveButton("Editar producto") { _, _ ->
                    onEditar(producto, position)
                }
                .setNegativeButton("Cancelar", null)
                .create()

            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(holder.itemView.context.getColor(R.color.primaryColor))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(holder.itemView.context.getColor(R.color.dangerColor))
        }

>>>>>>> 9f9f11d (Styles Producto)
    }
}