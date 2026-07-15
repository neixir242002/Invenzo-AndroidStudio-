package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
        
        applyEdgeToEdgeWithInsets(null)
        setContentView(R.layout.activity_control_inventario)

        NotificacionManager.init(this)
        NotificationUtils.setupNotificationButton(this)

        applyEdgeToEdgeWithInsets(findViewById(R.id.topBar))

        mostrarDatosUsuario()
        initViews()
        setupSpinners()
        setupViewPagers()
        setupBottomNavigation()
        cargarDatos()

        btnRegistrar.setOnClickListener { registrarMovimiento() }
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun mostrarDatosUsuario() {
        val txtNombre = findViewById<TextView>(R.id.txtUserNameHeader)
        val txtRoleCompany = findViewById<TextView>(R.id.txtUserRoleCompanyHeader)
        val imgProfile = findViewById<ImageView>(R.id.profileImageHeader)
        
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        var rol = prefs.getString("user_role", "Administrador Principal")
        val empresa = prefs.getString("user_company", "Empresa")
        val fotoPath = prefs.getString("user_photo", "")

        if (rol?.contains("admin", ignoreCase = true) == true && !rol.contains("Principal", ignoreCase = true)) {
            rol = "Administrador Principal"
        } else if (rol?.contains("principal", ignoreCase = true) == true) {
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
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .circleCrop()
                    .into(imgProfile)
            } else {
                imgProfile.setImageResource(R.drawable.ic_user)
            }
        }
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
        bottomNav?.selectedItemId = R.id.more

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val rol = prefs.getString("user_role", "Administrador Principal")
        
        if (rol == "Auxiliar") {
            bottomNav?.menu?.findItem(R.id.categoria)?.isVisible = false
        }

        bottomNav?.setOnItemSelectedListener { item ->
            val intent = when (item.itemId) {
                R.id.home -> Intent(this, ActivityInicio::class.java)
                R.id.products -> Intent(this, ProductosActivity::class.java)
                R.id.categoria -> Intent(this, CategoriaActivity::class.java)
                R.id.reports -> Intent(this, ReportesActivity::class.java)
                R.id.more -> Intent(this, MasOpcionesActivity::class.java)
                else -> null
            }

            intent?.let {
                startActivity(it)
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
                    val movimientos = resMov.body()!!.sortedByDescending { it.createdAt ?: "" }
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

        val nombreProducto = listaProductos.find { it.id == productoSeleccionadoId }?.nombre ?: "Desconocido"
        val tipoOriginal = spinnerTipo.text.toString()

        val request = MovimientoRequest(
            producto_id = productoSeleccionadoId,
            tipo = tipoOriginal.lowercase(),
            cantidad = cantStr.toInt(),
            observacion = etObservaciones.text.toString()
        )

        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.registrarMovimiento("Bearer $token", request)
                if (response.isSuccessful) {
                    registrarEnAuditoria("Registró $tipoOriginal de $cantStr unidades del producto: $nombreProducto", "Control Inventario")
                    
                    val movData = response.body()?.movimiento
                    val prodUpdated = movData?.producto

                    val msgMov = "${tipoOriginal.lowercase()} de $cantStr unidades"
                    NotificacionManager.addNotification(this@ControlInventarioActivity, "Control", msgMov, "MOVIMIENTO")

                    if (prodUpdated != null && prodUpdated.cantidad <= prodUpdated.stockMinimo) {
                        NotificacionManager.addNotification(
                            this@ControlInventarioActivity,
                            "Control",
                            "Control tiene stock bajo",
                            "STOCK"
                        )
                    }

                    Toast.makeText(this@ControlInventarioActivity, "Movimiento registrado correctamente", Toast.LENGTH_SHORT).show()
                    etCantidad.text?.clear()
                    etObservaciones.text?.clear()
                    spinnerProducto.text?.clear()
                    productoSeleccionadoId = -1
                    cargarDatos()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("CONTROL", "Error ${response.code()}: $errorBody")
                    Toast.makeText(this@ControlInventarioActivity, "Error ${response.code()}: $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("CONTROL", "Excepción", e)
                Toast.makeText(this@ControlInventarioActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registrarEnAuditoria(accion: String, modulo: String) {
        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                RetrofitClient.instance.registrarAuditoria("Bearer $token", AuditoriaRequest(accion, modulo))
            } catch (e: Exception) {
                Log.e("AUDIT", "Error al registrar auditoría", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mostrarDatosUsuario()
    }
}
