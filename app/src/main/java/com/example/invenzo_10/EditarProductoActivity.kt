package com.example.invenzo_10

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class EditarProductoActivity : AppCompatActivity() {

    private lateinit var edtNombre: EditText
    private lateinit var edtPrecio: EditText
    private lateinit var edtCodigo: EditText
    private lateinit var edtStock: EditText
    private lateinit var edtStockMinimo: EditText
    private lateinit var spCategoria: Spinner
    private lateinit var imgProducto: ImageView
    
    private var productoId: Int = -1
    private var listaCategorias: List<Categoria> = emptyList()
    private var nombreCategoriaActual: String? = null
    
    private var rutaImagenGuardada: String? = null
    private var imagenTemporalUri: Uri? = null
    private var imgPreviewDialog: ImageView? = null

    private val seleccionarImagen =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imagenTemporalUri = it
                imgPreviewDialog?.setImageURI(it)
                rutaImagenGuardada = guardarImagenLocal(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Habilitar Edge-to-Edge
        applyEdgeToEdgeWithInsets(null)
        setContentView(R.layout.activity_editar_producto)
        
        // 2. Aplicar insets a la TopBar
        val topBar = findViewById<View>(R.id.topBar)
        if (topBar != null) {
            applyEdgeToEdgeWithInsets(topBar)
        }

        // 3. Obtener datos con seguridad
        productoId = intent.getIntExtra("id", -1)
        val nombreProd = intent.getStringExtra("nombre") ?: ""
        val codigoProd = intent.getStringExtra("codigo") ?: ""
        nombreCategoriaActual = intent.getStringExtra("categoria")
        val rutaImagen = intent.getStringExtra("rutaImagen")
        
        // IMPORTANTE: Aseguramos que se lea como Double ya que se envía como tal
        val precioProd = try {
            intent.getDoubleExtra("precio", 0.0)
        } catch (e: Exception) {
            intent.getStringExtra("precio")?.toDoubleOrNull() ?: 0.0
        }
        
        val stockProd = intent.getIntExtra("stock", 0)
        val stockMinProd = intent.getIntExtra("stockmini", 0)

        // 4. Vincular vistas
        edtNombre = findViewById(R.id.edtNombre)
        edtPrecio = findViewById(R.id.edtPrecio)
        edtCodigo = findViewById(R.id.edtCodigo)
        edtStock = findViewById(R.id.edtStock)
        edtStockMinimo = findViewById(R.id.edtStockMinimo)
        spCategoria = findViewById(R.id.spCategoria)
        imgProducto = findViewById(R.id.imgProducto)
        val layoutImagen = findViewById<View>(R.id.layoutImagen)

        // 5. Poblar campos
        edtNombre.setText(nombreProd)
        edtCodigo.setText(codigoProd)
        edtPrecio.setText(precioProd.toString())
        edtStock.setText(stockProd.toString())
        edtStockMinimo.setText(stockMinProd.toString())

        // Cargar imagen actual
        if (!rutaImagen.isNullOrEmpty()) {
            val urlCompleta = if (rutaImagen.startsWith("http")) rutaImagen else "${RetrofitClient.BASE_URL}storage/$rutaImagen"
            Glide.with(this)
                .load(urlCompleta)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(imgProducto)
        }

        // 6. Cargar categorías
        cargarCategorias()

        // 7. Configurar listeners
        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener { 
            finish() 
        }
        
        findViewById<Button>(R.id.btnGuardar)?.setOnClickListener { 
            actualizarProducto() 
        }

        layoutImagen?.setOnClickListener {
            mostrarDialogoImagen()
        }
    }

    private fun cargarCategorias() {
        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCategorias("Bearer $token")
                if (response.isSuccessful) {
                    listaCategorias = response.body() ?: emptyList()
                    val nombres = listaCategorias.map { it.nombre }
                    val adapter = ArrayAdapter(this@EditarProductoActivity, android.R.layout.simple_spinner_item, nombres)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spCategoria.adapter = adapter

                    // Seleccionar la categoría actual
                    nombreCategoriaActual?.let { nombre ->
                        val index = nombres.indexOf(nombre)
                        if (index != -1) {
                            spCategoria.setSelection(index)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("EDIT", "Error al cargar categorías", e)
            }
        }
    }

    private fun actualizarProducto() {
        val nombre = edtNombre.text.toString().trim()
        val codigo = edtCodigo.text.toString().trim()
        val precio = edtPrecio.text.toString().toDoubleOrNull() ?: 0.0
        val stock = edtStock.text.toString().toIntOrNull() ?: 0
        val stockMin = edtStockMinimo.text.toString().toIntOrNull() ?: 0

        val indexSeleccionado = spCategoria.selectedItemPosition
        val categoriaId = if (indexSeleccionado != -1 && indexSeleccionado < listaCategorias.size) {
            listaCategorias[indexSeleccionado].id
        } else {
            1
        }

        if (nombre.isEmpty() || codigo.isEmpty()) {
            Toast.makeText(this, "Nombre y código son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = if (rutaImagenGuardada != null) {
                    // Actualización con nueva imagen (Multipart)
                    val archivo = File(rutaImagenGuardada!!)
                    val requestFile = archivo.asRequestBody("image/*".toMediaTypeOrNull())
                    val fotoPart = MultipartBody.Part.createFormData("foto", archivo.name, requestFile)

                    RetrofitClient.instance.actualizarProductoMultipart(
                        "Bearer $token",
                        productoId,
                        "PUT".toRequestBody("text/plain".toMediaTypeOrNull()),
                        nombre.toRequestBody("text/plain".toMediaTypeOrNull()),
                        codigo.toRequestBody("text/plain".toMediaTypeOrNull()),
                        categoriaId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                        precio.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                        stock.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                        stockMin.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                        fotoPart
                    )
                } else {
                    // Actualización sin cambiar imagen (JSON)
                    val request = EditarProductoRequest(nombre, codigo, categoriaId, stock, stockMin, precio)
                    RetrofitClient.instance.actualizarProducto("Bearer $token", productoId, request)
                }

                if (response.isSuccessful) {
                    registrarEnAuditoria("Editó el producto: $nombre", "Productos")
                    Toast.makeText(this@EditarProductoActivity, "Producto actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("EDIT", "Error: $error")
                    Toast.makeText(this@EditarProductoActivity, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("EDIT", "Error de red", e)
                Toast.makeText(this@EditarProductoActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoImagen() {
        val vista = layoutInflater.inflate(R.layout.dialog_imagen_producto, null)
        imgPreviewDialog = vista.findViewById(R.id.imgPreview)
        val btnSeleccionar = vista.findViewById<Button>(R.id.btnSeleccionar)

        // Cargar imagen actual en el preview del diálogo
        if (imagenTemporalUri != null) {
            imgPreviewDialog?.setImageURI(imagenTemporalUri)
        } else {
            val rutaImagen = intent.getStringExtra("rutaImagen")
            if (!rutaImagen.isNullOrEmpty()) {
                val urlCompleta = if (rutaImagen.startsWith("http")) rutaImagen else "${RetrofitClient.BASE_URL}storage/$rutaImagen"
                Glide.with(this).load(urlCompleta).into(imgPreviewDialog!!)
            }
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Seleccionar imagen")
            .setView(vista)
            .setCancelable(false)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
            .create()

        btnSeleccionar.setOnClickListener {
            seleccionarImagen.launch("image/*")
        }

        dialog.setOnShowListener {
            val btnGuardarDialogo = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnGuardarDialogo.setOnClickListener {
                if (imagenTemporalUri != null) {
                    imgProducto.setImageURI(imagenTemporalUri)
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun guardarImagenLocal(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            val directorio = File(filesDir, "productos").apply { if (!exists()) mkdirs() }
            val archivo = File(directorio, "producto_edit_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(archivo)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush()
            fos.close()
            archivo.absolutePath
        } catch (e: Exception) {
            Log.e("IMG", "Error al guardar imagen local", e)
            null
        }
    }

    private fun registrarEnAuditoria(accion: String, modulo: String) {
        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                RetrofitClient.instance.registrarAuditoria("Bearer $token", AuditoriaRequest(accion, modulo))
            } catch (e: Exception) { 
                Log.e("AUDIT", "Error: ${e.message}") 
            }
        }
    }
}
