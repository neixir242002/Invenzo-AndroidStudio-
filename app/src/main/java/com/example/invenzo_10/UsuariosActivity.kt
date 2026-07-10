package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class UsuariosActivity : AppCompatActivity() {

    private lateinit var rvUsuarios: RecyclerView
    private lateinit var usuarioAdapter: UsuarioAdapter
    private var listaUsuarios = mutableListOf<UserData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_usuarios)

        mostrarDatosUsuario()
        initViews()
        setupRecyclerView()
        setupBottomNavigation()
        cargarUsuarios()

        findViewById<FloatingActionButton>(R.id.nuevoUsuario).setOnClickListener {
            val intent = Intent(this, NuevoUsuarioActivity::class.java)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        rvUsuarios = findViewById(R.id.rvUsuarios)
    }

    private fun setupRecyclerView() {
        usuarioAdapter = UsuarioAdapter(listaUsuarios)
        rvUsuarios.layoutManager = LinearLayoutManager(this)
        rvUsuarios.adapter = usuarioAdapter
    }

    private fun mostrarDatosUsuario() {
        val txtNombre = findViewById<TextView>(R.id.txtUserNameHeader)
        val txtRoleCompany = findViewById<TextView>(R.id.txtUserRoleCompanyHeader)
        
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        val rol = prefs.getString("user_role", "Administrador")
        val empresa = prefs.getString("user_company", "Empresa")

        txtNombre?.text = nombre
        txtRoleCompany?.text = "$rol • $empresa"
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
                } else {
                    Log.e("UsuariosActivity", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("UsuariosActivity", "Error de red: ${e.message}")
                Toast.makeText(this@UsuariosActivity, "Error al cargar usuarios", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.selectedItemId = R.id.more

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val rol = prefs.getString("user_role", "Administrador")
        if (rol == "Auxiliar") {
            bottomNav?.menu?.findItem(R.id.categoria)?.isVisible = false
        }

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
                it.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(it)
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
                finish()
            }
            true
        }
    }
}
