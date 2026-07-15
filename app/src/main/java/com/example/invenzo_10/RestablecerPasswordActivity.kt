package com.example.invenzo_10

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.json.JSONObject

class RestablecerPasswordActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSendLink: MaterialButton
    private lateinit var cardSuccess: MaterialCardView
    private lateinit var tvSuccessMsg: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restablecer_password)

        etEmail = findViewById(R.id.etEmailReset)
        btnSendLink = findViewById(R.id.btnSendResetLink)
        cardSuccess = findViewById(R.id.cardSuccess)
        tvSuccessMsg = findViewById(R.id.tvSuccessMsg)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvBackToLogin).setOnClickListener { finish() }

        btnSendLink.setOnClickListener {
            enviarEnlaceRecuperacion()
        }
    }

    private fun enviarEnlaceRecuperacion() {
        val email = etEmail.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa tu correo electrónico", Toast.LENGTH_SHORT).show()
            return
        }

        btnSendLink.isEnabled = false
        btnSendLink.text = "Enviando..."
        cardSuccess.visibility = View.GONE // Ocultar si estaba visible

        lifecycleScope.launch {
            try {
                // Laravel espera un objeto JSON {"email": "..."}
                val params = mapOf("email" to email)
                val response = RetrofitClient.instance.sendResetLink(params)

                if (response.isSuccessful) {
                    // ÉXITO: Como en tu imagen
                    cardSuccess.visibility = View.VISIBLE
                    tvSuccessMsg.text = "Te enviamos un enlace a $email. Revisa tu bandeja de entrada y la carpeta de spam."
                    btnSendLink.visibility = View.GONE
                    Toast.makeText(this@RestablecerPasswordActivity, "Correo enviado correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    btnSendLink.isEnabled = true
                    btnSendLink.text = "Enviar enlace de recuperación"

                    val errorBody = response.errorBody()?.string() ?: ""
                    Log.e("RESET_PASS", "Error ${response.code()}: $errorBody")

                    val mensajeMostrable = try {
                        val json = JSONObject(errorBody)
                        // Intentamos obtener el mensaje de error del servidor
                        json.optString("message", json.optString("error", "El correo no está registrado"))
                    } catch (e: Exception) {
                        if (response.code() == 500) "Error 500: El servidor falló (revisa logs de Laravel)"
                        else "Error ${response.code()}: No se pudo procesar la solicitud"
                    }

                    Toast.makeText(this@RestablecerPasswordActivity, mensajeMostrable, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                btnSendLink.isEnabled = true
                btnSendLink.text = "Enviar enlace de recuperación"
                Log.e("RESET_PASS", "Excepción de red", e)
                Toast.makeText(this@RestablecerPasswordActivity, "Error de red. Asegúrate de ejecutar: adb reverse tcp:8000 tcp:8000", Toast.LENGTH_LONG).show()
            }
        }
    }
}
