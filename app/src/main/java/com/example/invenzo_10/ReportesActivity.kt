package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class ReportesActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var txtPagina: TextView
    private lateinit var pagerAdapter: ReporteProductoPagerAdapter
    private var listaCompleta = listOf<Producto>()
    private var filtroActual = "Todo"
    private var queryActual = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Habilitar Edge-to-Edge
        applyEdgeToEdgeWithInsets(null)

        setContentView(R.layout.activity_reportes)

        // 2. Aplicar insets a la TopBar
        applyEdgeToEdgeWithInsets(findViewById(R.id.topBar))

        NotificacionManager.init(this)
        NotificationUtils.setupNotificationButton(this)

        viewPager = findViewById(R.id.viewPagerProductosReporte)
        txtPagina = findViewById(R.id.txtPagina)

        pagerAdapter = ReporteProductoPagerAdapter(emptyList())
        viewPager.adapter = pagerAdapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                actualizarIndicadorPagina(position)
            }
        })

        mostrarDatosUsuario()
        setupBottomNavigation()
        cargarProductosReporte()
        setupSearch()
        setupFilters()
    }

    private fun actualizarIndicadorPagina(position: Int) {
        val totalPaginas = pagerAdapter.itemCount
        if (totalPaginas > 0) {
            txtPagina.text = "${position + 1} / $totalPaginas"
        } else {
            txtPagina.text = "0 / 0"
        }
    }

    private fun mostrarDatosUsuario() {
        val txtNombre = findViewById<TextView>(R.id.txtUserNameHeader)
        val txtRoleCompany = findViewById<TextView>(R.id.txtUserRoleCompanyHeader)
        val imgProfile = findViewById<ImageView>(R.id.profileImageHeader)


        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        var rol = prefs.getString("user_role", "Administrador")
        val empresa = prefs.getString("user_company", "Empresa")
        val fotoPath = prefs.getString("user_photo", "")

        // Asegurar que se muestre "Administrador Principal" correctamente
        if (rol?.contains("principal", ignoreCase = true) == true) {
            rol = "Administrador Principal"
        }

        txtNombre?.text = nombre
        txtRoleCompany?.text = "$rol • $empresa"

        if (imgProfile != null) {
            if (!fotoPath.isNullOrEmpty()) {
                val cleanPath = if (fotoPath.startsWith("/")) fotoPath.substring(1) else fotoPath
                val fullUrl = if (fotoPath.startsWith("http")) fotoPath else "${RetrofitClient.BASE_URL}storage/$cleanPath"

                Glide.with(this)
                    .load(fullUrl)
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Forzar descarga
                    .skipMemoryCache(true) // Ignorar caché en RAM
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .circleCrop()
                    .into(imgProfile)
            } else {
                imgProfile.setImageResource(R.drawable.ic_user)
            }
        }
    }

    private fun setupSearch() {
        val etBuscar = findViewById<EditText>(R.id.etBuscarReporte)
        etBuscar?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                queryActual = s.toString()
                aplicarFiltros()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilters() {
        val btnTodo = findViewById<TextView>(R.id.btnFiltroTodo)
        val btnCritico = findViewById<TextView>(R.id.btnFiltroCritico)
        val btnBajo = findViewById<TextView>(R.id.btnFiltroBajo)

        btnTodo?.setOnClickListener {
            filtroActual = "Todo"
            actualizarUIFiltros(btnTodo, btnCritico!!, btnBajo!!)
            aplicarFiltros()
        }

        btnCritico?.setOnClickListener {
            filtroActual = "Crítico"
            actualizarUIFiltros(btnCritico, btnTodo!!, btnBajo!!)
            aplicarFiltros()
        }

        btnBajo?.setOnClickListener {
            filtroActual = "Bajo"
            actualizarUIFiltros(btnBajo, btnTodo!!, btnCritico!!)
            aplicarFiltros()
        }
    }

    private fun actualizarUIFiltros(seleccionado: TextView, varall: TextView, varall2: TextView) {
        seleccionado.setTextColor(ContextCompat.getColor(this, R.color.primaryColor))
        seleccionado.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primaryLight))
        seleccionado.setTypeface(null, android.graphics.Typeface.BOLD)

        val gris = ContextCompat.getColor(this, R.color.textSecondary)
        varall.setTextColor(gris)
        varall.backgroundTintList = null
        varall.setTypeface(null, android.graphics.Typeface.NORMAL)

        varall2.setTextColor(gris)
        varall2.backgroundTintList = null
        varall2.setTypeface(null, android.graphics.Typeface.NORMAL)
    }

    private fun aplicarFiltros() {
        var listaFiltrada = listaCompleta
        if (queryActual.isNotEmpty()) {
            listaFiltrada = listaFiltrada.filter {
                it.nombre.contains(queryActual, ignoreCase = true) ||
                        it.codigo.contains(queryActual, ignoreCase = true)
            }
        }
        listaFiltrada = when (filtroActual) {
            "Crítico" -> listaFiltrada.filter { it.cantidad == 0 }
            "Bajo" -> listaFiltrada.filter { it.cantidad > 0 && it.cantidad <= it.stockMinimo }
            else -> listaFiltrada
        }
        actualizarViewPager(listaFiltrada)
    }

    private fun cargarProductosReporte() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getProductos("Bearer $token")
                if (response.isSuccessful) {
                    listaCompleta = response.body() ?: emptyList()
                    actualizarResumenCards()
                    aplicarFiltros()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun actualizarResumenCards() {
        val criticos = listaCompleta.count { it.cantidad == 0 }
        val bajos = listaCompleta.count { it.cantidad > 0 && it.cantidad <= it.stockMinimo }

        findViewById<TextView>(R.id.productsNumber)?.text = criticos.toString()
        findViewById<TextView>(R.id.stockNumber)?.text = bajos.toString()

        // Cálculo del valor necesario para reponer el stock hasta el mínimo permitido
        val valorReposicion = listaCompleta
            .filter { it.cantidad < it.stockMinimo }
            .sumOf { producto ->
                val faltante = producto.stockMinimo - producto.cantidad
                val precio = producto.precio.toDoubleOrNull() ?: 0.0
                faltante * precio
            }

        findViewById<TextView>(R.id.valueNumber)?.text = "$${String.format("%.2f", valorReposicion)}"
    }

    private fun actualizarViewPager(lista: List<Producto>) {
        pagerAdapter.actualizar(lista)
        if (lista.isNotEmpty()) {
            viewPager.post {
                viewPager.setCurrentItem(0, false)
                actualizarIndicadorPagina(0)
            }
        } else {
            txtPagina.text = "0 / 0"
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.selectedItemId = R.id.reports

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val rol = prefs.getString("user_role", "Administrador")
        if (rol == "Auxiliar") {
            bottomNav?.menu?.findItem(R.id.categoria)?.isVisible = false
        }

        bottomNav?.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.reports) return@setOnItemSelectedListener true
            val intent = when (item.itemId) {
                R.id.home -> Intent(this, ActivityInicio::class.java)
                R.id.products -> Intent(this, ProductosActivity::class.java)
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
}