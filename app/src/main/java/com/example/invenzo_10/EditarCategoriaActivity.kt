package com.example.invenzo_10

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class EditarCategoriaActivity : AppCompatActivity() {

    private var categoriaId: Int = -1
    private var estadoActual: Int = 1 // Variable para almacenar el estado
    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Aplicar Edge-to-Edge y manejo de insets para la TopBar
        applyEdgeToEdgeWithInsets(null)
        setContentView(R.layout.activity_editarcategoria)
        applyEdgeToEdgeWithInsets(findViewById(R.id.topBar))

        mostrarDatosUsuario()

        etNombre = findViewById(R.id.etNombreCategoria)
        etDescripcion = findViewById(R.id.etDescripcionCategoria)
        val btnGuardar = findViewById<MaterialButton>(R.id.btnGuardarCambios)
        val btnEliminar = findViewById<MaterialButton>(R.id.btnEliminarCategoria)
        
        categoriaId = intent.getIntExtra("ID_CATEGORIA", -1)
        estadoActual = intent.getIntExtra("ACTIVO_CATEGORIA", 1) // Recuperar estado del intent
        val nombre = intent.getStringExtra("NOMBRE_CATEGORIA")
        val descripcion = intent.getStringExtra("DESCRIPCION_CATEGORIA")

        etNombre.setText(nombre)
        etDescripcion.setText(descripcion)

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
        var rol = prefs.getString("user_role", "Administrador")
        val empresa = prefs.getString("user_company", "Empresa")

        if (rol?.contains("principal", ignoreCase = true) == true) {
            rol = "Administrador Principal"
        }

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
                // Pasamos explícitamente el estado capturado para cumplir con el constructor de CategoriaRequest
                val request = CategoriaRequest(
                    nombre = nombre, 
                    descripcion = descripcion,
                    activo = estadoActual,
                    activa = estadoActual
                )
                val response = RetrofitClient.instance.actualizarCategoria(
                    "Bearer $token", categoriaId, request
                )

                if (response.isSuccessful) {
                    try {
                        RetrofitClient.instance.registrarAuditoria(
                            "Bearer $token",
                            AuditoriaRequest("Actualizó la categoría: $nombre", "Categorías")
                        )
                    } catch (e: Exception) {
                        Log.e("AUDIT", "Error en auditoría", e)
                    }
                    
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
        val nombreCat = etNombre.text.toString()

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.eliminarCategoria("Bearer $token", categoriaId)
                if (response.isSuccessful) {
                    try {
                        RetrofitClient.instance.registrarAuditoria(
                            "Bearer $token",
                            AuditoriaRequest("Eliminó la categoría: $nombreCat", "Categorías")
                        )
                    } catch (e: Exception) {
                        Log.e("AUDIT", "Error en auditoría", e)
                    }

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
