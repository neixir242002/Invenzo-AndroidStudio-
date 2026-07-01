package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MasOpcionesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mas_opciones)

        setupClickListeners()
        setupBottomNavigation()
        mostrarNombre()
    }

    private fun mostrarNombre() {
        val txtNombre = findViewById<TextView>(R.id.txtName)
        // Usamos "auth" que es donde MainActivity guarda el nombre
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        txtNombre.text = nombre
    }

    private fun setupClickListeners() {
        // Perfil
        findViewById<android.view.View>(R.id.cardProfile).setOnClickListener {
            Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show()
        }
        // Administración
        findViewById<android.view.View>(R.id.optInventarios).setOnClickListener {
            startActivity(Intent(this, ControlInventarioActivity::class.java))
        }
        findViewById<android.view.View>(R.id.optUsuarios).setOnClickListener {
            startActivity(Intent(this, UsuariosActivity::class.java))
        }

        // Configuración
        findViewById<android.view.View>(R.id.optConfiguracion).setOnClickListener {
            startActivity(Intent(this, ConfiguracionActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.more

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.more) return@setOnItemSelectedListener true

            val intent = when (item.itemId) {
                R.id.home -> Intent(this, ActivityInicio::class.java)
                R.id.products -> Intent(this, ProductosActivity::class.java)
                R.id.categoria -> Intent(this, CategoriaActivity::class.java)
                R.id.reports -> Intent(this, ReportesActivity::class.java)
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