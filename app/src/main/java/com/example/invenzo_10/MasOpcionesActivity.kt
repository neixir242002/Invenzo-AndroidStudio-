package com.example.invenzo_10

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MasOpcionesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mas_opciones)

        setupOptions()
        setupBottomNavigation()
    }

    private fun setupOptions() {
        // Administración
        setupOption(R.id.optCategorias, R.drawable.ic_launcher_foreground, "Categorías")
        setupOption(R.id.optProveedores, R.drawable.ic_launcher_foreground, "Proveedores")
        setupOption(R.id.optUsuarios, R.drawable.ic_launcher_foreground, "Usuarios")
        setupOption(R.id.optAlmacenes, R.drawable.ic_launcher_foreground, "Almacenes")

        // Configuración
        setupOption(R.id.optPerfil, R.drawable.ic_launcher_foreground, "Perfil")
        setupOption(R.id.optConfigApp, R.drawable.ic_launcher_foreground, "Configuración de la app")
        setupOption(R.id.optNotificaciones, R.drawable.ic_launcher_foreground, "Notificaciones")
        setupOption(R.id.optAyuda, R.drawable.ic_launcher_foreground, "Ayuda y soporte")
    }

    private fun setupOption(viewId: Int, iconRes: Int, title: String) {
        val view = findViewById<android.view.View>(viewId)
        view.findViewById<ImageView>(R.id.optionIcon).setImageResource(iconRes)
        view.findViewById<TextView>(R.id.optionTitle).text = title
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.more

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.more) return@setOnItemSelectedListener true

            val intent = when (item.itemId) {
                R.id.home -> Intent(this, ActivityInicio::class.java)
                R.id.products -> Intent(this, ProductosActivity::class.java)
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