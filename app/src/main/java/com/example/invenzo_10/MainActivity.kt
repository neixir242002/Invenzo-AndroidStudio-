package com.example.invenzo_10

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        // 🔹 Botón iniciar sesión
        val inicioSecion: Button =
            findViewById(R.id.inicioSecion)

        inicioSecion.setOnClickListener {

            val intent =
                Intent(
                    this,
                    ActivityInicio::class.java
                )

            startActivity(intent)
        }

        // 🔹 Texto Registrate
        val registrate: TextView =
            findViewById(R.id.textView4)

        registrate.setOnClickListener {

            val intent =
                Intent(
                    this,
                    RegistroActivity::class.java
                )

            startActivity(intent)
        }
    }
}