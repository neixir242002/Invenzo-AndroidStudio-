package com.example.invenzo_10

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProductosActivity : AppCompatActivity() {

    private lateinit var recyclerProductos: RecyclerView

    private lateinit var adapter: ProductoAdapter

    private val listaProductos = mutableListOf<Producto>()

    private val agregarProductoLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {

                val data = result.data ?: return@registerForActivityResult

                val producto = Producto(
                    nombre = data.getStringExtra("nombre") ?: "",
                    categoria = data.getStringExtra("categoria") ?: "",
                    cantidad = data.getIntExtra("cantidad", 0),
                    precio = data.getDoubleExtra("precio", 0.0),
                    rutaImagen = data.getStringExtra("rutaImagen") ?: ""
                )

                listaProductos.add(producto)

                adapter.notifyItemInserted(
                    listaProductos.size - 1
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.productos)

        recyclerProductos =
            findViewById(R.id.recyclerProductos)

        adapter =
            ProductoAdapter(listaProductos)

        recyclerProductos.layoutManager =
            LinearLayoutManager(this)

        recyclerProductos.adapter = adapter

        val agregarProducto =
            findViewById<FloatingActionButton>(
                R.id.agregarProducto
            )

        agregarProducto.setOnClickListener {

            val intent =
                Intent(
                    this,
                    AgregarProductoActivity::class.java
                )

            agregarProductoLauncher.launch(intent)

            @Suppress("DEPRECATION")
            overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
            )
        }

        val bottomNav =
            findViewById<BottomNavigationView>(
                R.id.bottomNav
            )

        bottomNav.selectedItemId =
            R.id.products

        bottomNav.setOnItemSelectedListener { item ->

            if (item.itemId == R.id.products)
                return@setOnItemSelectedListener true

            val intent = when (item.itemId) {

                R.id.home ->
                    Intent(
                        this,
                        ActivityInicio::class.java
                    )

                R.id.categoria ->
                    Intent(
                        this,
                        CategoriaActivity::class.java
                    )

                R.id.reports ->
                    Intent(
                        this,
                        ReportesActivity::class.java
                    )

                R.id.more ->
                    Intent(
                        this,
                        MasOpcionesActivity::class.java
                    )

                else -> null
            }

            intent?.let {

                startActivity(it)

                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)

                finish()
            }

            true
        }
    }
}