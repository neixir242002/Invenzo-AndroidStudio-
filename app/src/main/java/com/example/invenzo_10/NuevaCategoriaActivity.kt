package com.example.invenzo_10

import android.annotation.SuppressLint
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

        setContentView(R.layout.activity_nuevo_categoria)

        inputNombre = findViewById(R.id.inputNombre)
        inputDescripcion = findViewById(R.id.inputDescripcion)

        editNombre = findViewById(R.id.editNombre)
        editDescripcion = findViewById(R.id.editDescripcion)

        btnCrear = findViewById(R.id.buttonCrear)

        // ICONOS los iconos de cada categotia que se desea agregar
        val icon1 = findViewById<ImageView>(R.id.icon1)
        val icon2 = findViewById<ImageView>(R.id.icon2)
//        val icon3 = findViewById<ImageView>(R.id.icon3)
//        val icon4 = findViewById<ImageView>(R.id.icon4)

        val icons = listOf(icon1, icon2/* , icon3, icon4*/)

        icons.forEach { icon ->

            icon.setOnClickListener {

                // Reset todos
                icons.forEach {
                    it.setBackgroundResource(R.drawable.bg_icon_normal)
                    it.setColorFilter(Color.GRAY)
                }

                // Seleccionado
                icon.setBackgroundResource(R.drawable.bg_icon_selected)
                icon.setColorFilter(getColor(R.color.bluePrimary))

                selectedIcon = icon
            }
        }

        // BOTON CREAR
        btnCrear.setOnClickListener {

            var valid = true

            val nombre =
                editNombre.text.toString().trim()

            val descripcion =
                editDescripcion.text.toString().trim()

            // VALIDAR NOMBRE
            if (nombre.isEmpty()) {

                inputNombre.boxStrokeColor =
                    Color.RED

                inputNombre.error =
                    "El nombre es obligatorio"

                valid = false

            } else {

                inputNombre.boxStrokeColor =
                    Color.GRAY

                inputNombre.error = null
            }

            // VALIDAR DESCRIPCION
            if (descripcion.length < 5) {

                inputDescripcion.boxStrokeColor =
                    Color.RED

                inputDescripcion.error =
                    "Descripción muy corta"

                valid = false

            } else {

                inputDescripcion.boxStrokeColor =
                    Color.GRAY

                inputDescripcion.error = null
            }

            if (valid) {

                // AQUÍ CREAS LA CATEGORIA
            }
        }

        // BOTTOM NAVIGATION
        val bottomNav =
            findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.selectedItemId = R.id.more

        bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.home -> {
                    true
                }

                R.id.products -> {
                    true
                }

                R.id.more -> {
                    true
                }

                R.id.reports -> {
                    true
                }

                R.id.more -> {
                    true
                }

                else -> false
            }
        }
    }
}