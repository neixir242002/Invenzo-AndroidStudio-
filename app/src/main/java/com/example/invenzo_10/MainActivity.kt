package com.example.invenzo_10

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val inicioSecion: Button = findViewById(R.id.inicioSecion)

        inicioSecion.setOnClickListener {
            val intent = Intent(this, ActivityInicio::class.java)
            startActivity(intent)
            // Transición suave de desvanecimiento
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }
    }
}