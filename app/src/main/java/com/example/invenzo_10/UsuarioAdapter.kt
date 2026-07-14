package com.example.invenzo_10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class UsuarioAdapter(
    private var listaUsuarios: List<UserData>,
    private val onItemClick: (UserData) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.txtName)
        val txtRole: TextView = view.findViewById(R.id.txtRole)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val badgeStatus: MaterialCardView = view.findViewById(R.id.badgeStatus)
        val txtInitials: TextView = view.findViewById(R.id.txtInitials)
        val imgProfile: View = view.findViewById(R.id.imgProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = listaUsuarios[position]
        val context = holder.itemView.context

        holder.txtName.text = usuario.nombre
        holder.txtRole.text = "${usuario.rol?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "Usuario"} • ${usuario.email}"

        // Lógica de iniciales
        val iniciales = usuario.nombre.split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .map { it[0].uppercase() }
            .joinToString("")
        
        holder.txtInitials.text = iniciales
        holder.txtInitials.visibility = View.VISIBLE
        holder.imgProfile.visibility = View.GONE

        holder.txtStatus.text = "Activo"
        holder.badgeStatus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.successLight))
        holder.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.successColor))

        holder.itemView.setOnClickListener {
            onItemClick(usuario)
        }
    }

    override fun getItemCount() = listaUsuarios.size

    fun actualizarLista(nuevaLista: List<UserData>) {
        listaUsuarios = nuevaLista
        notifyDataSetChanged()
    }
}
