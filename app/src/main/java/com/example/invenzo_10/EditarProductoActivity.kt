package com.example.invenzo_10

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        //--------------------------------------------------
        // Categorías
        //--------------------------------------------------

        val categorias = listOf(
            "Electrónica",
            "Alimentos",
            "Ropa",
            "Papelería"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categorias
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spCategoria.adapter = adapter

        //--------------------------------------------------
        // Recibir datos
        //--------------------------------------------------

        val nombre = intent.getStringExtra("nombre") ?: ""
        val codigo = intent.getStringExtra("codigo") ?: ""
        val categoria = intent.getStringExtra("categoria") ?: ""
        val precio = intent.getDoubleExtra("precio",0.0)
        val stock = intent.getIntExtra("stock",0)
        val stockMinimo = intent.getIntExtra("stockmini",0)
        val rutaImagen = intent.getStringExtra("rutaImagen") ?: ""

        activo = intent.getBooleanExtra("activo",true)

        //--------------------------------------------------
        // Mostrar datos
        //--------------------------------------------------

        edtNombre.setText(nombre)
        edtCodigo.setText(codigo)
        edtPrecio.setText(precio.toString())
        edtStock.setText(stock.toString())
        edtStockMinimo.setText(stockMinimo.toString())

        val posicion = categorias.indexOf(categoria)

        if (posicion != -1) {
            spCategoria.setSelection(posicion)
        }

        if (rutaImagen.isNotEmpty()) {

            val archivo = File(rutaImagen)

            if (archivo.exists()) {

                imgProducto.setImageBitmap(
                    BitmapFactory.decodeFile(
                        archivo.absolutePath
                    )
                )
            }
        }

        actualizarEstado()

        //--------------------------------------------------
        // Activar / Desactivar
        //--------------------------------------------------

        btnEstado.setOnClickListener {

            activo = !activo

            actualizarEstado()
        }

        //--------------------------------------------------
        // Guardar
        //--------------------------------------------------

        btnGuardar.setOnClickListener {

            val resultado = Intent()

            resultado.putExtra(
                "posicion",
                intent.getIntExtra("posicion",-1)
            )

            resultado.putExtra(
                "nombre",
                edtNombre.text.toString()
            )

            resultado.putExtra(
                "codigo",
                edtCodigo.text.toString()
            )

            resultado.putExtra(
                "categoria",
                spCategoria.selectedItem.toString()
            )

            resultado.putExtra(
                "precio",
                edtPrecio.text.toString().toDoubleOrNull() ?: 0.0
            )

            resultado.putExtra(
                "stock",
                edtStock.text.toString().toIntOrNull() ?: 0
            )

            resultado.putExtra(
                "stockmini",
                edtStockMinimo.text.toString().toIntOrNull() ?: 0
            )

            resultado.putExtra(
                "rutaImagen",
                rutaImagen
            )

            resultado.putExtra(
                "activo",
                activo
            )

            setResult(
                RESULT_OK,
                resultado
            )

            finish()
        }

    }

    //------------------------------------------------------

    private fun actualizarEstado() {

        if (activo) {

            txtEstado.text = "Activo"

            txtEstado.setTextColor(
                Color.parseColor("#4CAF50")
            )

            btnEstado.text = "Desactivar producto"

        } else {

            txtEstado.text = "Inactivo"

            txtEstado.setTextColor(
                Color.RED
            )

            btnEstado.text = "Activar producto"

        }

    }

}