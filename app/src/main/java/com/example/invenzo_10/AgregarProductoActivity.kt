package com.example.invenzo_10

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class AgregarProductoActivity : AppCompatActivity() {

    // En desarrollo.. agregar imagen y guardar profucto

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_producto)
        setupClickListeners()
    }
    private fun setupClickListeners() {
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener {
            startActivity(Intent(this, ProductosActivity::class.java))        }
    }

}