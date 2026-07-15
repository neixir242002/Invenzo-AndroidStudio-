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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class ActivityInicio : AppCompatActivity() {

    private lateinit var chartStock: BarChart
    private lateinit var swipeRefresh: SwipeRefreshLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        applyEdgeToEdgeWithInsets(null)
        setContentView(R.layout.activity_inicio)
        applyEdgeToEdgeWithInsets(findViewById(R.id.topBar))

        NotificacionManager.init(this)
        NotificationUtils.setupNotificationButton(this)

        chartStock = findViewById(R.id.chartInventario)
        swipeRefresh = findViewById(R.id.swipeRefresh)

        setupSwipeRefresh()
        
        cargarDatosDashboard()
        cargarGraficaCategorias()
        cargarGraficaMovimientos()
        mostrarDatosUsuario()

        val btnVerDetallesInventario = findViewById<TextView>(R.id.btnVerDetallesInventario)
        val btnVerDetallesMovimientos = findViewById<TextView>(R.id.btnVerDetallesMovimientos)
        val btnVerDetallesCategoris = findViewById<TextView>(R.id.btnVerDetallesCategoris)

        val verDetallesListener = View.OnClickListener {
            startActivity(Intent(this, ControlInventarioActivity::class.java))
        }

        val verDetallesListener2 = View.OnClickListener {
            startActivity(Intent(this, CategoriaActivity::class.java))
        }

        btnVerDetallesInventario?.setOnClickListener(verDetallesListener)
        btnVerDetallesMovimientos?.setOnClickListener(verDetallesListener)
        btnVerDetallesCategoris?.setOnClickListener(verDetallesListener2)

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

    override fun onResume() {
        super.onResume()
        mostrarDatosUsuario()
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.primaryColor)
        swipeRefresh.setOnRefreshListener {
            cargarDatosDashboard()
            cargarGraficaCategorias()
            cargarGraficaMovimientos()
            mostrarDatosUsuario()
            swipeRefresh.isRefreshing = false
        }
    }

    private fun mostrarDatosUsuario() {
        val txtNombre = findViewById<TextView>(R.id.txtUserNameHeader)
        val txtRoleCompany = findViewById<TextView>(R.id.txtUserRoleCompanyHeader)
        val txtWelcomeTitle = findViewById<TextView>(R.id.txtWelcomeTitle)
        val imgProfileHeader = findViewById<ImageView>(R.id.profileImageHeader)

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        var rol = prefs.getString("user_role", "Administrador")
        val empresa = prefs.getString("user_company", "Empresa")
        val fotoPath = prefs.getString("user_photo", "")

        if (rol?.contains("principal", ignoreCase = true) == true) rol = "Administrador Principal"

        txtNombre?.text = nombre
        txtRoleCompany?.text = "$rol • $empresa"
        txtWelcomeTitle?.text = "Bienvenido de nuevo, $nombre"

        if (!fotoPath.isNullOrEmpty()) {
            val fullUrl = RetrofitClient.obtenerUrlRealtime(fotoPath)
            Glide.with(this)
                .load(fullUrl)
                .signature(com.bumptech.glide.signature.ObjectKey(System.currentTimeMillis().toString()))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .circleCrop()
                .into(imgProfileHeader)
        } else {
            imgProfileHeader?.setImageResource(R.drawable.ic_user)
        }
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
        // Números grandes principales
        val txtProductos = findViewById<TextView>(R.id.productsNumber)
        val txtStockBajo = findViewById<TextView>(R.id.stockNumber)
        val txtCategorias = findViewById<TextView>(R.id.movementNumber)
        val txtValorTotal = findViewById<TextView>(R.id.valueNumber)
        
        // Textos pequeños de estado (Status)
        val txtStatusProductos = findViewById<TextView>(R.id.productsStatus)
        val txtStatusStock = findViewById<TextView>(R.id.stockStatus)
        val txtStatusCategorias = findViewById<TextView>(R.id.categoryStatus)
        val txtStatusValor = findViewById<TextView>(R.id.valueStatus)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.dashboardAndroid("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        // Actualizar números principales
                        txtProductos?.text = data.productos_total.toString()
                        txtStockBajo?.text = data.stock_bajo.toString()
                        txtCategorias?.text = data.categorias_total.toString()
                        txtValorTotal?.text = formatearPesos(data.valor_total.toDoubleOrNull() ?: 0.0)

                        // Actualizar textos de estado dinámicamente según solicitud
                        txtStatusProductos?.text = "${data.productos_total} registrados"
                        txtStatusCategorias?.text = "${data.categorias_total} registradas"
                        txtStatusStock?.text = "${data.stock_bajo} bajo"

                        if (data.stock_bajo > 0) {
                            txtStatusStock?.setTextColor(Color.parseColor("#DC2626")) // Rojo si hay stock bajo
                        } else {
                            txtStatusStock?.setTextColor(Color.parseColor("#64748B")) // Color neutro
                        }

                        // El de valor total no fue especificado pero se mantiene coherente
                        txtStatusValor?.text = "Inventario activo"

                        // Actualizar gráfica de salud
                        val entries = arrayListOf(
                            BarEntry(0f, data.stock_normal.toFloat()),
                            BarEntry(1f, data.stock_bajo.toFloat()),
                            BarEntry(2f, data.sin_stock.toFloat())
                        )
                        val dataSet = BarDataSet(entries, "Salud")
                        dataSet.colors = listOf(Color.parseColor("#4CAF50"), Color.parseColor("#FFC107"), Color.parseColor("#F44336"))
                        dataSet.valueTextSize = 12f

                        chartStock.data = BarData(dataSet).apply { barWidth = 0.5f }
                        chartStock.description.isEnabled = false
                        chartStock.legend.isEnabled = false
                        chartStock.animateY(1000)
                        chartStock.xAxis.apply {
                            valueFormatter = IndexAxisValueFormatter(listOf("Normal", "Bajo", "Sin Stock"))
                            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                        }
                        chartStock.axisLeft.axisMinimum = 0f
                        chartStock.axisRight.isEnabled = false
                        chartStock.invalidate()
                    }
                }
            } catch (e: Exception) { Log.e("DASHBOARD", "Error", e) }
        }
    }

    private fun cargarGraficaMovimientos() {
        val chart = findViewById<BarChart>(R.id.chartMovimientos) ?: return
        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMovimientosSemanales("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { lista ->
                        val entradas = ArrayList<BarEntry>()
                        val salidas = ArrayList<BarEntry>()
                        val dias = ArrayList<String>()
                        lista.forEachIndexed { i, item ->
                            entradas.add(BarEntry(i.toFloat(), item.entradas.toString().toFloatOrNull() ?: 0f))
                            salidas.add(BarEntry(i.toFloat(), item.salidas.toString().toFloatOrNull() ?: 0f))
                            dias.add(item.dia)
                        }
                        val setE = BarDataSet(entradas, "Entradas").apply { color = Color.parseColor("#1D4ED8") }
                        val setS = BarDataSet(salidas, "Salidas").apply { color = Color.parseColor("#3B82F6") }
                        chart.data = BarData(setE, setS).apply { barWidth = 0.35f }
                        chart.xAxis.apply {
                            valueFormatter = IndexAxisValueFormatter(dias)
                            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                            setCenterAxisLabels(true)
                            granularity = 1f
                            axisMinimum = 0f
                            axisMaximum = dias.size.toFloat()
                        }
                        chart.groupBars(0f, 0.25f, 0.05f)
                        chart.description.isEnabled = false
                        chart.axisRight.isEnabled = false
                        chart.animateY(1200)
                        chart.invalidate()
                    }
                }
            } catch (e: Exception) { Log.e("MOVIMIENTOS", "Error", e) }
        }
    }

    private fun cargarGraficaCategorias() {
        val chart = findViewById<PieChart>(R.id.chartCategorias) ?: return
        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCategoriasGrafica("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { lista ->
                        val entries = lista.map { PieEntry(it.total, it.categoria) }
                        val dataSet = PieDataSet(entries, "").apply {
                            colors = listOf(Color.parseColor("#2563EB"), Color.parseColor("#3B82F6"), Color.parseColor("#60A5FA"), Color.parseColor("#93C5FD"), Color.parseColor("#BFDBFE"), Color.parseColor("#1D4ED8"), Color.parseColor("#1E40AF"))
                            valueTextColor = Color.WHITE
                            valueTextSize = 12f
                        }
                        chart.data = PieData(dataSet)
                        chart.description.isEnabled = false
                        chart.isDrawHoleEnabled = false
                        chart.setUsePercentValues(true)
                        chart.setDrawEntryLabels(false)
                        chart.legend.apply { isEnabled = true; isWordWrapEnabled = true }
                        chart.animateY(1200)
                        chart.invalidate()
                    }
                }
            } catch (e: Exception) { Log.e("CATEGORIAS", "Error", e) }
        }
    }

}
