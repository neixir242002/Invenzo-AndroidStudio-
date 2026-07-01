package com.example.invenzo_10

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProductosActivity : AppCompatActivity() {

    private lateinit var tabTodos: TextView
    private lateinit var tabStockBajo: TextView
    private lateinit var tabSinStock: TextView

    private lateinit var editarProductoLauncher:
            ActivityResultLauncher<Intent>

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
                    stock = data.getIntExtra("stock", 0),
                    precio = data.getDoubleExtra("precio", 0.0),
                    codigo = data.getStringExtra("codigo") ?: "",
                    stockmini = data.getIntExtra("stockmini",0),
                    activo = data.getBooleanExtra("acciones",true),
                    rutaImagen = data.getStringExtra("rutaImagen") ?: ""
                )

                listaProductos.add(producto)
                adapter.notifyItemInserted(listaProductos.size - 1)

            }
        }

//    método para cambiar de pestaña

    private fun mostrarProductos(tipo: Int) {

        val listaMostrar = when (tipo) {

            0 -> {
                listaProductos
            }

            1 -> {
                listaProductos.filter {
                    it.stock > 0 &&
                            it.stock <= it.stockmini
                }.toMutableList()
            }

            else -> {
                listaProductos.filter {
                    it.stock == 0
                }.toMutableList()
            }
        }

        recyclerProductos.adapter = ProductoAdapter(
            listaMostrar
        ) { producto, posicion ->

            val intent = Intent(
                this,
                EditarProductoActivity::class.java
            )

            intent.putExtra("nombre", producto.nombre)
            intent.putExtra("codigo", producto.codigo)
            intent.putExtra("categoria", producto.categoria)
            intent.putExtra("stock", producto.stock)
            intent.putExtra("stockmini", producto.stockmini)
            intent.putExtra("precio", producto.precio)
            intent.putExtra("rutaImagen", producto.rutaImagen)
            intent.putExtra("activo", producto.activo)

            // Posición real del producto
            intent.putExtra(
                "posicion",
                listaProductos.indexOf(producto)
            )

            editarProductoLauncher.launch(intent)
        }
    }

//    Cambiar el color del Tab seleccionado
    private fun seleccionarTab(tab:Int){

        tabTodos.setTextColor(Color.GRAY)
        tabStockBajo.setTextColor(Color.GRAY)
        tabSinStock.setTextColor(Color.GRAY)

        tabTodos.setTypeface(null,Typeface.NORMAL)
        tabStockBajo.setTypeface(null,Typeface.NORMAL)
        tabSinStock.setTypeface(null,Typeface.NORMAL)

        when(tab){

            0->{

                tabTodos.setTextColor(
                    getColor(R.color.primaryColor)
                )

                tabTodos.setTypeface(
                    null,
                    Typeface.BOLD
                )

            }

            1->{

                tabStockBajo.setTextColor(
                    getColor(R.color.primaryColor)
                )

                tabStockBajo.setTypeface(
                    null,
                    Typeface.BOLD
                )

            }

            2->{

                tabSinStock.setTextColor(
                    getColor(R.color.primaryColor)
                )

                tabSinStock.setTypeface(
                    null,
                    Typeface.BOLD
                )

            }

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Cambiado de R.layout.productos a R.layout.activity_productos
        setContentView(R.layout.activity_productos)

        recyclerProductos = findViewById(R.id.recyclerProductos)
        adapter = ProductoAdapter(listaProductos) { producto, posicion ->

            val intent = Intent(
                this,
                EditarProductoActivity::class.java
            )

            intent.putExtra("nombre", producto.nombre)
            intent.putExtra("codigo", producto.codigo)
            intent.putExtra("categoria", producto.categoria)
            intent.putExtra("stock", producto.stock)
            intent.putExtra("stockmini", producto.stockmini)
            intent.putExtra("precio", producto.precio)
            intent.putExtra("rutaImagen", producto.rutaImagen)
            intent.putExtra("activo", producto.activo)

            // MUY IMPORTANTE
            intent.putExtra("posicion", posicion)

            editarProductoLauncher.launch(intent)

        }

        recyclerProductos.layoutManager = LinearLayoutManager(this)
        recyclerProductos.adapter = adapter
        tabTodos = findViewById(R.id.tabTodos)
        tabStockBajo = findViewById(R.id.tabStockBajo)
        tabSinStock = findViewById(R.id.tabSinStock)



        val agregarProducto = findViewById<FloatingActionButton>(R.id.agregarProducto)
        agregarProducto.setOnClickListener {
            val intent = Intent(this, AgregarProductoActivity::class.java)
            agregarProductoLauncher.launch(intent)
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.products

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

//        Eventos de los tabs
        tabTodos.setOnClickListener {

            seleccionarTab(0)

            mostrarProductos(0)

        }

        tabStockBajo.setOnClickListener {

            seleccionarTab(1)

            mostrarProductos(1)

        }

        tabSinStock.setOnClickListener {

            seleccionarTab(2)

            mostrarProductos(2)

        }

        editarProductoLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ){ result ->

                if(result.resultCode == RESULT_OK){

                    val data = result.data ?: return@registerForActivityResult

                    val posicion =
                        data.getIntExtra("posicion",-1)

                    if(posicion != -1){

                        listaProductos[posicion] = Producto(

                            nombre = data.getStringExtra("nombre")!!,

                            codigo = data.getStringExtra("codigo")!!,

                            categoria = data.getStringExtra("categoria")!!,

                            stock = data.getIntExtra("stock",0),

                            stockmini = data.getIntExtra("stockmini",0),

                            activo = data.getBooleanExtra("activo",true),

                            precio = data.getDoubleExtra("precio",0.0),

                            rutaImagen = data.getStringExtra("rutaImagen")!!
                        )

                        adapter.notifyItemChanged(posicion)
                    }
                }

            }
    }
}
