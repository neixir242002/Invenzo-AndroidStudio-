package com.example.invenzo_10

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.highlight.Highlight
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class ActivityInicio : AppCompatActivity() {

    private lateinit var chartStock: BarChart

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        applyEdgeToEdgeWithInsets(null)
        setContentView(R.layout.activity_inicio)
        applyEdgeToEdgeWithInsets(findViewById(R.id.topBar))

        NotificacionManager.init(this)
        NotificationUtils.setupNotificationButton(this)

        chartStock = findViewById(R.id.chartInventario)

        // Cargamos los datos
        cargarDatosDashboard()
        cargarGraficaCategorias()
        cargarGraficaMovimientos()

        mostrarDatosUsuario()

        val btnVerDetallesInventario = findViewById<TextView>(R.id.btnVerDetallesInventario)
        val btnVerDetallesMovimientos = findViewById<TextView>(R.id.btnVerDetallesMovimientos)

        val verDetallesListener = {
            val intent = Intent(this, ControlInventarioActivity::class.java)
            startActivity(intent)
        }



        // Configurar click en la tarjeta de categorías para navegar directamente
        val cardCategorias = findViewById<MaterialCardView>(R.id.cardCategorias)
        cardCategorias.setOnClickListener {
            val intent = Intent(this, CategoriaActivity::class.java)
            startActivity(intent)
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.home

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val rol = prefs.getString("user_role", "Administrador")
        if (rol == "Auxiliar") {
            bottomNav.menu.findItem(R.id.categoria)?.isVisible = false
        }

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.home) return@setOnItemSelectedListener true

            val intent = when (item.itemId) {
                R.id.products -> Intent(this, ProductosActivity::class.java)
                R.id.reports -> Intent(this, ReportesActivity::class.java)
                R.id.categoria -> Intent(this, CategoriaActivity::class.java)
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

    private fun mostrarDatosUsuario() {
        val txtNombre = findViewById<TextView>(R.id.txtUserNameHeader)
        val txtRoleCompany = findViewById<TextView>(R.id.txtUserRoleCompanyHeader)
        val txtWelcomeTitle = findViewById<TextView>(R.id.txtWelcomeTitle)

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        var rol = prefs.getString("user_role", "Administrador")
        val empresa = prefs.getString("user_company", "Empresa")

        if (rol?.contains("principal", ignoreCase = true) == true) {
            rol = "Administrador Principal"
        }

        txtNombre.text = nombre
        txtRoleCompany.text = "$rol • $empresa"
        txtWelcomeTitle.text = "Bienvenido de nuevo, $nombre"
    }

    private fun formatearPesos(valor: Double): String {
        return when {
            valor >= 1_000_000_000 -> String.format("$%.1f B", valor / 1_000_000_000)
            valor >= 1_000_000 -> String.format("$%.1f M", valor / 1_000_000)
            valor >= 1_000 -> String.format("$%.0f mil", valor / 1_000)
            else -> "$${valor.toInt()}"
        }
    }

    private fun cargarDatosDashboard() {
        val txtProductos = findViewById<TextView>(R.id.productsNumber)
        val txtStockBajo = findViewById<TextView>(R.id.stockNumber)
        val txtCategorias = findViewById<TextView>(R.id.movementNumber)
        val txtValorTotal = findViewById<TextView>(R.id.valueNumber)

        // IDs para los estados dinámicos
        val txtStatusProductos = findViewById<TextView>(R.id.productsStatus)
        val txtStatusStock = findViewById<TextView>(R.id.stockStatus)
        val txtStatusCategorias = findViewById<TextView>(R.id.categoryStatus)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.dashboardAndroid("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        txtProductos.text = data.productos_total.toString()
                        txtStatusProductos.text = "${data.productos_total} registrados"

                        // Sincronizar alertas de stock con el servidor
                        NotificacionManager.clearNotificationsByType(this@ActivityInicio, "STOCK")

                        txtStockBajo.text = data.stock_bajo.toString()
                        if (data.stock_bajo > 0) {
                            txtStatusStock.text = "${data.stock_bajo} alertas"
                            txtStatusStock.setTextColor(Color.parseColor("#DC2626")) // dangerColor
                            NotificacionManager.addNotification(this@ActivityInicio, "Alerta de Stock", "Tienes ${data.stock_bajo} productos con niveles bajos.", "STOCK")
                        } else {
                            txtStatusStock.text = "Sin alertas"
                            txtStatusStock.setTextColor(Color.parseColor("#64748B")) // textSecondary
                        }

                        txtCategorias.text = data.categorias_total.toString()
                        txtStatusCategorias.text = "${data.categorias_total} activas"

                        val valorDbl = data.valor_total.toDoubleOrNull() ?: 0.0
                        txtValorTotal.text = formatearPesos(valorDbl)

                        val entries = arrayListOf(
                            BarEntry(0f, data.stock_normal.toFloat()),
                            BarEntry(1f, data.stock_bajo.toFloat()),
                            BarEntry(2f, data.sin_stock.toFloat())
                        )
                        val dataSet = BarDataSet(entries, "Salud del Inventario")
                        dataSet.colors = listOf(
                            Color.parseColor("#4CAF50"),
                            Color.parseColor("#FFC107"),
                            Color.parseColor("#F44336")
                        )
                        dataSet.valueTextColor = Color.BLACK
                        dataSet.valueTextSize = 12f

                        val barData = BarData(dataSet)
                        barData.barWidth = 0.5f

                        chartStock.data = barData
                        chartStock.description.isEnabled = false
                        chartStock.legend.isEnabled = false
                        chartStock.setFitBars(true)
                        chartStock.animateY(1000)

                        chartStock.xAxis.apply {
                            valueFormatter = IndexAxisValueFormatter(listOf("Normal", "Bajo", "Sin Stock"))
                            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                            granularity = 1f
                            setDrawGridLines(false)
                        }
                        chartStock.axisLeft.axisMinimum = 0f
                        chartStock.axisRight.isEnabled = false

                        chartStock.invalidate()
                    }
                }
            } catch (e: Exception) {
                Log.e("DASHBOARD", "Error", e)
            }
        }
    }

    private fun cargarGraficaMovimientos() {
        val chart = findViewById<BarChart>(R.id.chartMovimientos)
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMovimientosSemanales("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { lista ->
                        val entradas = ArrayList<BarEntry>()
                        val salidas = ArrayList<BarEntry>()
                        val dias = ArrayList<String>()
                        lista.forEachIndexed { index, item ->
                            val eVal = item.entradas.toString().toFloatOrNull() ?: 0f
                            val sVal = item.salidas.toString().toFloatOrNull() ?: 0f
                            entradas.add(BarEntry(index.toFloat(), eVal))
                            salidas.add(BarEntry(index.toFloat(), sVal))
                            dias.add(item.dia)
                        }
                        val setEntradas = BarDataSet(entradas, "Entradas")
                        setEntradas.color = Color.parseColor("#1D4ED8")
                        val setSalidas = BarDataSet(salidas, "Salidas")
                        setSalidas.color = Color.parseColor("#3B82F6")
                        val data = BarData(setEntradas, setSalidas)
                        data.barWidth = 0.35f
                        chart.data = data
                        chart.xAxis.apply {
                            valueFormatter = IndexAxisValueFormatter(dias)
                            granularity = 1f
                            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                            setCenterAxisLabels(true)
                            axisMinimum = 0f
                            axisMaximum = dias.size.toFloat()
                        }
                        chart.groupBars(0f, 0.25f, 0.05f)
                        chart.axisRight.isEnabled = false
                        chart.description.isEnabled = false
                        chart.animateY(1200)
                        chart.invalidate()
                    }
                }
            } catch (e: Exception) {
                Log.e("MOVIMIENTOS", "Error", e)
            }
        }
    }

    private fun cargarGraficaCategorias() {
        val chart = findViewById<PieChart>(R.id.chartCategorias)
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCategoriasGrafica("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { lista ->
                        val entries = ArrayList<PieEntry>()
                        lista.forEach {
                            entries.add(PieEntry(it.total, it.categoria))
                        }
                        val dataSet = PieDataSet(entries, "")
                        dataSet.colors = listOf(
                            Color.parseColor("#2563EB"), Color.parseColor("#3B82F6"),
                            Color.parseColor("#60A5FA"), Color.parseColor("#93C5FD"),
                            Color.parseColor("#BFDBFE"), Color.parseColor("#1D4ED8"),
                            Color.parseColor("#1E40AF")
                        )
                        dataSet.valueTextColor = Color.WHITE
                        dataSet.valueTextSize = 12f
                        val data = PieData(dataSet)
                        chart.data = data
                        chart.description.isEnabled = false
                        chart.isDrawHoleEnabled = false
                        chart.setUsePercentValues(true)
                        chart.setDrawEntryLabels(false)
                        chart.legend.isEnabled = true
                        chart.legend.isWordWrapEnabled = true
                        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry?, h: Highlight?) {
                                if (e is PieEntry) {
                                    Toast.makeText(this@ActivityInicio, "${e.label}: ${e.value.toInt()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onNothingSelected() {}
                        })
                        chart.animateY(1200)
                        chart.invalidate()
                    }
                }
            } catch (e: Exception) {
                Log.e("CATEGORIAS", "Error", e)
            }
        }
    }
}
