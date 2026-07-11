package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ConfiguracionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        setupOptions()
        logout()
        mostrarNombre()
        setupBottomNavigation()
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun mostrarNombre() {
        val txtNombre = findViewById<TextView>(R.id.txtUserNameHeader)
        // Usamos "auth" que es donde MainActivity guarda el nombre
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        txtNombre.text = nombre
    }

    private fun setupOptions() {
        // CUENTA
        setupOption(R.id.optPerfil, R.drawable.ic_user, getString(R.string.profile))
        setupOption(R.id.optSeguridad, R.drawable.ic_lock_reset, getString(R.string.security))
        // PREFERENCIAS
        setupOption(R.id.optNotif, R.drawable.ic_bell, getString(R.string.notifications))
        // SOPORTE
        setupOption(R.id.optAyuda, R.drawable.ic_help, getString(R.string.help_support))
    }

    private fun logout() {
        // Corregido: Referencia al ID btnLogout
        val close = findViewById<View>(R.id.btnLogout)

        close.setOnClickListener {
            val prefs = getSharedPreferences(
                "auth",
                MODE_PRIVATE
            )

            // borrar token guardado
            prefs.edit().clear().apply()

            val intent = Intent(
                this,
                MainActivity::class.java
            )

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.more

        bottomNav.setOnItemSelectedListener { item ->
            navigateTo(item.itemId)
            true
        }
    }

    // Función reutilizable para navegar a cualquier item
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
        layout.findViewById<ImageView>(R.id.ivIcon).setImageResource(iconRes)
        layout.findViewById<TextView>(R.id.tvTitle).text = title
    }
}
