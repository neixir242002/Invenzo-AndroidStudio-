package com.example.invenzo_10

import android.content.Intent
import android.os.Bundle
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
    }

    private fun setupClickListeners() {
        // Perfil
        findViewById<android.view.View>(R.id.cardProfile).setOnClickListener {
            Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show()
        }

        // Administración
        findViewById<android.view.View>(R.id.optCategorias).setOnClickListener {
            startActivity(Intent(this, CategoriaActivity::class.java))
        }
        findViewById<android.view.View>(R.id.optInventarios).setOnClickListener {
            Toast.makeText(this, "Inventarios", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.optUsuarios).setOnClickListener {
            startActivity(Intent(this, UsuariosActivity::class.java))
        }

        // Configuración
        findViewById<android.view.View>(R.id.optConfiguracion).setOnClickListener {
            startActivity(Intent(this, ConfiguracionActivity::class.java))
        }

        // FAB
        findViewById<android.view.View>(R.id.fab).setOnClickListener {
            startActivity(Intent(this, AgregarProductoActivity::class.java))
        }
        findViewById<android.view.View>(R.id.optCategorias).setOnClickListener {
            startActivity(Intent(this, CategoriaActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.more

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, ActivityInicio::class.java))
                    finish()
                    true
                }
                R.id.products -> {
                    startActivity(Intent(this, ProductosActivity::class.java))
                    finish()
                    true
                }
                R.id.reports -> {
                    startActivity(Intent(this, ReportesActivity::class.java))
                    finish()
                    true
                }
                R.id.more -> true
                else -> false
            }
        }
    }
}