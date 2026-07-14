package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class HistorialInventarioActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: HistorialPagerAdapter
    private lateinit var txtEmpty: TextView
    private lateinit var etBuscar: EditText
    private lateinit var txtPageIndicator: TextView

    private var listaMovimientosCompleta = listOf<Movimiento>()
    private var listaActual = listOf<Movimiento>() // Lo que se ve actualmente (filtrado)

    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri ->
        uri?.let { exportarAExcel(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        applyEdgeToEdgeWithInsets(null)
        setContentView(R.layout.activity_historial_inventario)
        applyEdgeToEdgeWithInsets(findViewById(R.id.topBar))

        //Mostras notificaciones
        NotificacionManager.init(this)
        NotificationUtils.setupNotificationButton(this)


        txtEmpty = findViewById(R.id.txtEmpty)
        etBuscar = findViewById(R.id.etBuscarMovimiento)
        txtPageIndicator = findViewById(R.id.txtPageIndicator)
        viewPager = findViewById(R.id.vpHistorial)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        
        findViewById<View>(R.id.btnExportar).setOnClickListener {
            if (listaActual.isEmpty()) {
                Toast.makeText(this, "No hay datos para exportar", Toast.LENGTH_SHORT).show()
            } else {
                createDocumentLauncher.launch("Historial_Inventario_${System.currentTimeMillis()}.xlsx")
            }
        }

        adapter = HistorialPagerAdapter(emptyList())
        viewPager.adapter = adapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.offscreenPageLimit = 1

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                actualizarIndicadorPagina(position)
            }
        })

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrar(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        cargarMovimientos()
        mostrarDatosUsuario()
        setupBottomNavigation()
    }

    private fun exportarAExcel(uri: Uri) {
        // Capturamos la lista actual para evitar cambios durante el proceso
        val datosAExportar = listaActual.toList()
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Historial Inventario")

                // 1. Crear Cabeceras
                val headerRow = sheet.createRow(0)
                val headers = arrayOf("Producto", "Código", "Tipo", "Cantidad", "Fecha", "Usuario", "Categoría", "Notas")
                headers.forEachIndexed { index, title ->
                    headerRow.createCell(index).setCellValue(title)
                }

                // 2. Llenar Datos
                var rowIdx = 1
                var totalEntradas = 0
                var totalSalidas = 0

                for (mov in datosAExportar) {
                    val row = sheet.createRow(rowIdx++)
                    row.createCell(0).setCellValue(mov.producto.nombre)
                    row.createCell(1).setCellValue(mov.producto.codigo)
                    row.createCell(2).setCellValue(mov.tipo)
                    row.createCell(3).setCellValue(mov.cantidad.toDouble())
                    row.createCell(4).setCellValue(formatearFecha(mov.createdAt))
                    row.createCell(5).setCellValue(mov.usuario?.nombre ?: "N/A")
                    row.createCell(6).setCellValue(mov.producto.categoria?.nombre ?: "N/A")
                    row.createCell(7).setCellValue(mov.observacion ?: "—")

                    if (mov.tipo.lowercase().contains("entrada")) totalEntradas++ else totalSalidas++
                }

                // 3. Añadir Resumen
                rowIdx += 2
                val resumenRow = sheet.createRow(rowIdx++)
                resumenRow.createCell(0).setCellValue("RESUMEN")

                val totalRow = sheet.createRow(rowIdx++)
                totalRow.createCell(0).setCellValue("Total")
                totalRow.createCell(1).setCellValue(datosAExportar.size.toDouble())

                val entRow = sheet.createRow(rowIdx++)
                entRow.createCell(0).setCellValue("Entradas")
                entRow.createCell(1).setCellValue(totalEntradas.toDouble())

                val salRow = sheet.createRow(rowIdx++)
                salRow.createCell(0).setCellValue("Salidas")
                salRow.createCell(1).setCellValue(totalSalidas.toDouble())

                // 4. Escribir al Stream
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    workbook.write(outputStream)
                    outputStream.flush()
                }
                workbook.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HistorialInventarioActivity, "Exportación exitosa", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ExcelExport", "Error exportando: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HistorialInventarioActivity, "Error al generar el archivo", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun filtrar(query: String) {
        listaActual = if (query.isBlank()) {
            listaMovimientosCompleta
        } else {
            listaMovimientosCompleta.filter {
                it.producto.nombre.contains(query, ignoreCase = true) ||
                it.tipo.contains(query, ignoreCase = true) ||
                (it.observacion?.contains(query, ignoreCase = true) ?: false)
            }
        }
        adapter.actualizar(listaActual)
        actualizarEstadoVacio(listaActual.isEmpty())
        actualizarIndicadorPagina(viewPager.currentItem)
    }

    private fun cargarMovimientos() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMovimientos("Bearer $token")
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyList()
                    // Ordenamos por fecha descendente
                    listaMovimientosCompleta = body.sortedByDescending { it.createdAt ?: "" }
                    listaActual = listaMovimientosCompleta
                    adapter.actualizar(listaActual)
                    actualizarEstadoVacio(listaActual.isEmpty())
                } else {
                    Toast.makeText(this@HistorialInventarioActivity, "Error al cargar datos del servidor", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@HistorialInventarioActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarEstadoVacio(isEmpty: Boolean) {
        txtEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        viewPager.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun actualizarIndicadorPagina(position: Int) {
        val totalPaginas = adapter.itemCount
        txtPageIndicator.text = if (totalPaginas == 0) "0 / 0" else "${position + 1} / $totalPaginas"
    }

    private fun formatearFecha(fechaStr: String?): String {
        if (fechaStr.isNullOrEmpty()) return "—"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(fechaStr)
            val formatter = SimpleDateFormat("dd/MM/yyyy, hh:mm:ss a", Locale.getDefault())
            date?.let { formatter.format(it) } ?: fechaStr
        } catch (e: Exception) {
            fechaStr ?: "—"
        }
    }

    private fun mostrarDatosUsuario() {
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        findViewById<TextView>(R.id.txtUserNameHeader)?.text = prefs.getString("user_name", "Usuario")
        findViewById<TextView>(R.id.txtUserRoleCompanyHeader)?.text =
            "${prefs.getString("user_role", "Admin")} • ${prefs.getString("user_company", "Empresa")}"
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.selectedItemId = R.id.more
        bottomNav?.setOnItemSelectedListener { item ->
            val intent = when (item.itemId) {
                R.id.home -> Intent(this, ActivityInicio::class.java)
                R.id.products -> Intent(this, ProductosActivity::class.java)
                R.id.categoria -> Intent(this, CategoriaActivity::class.java)
                R.id.reports -> Intent(this, ReportesActivity::class.java)
                R.id.more -> Intent(this, MasOpcionesActivity::class.java)
                else -> null
            }
            intent?.let { startActivity(it); finish() }
            true
        }
    }
}
