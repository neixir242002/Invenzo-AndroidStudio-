package com.example.invenzo_10

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CategoriaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categoria)

        setupBottomNavigation()
        setupFab()
    }

    private fun setupBottomNavigation() {

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // Marca la opción actual
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

    private fun setupFab() {

        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)

        fab.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AgregarProductoActivity::class.java
                )
            )
        }
    }
}