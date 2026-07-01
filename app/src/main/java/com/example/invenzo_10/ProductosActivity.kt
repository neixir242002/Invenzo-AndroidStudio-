package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_productos)

        mostrarNombre()
        
        recyclerProductos = findViewById(R.id.recyclerProductos)
        adapter = ProductoAdapter(listaProductos)
        recyclerProductos.layoutManager = LinearLayoutManager(this)
        recyclerProductos.adapter = adapter

        findViewById<FloatingActionButton>(R.id.agregarProducto).setOnClickListener {
            val intent = Intent(this, AgregarProductoActivity::class.java)
            agregarProductoLauncher.launch(intent)
        }

        configurarNavegacion()
    }

    private fun mostrarNombre() {
        val txtNombre = findViewById<TextView>(R.id.txtUserNameHeader)
        // Usamos "auth" que es donde MainActivity guarda el nombre
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        txtNombre.text = nombre
    }

    private fun configurarNavegacion() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.products
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.products) return@setOnItemSelectedListener true
            val intent = when (item.itemId) {
                R.id.home -> Intent(this, ActivityInicio::class.java)
                R.id.categoria -> Intent(this, CategoriaActivity::class.java)
                R.id.reports -> Intent(this, ReportesActivity::class.java)
                R.id.more -> Intent(this, MasOpcionesActivity::class.java)
                else -> null
            }
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(it)
                finish()
            }
            true
        }
    }

    private val agregarProductoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
            adapter.notifyItemInserted(listaProductos.size - 1)
        }
    }
}
