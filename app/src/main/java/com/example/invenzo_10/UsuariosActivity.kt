package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class UsuariosActivity : AppCompatActivity() {

    private lateinit var rvUsuarios: RecyclerView
    private lateinit var usuarioAdapter: UsuarioAdapter
    private var listaUsuarios = mutableListOf<UserData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val rolActual = prefs.getString("user_role", "")
        if (rolActual != "Administrador Principal") {
            Toast.makeText(this, "Acceso denegado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        applyEdgeToEdgeWithInsets(null)
        setContentView(R.layout.activity_usuarios)
        applyEdgeToEdgeWithInsets(findViewById(R.id.topBar))

        NotificacionManager.init(this)
        NotificationUtils.setupNotificationButton(this)

        initViews()
        setupRecyclerView()
        setupBottomNavigation()

        findViewById<FloatingActionButton>(R.id.nuevoUsuario).setOnClickListener {
            val intent = Intent(this, NuevoUsuarioActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        cargarUsuarios()
        mostrarDatosUsuario()
    }

    private fun initViews() {
        rvUsuarios = findViewById(R.id.rvUsuarios)
    }

    private fun setupRecyclerView() {
        usuarioAdapter = UsuarioAdapter(listaUsuarios) { usuario ->
            mostrarDialogoEdicion(usuario)
        }
        rvUsuarios.layoutManager = LinearLayoutManager(this)
        rvUsuarios.adapter = usuarioAdapter
    }

    private fun mostrarDialogoEdicion(usuario: UserData) {
        AlertDialog.Builder(this)
            .setTitle("Editar Usuario")
            .setMessage("¿Deseas editar al usuario ${usuario.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                val intent = Intent(this, EditarUsuarioActivity::class.java).apply {
                    putExtra("user_id", usuario.id)
                    putExtra("user_name", usuario.nombre)
                    putExtra("user_email", usuario.email)
                    putExtra("user_role", usuario.rol)
                    putExtra("user_photo", usuario.foto)
                    putExtra("is_editing_other", true)
                }
                startActivity(intent)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cargarUsuarios() {
        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getUsuarios("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    listaUsuarios.clear()
                    listaUsuarios.addAll(response.body()!!)
                    usuarioAdapter.actualizarLista(listaUsuarios)
                }
            } catch (e: Exception) {
                Log.e("UsuariosActivity", "Error al refrescar lista: ${e.message}")
            }
        }
    }

    private fun mostrarDatosUsuario() {
        val txtNombre = findViewById<TextView>(R.id.txtUserNameHeader)
        val txtRoleCompany = findViewById<TextView>(R.id.txtUserRoleCompanyHeader)
        val imgProfile = findViewById<ImageView>(R.id.profileImageHeader)
        
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        val rol = prefs.getString("user_role", "")
        val empresa = prefs.getString("user_company", "")
        val fotoPath = prefs.getString("user_photo", "")
        
        txtNombre?.text = nombre
        txtRoleCompany?.text = "$rol • $empresa"

        if (imgProfile != null) {
            if (!fotoPath.isNullOrEmpty()) {
                // USAMOS OBTENER URL REALTIME PARA CARGA INSTANTÁNEA
                val fullUrl = RetrofitClient.obtenerUrlRealtime(fotoPath)
                
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

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.selectedItemId = R.id.more
        bottomNav?.setOnItemSelectedListener { item ->
            val intent = when (item.itemId) {
                R.id.home -> Intent(this, ActivityInicio::class.java)
                R.id.products -> Intent(this, ProductosActivity::class.java)
                R.id.categoria -> Intent(this, CategoriaActivity::class.java)
                R.id.reports -> Intent(this, ReportesActivity::class.java)
                R.id.more -> Intent(this, MasOpcionesActivity::class.java)
                else -> null
            }
            intent?.let {
                startActivity(it)
                finish()
            }
            true
        }
    }
}
