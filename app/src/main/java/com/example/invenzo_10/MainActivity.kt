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

                    if (!token.isNullOrEmpty()) {
                        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            putString("token", token)
                            putString("user_name", loginResponse.user?.nombre)
                            apply()
                        }
                        Toast.makeText(this@MainActivity, "Bienvenido ${loginResponse.user?.nombre}", Toast.LENGTH_SHORT).show()
                        navigateTo(ActivityInicio::class.java, true)
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("LoginError", "Error servidor: $errorMsg")
                    Toast.makeText(this@MainActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // AQUÍ VERÁS EL ERROR REAL EN LOGCAT
                Log.e("LoginError", "Fallo de conexión: ${e.message}", e)
                Toast.makeText(this@MainActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
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
