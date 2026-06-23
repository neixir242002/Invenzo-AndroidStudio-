package com.example.invenzo_10

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class ControlInventarioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_control_inventario)

        setupSpinners()
        setupBottomNavigation()
    }

    private fun setupSpinners() {
        // Productos
        val productos = arrayOf("Laptop Dell XPS", "Mouse Logitech MX", "Teclado HP K120", "Monitor LG 24\"")
        val adapterProductos = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, productos)
        findViewById<MaterialAutoCompleteTextView>(R.id.spinnerProducto).setAdapter(adapterProductos)

        // Tipos de Movimiento
        val tipos = arrayOf("Entrada", "Salida")
        val adapterTipos = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tipos)
        findViewById<MaterialAutoCompleteTextView>(R.id.spinnerTipo).setAdapter(adapterTipos)
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.more 

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == bottomNav.selectedItemId && item.itemId != R.id.more) return@setOnItemSelectedListener true

            val intent = when (item.itemId) {
                R.id.home -> Intent(this, ActivityInicio::class.java)
                R.id.products -> Intent(this, ProductosActivity::class.java)
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