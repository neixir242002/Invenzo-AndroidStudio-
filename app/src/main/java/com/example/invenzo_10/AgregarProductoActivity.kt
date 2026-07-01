package com.example.invenzo_10

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream

class AgregarProductoActivity : AppCompatActivity() {

    private lateinit var imgProducto: ImageView
    private lateinit var edtNombre: EditText
    private lateinit var edtCodigo: EditText
    private lateinit var edtPrecio: EditText
    private lateinit var edtStock: EditText
    private lateinit var edtStockMinimo: EditText
    private lateinit var edtDescripcion: EditText
    private lateinit var spCategoria: Spinner

    private var rutaImagenGuardada: String? = null
    private var imagenTemporalUri: Uri? = null
    private var imgPreviewDialog: ImageView? = null

    private val seleccionarImagen =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imagenTemporalUri = it
                imgPreviewDialog?.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agregar_producto)

        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViews()
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

        val categorias = listOf("Electrónica", "Alimentos", "Ropa", "Papelería")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategoria.adapter = adapter
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.layoutSeleccionarImagen).setOnClickListener {
            mostrarDialogoImagen()
        }

        findViewById<Button>(R.id.btnGuardarProducto).setOnClickListener {
            validarYGuardar()
        }
    }

    private fun validarYGuardar() {
        val nombre = edtNombre.text.toString().trim()
        val codigo = edtCodigo.text.toString().trim()
        val precioTxt = edtPrecio.text.toString().trim()
        val stockTxt = edtStock.text.toString().trim()
        val stockMinimoTxt = edtStockMinimo.text.toString().trim()
        val descripcion = edtDescripcion.text.toString().trim()

        if (nombre.isEmpty() || codigo.isEmpty() || precioTxt.isEmpty() || stockTxt.isEmpty()) {
            Toast.makeText(this, "Por favor, complete los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        if (imagenTemporalUri == null && rutaImagenGuardada == null) {
            Toast.makeText(this, "Debe seleccionar una imagen", Toast.LENGTH_SHORT).show()
            return
        }

        if (rutaImagenGuardada == null && imagenTemporalUri != null) {
            rutaImagenGuardada = guardarImagenLocal(imagenTemporalUri!!)
        }

        val intent = Intent().apply {
            putExtra("nombre", nombre)
            putExtra("codigo", codigo)
            putExtra("categoria", spCategoria.selectedItem.toString())
            putExtra("precio", precioTxt.toDoubleOrNull() ?: 0.0)
            putExtra("stock", stockTxt.toIntOrNull() ?: 0)
            putExtra("stockmini", stockMinimoTxt.toIntOrNull() ?: 0)
            putExtra("descripcion", descripcion)
            putExtra("rutaImagen", rutaImagenGuardada)
            putExtra("activo", true)
        }

        setResult(RESULT_OK, intent)
        Toast.makeText(this, "Producto guardado correctamente", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun mostrarDialogoImagen() {
        val vista = layoutInflater.inflate(R.layout.dialog_imagen_producto, null)
        imgPreviewDialog = vista.findViewById(R.id.imgPreview)
        val btnSeleccionar = vista.findViewById<Button>(R.id.btnSeleccionar)

        imagenTemporalUri?.let { imgPreviewDialog?.setImageURI(it) }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Seleccionar Imagen")
            .setView(vista)
            .setPositiveButton("Aceptar") { _, _ ->
                imagenTemporalUri?.let {
                    imgProducto.setImageURI(it)
                    rutaImagenGuardada = guardarImagenLocal(it)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()

        btnSeleccionar.setOnClickListener {
            seleccionarImagen.launch("image/*")
        }
    }

    private fun guardarImagenLocal(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val directorio = File(filesDir, "productos")
            if (!directorio.exists()) directorio.mkdirs()

            val archivo = File(directorio, "prod_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(archivo)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.close()
            archivo.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
