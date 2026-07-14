package com.example.invenzo_10

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
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
        
        applyEdgeToEdgeWithInsets(null)
        setContentView(R.layout.activity_nuevo_categoria)
        applyEdgeToEdgeWithInsets(findViewById(R.id.topBar))

        initViews()
        setupStatusDropdown()
        setupValidation()
        setupClickListeners()
    }

    private fun initViews() {
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
        statusTipe.setText(items[0], false)
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
        val nombreCat = editNombre.text.toString().trim()

        lifecycleScope.launch {
            try {
                btnCrear.isEnabled = false
                val request = CategoriaRequest(
                    nombre = nombreCat,
                    descripcion = editDescripcion.text.toString().trim(),
                    activa = isActive
                )

                val response = RetrofitClient.instance.agregarCategoria("Bearer $token", request)

                if (response.isSuccessful) {
                    // ESPERAR a que la auditoría se registre antes de hacer finish()
                    try {
                        RetrofitClient.instance.registrarAuditoria(
                            "Bearer $token", 
                            AuditoriaRequest("Creó la categoría: $nombreCat", "Categorías")
                        )
                    } catch (e: Exception) {
                        Log.e("AUDIT", "Error silencioso en auditoría", e)
                    }
                    
                    Toast.makeText(this@NuevaCategoriaActivity, "Categoría creada con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@NuevaCategoriaActivity, "Error al guardar en el servidor", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@NuevaCategoriaActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                btnCrear.isEnabled = true
            }
        }
    }
}
