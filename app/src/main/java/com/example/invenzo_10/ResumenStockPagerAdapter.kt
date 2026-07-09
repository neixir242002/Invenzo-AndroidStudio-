package com.example.invenzo_10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class ResumenStockPageAdapter(
    private var productos: MutableList<Producto>
) : RecyclerView.Adapter<ResumenStockPageAdapter.PageViewHolder>() {

    private val productosPorPagina = 5

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recycler: RecyclerView =
            view.findViewById(R.id.rvProductosPage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.page_resumen_stock, parent, false)

        return PageViewHolder(view)
    }

    override fun getItemCount(): Int {

        return kotlin.math.ceil(
            productos.size / productosPorPagina.toDouble()
        ).toInt()
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {

        val inicio = position * productosPorPagina

        val fin = minOf(
            inicio + productosPorPagina,
            productos.size
        )

        val listaPagina = productos.subList(inicio, fin)

        holder.recycler.layoutManager =
            LinearLayoutManager(holder.itemView.context)

        holder.recycler.adapter =
            ResumenStockAdapter(listaPagina.toMutableList())
    }

    fun actualizar(lista: MutableList<Producto>) {

        productos = lista

        notifyDataSetChanged()
    }
}