package com.example.invenzo_10

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class ProductosActivity : AppCompatActivity() {

    private lateinit var vpProductos: ViewPager2
    private lateinit var pagerAdapter: ProductoPagerAdapter
    private lateinit var txtPageIndicator: TextView
    private lateinit var etBuscar: EditText
    private lateinit var btnExportar: View
    
    private val listaProductosAMostrar = mutableListOf<Producto>()
    private var listaCompleta = mutableListOf<Producto>()
    
    private var filtroActual = "TODOS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        applyEdgeToEdgeWithInsets(null)
        setContentView(R.layout.activity_productos)
        
        val topBar = findViewById<View>(R.id.topBar)
        applyEdgeToEdgeWithInsets(topBar)

        NotificacionManager.init(this)
        NotificationUtils.setupNotificationButton(this)

        mostrarDatosUsuario()
        
        etBuscar = findViewById(R.id.editTextText)
        btnExportar = findViewById(R.id.imageViewExport)
        txtPageIndicator = findViewById(R.id.txtPageIndicator)
        vpProductos = findViewById(R.id.vpProductos)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val rol = prefs.getString("user_role", "Administrador")

        pagerAdapter = ProductoPagerAdapter(listaProductosAMostrar) { producto, _, action ->
            if (action == "SHOW_OPTIONS") {
                if (rol == "Auxiliar") {
                    Toast.makeText(this, "No tienes permisos para gestionar productos", Toast.LENGTH_SHORT).show()
                } else {
                    mostrarOpcionesProducto(producto)
                }
            }
        }
        vpProductos.adapter = pagerAdapter

        vpProductos.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                actualizarIndicadorPagina(position)
            }
        })

        setupBuscador()
        setupTabs()
        cargarProductos()

        btnExportar?.setOnClickListener {
            mostrarDialogoExportar()
        }

        findViewById<FloatingActionButton>(R.id.agregarProducto)?.setOnClickListener {
            startActivity(Intent(this, AgregarProductoActivity::class.java))
        }

        configurarNavegacion()
    }

    private fun mostrarOpcionesProducto(producto: Producto) {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_producto_options, null)
        
        val btnEditar = view.findViewById<LinearLayout>(R.id.btnEditarProducto)
        val btnCambiarEstado = view.findViewById<LinearLayout>(R.id.btnCambiarEstadoProducto)
        val btnEliminar = view.findViewById<LinearLayout>(R.id.btnEliminarProducto)
        val txtStatusAction = view.findViewById<TextView>(R.id.txtStatusActionProducto)
        val imgStatusIcon = view.findViewById<ImageView>(R.id.imgStatusIconProducto)

        val isActive = producto.activo == 1

        if (isActive) {
            txtStatusAction.text = "Desactivar Producto"
            imgStatusIcon.setImageResource(R.drawable.icons8_alerta_24)
            imgStatusIcon.setColorFilter(Color.parseColor("#DC2626"))
        } else {
            txtStatusAction.text = "Activar Producto"
            imgStatusIcon.setImageResource(R.drawable.ic_check)
            imgStatusIcon.setColorFilter(Color.parseColor("#059669"))
        }
        
        btnEditar.setOnClickListener {
            abrirEditarProducto(producto)
            dialog.dismiss()
        }
        
        btnCambiarEstado.setOnClickListener {
            toggleEstado(producto)
            dialog.dismiss()
        }

        btnEliminar.setOnClickListener {
            dialog.dismiss()
            AlertDialog.Builder(this)
                .setTitle("¿Eliminar Producto?")
                .setMessage("¿Estás seguro de que deseas eliminar '${producto.nombre}'? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar") { _, _ -> eliminarProductoDefinitivo(producto) }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        
        dialog.setContentView(view)
        dialog.show()
    }

    private fun mostrarDatosUsuario() {
        val txtNombre = findViewById<TextView>(R.id.txtUserNameHeader)
        val txtRoleCompany = findViewById<TextView>(R.id.txtUserRoleCompanyHeader)
        
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        var rol = prefs.getString("user_role", "Administrador")
        val empresa = prefs.getString("user_company", "Empresa")

        if (rol?.contains("principal", ignoreCase = true) == true) {
            rol = "Administrador Principal"
        }

        txtNombre?.text = nombre
        txtRoleCompany?.text = "$rol • $empresa"
    }

    private fun abrirEditarProducto(producto: Producto) {
        try {
            val intent = Intent(this, EditarProductoActivity::class.java)
            intent.putExtra("id", producto.id)
            intent.putExtra("nombre", producto.nombre)
            intent.putExtra("codigo", producto.codigo)
            intent.putExtra("categoria", producto.categoria?.nombre)
            intent.putExtra("precio", producto.precio.toDoubleOrNull() ?: 0.0)
            intent.putExtra("stock", producto.cantidad)
            intent.putExtra("stockmini", producto.stockMinimo)
            intent.putExtra("activo", producto.activo == 1)
            intent.putExtra("rutaImagen", producto.foto)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("EDITAR", "Error al abrir actividad: ${e.message}")
            Toast.makeText(this, "Error al abrir edición", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleEstado(producto: Producto) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val nuevoEstado = if (producto.activo == 1) 0 else 1

                val response = RetrofitClient.instance.toggleStatusProducto(
                    "Bearer $token",
                    producto.id,
                    EstadoProductoRequest(nuevoEstado)
                )
                if (response.isSuccessful) {
                    Toast.makeText(this@ProductosActivity, "Producto actualizado con éxito", Toast.LENGTH_SHORT).show()
                    cargarProductos()
                } else {
                    Toast.makeText(this@ProductosActivity, "Error al actualizar estado", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProductosActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eliminarProductoDefinitivo(producto: Producto) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.eliminarProducto("Bearer $token", producto.id)
                if (response.isSuccessful) {
                    Toast.makeText(this@ProductosActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                    cargarProductos()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProductosActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarIndicadorPagina(position: Int) {
        val totalPaginas = pagerAdapter.itemCount
        if (totalPaginas > 0) {
            txtPageIndicator.text = "${position + 1} / $totalPaginas"
        } else {
            txtPageIndicator.text = "0 / 0"
        }
    }

    private fun setupBuscador() {
        etBuscar?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                aplicarFiltros()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupTabs() {
        val tabTodos = findViewById<TextView>(R.id.tabTodos)
        val tabBajo = findViewById<TextView>(R.id.tabStockBajo)
        val tabSin = findViewById<TextView>(R.id.tabSinStock)
        val tabs = listOf(tabTodos, tabBajo, tabSin)

        tabTodos?.setOnClickListener { filtroActual = "TODOS"; actualizarUITabs(tabTodos, tabs) }
        tabBajo?.setOnClickListener { filtroActual = "BAJO"; actualizarUITabs(tabBajo, tabs) }
        tabSin?.setOnClickListener { filtroActual = "SIN"; actualizarUITabs(tabSin, tabs) }
    }

    private fun actualizarUITabs(seleccionada: TextView, todas: List<TextView>) {
        todas.forEach {
            it?.setTextColor(ContextCompat.getColor(this, R.color.textSecondary))
            it?.setTypeface(null, android.graphics.Typeface.NORMAL)
            it?.background = null
        }
        seleccionada?.setTextColor(ContextCompat.getColor(this, R.color.primaryColor))
        seleccionada?.setTypeface(null, android.graphics.Typeface.BOLD)
        seleccionada?.setBackgroundResource(R.drawable.bg_user_pill)
        seleccionada?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primaryLight)

        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val textoBusqueda = etBuscar?.text.toString().lowercase()
        val filtrados = listaCompleta.filter { producto ->
            val coincideBusqueda = producto.nombre.lowercase().contains(textoBusqueda) || 
                                 producto.codigo.lowercase().contains(textoBusqueda)
            val coincidePestaña = when(filtroActual) {
                "BAJO" -> producto.cantidad > 0 && producto.cantidad <= producto.stockMinimo
                "SIN" -> producto.cantidad == 0
                else -> true
            }
            coincideBusqueda && coincidePestaña
        }
        listaProductosAMostrar.clear()
        listaProductosAMostrar.addAll(filtrados)
        pagerAdapter.actualizar(listaProductosAMostrar)
        vpProductos.post {
            if (pagerAdapter.itemCount > 0) {
                vpProductos.setCurrentItem(0, false)
                actualizarIndicadorPagina(0)
            } else {
                actualizarIndicadorPagina(-1)
            }
        }
    }

    private fun cargarProductos() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getProductos("Bearer $token")
                if (response.isSuccessful) {
                    listaCompleta.clear()
                    listaCompleta.addAll(response.body() ?: emptyList())
                    aplicarFiltros()
                }
            } catch (e: Exception) {
                Log.e("API", "Error: ${e.message}")
            }
        }
    }

    private fun mostrarDialogoExportar() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exportar")
            .setMessage("¿Desea exportar a Excel?")
            .setPositiveButton("Sí") { _, _ -> exportarAExcel() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun exportarAExcel() {
        if (listaCompleta.isEmpty()) return
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Productos")
            val headerRow = sheet.createRow(0)
            val headers = arrayOf("Nombre", "Código", "Categoría", "Stock", "Precio")
            for (i in headers.indices) { headerRow.createCell(i).setCellValue(headers[i]) }

            var rowNum = 1
            for (prod in listaCompleta) {
                val row = sheet.createRow(rowNum++)
                row.createCell(0).setCellValue(prod.nombre)
                row.createCell(1).setCellValue(prod.codigo)
                row.createCell(2).setCellValue(prod.categoria?.nombre ?: "Sin categoría")
                row.createCell(3).setCellValue(prod.cantidad.toDouble())
                row.createCell(4).setCellValue(prod.precio.toDoubleOrNull() ?: 0.0)
            }

            val fileName = "Inventario_${System.currentTimeMillis()}.xlsx"
            val outputStream: OutputStream?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = uri?.let { contentResolver.openOutputStream(it) }
            } else {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                outputStream = FileOutputStream(file)
            }

            outputStream?.use { workbook.write(it); Toast.makeText(this, "Excel guardado", Toast.LENGTH_SHORT).show() }
            workbook.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al generar Excel", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() { super.onResume(); cargarProductos() }

    private fun configurarNavegacion() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.products
        
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val rol = prefs.getString("user_role", "Administrador")
        if (rol == "Auxiliar") {
            bottomNav.menu.findItem(R.id.categoria)?.isVisible = false
        }

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
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
                finish()
            }
            true
        }
    }
}
