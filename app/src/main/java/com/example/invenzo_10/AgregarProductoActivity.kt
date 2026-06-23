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

class AgregarProductoActivity : AppCompatActivity() {

    private lateinit var imgProducto: ImageView

    private lateinit var edtNombre: EditText
    private lateinit var edtCantidad: EditText
    private lateinit var edtPrecio: EditText
    private lateinit var spCategoria: Spinner

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

        enableEdgeToEdge()

        setContentView(R.layout.activity_agregar_producto)

        setupClickListeners()


    }
    private fun setupClickListeners() {
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener {
            startActivity(Intent(this, ProductosActivity::class.java))        }


        imgProducto = findViewById(R.id.imgProducto)

        edtNombre = findViewById(R.id.edtNombreProducto)
        edtCantidad = findViewById(R.id.edtStock)
        edtPrecio = findViewById(R.id.edtPrecio)
        spCategoria = findViewById(R.id.spCategoria)

        val categorias = listOf(
            "Electrónica",
            "Alimentos",
            "Ropa",
            "Papelería"
        )

        spCategoria.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categorias
        ).apply {
            setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )
        }

        val layoutImagen =
            findViewById<LinearLayout>(R.id.layoutSeleccionarImagen)

        val btnGuardar =
            findViewById<Button>(R.id.btnGuardarProducto)

        layoutImagen.setOnClickListener {
            mostrarDialogoImagen()
        }

        btnGuardar.setOnClickListener {

            val nombreProducto = edtNombre.text.toString().trim()

            val cantidadTexto = edtCantidad.text.toString().trim()

            val precioTexto = edtPrecio.text.toString().trim()

            val categoria = spCategoria.selectedItem.toString()

            if (nombreProducto.isEmpty()) {
                Toast.makeText(
                    this,
                    "Ingrese el nombre del producto",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (cantidadTexto.isEmpty()) {
                Toast.makeText(
                    this,
                    "Ingrese el stock",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (precioTexto.isEmpty()) {
                Toast.makeText(
                    this,
                    "Ingrese el precio",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (rutaImagenGuardada == null) {
                Toast.makeText(
                    this,
                    "Seleccione una imagen",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val cantidad = cantidadTexto.toIntOrNull() ?: 0

            val precio = precioTexto.toDoubleOrNull() ?: 0.0

            val intent = Intent()

            intent.putExtra(
                "nombre",
                nombreProducto
            )

            intent.putExtra(
                "categoria",
                categoria
            )

            intent.putExtra(
                "cantidad",
                cantidad
            )

            intent.putExtra(
                "precio",
                precio
            )

            intent.putExtra(
                "rutaImagen",
                rutaImagenGuardada
            )

            setResult(
                RESULT_OK,
                intent
            )

            Toast.makeText(
                this,
                "Producto guardado correctamente",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }


    private fun mostrarDialogoImagen() {

        val vista = layoutInflater.inflate(
            R.layout.dialog_imagen_producto,
            null
        )

        imgPreviewDialog =
            vista.findViewById(R.id.imgPreview)

        val btnSeleccionar =
            vista.findViewById<Button>(R.id.btnSeleccionar)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Seleccionar imagen")
            .setView(vista)
            .setCancelable(false)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar") { d, _ ->
                d.dismiss()
            }
            .create()

        btnSeleccionar.setOnClickListener {

            seleccionarImagen.launch("image/*")
        }

        dialog.setOnShowListener {

            val btnGuardarDialogo =
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            btnGuardarDialogo.setOnClickListener {

                if (imagenTemporalUri == null) {

                    Toast.makeText(
                        this,
                        "Seleccione una imagen",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                imgProducto.setImageURI(imagenTemporalUri)

                Toast.makeText(
                    this,
                    "Imagen guardada correctamente",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun guardarImagenLocal(uri: Uri): String? {

        return try {

            val inputStream =
                contentResolver.openInputStream(uri)

            val bitmap =
                BitmapFactory.decodeStream(inputStream)

            inputStream?.close()

            val directorio =
                File(filesDir, "productos")

            if (!directorio.exists()) {
                directorio.mkdirs()
            }

            val archivo = File(
                directorio,
                "producto_${System.currentTimeMillis()}.jpg"
            )

            val fos = FileOutputStream(archivo)

            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                90,
                fos
            )

            fos.flush()
            fos.close()

            archivo.absolutePath

        } catch (e: Exception) {

            e.printStackTrace()
            null
        }

    }
}

