package com.example.invenzo_10

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class NuevoUsuarioActivity : AppCompatActivity() {

    private lateinit var etNombre: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
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

        // Configurar Spinner de Roles
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
        val rol = spinnerRol.text.toString().trim()

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || rol.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        val request = UserCreateRequest(nombre, email, password, rol)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.crearUsuario("Bearer $token", request)
                if (response.isSuccessful) {
                    Toast.makeText(this@NuevoUsuarioActivity, "Usuario creado con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@NuevoUsuarioActivity, "Error al crear usuario", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@NuevoUsuarioActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
