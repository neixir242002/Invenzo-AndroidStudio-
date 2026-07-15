package com.example.invenzo_10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AuditoriaAdapter(private var lista: List<Auditoria>) :
    RecyclerView.Adapter<AuditoriaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtModulo: TextView = view.findViewById(R.id.txtModuloAudit)
        val txtFecha: TextView = view.findViewById(R.id.txtFechaAudit)
        val txtUsuario: TextView = view.findViewById(R.id.txtUsuarioAudit)
        val txtAccion: TextView = view.findViewById(R.id.txtAccionAudit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_auditoria, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.txtModulo.text = item.modulo ?: "—"
        holder.txtFecha.text = formatearFecha(item.fecha)
        holder.txtUsuario.text = item.usuario?.nombre ?: "—"
        holder.txtAccion.text = item.accion ?: "—"
    }

    override fun getItemCount(): Int = lista.size

    private fun formatearFecha(fechaStr: String?): String {
        if (fechaStr.isNullOrEmpty()) return "Fecha no disponible"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(fechaStr)
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            date?.let { formatter.format(it) } ?: fechaStr
        } catch (e: Exception) {
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = parser.parse(fechaStr)
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                date?.let { formatter.format(it) } ?: fechaStr
            } catch (e2: Exception) {
                fechaStr
            }
        }
    }

    fun actualizar(nuevaLista: List<Auditoria>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}