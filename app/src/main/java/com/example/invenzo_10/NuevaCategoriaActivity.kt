package com.example.invenzo_10

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
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
        super.onCreate(savedInstanceState)
//
//        val guardarCategoria = findViewById<Button>(R.id.buttonCrear)
//        guardarCategoria.setOnClickListener {
//            val intent = Intent(this, CategoriaActivity::class.java)
//            startActivity(intent)
//
//            @Suppress("DEPRECATION")
//            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
//            finish()
//        }

        setContentView(R.layout.activity_nuevo_categoria)

        inputNombre = findViewById(R.id.inputNombre)
        inputDescripcion = findViewById(R.id.inputDescripcion)

        editNombre = findViewById(R.id.editNombre)
        editDescripcion = findViewById(R.id.editDescripcion)

        btnCrear = findViewById(R.id.buttonCrear)

        // ICONOS los iconos de cada categotia que se desea agregar
        val icon1 = findViewById<ImageView>(R.id.icon1)
        val icon2 = findViewById<ImageView>(R.id.icon2)

        val icons = listOf(icon1, icon2)

        icons.forEach { icon ->
            icon.setOnClickListener {
                // Reset todos
                icons.forEach {
                    it.setBackgroundResource(R.drawable.fondo_redondo)
                    it.setColorFilter(Color.GRAY)
                }

                // Seleccionado
                icon.setBackgroundResource(R.drawable.fondo_redondo)
                icon.setColorFilter(getColor(R.color.primaryColor))

                selectedIcon = icon
            }
        }

        // BOTON CREAR
        btnCrear.setOnClickListener {
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

            if (valid) {
                // AQUÍ CREAS LA CATEGORIA
            }
        }

        // BOTTOM NAVIGATION
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, ActivityInicio::class.java))
                    true
                }
                R.id.products -> {
                    startActivity(Intent(this, ProductosActivity::class.java))
                    true
                }
                R.id.reports -> {
                    startActivity(Intent(this, ReportesActivity::class.java))
                    true
                }
                R.id.more -> {
                    startActivity(Intent(this, MasOpcionesActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
