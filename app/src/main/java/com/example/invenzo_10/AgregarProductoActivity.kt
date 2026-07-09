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
import java.io.File
import java.io.FileOutputStream
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.content.Context
import android.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AgregarProductoActivity : AppCompatActivity() {

    private lateinit var imgProducto: ImageView
    private lateinit var edtNombre: EditText
    private lateinit var edtPrecio: EditText
    private lateinit var spCategoria: Spinner
    private lateinit var edtCodigo: EditText
    private lateinit var edtStock: EditText
    private lateinit var edtStockMinimo: EditText

    private lateinit var edtCodigo: EditText
    private lateinit var edtStock: EditText
    private lateinit var edtStockMinimo: EditText

//    Imagen del Producto
    private var rutaImagenGuardada: String? = null
    private var imagenTemporalUri: Uri? = null

    private var imgPreviewDialog: ImageView? = null
    private val listaCategorias = mutableListOf<Categoria>()
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
        enableEdgeToEdge()

        setContentView(R.layout.activity_agregar_producto)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)


        cargarCategorias()

        btnBack.setOnClickListener {
            finish()
        }

        imgProducto = findViewById(R.id.imgProducto)
        edtNombre = findViewById(R.id.edtNombreProducto)
        edtStock = findViewById(R.id.edtStock)
        edtCodigo = findViewById(R.id.edtcodigo)
        edtStockMinimo = findViewById(R.id.edit_stockmini)
        edtPrecio = findViewById(R.id.edtPrecio)
        spCategoria = findViewById(R.id.spCategoria)

        val layoutImagen = findViewById<LinearLayout>(R.id.layoutSeleccionarImagen)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarProducto)

        layoutImagen.setOnClickListener {
            mostrarDialogoImagen()
        }

        btnGuardar.setOnClickListener {
            val nombreProducto = edtNombre.text.toString().trim()

            val stockTexto = edtStock.text.toString().trim()
            val stockMinimoTexto = edtStockMinimo.text.toString().trim()
            val codigo = edtCodigo.text.toString().trim()
            val precioTexto = edtPrecio.text.toString().trim()

            if (nombreProducto.isEmpty() || codigo.isEmpty() || stockTexto.isEmpty() || stockMinimoTexto.isEmpty() || precioTexto.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
    return@setOnClickListener
            }

            if (rutaImagenGuardada == null) {
                Toast.makeText(this, "Seleccione una imagen", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val stock = stockTexto.toIntOrNull() ?: 0

            val precio = precioTexto.toDoubleOrNull() ?: 0.0
            val stockMinimo = stockMinimoTexto.toIntOrNull() ?: 0

            if (listaCategorias.isEmpty()) {
                Toast.makeText(this, "Cargando categorías...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val categoriaSeleccionada = listaCategorias[spCategoria.selectedItemPosition]

            val producto = ProductoRequest(
                nombre = nombreProducto,
                codigo = codigo,
                categoria_id = categoriaSeleccionada.id,
                cantidad = stock,
                stock_minimo = stockMinimo,
                precio = precio,
                activo = 1
            )

            guardarProducto(producto)
        }
    }
    private fun guardarProducto(producto: ProductoRequest) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            return
        }

        val archivo = File(rutaImagenGuardada!!)
        val requestFile = archivo.asRequestBody("image/*".toMediaTypeOrNull())
        val foto = MultipartBody.Part.createFormData("foto", archivo.name, requestFile)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.agregarProducto(
                    "Bearer $token",
                    producto.nombre.toRequestBody("text/plain".toMediaTypeOrNull()),
                    producto.codigo.toRequestBody("text/plain".toMediaTypeOrNull()),
                    producto.categoria_id.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    producto.precio.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    producto.cantidad.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    producto.stock_minimo.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    foto
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@AgregarProductoActivity, "Producto agregado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Log.e("API", "Error ${response.code()}: $errorBody")
                    Toast.makeText(this@AgregarProductoActivity, "Error ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("HTTP", "Excepción", e)
                Toast.makeText(this@AgregarProductoActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cargarCategorias() {
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCategorias("Bearer $token")
                if (response.isSuccessful) {
                    listaCategorias.clear()
                    response.body()?.let {
                        listaCategorias.addAll(it)
                        val nombres = listaCategorias.map { it.nombre }
                        val adapter = ArrayAdapter(this@AgregarProductoActivity, android.R.layout.simple_spinner_item, nombres)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spCategoria.adapter = adapter
                    }
                }
            } catch (e: Exception) {
                Log.e("JSON", "Error categorías", e)
            }
        }
    }


    private fun mostrarDialogoImagen() {
        val vista = layoutInflater.inflate(R.layout.dialog_imagen_producto, null)
        imgPreviewDialog = vista.findViewById(R.id.imgPreview)
        val btnSeleccionar = vista.findViewById<Button>(R.id.btnSeleccionar)

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
                if (imagenTemporalUri == null) {
                    Toast.makeText(this, "Seleccione una imagen", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                imgProducto.setImageURI(imagenTemporalUri)
                dialog.dismiss()
            }
        }
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(getColor(R.color.primaryColor))


        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(getColor(R.color.dangerColor))

    }

    private fun guardarImagenLocal(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            val directorio = File(filesDir, "productos").apply { if (!exists()) mkdirs() }
            val archivo = File(directorio, "producto_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(archivo)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush()
            fos.close()
            archivo.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

