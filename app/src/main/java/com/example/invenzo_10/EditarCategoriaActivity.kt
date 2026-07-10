package com.example.invenzo_10

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class EditarCategoriaActivity : AppCompatActivity() {

    private var categoriaId: Int = -1
    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editarcategoria)

        // Se muestran los datos del usuario en el TopBar
        mostrarDatosUsuario()

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombreCategoria)
        etDescripcion = findViewById(R.id.etDescripcionCategoria)
        val btnGuardar = findViewById<MaterialButton>(R.id.btnGuardarCambios)
        val btnEliminar = findViewById<MaterialButton>(R.id.btnEliminarCategoria)
        
        // Cargar datos del Intent
        categoriaId = intent.getIntExtra("ID_CATEGORIA", -1)
        val nombre = intent.getStringExtra("NOMBRE_CATEGORIA")
        val descripcion = intent.getStringExtra("DESCRIPCION_CATEGORIA")

        etNombre.setText(nombre)
        etDescripcion.setText(descripcion)

        // Botones
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }

        btnGuardar.setOnClickListener {
            actualizarCategoria()
        }

        btnEliminar.setOnClickListener {
            mostrarConfirmacionEliminar()
        }
    }

    private fun mostrarDatosUsuario() {
        val txtNombre = findViewById<TextView>(R.id.txtUserNameHeader)
        val txtRoleCompany = findViewById<TextView>(R.id.txtUserRoleCompanyHeader)
        
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        val rol = prefs.getString("user_role", "Administrador")
        val empresa = prefs.getString("user_company", "Empresa")

        txtNombre?.text = nombre
        txtRoleCompany?.text = "$rol • $empresa"
    }

    private fun actualizarCategoria() {
        val nombre = etNombre.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val request = CategoriaRequest(nombre, descripcion)
                val response = RetrofitClient.instance.actualizarCategoria(
                    "Bearer $token", categoriaId, request
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@EditarCategoriaActivity, "Categoría actualizada", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditarCategoriaActivity, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditarCategoriaActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarConfirmacionEliminar() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Categoría")
            .setMessage("¿Estás seguro de que deseas eliminar esta categoría? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarCategoria()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarCategoria() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.eliminarCategoria("Bearer $token", categoriaId)
                if (response.isSuccessful) {
                    Toast.makeText(this@EditarCategoriaActivity, "Categoría eliminada", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditarCategoriaActivity, "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditarCategoriaActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
