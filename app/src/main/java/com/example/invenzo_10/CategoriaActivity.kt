package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CategoriaActivity : AppCompatActivity() {
    
    private lateinit var adapter: CategoriaAdapter
    private var listaCompleta: List<Categoria> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        applyEdgeToEdgeWithInsets(null)
        setContentView(R.layout.activity_categoria)
        applyEdgeToEdgeWithInsets(findViewById(R.id.topBar))

        NotificacionManager.init(this)
        NotificationUtils.setupNotificationButton(this)

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
            findViewById<View>(R.id.agregarCategoria)?.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        cargarCategorias()
    }

    private fun setupRecyclerView() {
        val recycler = findViewById<RecyclerView>(R.id.rvCategorias)
        recycler?.layoutManager = LinearLayoutManager(this)
        
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val rol = prefs.getString("user_role", "Administrador")

        adapter = CategoriaAdapter(emptyList()) { categoria ->
            if (rol != "Auxiliar") {
                mostrarOpcionesCategoria(categoria)
            }
        }
        recycler?.adapter = adapter
    }

    private fun getStatus(categoria: Categoria): Boolean {
        return when (val a = categoria.activo) {
            is Number -> a.toInt() == 1
            is String -> a == "1" || a.equals("active", true) || a.equals("activo", true)
            is Boolean -> a
            else -> true
        }
    }

    private fun mostrarOpcionesCategoria(categoria: Categoria) {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_categoria_options, null)
        
        val btnEditar = view.findViewById<LinearLayout>(R.id.btnEditarCategoria)
        val btnCambiarEstado = view.findViewById<LinearLayout>(R.id.btnCambiarEstadoCategoria)
        val btnEliminar = view.findViewById<LinearLayout>(R.id.btnEliminarCategoria)
        val txtStatusAction = view.findViewById<TextView>(R.id.txtStatusAction)
        val imgStatusIcon = view.findViewById<ImageView>(R.id.imgStatusIcon)

        val isActive = getStatus(categoria)

        if (isActive) {
            txtStatusAction.text = "Desactivar Categoría"
            imgStatusIcon.setImageResource(R.drawable.icons8_alerta_24)
            imgStatusIcon.setColorFilter(Color.parseColor("#DC2626"))
        } else {
            txtStatusAction.text = "Activar Categoría"
            imgStatusIcon.setImageResource(R.drawable.ic_check)
            imgStatusIcon.setColorFilter(Color.parseColor("#059669"))
        }
        
        btnEditar.setOnClickListener {
            val intent = Intent(this, EditarCategoriaActivity::class.java)
            intent.putExtra("ID_CATEGORIA", categoria.id)
            intent.putExtra("NOMBRE_CATEGORIA", categoria.nombre)
            intent.putExtra("DESCRIPCION_CATEGORIA", categoria.descripcion)
            // Pasar el estado actual para que no se pierda al editar
            intent.putExtra("ACTIVO_CATEGORIA", if (isActive) 1 else 0)
            startActivity(intent)
            dialog.dismiss()
        }
        
        btnCambiarEstado.setOnClickListener {
            cambiarEstadoCategoria(categoria, !isActive)
            dialog.dismiss()
        }

        btnEliminar?.setOnClickListener {
            dialog.dismiss()
            AlertDialog.Builder(this)
                .setTitle("¿Eliminar Categoría?")
                .setMessage("¿Estás seguro de que deseas eliminar '${categoria.nombre}'?")
                .setPositiveButton("Eliminar") { _, _ -> eliminarCategoria(categoria.id) }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        
        dialog.setContentView(view)
        dialog.show()
    }

    private fun cambiarEstadoCategoria(categoria: Categoria, nuevoEstado: Boolean) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        val request = CategoriaRequest(
            nombre = categoria.nombre,
            descripcion = categoria.descripcion,
            activo = if (nuevoEstado) 1 else 0,
            activa = if (nuevoEstado) 1 else 0
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.actualizarCategoria("Bearer $token", categoria.id, request)
                if (response.isSuccessful) {
                    categoria.activo = if (nuevoEstado) 1 else 0
                    adapter.notifyDataSetChanged()
                    
                    val msg = if (nuevoEstado) "Activada" else "Desactivada"
                    Toast.makeText(this@CategoriaActivity, "Categoría $msg con éxito", Toast.LENGTH_SHORT).show()
                    
                    delay(500)
                    cargarCategorias()
                } else {
                    Toast.makeText(this@CategoriaActivity, "Error al actualizar estado", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("API", "Error: ${e.message}")
                Toast.makeText(this@CategoriaActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eliminarCategoria(id: Int) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.eliminarCategoria("Bearer $token", id)
                if (response.isSuccessful) {
                    Toast.makeText(this@CategoriaActivity, "Categoría eliminada", Toast.LENGTH_SHORT).show()
                    cargarCategorias()
                } else {
                    Toast.makeText(this@CategoriaActivity, "No se puede eliminar: tiene productos asociados", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CategoriaActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDatosUsuario() {
        val txtNombre = findViewById<TextView>(R.id.txtUserNameHeader)
        val txtRoleCompany = findViewById<TextView>(R.id.txtUserRoleCompanyHeader)
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        var rol = prefs.getString("user_role", "Administrador")
        val empresa = prefs.getString("user_company", "Empresa")
        if (rol?.contains("principal", ignoreCase = true) == true) rol = "Admin Principal"
        txtNombre?.text = nombre
        txtRoleCompany?.text = "$rol • $empresa"
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.agregarCategoria)?.setOnClickListener {
            startActivity(Intent(this, NuevaCategoriaActivity::class.java))
        }
        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }
    }

    private fun setupSearch() {
        findViewById<EditText>(R.id.etBuscarCategoria)?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarCategorias(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filtrarCategorias(query: String) {
        val filtered = if (query.isEmpty()) listaCompleta else listaCompleta.filter { it.nombre.contains(query, true) }
        adapter.updateData(filtered)
        actualizarContador(filtered.size)
    }

    private fun cargarCategorias() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        lifecycleScope.launch { 
            try {
                val response = RetrofitClient.instance.getCategorias("Bearer $token")
                if (response.isSuccessful) {
                    val categorias = response.body() ?: emptyList()
                    
                    val prodResp = RetrofitClient.instance.getProductos("Bearer $token")
                    if (prodResp.isSuccessful) {
                        val productos = prodResp.body() ?: emptyList()
                        listaCompleta = categorias.map { cat ->
                            cat.copy(productosCount = productos.count { it.categoria?.id == cat.id })
                        }
                    } else {
                        listaCompleta = categorias
                    }
                    
                    adapter.updateData(listaCompleta)
                    actualizarContador(listaCompleta.size)
                }
            } catch (e: Exception) {
                Log.e("API", "Error: ${e.message}")
            }
        }
    }

    private fun actualizarContador(total: Int) {
        findViewById<TextView>(R.id.txtPageIndicator)?.text = "Total: $total"
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.selectedItemId = R.id.categoria
        bottomNav?.setOnItemSelectedListener { item ->
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
