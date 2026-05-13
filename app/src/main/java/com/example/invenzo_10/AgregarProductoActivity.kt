package com.example.invenzo_10

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class AgregarProductoActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_agregar_producto)

        // Bottom Navigation
//        val bottomNav =
//            findViewById<BottomNavigationView>(
//                R.id.bottomNav
//            )
//
//        bottomNav.setOnItemSelectedListener { item ->
//
//            when (item.itemId) {
//
//                R.id.home -> {
//
//                    finish()
//
//                    true
//                }
//
//                R.id.products -> {
//
//                    true
//                }
//
//                R.id.reports -> {
//
//                    true
//                }
//
//                R.id.more -> {
//
//                    true
//                }
//
//                else -> false
//            }
//        }
    }
}