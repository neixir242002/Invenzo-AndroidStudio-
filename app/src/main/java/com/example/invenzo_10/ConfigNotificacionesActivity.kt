package com.example.invenzo_10

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

class ConfigNotificacionesActivity : AppCompatActivity() {

    private lateinit var switchStock: SwitchMaterial
    private lateinit var switchMovements: SwitchMaterial
    private lateinit var switchNewProducts: SwitchMaterial
    private lateinit var btnSave: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyEdgeToEdgeWithInsets(null)
        setContentView(R.layout.activity_config_notificaciones)
        applyEdgeToEdgeWithInsets(findViewById(R.id.layoutHeader))

        initViews()
        loadSettings()
        setupListeners()
        mostrarDatosUsuario()
    }

    private fun initViews() {
        switchStock = findViewById(R.id.switchStockAlerts)
        switchMovements = findViewById(R.id.switchInventoryMovements)
        switchNewProducts = findViewById(R.id.switchNewProducts)
        btnSave = findViewById(R.id.btnSaveConfig)

        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("settings_notif", Context.MODE_PRIVATE)
        switchStock.isChecked = prefs.getBoolean("stock_alerts", true)
        switchMovements.isChecked = prefs.getBoolean("movements_alerts", true)
        switchNewProducts.isChecked = prefs.getBoolean("new_products_alerts", true)
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun saveSettings() {
        val prefs = getSharedPreferences("settings_notif", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("stock_alerts", switchStock.isChecked)
            putBoolean("movements_alerts", switchMovements.isChecked)
            putBoolean("new_products_alerts", switchNewProducts.isChecked)
            apply()
        }
        
        // Notificar al manager que la configuración cambió para actualizar el badge y la lista
        NotificacionManager.triggerChange()

        Toast.makeText(this, "Configuración guardada correctamente", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun mostrarDatosUsuario() {
        val txtNombreHeader = findViewById<TextView>(R.id.txtUserNameHeader)
        val txtRoleHeader = findViewById<TextView>(R.id.txtUserRoleCompanyHeader)

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nombre = prefs.getString("user_name", "Usuario")
        var rol = prefs.getString("user_role", "Administrador")
        val empresa = prefs.getString("user_company", "Empresa")

        if (rol?.contains("principal", ignoreCase = true) == true) {
            rol = "Administrador Principal"
        }

        txtNombreHeader?.text = nombre
        txtRoleHeader?.text = "$rol • $empresa"
    }
}
