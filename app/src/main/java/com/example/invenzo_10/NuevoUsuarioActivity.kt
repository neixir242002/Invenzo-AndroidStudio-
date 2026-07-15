package com.example.invenzo_10

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class NuevoUsuarioActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var spinnerRol: AutoCompleteTextView
    private lateinit var btnGuardar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nuevousuario)

        etNombre = findViewById(R.id.etNombreUsuario)
        etEmail = findViewById(R.id.etEmailUsuario)
        etPassword = findViewById(R.id.etPasswordUsuario)
        spinnerRol = findViewById(R.id.Edit_rol)
        btnGuardar = findViewById(R.id.btnGuardarUsuario)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val roles = arrayOf("Administrador", "Auxiliar")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        spinnerRol.setAdapter(adapter)

        btnGuardar.setOnClickListener {
            guardarUsuario()
        }
    }

    private fun guardarUsuario() {
        val nombre = etNombre.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val rolVisual = spinnerRol.text.toString().trim()

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || rolVisual.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val rolApi = when (rolVisual) {
            "Administrador" -> "administrador"
            "Auxiliar" -> "auxiliar"
            else -> "auxiliar"
        }

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        val request = UserCreateRequest(nombre, email, password, rolApi)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.crearUsuario("Bearer $token", request)
                if (response.isSuccessful) {
                    registrarEnAuditoria("Creó al usuario: $nombre con el rol: $rolVisual", "Usuarios")
                    Toast.makeText(this@NuevoUsuarioActivity, "Usuario creado con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val json = JSONObject(errorBody ?: "{}")
                    val mensaje = json.optString("message", "Error al crear usuario")
                    Toast.makeText(this@NuevoUsuarioActivity, mensaje, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@NuevoUsuarioActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registrarEnAuditoria(accion: String, modulo: String) {
        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                RetrofitClient.instance.registrarAuditoria("Bearer $token", AuditoriaRequest(accion, modulo))
            } catch (e: Exception) {
                Log.e("AUDIT", "Error al registrar auditoría", e)
            }
        }
    }
}
