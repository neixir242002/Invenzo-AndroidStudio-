package com.example.invenzo_10

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
                Log.d("NOTIF_DEBUG", "Iniciando carga con token: ${token.take(10)}...")
                val response = RetrofitClient.instance.getNotificaciones("Bearer $token")
                
                if (response.isSuccessful) {
                    val notifications = response.body() ?: emptyList()
                    Log.d("NOTIF_DEBUG", "Carga exitosa. Recibidas: ${notifications.size}")
                    
                    // DIAGNÓSTICO EN PANTALLA
                    Toast.makeText(this@NotificacionesActivity, "Notificaciones recibidas: ${notifications.size}", Toast.LENGTH_SHORT).show()
                    
                    if (notifications.isNotEmpty()) {
                        Log.d("NOTIF_DEBUG", "Primera notif: Título=${notifications[0].titulo}, Mensaje=${notifications[0].mensaje}")
                    }
                    
                    actualizarUI(notifications)
                } else {
                    val error = response.errorBody()?.string() ?: "Sin error body"
                    Log.e("NOTIF_DEBUG", "Error ${response.code()}: $error")
                    Toast.makeText(this@NotificacionesActivity, "Error al cargar: ${response.code()}", Toast.LENGTH_LONG).show()
                    actualizarUI(emptyList())
                }
            } catch (e: Exception) {
                Log.e("NOTIF_DEBUG", "Excepción: ${e.message}", e)
                Toast.makeText(this@NotificacionesActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
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
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        findViewById<TextView>(R.id.txtUserNameHeader)?.text = prefs.getString("user_name", "Usuario")
        findViewById<TextView>(R.id.txtUserRoleCompanyHeader)?.text =
            "${prefs.getString("user_role", "Admin")} • ${prefs.getString("user_company", "Empresa")}"
    }

    override fun onResume() {
        super.onResume()
        cargarDesdeBaseDeDatos()
    }
}
