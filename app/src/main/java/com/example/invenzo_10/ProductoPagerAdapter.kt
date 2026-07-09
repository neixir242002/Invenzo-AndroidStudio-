package com.example.invenzo_10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProductoPagerAdapter(
    private var todosLosProductos: List<Producto>,
    private val onAction: (Producto, Int, String) -> Unit
) : RecyclerView.Adapter<ProductoPagerAdapter.PageViewHolder>() {

    private val itemsPorPagina = 5

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rvPage: RecyclerView = view.findViewById(R.id.rvProductosPage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_productos_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val inicio = position * itemsPorPagina
        val fin = minOf(inicio + itemsPorPagina, todosLosProductos.size)
        val subLista = todosLosProductos.subList(inicio, fin)

        holder.rvPage.layoutManager = LinearLayoutManager(holder.itemView.context)
        // Pasamos la acción al adaptador de la página, ajustando el índice global si es necesario
        // Pero para simplificar, el adaptador recibe el objeto Producto y su índice en la sublista.
        // Usaremos el objeto Producto para las acciones de API.
        holder.rvPage.adapter = ProductoAdapter(subLista.toMutableList(), onAction)
    }

    override fun getItemCount(): Int {
        if (todosLosProductos.isEmpty()) return 0
        return kotlin.math.ceil(todosLosProductos.size.toDouble() / itemsPorPagina).toInt()
    }

    fun actualizar(nuevaLista: List<Producto>) {
        todosLosProductos = nuevaLista
        notifyDataSetChanged()
    }
}