package com.example.invenzo_10

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProductosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.productos)

        // 🔹 Bottom Navigation
        val bottomNav =
            findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.home -> {
                    startActivity(Intent(this, ActivityInicio::class.java))
                    true
                }

                R.id.products -> {

                    true
                }

                R.id.reports -> {
                    startActivity(Intent(this, ReportesActivity::class.java))
                    true
                }

                R.id.more -> {
                    startActivity(Intent(this, MasOpcionesActivity::class.java))
                    true
                }

                else -> false
            }
        }

        // 🔹 Floating Button
        val fab =
            findViewById<FloatingActionButton>(
                R.id.floatingActionButton
            )


        // 🔹 Botón agregar producto
        val agregarProducto =
            findViewById<FloatingActionButton>(
                R.id.agregar_producto
            )

        agregarProducto.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    AgregarProductoActivity::class.java
                )
            )
        }
    }
}