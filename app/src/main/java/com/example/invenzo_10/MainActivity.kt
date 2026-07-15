package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        if (!prefs.getString("token", null).isNullOrEmpty()) {
            navigateTo(ActivityInicio::class.java, true)
            return
        }

        // isLightStatusBar = false para que los iconos de la barra sean blancos sobre el fondo azul
        applyEdgeToEdgeWithInsets(null, false)
        setContentView(R.layout.activity_main)
        
        // Ajustamos el padding del contenedor del logo para evitar el notch/barra de estado
        applyEdgeToEdgeWithInsets(findViewById(R.id.logoContainer), false)

        val inputCorreo = findViewById<TextInputLayout>(R.id.inputCorreo)
        val inputPassword = findViewById<TextInputLayout>(R.id.inputPassword)
        val btnLogin = findViewById<Button>(R.id.inicioSecion)
        val tvRegister = findViewById<TextView>(R.id.register)
        val tvForgotPass = findViewById<TextView>(R.id.restContraseña)

        btnLogin?.setOnClickListener {
            val correo = inputCorreo?.editText?.text.toString().trim()
            val password = inputPassword?.editText?.text.toString().trim()

            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ejecutarLogin(correo, password)
        }

        tvRegister?.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        tvForgotPass?.setOnClickListener {
            startActivity(Intent(this, RestablecerPasswordActivity::class.java))
        }
    }

    private fun ejecutarLogin(email: String, pass: String) {
        val request = LoginRequest(email, pass)
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.login(request)
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val token = loginResponse?.token
                    val user = loginResponse?.user

                    if (!token.isNullOrEmpty() && user != null) {
                        val rolOriginal = user.rol ?: ""
                        val rolNormalizado = when {
                            rolOriginal.equals("administrador_principal", ignoreCase = true) -> "Administrador Principal"
                            rolOriginal.equals("administrador", ignoreCase = true) -> "Administrador"
                            rolOriginal.contains("aux", ignoreCase = true) -> "Auxiliar"
                            else -> "Administrador Principal"
                        }

                        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            putString("token", token)
                            putInt("user_id", user.id)
                            putString("user_name", user.nombre)
                            putString("user_email", user.email)
                            putString("user_role", rolNormalizado)
                            putString("user_company", user.empresa?.nombre ?: "Empresa")
                            putString("user_photo", user.foto)
                            apply()
                        }
                        Toast.makeText(this@MainActivity, "Bienvenido ${user.nombre}", Toast.LENGTH_SHORT).show()
                        navigateTo(ActivityInicio::class.java, true)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateTo(destination: Class<*>, finishCurrent: Boolean = false) {
        val intent = Intent(this, destination)
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        if (finishCurrent) finish()
    }
}