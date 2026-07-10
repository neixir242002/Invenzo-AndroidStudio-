package com.example.invenzo_10

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.highlight.Highlight
import android.widget.Toast
import kotlinx.coroutines.launch

class ActivityInicio : AppCompatActivity() {
    private lateinit var BarChart: BarChart
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)
        BarChart = findViewById(R.id.chartInventario)

        cargarResumen()
        cargarGraficaStock()
        cargarGraficaCategorias()
        cargarGraficaMovimientos()

        // Se muestran los datos del usuario en el TopBar
        mostrarDatosUsuario()

        // Redirección a Control de Inventario
        val btnVerDetallesInventario = findViewById<TextView>(R.id.btnVerDetallesInventario)
        val btnVerDetallesMovimientos = findViewById<TextView>(R.id.btnVerDetallesMovimientos)

        val verDetallesListener = {
            val intent = Intent(this, ControlInventarioActivity::class.java)
            startActivity(intent)
        }

        btnVerDetallesInventario.setOnClickListener { verDetallesListener() }
        btnVerDetallesMovimientos.setOnClickListener { verDetallesListener() }


        // 🔹 Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.home

        // Aplicar restricciones al menú
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
        val rol = prefs.getString("user_role", "Administrador")
        val empresa = prefs.getString("user_company", "Empresa")

        txtNombre.text = nombre
        txtRoleCompany.text = "$rol • $empresa"
        txtWelcomeTitle.text = "Bienvenido de nuevo, $nombre"
    }

    private fun formatearPesos(valor: Double): String {
        return when {
            valor >= 1_000_000_000 ->
                String.format("$%.1f B", valor / 1_000_000_000)
            valor >= 1_000_000 ->
                String.format("$%.1f M", valor / 1_000)
            valor >= 1_000 ->
                String.format("$%.0f mil", valor / 1_000)
            else ->
                "$${valor.toInt()}"
        }
    }

    private fun cargarResumen() {
        val txtProductos = findViewById<TextView>(R.id.productsNumber)
        val txtStockBajo = findViewById<TextView>(R.id.stockNumber)
        val txtCategorias = findViewById<TextView>(R.id.movementNumber)
        val txtValorTotal = findViewById<TextView>(R.id.valueNumber)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.dashboardAndroid("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { dashboard ->
                        txtProductos.text = dashboard.productos_total.toString()
                        txtStockBajo.text = dashboard.stock_bajo.toString()
                        txtCategorias.text = dashboard.categorias_total.toString()
                        txtValorTotal.text = formatearPesos(dashboard.valor_total)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun cargarGraficaStock() {
        val chart = findViewById<BarChart>(R.id.chartInventario)
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.dashboardAndroid("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { datos ->
                        val entries = arrayListOf(
                            BarEntry(0f, datos.stock_normal.toFloat()),
                            BarEntry(1f, datos.stock_bajo.toFloat()),
                            BarEntry(2f, datos.sin_stock.toFloat())
                        )
                        val dataSet = BarDataSet(entries, "")
                        dataSet.color = Color.parseColor("#4CAF50")
                        dataSet.valueTextColor = Color.BLACK
                        dataSet.valueTextSize = 12f
                        val data = BarData(dataSet)
                        data.barWidth = 0.45f
                        chart.data = data
                        chart.description.isEnabled = false
                        chart.legend.isEnabled = false
                        chart.setFitBars(true)
                        chart.animateY(1200)
                        chart.axisRight.isEnabled = false
                        chart.xAxis.apply {
                            valueFormatter = IndexAxisValueFormatter(listOf("Stock", "Bajo", "Sin Stock"))
                            granularity = 1f
                            setDrawGridLines(false)
                            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                        }
                        chart.axisLeft.apply {
                            axisMinimum = 0f
                            setDrawGridLines(false)
                        }
                        chart.invalidate()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
                            entradas.add(BarEntry(index.toFloat(), item.entradas.toFloat()))
                            salidas.add(BarEntry(index.toFloat(), item.salidas.toFloat()))
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
                e.printStackTrace()
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
                        chart.legend.textSize = 12f
                        chart.legend.formSize = 14f
                        chart.legend.xEntrySpace = 10f
                        chart.legend.yEntrySpace = 6f
                        chart.legend.isWordWrapEnabled = true
                        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry?, h: Highlight?) {
                                if (e is PieEntry) {
                                    Toast.makeText(this@ActivityInicio, "Categoría: ${e.label}\nProductos: ${e.value.toInt()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onNothingSelected() {}
                        })
                        chart.animateY(1200)
                        chart.invalidate()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
