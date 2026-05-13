package com.example.invenzo_10

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProductosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.productos)

        // 🔹 Bottom Navigation
        val bottomNav =
            findViewById<BottomNavigationView>(
                R.id.bottomNav
            )

        bottomNav.selectedItemId = R.id.products

        bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.home -> {

                    startActivity(
                        Intent(
                            this,
                            ActivityInicio::class.java
                        )
                    )

                    true
                }

                R.id.products -> {

                    true
                }

                R.id.reports -> {

                    true
                }

                R.id.more -> {

                    true
                }

                else -> false
            }
        }

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