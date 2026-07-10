package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ConfiguracionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_configuracion)

        setupOptions()
        logout()
        mostrarDatosUsuario()
        setupBottomNavigation()
        
        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }
        
        findViewById<View>(R.id.btnEditProfile)?.setOnClickListener {
            // Navegar a editar perfil si existe
        }
    }

    private fun mostrarDatosUsuario() {
        val txtNombre = findViewById<TextView>(R.id.txtName)
        val txtRoleCompany = findViewById<TextView>(R.id.txtRole) // En esta pantalla el ID es txtRole
        
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        val rol = prefs.getString("user_role", "Administrador")
        val empresa = prefs.getString("user_company", "Empresa")
        
        txtNombre?.text = nombre
        txtRoleCompany?.text = "$rol • $empresa"
    }

    private fun setupOptions() {
        setupOption(R.id.optPerfil, R.drawable.ic_user, getString(R.string.profile))
        setupOption(R.id.optSeguridad, R.drawable.ic_lock_reset, getString(R.string.security))
        setupOption(R.id.optNotif, R.drawable.ic_bell, getString(R.string.notifications))
        setupOption(R.id.optAyuda, R.drawable.ic_help, getString(R.string.help_support))
    }

    private fun logout() {
        findViewById<View>(R.id.btnLogout)?.setOnClickListener {
            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
            prefs.edit().clear().apply()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.selectedItemId = R.id.more 

        bottomNav?.setOnItemSelectedListener { item ->
            navigateTo(item.itemId)
            true
        }
    }

    private fun navigateTo(itemId: Int) {
        val intent = when (itemId) {
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
    }

    private fun setupOption(layoutId: Int, iconRes: Int, title: String) {
        val layout = findViewById<View>(layoutId)
        layout?.findViewById<ImageView>(R.id.ivIcon)?.setImageResource(iconRes)
        layout?.findViewById<TextView>(R.id.tvTitle)?.text = title
    }
}
