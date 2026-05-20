package com.example.invenzo_10

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class RegistroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_register)

        // 🔹 Botón crear cuenta
        val buttonRegistro =
            findViewById<Button>(
                R.id.buttonRegistro
            )

        buttonRegistro.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )
        }

        // 🔹 Texto iniciar sesión
        val textLogin =
            findViewById<TextView>(
                R.id.textLogin
            )

        textLogin.setOnClickListener {

            finish()
        }
    }
}