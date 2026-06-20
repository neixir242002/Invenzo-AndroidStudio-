package com.example.invenzo_10

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

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
    }
}