package com.example.invenzo_10


import android.app.Activity
import android.content.Context

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle

import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File

class EditarProductoActivity : AppCompatActivity() {

    private lateinit var imgProducto: ImageView
    private lateinit var edtNombre: EditText
    private lateinit var edtCodigo: EditText
    private lateinit var edtPrecio: EditText
    private lateinit var edtStock: EditText
    private lateinit var edtStockMinimo: EditText
    private lateinit var spCategoria: Spinner
    private lateinit var txtEstado: TextView
    private lateinit var btnEstado: Button
    private lateinit var btnGuardar: Button

    private var activo = true
    private var productId: Int = -1
    private val listaCategorias = mutableListOf<Categoria>()
    private var categoriaNombreInicial: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_editar_producto)

            imgProducto = findViewById(R.id.imgProducto)
            edtNombre = findViewById(R.id.edtNombre)
            edtCodigo = findViewById(R.id.edtCodigo)
            edtPrecio = findViewById(R.id.edtPrecio)
            edtStock = findViewById(R.id.edtStock)
            edtStockMinimo = findViewById(R.id.edtStockMinimo)
            spCategoria = findViewById(R.id.spCategoria)
            txtEstado = findViewById(R.id.txtEstado)
            btnEstado = findViewById(R.id.btnEstado)
            btnGuardar = findViewById(R.id.btnGuardar)

            findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

            productId = intent.getIntExtra("id", -1)
            val nombre = intent.getStringExtra("nombre") ?: ""
            val codigo = intent.getStringExtra("codigo") ?: ""
            categoriaNombreInicial = intent.getStringExtra("categoria")
            val precio = intent.getDoubleExtra("precio", 0.0)
            val stock = intent.getIntExtra("stock", 0)
            val stockMinimo = intent.getIntExtra("stockmini", 0)
            val rutaImagen = intent.getStringExtra("rutaImagen") ?: ""
            activo = intent.getBooleanExtra("activo", true)

            cargarCategorias()

            edtNombre.setText(nombre)
            edtCodigo.setText(codigo)
            edtPrecio.setText(precio.toString())
            edtStock.setText(stock.toString())
            edtStockMinimo.setText(stockMinimo.toString())

            if (rutaImagen.isNotEmpty()) {
                val archivo = File(rutaImagen)
                if (archivo.exists()) {
                    imgProducto.setImageBitmap(BitmapFactory.decodeFile(archivo.absolutePath))
                }
            }

            actualizarEstadoUI()

            btnEstado.setOnClickListener { toggleEstadoEnServidor() }
            btnGuardar.setOnClickListener { guardarCambiosEnServidor() }

        } catch (e: Exception) {
            Log.e("EDITAR", "Error al iniciar: ${e.message}")
            Toast.makeText(this, "Error al abrir edición", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun toggleEstadoEnServidor() {
        if (productId == -1) return
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val nuevoEstado = if (activo) 0 else 1

                val response = RetrofitClient.instance.toggleStatusProducto(
                    "Bearer $token",
                    productId,
                    EstadoProductoRequest(nuevoEstado)
                )
                if (response.isSuccessful) {
                    activo = nuevoEstado == 1
                    actualizarEstadoUI()

                    Toast.makeText(
                        this@EditarProductoActivity,
                        "Estado actualizado",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("EDITAR", "Servidor rechazó toggle: $errorMsg")
                    Toast.makeText(this@EditarProductoActivity, "El servidor rechazó el cambio", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditarProductoActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarCambiosEnServidor() {
        if (productId == -1) return

        val nombre = edtNombre.text.toString().trim()
        val codigo = edtCodigo.text.toString().trim()
        val precio = edtPrecio.text.toString().toDoubleOrNull() ?: 0.0
        val stock = edtStock.text.toString().toIntOrNull() ?: 0
        val stockMin = edtStockMinimo.text.toString().toIntOrNull() ?: 0

        if (nombre.isEmpty() || codigo.isEmpty() || listaCategorias.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val catId = listaCategorias[spCategoria.selectedItemPosition].id

        val request = EditarProductoRequest(
            nombre = nombre,
            codigo = codigo,
            categoria_id = catId,
            cantidad = stock,
            stock_minimo = stockMin,
            precio = precio
        )
        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", "") ?: ""

        lifecycleScope.launch {
            try {

                val response = RetrofitClient.instance.actualizarProducto("Bearer $token", productId, request)
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@EditarProductoActivity,
                        "Producto actualizado",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                Log.e("EDITAR", "Excepción al guardar", e)
                Toast.makeText(this@EditarProductoActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarCategorias() {
        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCategorias("Bearer $token")
                if (response.isSuccessful) {
                    listaCategorias.clear()
                    response.body()?.let { categorias ->
                        listaCategorias.addAll(categorias)
                        val nombres = listaCategorias.map { it.nombre }
                        val adapter = ArrayAdapter(this@EditarProductoActivity, android.R.layout.simple_spinner_item, nombres)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spCategoria.adapter = adapter

                        categoriaNombreInicial?.let { nom ->
                            val pos = nombres.indexOf(nom)
                            if (pos != -1) spCategoria.setSelection(pos)
                        }
                    }
                }
            } catch (e: Exception) { Log.e("EDITAR", "Error categorías", e) }
        }
    }

    private fun actualizarEstadoUI() {
        if (activo) {
            txtEstado.text = "Activo"
            txtEstado.setTextColor(Color.parseColor("#4CAF50"))
            btnEstado.text = "Desactivar producto"
            btnEstado.setBackgroundColor(ContextCompat.getColor(this, R.color.desactivar))
        } else {
            txtEstado.text = "Inactivo"
            txtEstado.setTextColor(Color.RED)
            btnEstado.text = "Activar producto"
            btnEstado.setBackgroundColor(ContextCompat.getColor(this, R.color.activar))
        }
    }
}