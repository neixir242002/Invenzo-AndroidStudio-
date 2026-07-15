package com.example.invenzo_10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.ceil

class HistorialPagerAdapter(
    private var todosLosMovimientos: List<Movimiento>
) : RecyclerView.Adapter<HistorialPagerAdapter.PageViewHolder>() {

    private val itemsPorPagina = 8

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rvPage: RecyclerView = view.findViewById(R.id.rvMovimientosPage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_movimientos_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val inicio = position * itemsPorPagina
        val fin = minOf(inicio + itemsPorPagina, todosLosMovimientos.size)
        val subLista = todosLosMovimientos.subList(inicio, fin)

        holder.rvPage.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.rvPage.adapter = MovimientoAdapter(subLista.toMutableList())
    }

    override fun getItemCount(): Int {
        if (todosLosMovimientos.isEmpty()) return 0
        return ceil(todosLosMovimientos.size / itemsPorPagina.toDouble()).toInt()
    }

    fun actualizar(nuevaLista: List<Movimiento>) {
        todosLosMovimientos = nuevaLista
        notifyDataSetChanged()
    }
}
