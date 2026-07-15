package com.example.invenzo_10

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import kotlinx.coroutines.launch

class NotificacionesActivity : AppCompatActivity() {

    private lateinit var rvNotificaciones: RecyclerView
    private lateinit var txtEmpty: TextView
    private lateinit var adapter: NotificacionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyEdgeToEdgeWithInsets(null)
        setContentView(R.layout.activity_notificaciones)
        applyEdgeToEdgeWithInsets(findViewById(R.id.layoutHeader))

        initViews()
        mostrarDatosUsuario()
        
        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }
        findViewById<View>(R.id.btnDeleteAllNotif)?.setOnClickListener { borrarTodo() }
    }

    private fun initViews() {
        rvNotificaciones = findViewById(R.id.rvNotificacionesFull)
        txtEmpty = findViewById(R.id.txtEmptyNotif)
        rvNotificaciones.layoutManager = LinearLayoutManager(this)
    }

    private fun cargarDesdeBaseDeDatos() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getNotificaciones("Bearer $token")
                
                if (response.isSuccessful) {
                    val notifications = response.body() ?: emptyList()
                    actualizarUI(notifications)
                } else {
                    actualizarUI(emptyList())
                }
            } catch (e: Exception) {
                actualizarUI(emptyList())
            }
        }
    }

    private fun actualizarUI(lista: List<Notificacion>) {
        if (lista.isEmpty()) {
            txtEmpty.visibility = View.VISIBLE
            rvNotificaciones.visibility = View.GONE
        } else {
            txtEmpty.visibility = View.GONE
            rvNotificaciones.visibility = View.VISIBLE
            adapter = NotificacionAdapter(lista)
            rvNotificaciones.adapter = adapter
        }
    }

    private fun borrarTodo() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.limpiarNotificaciones("Bearer $token")
                if (response.isSuccessful) {
                    Toast.makeText(this@NotificacionesActivity, "Notificaciones marcadas como leídas", Toast.LENGTH_SHORT).show()
                    cargarDesdeBaseDeDatos()
                }
            } catch (e: Exception) {
                Log.e("NOTIF", "Error", e)
            }
        }
    }

    private fun mostrarDatosUsuario() {
        val txtNombre = findViewById<TextView>(R.id.txtUserNameHeader)
        val txtRoleCompany = findViewById<TextView>(R.id.txtUserRoleCompanyHeader)
        val imgProfile = findViewById<ImageView>(R.id.profileImageHeader)
        
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        val rol = prefs.getString("user_role", "Admin")
        val empresa = prefs.getString("user_company", "Empresa")
        val fotoPath = prefs.getString("user_photo", "")
        
        txtNombre?.text = nombre
        txtRoleCompany?.text = "$rol • $empresa"

        if (imgProfile != null) {
            if (!fotoPath.isNullOrEmpty()) {
                val cleanPath = if (fotoPath.startsWith("/")) fotoPath.substring(1) else fotoPath
                val fullUrl = if (fotoPath.startsWith("http")) fotoPath else "${RetrofitClient.BASE_URL}storage/$cleanPath"
                
                Glide.with(this)
                    .load(fullUrl)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .circleCrop()
                    .into(imgProfile)
            } else {
                imgProfile.setImageResource(R.drawable.ic_user)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDesdeBaseDeDatos()
        mostrarDatosUsuario()
    }
}
