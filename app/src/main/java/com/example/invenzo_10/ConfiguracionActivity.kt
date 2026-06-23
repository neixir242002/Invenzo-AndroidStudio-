package com.example.invenzo_10

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ConfiguracionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        setupOptions()

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun setupOptions() {
        // CUENTA
        setupOption(R.id.optPerfil, R.drawable.ic_user, getString(R.string.profile))
        setupOption(R.id.optSeguridad, R.drawable.ic_lock_reset, getString(R.string.security))

        // PREFERENCIAS
        setupOption(R.id.optNotif, R.drawable.ic_bell, getString(R.string.notifications))
        setupOption(R.id.optTema, R.drawable.ic_settings, getString(R.string.theme)) // Placeholder icon
        setupOption(R.id.optIdioma, R.drawable.ic_categories, getString(R.string.language)) // Placeholder icon

        // SISTEMA
        setupOption(R.id.optBackup, R.drawable.ic_inventory, getString(R.string.backup)) // Placeholder icon
        setupOption(R.id.optExport, R.drawable.ic_category, getString(R.string.export_data)) // Placeholder icon

        // SOPORTE
        setupOption(R.id.optAyuda, R.drawable.ic_help, getString(R.string.help_support))
        setupOption(R.id.optTerms, R.drawable.ic_more, getString(R.string.terms_conditions)) // Placeholder icon
        setupOption(R.id.optAbout, R.drawable.ic_launcher_foreground, getString(R.string.about_app)) // Placeholder icon
    }

    private fun setupOption(layoutId: Int, iconRes: Int, title: String) {
        val layout = findViewById<View>(layoutId)
        layout.findViewById<ImageView>(R.id.ivIcon).setImageResource(iconRes)
        layout.findViewById<TextView>(R.id.tvTitle).text = title
    }
}
