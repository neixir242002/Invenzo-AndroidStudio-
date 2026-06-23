package com.example.invenzo_10

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val inputNombre = findViewById<TextInputLayout>(R.id.inputNombre)
        val inputEmpresa = findViewById<TextInputLayout>(R.id.inputEmpresa)
        val inputCorreo = findViewById<TextInputLayout>(R.id.inputCorreo)
        val inputPass = findViewById<TextInputLayout>(R.id.inputPassword)
        val inputConfirmar = findViewById<TextInputLayout>(R.id.inputConfirmar)
        val btnRegistrar = findViewById<Button>(R.id.buttonRegistro)
        val textLogin = findViewById<TextView>(R.id.textLogin)

        btnRegistrar.setOnClickListener {
            val nombre = inputNombre.editText?.text.toString().trim()
            val empresa = inputEmpresa.editText?.text.toString().trim()
            val email = inputCorreo.editText?.text.toString().trim()
            val pass = inputPass.editText?.text.toString().trim()
            val confirmPass = inputConfirmar.editText?.text.toString().trim()

            if (nombre.isNotEmpty() && empresa.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    if (pass.length >= 8) {
                        ejecutarRegistroEnLaravel(nombre, email, pass, confirmPass, empresa)
                    } else {
                        Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        textLogin.setOnClickListener {
            irALogin()
        }
    }

    private fun ejecutarRegistroEnLaravel(nombre: String, email: String, pass: String, confirmPass: String, empresa: String) {
//        val request = RegisterRequest(nombre, email, pass, confirmPass, empresa)
//
//        RetrofitClient.instance.register(request).enqueue(object : Callback<GenericResponse> {
//            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
//                if (response.isSuccessful) {
//                    Toast.makeText(this@RegistroActivity, "¡Usuario creado correctamente!", Toast.LENGTH_SHORT).show()
//                    irALogin()
//                } else {
//                    val errorBody = response.errorBody()?.string()
//                    Log.e("RegistroError", "Error: $errorBody")
//                    Toast.makeText(this@RegistroActivity, "Error del servidor: Verifique los datos o si el correo ya existe", Toast.LENGTH_LONG).show()
//                }
//            }
//
//            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
//                Log.e("RegistroError", "Fallo de conexión", t)
//                Toast.makeText(this@RegistroActivity, "No se pudo conectar: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
    }

    private fun irALogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}