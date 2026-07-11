package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val inputCorreo = findViewById<TextInputLayout>(R.id.inputCorreo)
        val inputPassword = findViewById<TextInputLayout>(R.id.inputPassword)
        val btnLogin = findViewById<Button>(R.id.inicioSecion)
        val tvRegister = findViewById<TextView>(R.id.register)

        btnLogin.setOnClickListener {
            val correo = inputCorreo.editText?.text.toString().trim()
            val password = inputPassword.editText?.text.toString().trim()

            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ejecutarLogin(correo, password)
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
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
                        // Normalización de roles para compatibilidad con las restricciones de la app
                        val rolOriginal = user.rol ?: "Usuario"
                        val rolNormalizado = when {
                            rolOriginal.contains("admin", ignoreCase = true) -> "Administrador"
                            rolOriginal.contains("aux", ignoreCase = true) -> "Auxiliar"
                            else -> rolOriginal
                        }

                        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            putString("token", token)
                            putInt("user_id", user.id)
                            putString("user_name", user.nombre)
                            putString("user_email", user.email)
                            putString("user_role", rolNormalizado)
                            // Extraemos el nombre de la empresa del objeto EmpresaData
                            putString("user_company", user.empresa?.nombre ?: "Empresa")
                            apply()
                        }
                        Toast.makeText(this@MainActivity, "Bienvenido ${user.nombre}", Toast.LENGTH_SHORT).show()
                        navigateTo(ActivityInicio::class.java, true)
                    }
                } else {
                    Log.e("LoginError", "Error servidor: ${response.code()}")
                    Toast.makeText(this@MainActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("LoginError", "Error: ${e.message}", e)
                Toast.makeText(this@MainActivity, "Error de conexión con el servidor", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateTo(destination: Class<*>, finishCurrent: Boolean = false) {
        val intent = Intent(this, destination)
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        if (finishCurrent) finish()
    }
}
