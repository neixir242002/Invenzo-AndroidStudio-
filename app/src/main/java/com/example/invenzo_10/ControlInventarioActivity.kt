package com.example.invenzo_10

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ControlInventarioActivity : AppCompatActivity() {
    private lateinit var vpMovimientos: ViewPager2
    private lateinit var pagerAdapter: MovimientoPagerAdapter
    private lateinit var txtPageIndicator: TextView
    private var todosMovimientos = mutableListOf<Movimiento>()
    private val movimientosPorPagina = 5
    private lateinit var spinnerProducto: MaterialAutoCompleteTextView
    private lateinit var spinnerTipo: MaterialAutoCompleteTextView
    private lateinit var etCantidad: TextInputEditText
    private lateinit var etObservaciones: TextInputEditText
    private lateinit var btnRegistrar: MaterialButton
    private lateinit var btnBack: ImageView
    private var listaProductos = mutableListOf<Producto>()

    private lateinit var vpResumenStock: ViewPager2
    private lateinit var resumenAdapter: ResumenStockPageAdapter
    private lateinit var txtResumenPage: TextView

    private val listaResumen = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_control_inventario)

        vpResumenStock = findViewById(R.id.vpResumenStock)
        txtResumenPage = findViewById(R.id.txtResumenPage)
        resumenAdapter = ResumenStockPageAdapter(mutableListOf())
        vpResumenStock.adapter = resumenAdapter

        vpMovimientos = findViewById(R.id.vpMovimientos)
        txtPageIndicator = findViewById(R.id.txtPageIndicator)
        
        pagerAdapter = MovimientoPagerAdapter(mutableListOf())
        vpMovimientos.adapter = pagerAdapter

        vpMovimientos.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                actualizarIndicadorPagina(position)
            }
        })

        vpResumenStock.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (resumenAdapter.itemCount > 0) {
                    txtResumenPage.text = "${position + 1} / ${resumenAdapter.itemCount}"
                } else {
                    txtResumenPage.text = "0 / 0"
                }
            }
        })

        spinnerProducto = findViewById(R.id.spinnerProducto)
        spinnerTipo = findViewById(R.id.spinnerTipo)
        etCantidad = findViewById(R.id.etCantidad)
        etObservaciones = findViewById(R.id.etObservaciones)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        btnBack = findViewById(R.id.btnBack)

        btnRegistrar.setOnClickListener {
            registrarMovimiento()
        }

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupSpinners()
        mostrarNombre()
        cargarProductos()
        cargarMovimientos()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        cargarProductos()
        cargarMovimientos()
    }

    private fun actualizarIndicadorPagina(position: Int) {
        val totalPaginas = pagerAdapter.itemCount
        if (totalPaginas > 0) {
            txtPageIndicator.text = "${position + 1} / $totalPaginas"
        } else {
            txtPageIndicator.text = "0 / 0"
        }
    }

    private fun mostrarNombre() {
        val txtNombre = findViewById<TextView>(R.id.txtUserNameHeader)
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        txtNombre.text = nombre
    }

    private fun cargarProductos() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "")
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getProductos("Bearer $token")
                if (response.isSuccessful) {
                    listaProductos.clear()
                    response.body()?.let { list ->
                        listaProductos.addAll(list)
                        
                        val nombres = listaProductos.map { it.nombre }
                        val adapter = ArrayAdapter(this@ControlInventarioActivity, android.R.layout.simple_dropdown_item_1line, nombres)
                        spinnerProducto.setAdapter(adapter)

                        listaResumen.clear()
                        listaResumen.addAll(listaProductos)
                        resumenAdapter.actualizar(listaResumen)

                        if (listaResumen.isNotEmpty()) {
                            vpResumenStock.post {
                                txtResumenPage.text = "${vpResumenStock.currentItem + 1} / ${resumenAdapter.itemCount}"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun registrarMovimiento() {
        val prodNombre = spinnerProducto.text.toString()
        val tipoStr = spinnerTipo.text.toString()
        val cantStr = etCantidad.text.toString()

        if (prodNombre.isEmpty() || tipoStr.isEmpty() || cantStr.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val producto = listaProductos.find { it.nombre == prodNombre }
        if (producto == null) {
            Toast.makeText(this, "Seleccione un producto válido de la lista", Toast.LENGTH_SHORT).show()
            return
        }

        val cantidadNum = cantStr.toIntOrNull() ?: 0
        if (cantidadNum <= 0) {
            Toast.makeText(this, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }

        val movimientoReq = MovimientoRequest(
            producto_id = producto.id,
            tipo = tipoStr.lowercase(),
            cantidad = cantidadNum,
            observacion = etObservaciones.text.toString()
        )

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.registrarMovimiento("Bearer $token", movimientoReq)
                if (response.isSuccessful) {
                    // ACTUALIZACIÓN EN TIEMPO REAL
                    val movResp = response.body()
                    movResp?.movimiento?.producto?.let { prodActualizado ->
                        // Actualizar en la lista local para respuesta inmediata
                        val index = listaProductos.indexOfFirst { it.id == prodActualizado.id }
                        if (index != -1) {
                            listaProductos[index] = prodActualizado
                            listaResumen.clear()
                            listaResumen.addAll(listaProductos)
                            resumenAdapter.actualizar(listaResumen)
                        }
                    }

                    Toast.makeText(this@ControlInventarioActivity, "Movimiento registrado con éxito", Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                    cargarMovimientos()
                    // cargarProductos() // Opcional, ya actualizamos localmente lo más importante
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error del servidor"
                    Toast.makeText(this@ControlInventarioActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ControlInventarioActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarMovimientos() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMovimientos("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        todosMovimientos.clear()
                        todosMovimientos.addAll(it)
                        pagerAdapter.actualizar(todosMovimientos)
                        if (todosMovimientos.isNotEmpty()) {
                            vpMovimientos.post { actualizarIndicadorPagina(vpMovimientos.currentItem) }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupSpinners() {
        val tipos = arrayOf("Entrada", "Salida")
        spinnerTipo.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tipos))
    }

    private fun limpiarFormulario() {
        spinnerProducto.setText("", false)
        spinnerTipo.setText("", false)
        etCantidad.setText("")
        etObservaciones.setText("")
        spinnerProducto.clearFocus()
        spinnerTipo.clearFocus()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.more 
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == bottomNav.selectedItemId && item.itemId != R.id.more) return@setOnItemSelectedListener true
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