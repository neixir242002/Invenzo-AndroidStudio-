package com.example.invenzo_10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.ceil

class AuditoriaPagerAdapter(
    private var todasLasAuditorias: List<Auditoria>
) : RecyclerView.Adapter<AuditoriaPagerAdapter.PageViewHolder>() {

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
        val fin = minOf(inicio + itemsPorPagina, todasLasAuditorias.size)
        val subLista = todasLasAuditorias.subList(inicio, fin)

        holder.rvPage.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.rvPage.adapter = AuditoriaAdapter(subLista)
    }

    override fun getItemCount(): Int {
        if (todasLasAuditorias.isEmpty()) return 0
        return ceil(todasLasAuditorias.size / itemsPorPagina.toDouble()).toInt()
    }

    fun actualizar(nuevaLista: List<Auditoria>) {
        todasLasAuditorias = nuevaLista
        notifyDataSetChanged()
    }
}