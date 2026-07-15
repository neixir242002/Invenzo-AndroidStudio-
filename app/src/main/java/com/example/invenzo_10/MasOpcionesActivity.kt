package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomnavigation.BottomNavigationView

class MasOpcionesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyEdgeToEdgeWithInsets(null)
        setContentView(R.layout.activity_mas_opciones)

        NotificacionManager.init(this)
        NotificationUtils.setupNotificationButton(this)

        setupClickListeners()
        setupBottomNavigation()
        mostrarDatosUsuario()
        aplicarRestricciones()
    }

    private fun mostrarDatosUsuario() {
        val txtNombre = findViewById<TextView>(R.id.txtName)
        val txtRole = findViewById<TextView>(R.id.txtRole)
        val imgProfileCard = findViewById<ImageView>(R.id.imgProfile)

        val txtNombreHeader = findViewById<TextView>(R.id.txtUserNameHeader)
        val txtRoleHeader = findViewById<TextView>(R.id.txtUserRoleCompanyHeader)
        val imgProfileHeader = findViewById<ImageView>(R.id.profileImageHeader)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        var rol = prefs.getString("user_role", "Administrador")
        val empresa = prefs.getString("user_company", "Empresa")
        val fotoPath = prefs.getString("user_photo", "")

        if (rol?.contains("principal", ignoreCase = true) == true) {
            rol = "Administrador Principal"
        }

        val infoCompleta = "$rol • $empresa"

        txtNombre?.text = nombre
        txtRole?.text = infoCompleta
        txtNombreHeader?.text = nombre
        txtRoleHeader?.text = infoCompleta

        if (!fotoPath.isNullOrEmpty()) {
            // USAMOS URL REALTIME PARA QUE SE ACTUALICE AL INSTANTE
            val fullUrl = RetrofitClient.obtenerUrlRealtime(fotoPath)
            
            val glideRequest = Glide.with(this)
                .load(fullUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .circleCrop()

            imgProfileCard?.let { glideRequest.into(it) }
            imgProfileHeader?.let { glideRequest.into(it) }
        } else {
            imgProfileCard?.setImageResource(R.drawable.ic_user)
            imgProfileHeader?.setImageResource(R.drawable.ic_user)
        }
    }

    override fun onResume() {
        super.onResume()
        mostrarDatosUsuario()
    }

    private fun aplicarRestricciones() {
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val rol = prefs.getString("user_role", "Administrador Principal")

        when (rol) {
            "Auxiliar" -> {
                findViewById<View>(R.id.optUsuarios)?.visibility = View.GONE
                findViewById<View>(R.id.optCategorias)?.visibility = View.GONE
                findViewById<View>(R.id.optHistorialInventario)?.visibility = View.GONE
                findViewById<BottomNavigationView>(R.id.bottomNav)?.menu?.findItem(R.id.categoria)?.isVisible = false
            }
            "Administrador" -> {
                findViewById<View>(R.id.optUsuarios)?.visibility = View.GONE
                findViewById<View>(R.id.optCategorias)?.visibility = View.GONE 
                findViewById<View>(R.id.optHistorialInventario)?.visibility = View.VISIBLE
            }
            else -> {
                findViewById<View>(R.id.optUsuarios)?.visibility = View.VISIBLE
                findViewById<View>(R.id.optCategorias)?.visibility = View.VISIBLE
                findViewById<View>(R.id.optHistorialInventario)?.visibility = View.VISIBLE
            }
        }
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.cardProfileHeader)?.setOnClickListener { mostrarDialogoEditar() }
        findViewById<View>(R.id.optCategorias)?.setOnClickListener { startActivity(Intent(this, AuditoriaActivity::class.java)) }
        findViewById<View>(R.id.optInventarios)?.setOnClickListener { startActivity(Intent(this, ControlInventarioActivity::class.java)) }
        findViewById<View>(R.id.optUsuarios)?.setOnClickListener { startActivity(Intent(this, UsuariosActivity::class.java)) }
        findViewById<View>(R.id.optHistorialInventario)?.setOnClickListener { startActivity(Intent(this, HistorialInventarioActivity::class.java)) }
        findViewById<View>(R.id.optNotificaciones)?.setOnClickListener { startActivity(Intent(this, ConfigNotificacionesActivity::class.java)) }
        findViewById<View>(R.id.btnCerrarSesion)?.setOnClickListener { logout() }
    }

    private fun mostrarDialogoEditar() {
        AlertDialog.Builder(this)
            .setTitle("Editar Usuario")
            .setMessage("¿Deseas editar tu información de usuario?")
            .setPositiveButton("Sí") { _, _ -> startActivity(Intent(this, EditarUsuarioActivity::class.java)) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logout() {
        getSharedPreferences("auth", Context.MODE_PRIVATE).edit().clear().apply()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.selectedItemId = R.id.more
        bottomNav?.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.more) return@setOnItemSelectedListener true
            val intent = when (item.itemId) {
                R.id.home -> Intent(this, ActivityInicio::class.java)
                R.id.products -> Intent(this, ProductosActivity::class.java)
                R.id.categoria -> Intent(this, CategoriaActivity::class.java)
                R.id.reports -> Intent(this, ReportesActivity::class.java)
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
