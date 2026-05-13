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
        // Spinner de Productos
        val productos = arrayOf("Laptop Dell XPS", "Mouse Logitech MX", "Teclado HP K120", "Monitor LG 24\"")
        val adapterProductos = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, productos)
        findViewById<MaterialAutoCompleteTextView>(R.id.spinnerProducto).setAdapter(adapterProductos)

        // Spinner de Tipos de Movimiento
        val tipos = arrayOf("Entrada", "Salida")
        val adapterTipos = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tipos)
        findViewById<MaterialAutoCompleteTextView>(R.id.spinnerTipo).setAdapter(adapterTipos)
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        // No marcamos ninguno como seleccionado por defecto si no es una de las 4 pestañas principales, 
        // o podemos marcar 'Más' si se considera parte de esa sección.
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
                R.id.more -> {
                    startActivity(Intent(this, MasOpcionesActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}