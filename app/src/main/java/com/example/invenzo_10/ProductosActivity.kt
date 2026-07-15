package com.example.invenzo_10

import android.content.ContentValues
import android.content.Context
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ProductosActivity : AppCompatActivity() {

    private lateinit var vpProductos: ViewPager2
    private lateinit var pagerAdapter: ProductoPagerAdapter
    private lateinit var txtPageIndicator: TextView
    private lateinit var etBuscar: EditText
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private val listaProductosAMostrar = mutableListOf<Producto>()
    private var listaCompleta = mutableListOf<Producto>()
    private var filtroActual = "TODOS"
//    método para cambiar de pestaña

//    Cambiar el color del Tab seleccionado
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyEdgeToEdgeWithInsets(null)
        setContentView(R.layout.activity_productos)
        applyEdgeToEdgeWithInsets(findViewById(R.id.topBar))

        NotificacionManager.init(this)
        NotificationUtils.setupNotificationButton(this)

        initViews()
        setupBuscador()
        setupTabs()
        setupSwipeRefresh()
        configurarNavegacion()

        cargarProductos()
        mostrarDatosUsuario()
    }

    private fun initViews() {
        etBuscar = findViewById(R.id.editTextText)
        txtPageIndicator = findViewById(R.id.txtPageIndicator)
        vpProductos = findViewById(R.id.vpProductos)
        swipeRefresh = findViewById(R.id.swipeRefresh)

        pagerAdapter = ProductoPagerAdapter(listaProductosAMostrar) { producto, _, action ->
            if (action == "SHOW_OPTIONS") {
                val rol = getSharedPreferences("auth", Context.MODE_PRIVATE).getString("user_role", "")
                if (rol == "Auxiliar") {
                    Toast.makeText(this, "No tienes permisos", Toast.LENGTH_SHORT).show()
                } else {
                    mostrarOpcionesProducto(producto)
                }
            }
        }
        vpProductos.adapter = pagerAdapter
        vpProductos.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                actualizarIndicadorPagina(position)
            }
        })

        findViewById<View>(R.id.imageViewExport)?.setOnClickListener { mostrarDialogoExportar() }
        findViewById<FloatingActionButton>(R.id.agregarProducto)?.setOnClickListener {
            startActivity(Intent(this, AgregarProductoActivity::class.java))
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.primaryColor)
        swipeRefresh.setOnRefreshListener {
            // Esto permite que el usuario actualice en "tiempo real" deslizando hacia abajo
            cargarProductos()
            mostrarDatosUsuario()
        }
    }

    private fun cargarProductos() {
        val token = getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", "") ?: ""
        lifecycleScope.launch {
            try {

                swipeRefresh.isRefreshing = true
                val response = RetrofitClient.instance.getProductos("Bearer $token")

                if (response.isSuccessful) {
                    listaCompleta.clear()
                    listaCompleta.addAll(response.body() ?: emptyList())
                    aplicarFiltros()
                }
            } catch (e: Exception) {
                Log.e("API", "Error: ${e.message}")
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun mostrarDatosUsuario() {
        val txtNombre = findViewById<TextView>(R.id.txtUserNameHeader)
        val txtRoleCompany = findViewById<TextView>(R.id.txtUserRoleCompanyHeader)
        val imgProfile = findViewById<ImageView>(R.id.profileImageHeader)

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        val rol = prefs.getString("user_role", "Administrador")
        val empresa = prefs.getString("user_company", "Empresa")
        val fotoPath = prefs.getString("user_photo", "")

        txtNombre?.text = nombre
        txtRoleCompany?.text = "${if(rol?.contains("principal") == true) "Administrador Principal" else rol} • $empresa"

        if (imgProfile != null) {
            if (!fotoPath.isNullOrEmpty()) {
                val fullUrl = RetrofitClient.obtenerUrlRealtime(fotoPath)
                Glide.with(this)
                    .load(fullUrl)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .circleCrop()
                    .into(imgProfile)
            } else {
                imgProfile.setImageResource(R.drawable.ic_user)
            }
        }
    }

    private fun aplicarFiltros() {
        val textoBusqueda = etBuscar.text.toString().lowercase()
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
        if (pagerAdapter.itemCount > 0) {
            vpProductos.setCurrentItem(0, false)
            actualizarIndicadorPagina(0)
        } else {
            actualizarIndicadorPagina(-1)
        }
    }

    private fun actualizarIndicadorPagina(position: Int) {
        val total = pagerAdapter.itemCount
        txtPageIndicator.text = if (total > 0) "${position + 1} / $total" else "0 / 0"
    }

    private fun setupBuscador() {
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { aplicarFiltros() }
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

    private fun mostrarOpcionesProducto(producto: Producto) {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_producto_options, null)

        view.findViewById<LinearLayout>(R.id.btnEditarProducto).setOnClickListener {
            val intent = Intent(this, EditarProductoActivity::class.java).apply {
                putExtra("id", producto.id)
                putExtra("nombre", producto.nombre)
                putExtra("codigo", producto.codigo)
                putExtra("categoria", producto.categoria?.nombre)
                putExtra("precio", producto.precio.toDoubleOrNull() ?: 0.0)
                putExtra("stock", producto.cantidad)
                putExtra("stockmini", producto.stockMinimo)
                putExtra("rutaImagen", producto.foto)
            }
            startActivity(intent)
            dialog.dismiss()
        }

        view.findViewById<LinearLayout>(R.id.btnEliminarProducto).setOnClickListener {
            dialog.dismiss()
            AlertDialog.Builder(this)
                .setTitle("¿Eliminar?")
                .setMessage("¿Estás seguro de eliminar '${producto.nombre}'?")
                .setPositiveButton("Eliminar") { _, _ -> eliminarProducto(producto.id) }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun eliminarProducto(id: Int) {
        val token = getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.eliminarProducto("Bearer $token", id)
                if (response.isSuccessful) {
                    Toast.makeText(this@ProductosActivity, "Eliminado", Toast.LENGTH_SHORT).show()
                    cargarProductos()
                }
            } catch (e: Exception) { Log.e("API", "Error: ${e.message}") }
        }
    }

    private fun mostrarDialogoExportar() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exportar")
            .setMessage("¿Exportar a Excel?")
            .setPositiveButton("Sí") { _, _ -> exportarAExcel() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun exportarAExcel() {
        if (listaCompleta.isEmpty()) return
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Productos")
            val headers = arrayOf("Nombre", "Código", "Categoría", "Stock", "Precio")
            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { i, h -> headerRow.createCell(i).setCellValue(h) }

            listaCompleta.forEachIndexed { i, p ->
                val row = sheet.createRow(i + 1)
                row.createCell(0).setCellValue(p.nombre)
                row.createCell(1).setCellValue(p.codigo)
                row.createCell(2).setCellValue(p.categoria?.nombre ?: "—")
                row.createCell(3).setCellValue(p.cantidad.toDouble())
                row.createCell(4).setCellValue(p.precio.toDoubleOrNull() ?: 0.0)
            }

            val fileName = "Inventario_${System.currentTimeMillis()}.xlsx"
            val outputStream: OutputStream?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                outputStream = contentResolver.openOutputStream(contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)!!)
            } else {
                outputStream = FileOutputStream(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName))
            }

            outputStream?.use { workbook.write(it); Toast.makeText(this, "Excel guardado", Toast.LENGTH_SHORT).show() }
            workbook.close()
        } catch (e: Exception) { Toast.makeText(this, "Error al exportar", Toast.LENGTH_SHORT).show() }
    }

    private fun configurarNavegacion() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.products

        bottomNav.setOnItemSelectedListener { item ->

            if (item.itemId == R.id.products) return@setOnItemSelectedListener true
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

    override fun onResume() {
        super.onResume()
        cargarProductos()
        mostrarDatosUsuario()
    }
}
