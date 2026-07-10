package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ControlInventarioActivity : AppCompatActivity() {

    private lateinit var spinnerProducto: MaterialAutoCompleteTextView
    private lateinit var spinnerTipo: MaterialAutoCompleteTextView
    private lateinit var etCantidad: TextInputEditText
    private lateinit var etObservaciones: TextInputEditText
    private lateinit var btnRegistrar: Button
    
    // ViewPagers y Adapters
    private lateinit var vpMovimientos: ViewPager2
    private lateinit var vpResumenStock: ViewPager2
    private lateinit var txtPageIndicator: TextView
    private lateinit var txtResumenPage: TextView
    
    private lateinit var movimientoPagerAdapter: MovimientoPagerAdapter
    private lateinit var resumenStockPagerAdapter: ResumenStockPageAdapter

    private var listaProductos = mutableListOf<Producto>()
    private var productoSeleccionadoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_control_inventario)

        mostrarDatosUsuario()
        initViews()
        setupSpinners()
        setupViewPagers()
        setupBottomNavigation() // <-- Agregado para que funcione el Navbar
        cargarDatos()

        btnRegistrar.setOnClickListener { registrarMovimiento() }
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
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

    private fun initViews() {
        spinnerProducto = findViewById(R.id.spinnerProducto)
        spinnerTipo = findViewById(R.id.spinnerTipo)
        etCantidad = findViewById(R.id.etCantidad)
        etObservaciones = findViewById(R.id.etObservaciones)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        
        vpMovimientos = findViewById(R.id.vpMovimientos)
        vpResumenStock = findViewById(R.id.vpResumenStock)
        txtPageIndicator = findViewById(R.id.txtPageIndicator)
        txtResumenPage = findViewById(R.id.txtResumenPage)
    }

    private fun setupSpinners() {
        val tipos = arrayOf("Entrada", "Salida")
        val adapterTipo = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tipos)
        spinnerTipo.setAdapter(adapterTipo)
        spinnerTipo.setText(tipos[0], false)

        spinnerProducto.setOnItemClickListener { parent, _, position, _ ->
            val nombreSeleccionado = parent.getItemAtPosition(position) as String
            productoSeleccionadoId = listaProductos.find { it.nombre == nombreSeleccionado }?.id ?: -1
        }
    }

    private fun setupViewPagers() {
        movimientoPagerAdapter = MovimientoPagerAdapter(emptyList())
        vpMovimientos.adapter = movimientoPagerAdapter

        resumenStockPagerAdapter = ResumenStockPageAdapter(mutableListOf())
        vpResumenStock.adapter = resumenStockPagerAdapter

        vpMovimientos.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val total = movimientoPagerAdapter.itemCount
                txtPageIndicator.text = "${position + 1} / ${if (total == 0) 1 else total}"
            }
        })

        vpResumenStock.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val total = resumenStockPagerAdapter.itemCount
                txtResumenPage.text = "${position + 1} / ${if (total == 0) 1 else total}"
            }
        })
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        
        // Marcar "Más" como seleccionado
        bottomNav?.selectedItemId = R.id.more

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val rol = prefs.getString("user_role", "Administrador")
        
        // Restricción para Auxiliar
        if (rol == "Auxiliar") {
            bottomNav?.menu?.findItem(R.id.categoria)?.isVisible = false
        }

        bottomNav?.setOnItemSelectedListener { item ->
            // Si ya estamos en una subsección de "More", y pulsan "More" de nuevo, 
            // volvemos al menú principal de opciones.
            val intent = when (item.itemId) {
                R.id.home -> Intent(this, ActivityInicio::class.java)
                R.id.products -> Intent(this, ProductosActivity::class.java)
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

    private fun cargarDatos() {
        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", "") ?: ""
        val authHeader = "Bearer $token"

        lifecycleScope.launch {
            try {
                val resProd = RetrofitClient.instance.getProductos(authHeader)
                if (resProd.isSuccessful && resProd.body() != null) {
                    listaProductos.clear()
                    listaProductos.addAll(resProd.body()!!)
                    
                    val nombres = listaProductos.map { it.nombre }
                    val adapterProd = ArrayAdapter(this@ControlInventarioActivity, android.R.layout.simple_dropdown_item_1line, nombres)
                    spinnerProducto.setAdapter(adapterProd)
                    
                    resumenStockPagerAdapter.actualizar(listaProductos.toMutableList())
                    txtResumenPage.text = "1 / ${resumenStockPagerAdapter.itemCount}"
                }

                val resMov = RetrofitClient.instance.getMovimientos(authHeader)
                if (resMov.isSuccessful && resMov.body() != null) {
                    val movimientos = resMov.body()!!.reversed()
                    movimientoPagerAdapter.actualizar(movimientos)
                    txtPageIndicator.text = "1 / ${movimientoPagerAdapter.itemCount}"
                }

            } catch (e: Exception) {
                Log.e("CONTROL", "Error: ${e.message}")
            }
        }
    }

    private fun registrarMovimiento() {
        val cantStr = etCantidad.text.toString()
        if (productoSeleccionadoId == -1 || cantStr.isEmpty()) {
            Toast.makeText(this, "Seleccione un producto y cantidad", Toast.LENGTH_SHORT).show()
            return
        }

        val request = MovimientoRequest(
            producto_id = productoSeleccionadoId,
            tipo = spinnerTipo.text.toString(),
            cantidad = cantStr.toInt(),
            observacion = etObservaciones.text.toString()
        )

        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.registrarMovimiento("Bearer $token", request)
                if (response.isSuccessful) {
                    Toast.makeText(this@ControlInventarioActivity, "Movimiento registrado", Toast.LENGTH_SHORT).show()
                    etCantidad.text?.clear()
                    etObservaciones.text?.clear()
                    spinnerProducto.text?.clear()
                    productoSeleccionadoId = -1
                    cargarDatos() // Recargar para ver cambios
                } else {
                    Toast.makeText(this@ControlInventarioActivity, "Error al registrar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ControlInventarioActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
