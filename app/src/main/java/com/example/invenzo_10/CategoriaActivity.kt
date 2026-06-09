package com.example.invenzo_10

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CategoriaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_categoria)
        setupBottomNavigation()

        val agregarCategoria = findViewById<FloatingActionButton>(R.id.agregarCategoria)

        agregarCategoria.setOnClickListener {
            val intent = Intent(this, NuevaCategoriaActivity::class.java)
            startActivity(intent)
            // Transición suave de desvanecimiento
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

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