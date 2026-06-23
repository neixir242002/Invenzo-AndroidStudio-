package com.example.invenzo_10

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

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
        setupBottomNavigation()
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
        // Valor por defecto
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
        // Aquí iría la lógica de guardado
        Toast.makeText(this, "Categoría creada correctamente", Toast.LENGTH_SHORT).show()
        finish() // Regresar a la lista de categorías
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.categoria

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.categoria) return@setOnItemSelectedListener true

            val intent = when (item.itemId) {
                R.id.home -> Intent(this, ActivityInicio::class.java)
                R.id.products -> Intent(this, ProductosActivity::class.java)
                R.id.reports -> Intent(this, ReportesActivity::class.java)
                R.id.more -> Intent(this, MasOpcionesActivity::class.java)
                else -> null
            }

            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(it)
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
                finish()
            }
            true
        }
    }
}
