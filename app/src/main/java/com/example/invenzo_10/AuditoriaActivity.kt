package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class AuditoriaActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: AuditoriaPagerAdapter
    private lateinit var txtEmpty: TextView
    private lateinit var etBuscar: EditText
    private lateinit var txtPageIndicator: TextView
    private lateinit var spinnerModulos: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        applyEdgeToEdgeWithInsets(null)
        setContentView(R.layout.activity_auditoria_sistema)
        applyEdgeToEdgeWithInsets(findViewById(R.id.topBar))

        NotificacionManager.init(this)
        NotificationUtils.setupNotificationButton(this)

        txtEmpty = findViewById(R.id.txtEmpty)
        etBuscar = findViewById(R.id.etBuscarAuditoria)
        txtPageIndicator = findViewById(R.id.txtPageIndicator)
        viewPager = findViewById(R.id.vpAuditoria)
        spinnerModulos = findViewById(R.id.spinnerModulos)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        
        findViewById<View>(R.id.btnLimpiarAuditorias).setOnClickListener {
            mostrarConfirmacionLimpiar()
        }

        setupSpinner()

        adapter = AuditoriaPagerAdapter(emptyList())
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
                aplicarFiltros()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        cargarAuditorias()
        mostrarDatosUsuario()
        setupBottomNavigation()
    }

    private fun setupSpinner() {
        val opciones = arrayOf("Todos los módulos", "Productos", "Categorías", "Inventario", "Usuarios")
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerModulos.adapter = adapterSpinner

        spinnerModulos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                moduloSeleccionado = opciones[position]
                aplicarFiltros()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private var listaAuditoriaCompleta = listOf<Auditoria>()
    private var moduloSeleccionado = "Todos los módulos"

    private fun mostrarConfirmacionLimpiar() {
        if (listaAuditoriaCompleta.isEmpty()) {
            Toast.makeText(this, "El historial ya está vacío", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Limpiar Historial")
            .setMessage("¿Estás seguro de eliminar todos los registros de auditoría? Esta acción no se puede deshacer.")
            .setPositiveButton("Limpiar Todo") { _, _ -> eliminarAuditorias() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarAuditorias() {
        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", "") ?: ""
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.limpiarAuditorias("Bearer $token")
                if (response.isSuccessful) {
                    listaAuditoriaCompleta = emptyList()
                    aplicarFiltros()
                    Toast.makeText(this@AuditoriaActivity, "Historial limpiado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AuditoriaActivity, "Error al limpiar el historial", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AuditoriaActivity, "Error de red", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun aplicarFiltros() {
        val query = etBuscar.text.toString()
        
        val filtrada = listaAuditoriaCompleta.filter { audit ->
            val moduloNormalizado = audit.modulo?.lowercase()?.replace("í", "i") ?: ""
            val seleccionadoNormalizado = moduloSeleccionado.lowercase().replace("í", "i")

            val coincideModulo = if (moduloSeleccionado == "Todos los módulos") {
                true
            } else {
                moduloNormalizado.contains(seleccionadoNormalizado)
            }

            val coincideTexto = if (query.isBlank()) {
                true
            } else {
                audit.usuario?.nombre?.contains(query, ignoreCase = true) == true ||
                audit.accion?.contains(query, ignoreCase = true) == true ||
                audit.modulo?.contains(query, ignoreCase = true) == true
            }

            coincideModulo && coincideTexto
        }

        adapter.actualizar(filtrada)
        actualizarEstadoVacio(filtrada.isEmpty())
        actualizarIndicadorPagina(0)
    }

    private fun cargarAuditorias() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getAuditorias("Bearer $token")
                if (response.isSuccessful) {
                    listaAuditoriaCompleta = response.body()?.sortedByDescending { it.fecha ?: "" } ?: emptyList()
                    aplicarFiltros()
                } else {
                    Toast.makeText(this@AuditoriaActivity, "Error al obtener datos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AuditoriaActivity, "Error de red", Toast.LENGTH_LONG).show()
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

    override fun onResume() {
        super.onResume()
        mostrarDatosUsuario()
    }

    private fun mostrarDatosUsuario() {
        val txtNombre = findViewById<TextView>(R.id.txtUserNameHeader)
        val txtRoleHeader = findViewById<TextView>(R.id.txtUserRoleCompanyHeader)
        val imgProfile = findViewById<ImageView>(R.id.profileImageHeader)

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        val rol = prefs.getString("user_role", "Admin")
        val empresa = prefs.getString("user_company", "Empresa")
        val fotoPath = prefs.getString("user_photo", "")

        txtNombre?.text = nombre
        txtRoleHeader?.text = "$rol • $empresa"

        if (imgProfile != null) {
            if (!fotoPath.isNullOrEmpty()) {
                val cleanPath = if (fotoPath.startsWith("/")) fotoPath.substring(1) else fotoPath
                val fullUrl = if (fotoPath.startsWith("http")) fotoPath else "${RetrofitClient.BASE_URL}storage/$cleanPath"
                
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
