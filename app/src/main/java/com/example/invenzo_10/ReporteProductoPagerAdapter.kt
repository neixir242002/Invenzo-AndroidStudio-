package com.example.invenzo_10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReporteProductoPagerAdapter(private var todosLosProductos: List<Producto>) :
    RecyclerView.Adapter<ReporteProductoPagerAdapter.PageViewHolder>() {

    private val itemsPorPagina = 5

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recycler: RecyclerView = view.findViewById(R.id.recyclerProductos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.page_reporte_productos, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val inicio = position * itemsPorPagina
        val fin = minOf(inicio + itemsPorPagina, todosLosProductos.size)
        val subLista = todosLosProductos.subList(inicio, fin)

        holder.recycler.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.recycler.adapter = ReporteProductoAdapter(subLista)
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