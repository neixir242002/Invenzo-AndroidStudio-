package com.example.invenzo_10

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class NuevaCategoriaActivity : AppCompatActivity() {
    private lateinit var inputNombre: TextInputLayout
    private lateinit var editNombre: TextInputEditText
    private lateinit var editDescripcion: TextInputEditText
    private lateinit var statusTipe: MaterialAutoCompleteTextView
    private lateinit var btnCrear: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nuevo_categoria)

        initViews()
        setupStatusDropdown()
        setupValidation()
        setupClickListeners()
    }

    private fun initViews() {
        // Asegúrate de que estos IDs coincidan exactamente con el XML
        inputNombre = findViewById(R.id.inputNombre)
        editNombre = findViewById(R.id.editNombre)
        editDescripcion = findViewById(R.id.editDescripcion)
        statusTipe = findViewById(R.id.status_tipe)
        btnCrear = findViewById(R.id.buttonCrear)
    }

    private fun setupStatusDropdown() {
        val items = arrayOf("Activo", "Inactivo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        statusTipe.setAdapter(adapter)
        statusTipe.setText(items[0], false) // Por defecto "Activo"
    }

    private fun setupValidation() {
        btnCrear.setOnClickListener {
            val nombre = editNombre.text.toString().trim()
            if (nombre.isEmpty()) {
                inputNombre.error = "El nombre es obligatorio"
            } else {
                inputNombre.error = null
                crearCategoria()
            }
        }
    }

    private fun setupClickListeners() {
        findViewById<android.view.View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }

    private fun crearCategoria() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        val isActive = if (statusTipe.text.toString() == "Activo") 1 else 0

        // El modelo CategoriaRequest usa 'activa' (en femenino)
        val request = CategoriaRequest(
            nombre = editNombre.text.toString().trim(),
            descripcion = editDescripcion.text.toString().trim(),
            activa = isActive
        )

        lifecycleScope.launch {
            try {
                btnCrear.isEnabled = false
                val response = RetrofitClient.instance.agregarCategoria("Bearer $token", request)

                if (response.isSuccessful) {
                    Toast.makeText(this@NuevaCategoriaActivity, "Categoría creada con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("API_ERROR", error)
                    Toast.makeText(this@NuevaCategoriaActivity, "Error al guardar en el servidor", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("API_EXCEPTION", e.toString())
                Toast.makeText(this@NuevaCategoriaActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                btnCrear.isEnabled = true
            }
        }
    }
}
