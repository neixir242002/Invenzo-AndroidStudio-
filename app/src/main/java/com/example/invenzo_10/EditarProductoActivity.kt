package com.example.invenzo_10

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

class EditarProductoActivity : AppCompatActivity() {

    private lateinit var imgProducto: ImageView
    private lateinit var edtNombre: EditText
    private lateinit var edtCodigo: EditText
    private lateinit var edtPrecio: EditText
    private lateinit var edtStock: EditText
    private lateinit var edtStockMinimo: EditText
    private lateinit var edtDescripcion: EditText
    private lateinit var spCategoria: Spinner
    private lateinit var txtEstado: TextView
    private lateinit var btnEstado: Button
    private lateinit var btnGuardar: Button

    private var activo = true
    private var rutaImagenActual: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_producto)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViews()
        cargarDatos()
        setupClickListeners()
    }

    private fun setupViews() {
        imgProducto = findViewById(R.id.imgProducto)
        edtNombre = findViewById(R.id.edtNombre)
        edtCodigo = findViewById(R.id.edtCodigo)
        edtPrecio = findViewById(R.id.edtPrecio)
        edtStock = findViewById(R.id.edtStock)
        edtStockMinimo = findViewById(R.id.edtStockMinimo)
        edtDescripcion = findViewById(R.id.edtDescripcion)
        spCategoria = findViewById(R.id.spCategoria)
        txtEstado = findViewById(R.id.txtEstado)
        btnEstado = findViewById(R.id.btnEstado)
        btnGuardar = findViewById(R.id.btnGuardar)

        val categorias = listOf("Electrónica", "Alimentos", "Ropa", "Papelería")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategoria.adapter = adapter
    }

    private fun cargarDatos() {
        val nombre = intent.getStringExtra("nombre") ?: ""
        val codigo = intent.getStringExtra("codigo") ?: ""
        val categoria = intent.getStringExtra("categoria") ?: ""
        val precio = intent.getDoubleExtra("precio", 0.0)
        val stock = intent.getIntExtra("stock", 0)
        val stockMinimo = intent.getIntExtra("stockmini", 0)
        val descripcion = intent.getStringExtra("descripcion") ?: ""
        rutaImagenActual = intent.getStringExtra("rutaImagen")
        activo = intent.getBooleanExtra("activo", true)

        edtNombre.setText(nombre)
        edtCodigo.setText(codigo)
        edtPrecio.setText(precio.toString())
        edtStock.setText(stock.toString())
        edtStockMinimo.setText(stockMinimo.toString())
        edtDescripcion.setText(descripcion)

        val categorias = listOf("Electrónica", "Alimentos", "Ropa", "Papelería")
        val posicion = categorias.indexOf(categoria)
        if (posicion != -1) spCategoria.setSelection(posicion)

        rutaImagenActual?.let {
            val archivo = File(it)
            if (archivo.exists()) {
                imgProducto.setImageBitmap(BitmapFactory.decodeFile(archivo.absolutePath))
            }
        }

        actualizarUIEstado()
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnEstado.setOnClickListener {
            activo = !activo
            actualizarUIEstado()
        }

        btnGuardar.setOnClickListener {
            val intentResultado = Intent().apply {
                putExtra("posicion", intent.getIntExtra("posicion", -1))
                putExtra("nombre", edtNombre.text.toString().trim())
                putExtra("codigo", edtCodigo.text.toString().trim())
                putExtra("categoria", spCategoria.selectedItem.toString())
                putExtra("precio", edtPrecio.text.toString().toDoubleOrNull() ?: 0.0)
                putExtra("stock", edtStock.text.toString().toIntOrNull() ?: 0)
                putExtra("stockmini", edtStockMinimo.text.toString().toIntOrNull() ?: 0)
                putExtra("descripcion", edtDescripcion.text.toString().trim())
                putExtra("rutaImagen", rutaImagenActual)
                putExtra("activo", activo)
            }
            setResult(RESULT_OK, intentResultado)
            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun actualizarUIEstado() {
        if (activo) {
            txtEstado.text = "Activo"
            txtEstado.setTextColor(Color.parseColor("#4CAF50"))
            btnEstado.text = "Desactivar producto"
            btnEstado.setBackgroundColor(Color.parseColor("#F44336"))
        } else {
            txtEstado.text = "Inactivo"
            txtEstado.setTextColor(Color.RED)
            btnEstado.text = "Activar producto"
            btnEstado.setBackgroundColor(Color.parseColor("#4CAF50"))
        }
    }
}
