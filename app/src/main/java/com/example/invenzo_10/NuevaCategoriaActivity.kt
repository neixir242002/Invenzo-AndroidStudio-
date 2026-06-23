package com.example.invenzo_10

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class NuevaCategoriaActivity : AppCompatActivity() {
    private lateinit var inputNombre: TextInputLayout
    private lateinit var inputDescripcion: TextInputLayout
    private lateinit var editNombre: TextInputEditText
    private lateinit var editDescripcion: TextInputEditText
    private lateinit var btnCrear: Button

    private var selectedIcon: ImageView? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_categoria)

        initViews()
        setupBottomNavigation()
        setupValidation()
        setupClickListeners()
    }
    private fun setupClickListeners() {
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener {
            startActivity(Intent(this, CategoriaActivity::class.java))        }
    }
    private fun initViews() {
        inputNombre = findViewById(R.id.inputNombre)
        inputDescripcion = findViewById(R.id.inputDescripcion)
        editNombre = findViewById(R.id.editNombre)
        editDescripcion = findViewById(R.id.editDescripcion)
        btnCrear = findViewById(R.id.buttonCrear)
    }

    private fun setupValidation() {
        // Lógica del BOTON CREAR
        btnCrear.setOnClickListener {
            if (validateFields()) {
                // AQUÍ CREAS LA CATEGORIA
                crearCategoria()
            }
        }
    }

    private fun validateFields(): Boolean {
        var valid = true

        val nombre = editNombre.text.toString().trim()
        val descripcion = editDescripcion.text.toString().trim()

        // VALIDAR NOMBRE
        if (nombre.isEmpty()) {
            inputNombre.error = "El nombre es obligatorio"
            valid = false
        } else {
            inputNombre.error = null
        }

        // VALIDAR DESCRIPCION
        if (descripcion.length < 5) {
            inputDescripcion.error = "Descripción muy corta"
            valid = false
        } else {
            inputDescripcion.error = null
        }

        return valid
    }

    private fun crearCategoria() {
        // Aquí puedes poner la lógica para guardar en la base de datos o API
        // Por ejemplo: Toast.makeText(this, "Categoría creada", Toast.LENGTH_SHORT).show()
    }

    // setupBottomNavigation
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
