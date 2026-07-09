package com.example.invenzo_10

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import org.json.JSONObject

class RegistroActivity : AppCompatActivity() {

    private lateinit var btnRegistrar: Button
    private lateinit var progressRegistro: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        val inputNombre = findViewById<TextInputLayout>(R.id.inputNombre)
        val inputEmpresa = findViewById<TextInputLayout>(R.id.inputEmpresa)
        val inputCorreo = findViewById<TextInputLayout>(R.id.inputCorreo)
        val inputPass = findViewById<TextInputLayout>(R.id.inputPassword)
        val inputConfirmar = findViewById<TextInputLayout>(R.id.inputConfirmar)
        
        btnRegistrar = findViewById(R.id.buttonRegistro)
        progressRegistro = findViewById(R.id.progressRegistro)
        
        val textLogin = findViewById<TextView>(R.id.textLogin)

        btnRegistrar.setOnClickListener {
            val nombre = inputNombre.editText?.text.toString().trim()
            val empresa = inputEmpresa.editText?.text.toString().trim()
            val email = inputCorreo.editText?.text.toString().trim()
            val pass = inputPass.editText?.text.toString().trim()
            val confirmPass = inputConfirmar.editText?.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty() || empresa.isEmpty()) {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 8) {
                Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirmPass) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ejecutarRegistro(nombre, email, pass, confirmPass, empresa)
        }

        textLogin.setOnClickListener {
            finish()
        }
    }

    private fun ejecutarRegistro(nombre: String, email: String, pass: String, confirm: String, empresa: String) {
        val request = RegisterRequest(nombre, email, pass, confirm, empresa)

        // Bloquear UI para evitar doble clic y mostrar carga
        btnRegistrar.isEnabled = false
        progressRegistro.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.register(request)
                if (response.isSuccessful) {
                    Toast.makeText(this@RegistroActivity, "¡Registro exitoso! Revisa tu correo", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorJson = response.errorBody()?.string()
                    Log.e("RegistroError", "Error del servidor: $errorJson")
                    
                    val mensaje = try {
                        val json = JSONObject(errorJson ?: "{}")
                        json.optString("message", "Error en los datos")
                    } catch (e: Exception) {
                        "Datos inválidos"
                    }
                    Toast.makeText(this@RegistroActivity, mensaje, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("RegistroError", "Error de red: ${e.message}", e)
                Toast.makeText(this@RegistroActivity, "Error de conexión con el servidor", Toast.LENGTH_LONG).show()
            } finally {
                // Desbloquear UI
                btnRegistrar.isEnabled = true
                progressRegistro.visibility = View.GONE
            }
        }
    }
}
