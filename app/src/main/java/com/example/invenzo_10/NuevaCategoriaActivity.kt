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
    private lateinit var inputDescripcion: TextInputLayout
    private lateinit var editNombre: TextInputEditText
    private lateinit var editDescripcion: TextInputEditText
    private lateinit var btnCrear: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nuevo_categoria)

        initViews()
        setupValidation()
        setupClickListeners()
        setupStatusDropdown()
    }

    private fun initViews() {
        inputNombre = findViewById(R.id.inputNombre)
        inputDescripcion = findViewById(R.id.inputDescripcion)
        editNombre = findViewById(R.id.editNombre)
        editDescripcion = findViewById(R.id.editDescripcion)
        btnCrear = findViewById(R.id.buttonCrear)
    }

    private fun setupClickListeners() {
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupStatusDropdown() {
        val statusTipe = findViewById<MaterialAutoCompleteTextView>(R.id.status_tipe)
        val items = arrayOf("Activo", "Inactivo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        statusTipe.setAdapter(adapter)
        statusTipe.setText(items[0], false)
    }

    private fun setupValidation() {
        btnCrear.setOnClickListener {
            if (validateFields()) {
                crearCategoria()
            }
        }
    }

    private fun validateFields(): Boolean {
        var valid = true
        val nombre = editNombre.text.toString().trim()
        val descripcion = editDescripcion.text.toString().trim()

        if (nombre.isEmpty()) {
            inputNombre.error = "El nombre es obligatorio"
            valid = false
        } else {
            inputNombre.error = null
        }

        if (descripcion.isEmpty()) {
            inputDescripcion.error = "La descripción es obligatoria"
            valid = false
        } else if (descripcion.length < 5) {
            inputDescripcion.error = "Descripción muy corta"
            valid = false
        } else {
            inputDescripcion.error = null
        }
        return valid
    }

    private fun crearCategoria() {

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        val request = CategoriaRequest(
            nombre = editNombre.text.toString(),
            descripcion = editDescripcion.text.toString()
        )

        lifecycleScope.launch {

            try {

                val response = RetrofitClient.instance.agregarCategoria(
                    "Bearer $token",
                    request
                )

                if (response.isSuccessful) {

                    Toast.makeText(
                        this@NuevaCategoriaActivity,
                        "Categoría creada",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()

                } else {

                    Log.e("CATEGORIA", response.errorBody()?.string() ?: "")

                    Toast.makeText(
                        this@NuevaCategoriaActivity,
                        "Error al crear categoría",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            } catch (e: Exception) {

                Log.e("CATEGORIA", e.toString())

            }

        }

    }
}
