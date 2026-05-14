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
            Toast.makeText(this, "Categorías", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.optInventarios).setOnClickListener {
            Toast.makeText(this, "Inventarios", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.optUsuarios).setOnClickListener {
            Toast.makeText(this, "Usuarios", Toast.LENGTH_SHORT).show()
        }

        // Configuración
        findViewById<android.view.View>(R.id.optPerfil).setOnClickListener {
            Toast.makeText(this, "Ajustes de Perfil", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.optConfigApp).setOnClickListener {
            Toast.makeText(this, "Configuración de la app", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.optNotificaciones).setOnClickListener {
            Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.optAyuda).setOnClickListener {
            Toast.makeText(this, "Ayuda y soporte", Toast.LENGTH_SHORT).show()
        }

        // Cerrar sesión
        findViewById<android.view.View>(R.id.btnLogout).setOnClickListener {
            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show()
            // Aquí iría la lógica de logout
        }
        
        // FAB
        findViewById<android.view.View>(R.id.fab).setOnClickListener {
            startActivity(Intent(this, AgregarProductoActivity::class.java))
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