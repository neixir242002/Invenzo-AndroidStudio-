package com.example.invenzo_10

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CategoriaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_categoria)

        mostrarNombre()
        setupBottomNavigation()
        setupClickListeners()

    }
    private fun mostrarNombre(){

        //Se puede ver el nombre de usuario en el TopBar
        val txtNombre = findViewById<TextView>(
            R.id.txtUserNameHeader
        )


        val datos = getSharedPreferences(
            "usuario_prueba",
            MODE_PRIVATE
        )


        val nombre = datos.getString(
            "nombre",
            "Usuario"
        )


        txtNombre.text = nombre
    }
    private fun setupClickListeners() {
        // Perfil
        findViewById<android.view.View>(R.id.agregarCategoria).setOnClickListener {
            startActivity(Intent(this, NuevaCategoriaActivity::class.java))        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.categoria

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.categoria) return@setOnItemSelectedListener true

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