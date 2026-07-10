package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class CategoriaActivity : AppCompatActivity() {
    
    private lateinit var adapter: CategoriaAdapter
    private var listaCompleta: List<Categoria> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_categoria)

        setupRecyclerView()
        mostrarDatosUsuario()
        setupBottomNavigation()
        setupClickListeners()
        setupSearch()
        aplicarRestricciones()
    }

    private fun aplicarRestricciones() {
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val rol = prefs.getString("user_role", "Administrador")

        if (rol == "Auxiliar") {
            // Ocultar botón de agregar categoría para Auxiliar
            findViewById<View>(R.id.agregarCategoria)?.visibility = View.GONE
            // También deshabilitar edición en el adapter si es necesario, 
            // pero por ahora ocultamos el punto de entrada principal.
        }
    }

    override fun onResume() {
        super.onResume()
        cargarCategorias()
    }

    private fun setupRecyclerView() {
        val recycler = findViewById<RecyclerView>(R.id.rvCategorias)
        recycler.layoutManager = LinearLayoutManager(this)
        
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val rol = prefs.getString("user_role", "Administrador")

        adapter = CategoriaAdapter(emptyList()) { categoria ->
            if (rol != "Auxiliar") {
                mostrarDialogoEditar(categoria)
            }
        }
        recycler.adapter = adapter
    }

    private fun mostrarDialogoEditar(categoria: Categoria) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Categoría")
        builder.setMessage("¿Deseas editar la categoría \"${categoria.nombre}\"?")
        
        builder.setPositiveButton("Sí") { _, _ ->
            val intent = Intent(this, EditarCategoriaActivity::class.java)
            intent.putExtra("ID_CATEGORIA", categoria.id)
            intent.putExtra("NOMBRE_CATEGORIA", categoria.nombre)
            intent.putExtra("DESCRIPCION_CATEGORIA", categoria.descripcion)
            startActivity(intent)
        }
        
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun mostrarDatosUsuario() {
        val txtNombre = findViewById<TextView>(R.id.txtUserNameHeader)
        val txtRoleCompany = findViewById<TextView>(R.id.txtUserRoleCompanyHeader)
        
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        val rol = prefs.getString("user_role", "Administrador")
        val empresa = prefs.getString("user_company", "Empresa")

        txtNombre?.text = nombre
        txtRoleCompany?.text = "$rol • $empresa"
    }

    private fun setupClickListeners() {
        findViewById<android.view.View>(R.id.agregarCategoria).setOnClickListener {
            startActivity(Intent(this, NuevaCategoriaActivity::class.java))
        }
        
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupSearch() {
        val etBuscar = findViewById<EditText>(R.id.etBuscarCategoria)
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarCategorias(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filtrarCategorias(query: String) {
        val filteredList = if (query.isEmpty()) {
            listaCompleta
        } else {
            listaCompleta.filter { it.nombre.contains(query, ignoreCase = true) }
        }
        adapter.updateData(filteredList)
        actualizarContador(filteredList.size)
    }

    private fun cargarCategorias() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        lifecycleScope.launch { 
            try {
                val responseCategorias = RetrofitClient.instance.getCategorias("Bearer $token")
                val responseProductos = RetrofitClient.instance.getProductos("Bearer $token")
                if (responseCategorias.isSuccessful && responseProductos.isSuccessful) {
                    val categorias = responseCategorias.body() ?: emptyList()
                    val productos = responseProductos.body() ?: emptyList()
                    listaCompleta = categorias.map { categoria ->
                        val cantidad = productos.count { it.categoria?.id == categoria.id }
                        categoria.copy(productosCount = cantidad) 
                    }
                    adapter.updateData(listaCompleta)
                    actualizarContador(listaCompleta.size)
                }
            } catch (e: Exception) {
                e.printStackTrace() 
            }
        }
    }

    private fun actualizarContador(total: Int) {
        findViewById<TextView>(R.id.txtPageIndicator)?.text = "Total: $total"
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.categoria

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.categoria) return@setOnItemSelectedListener true

            val intent = when (item.itemId) {
                R.id.home -> Intent(this, ActivityInicio::class.java)
                R.id.products -> Intent(this, ProductosActivity::class.java)
                R.id.reports -> Intent(this, ReportesActivity::class.java)
                R.id.more -> Intent(this, MasOpcionesActivity::class.java)
                else -> null
            }

            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(it)
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
                finish()
            }
            true
        }
    }
}
