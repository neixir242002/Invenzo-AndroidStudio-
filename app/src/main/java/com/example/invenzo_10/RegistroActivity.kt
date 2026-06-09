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

        // Botón crear cuenta
        val buttonRegistro = findViewById<Button>(R.id.buttonRegistro)
        buttonRegistro.setOnClickListener {
            // Regresar al login (MainActivity) después de registrar
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Texto iniciar sesión (si ya tiene cuenta)
        val textLogin = findViewById<TextView>(R.id.textLogin)
        textLogin.setOnClickListener {
            finish()
        }
    }
}
