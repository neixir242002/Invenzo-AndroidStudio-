package com.example.invenzo_10

import android.content.Intent
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
        closeSection()
        mostrarNombre()
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }
    private fun mostrarNombre(){

        //Se puede ver el nombre de usuario en el TopBar
        val txtNombre = findViewById<TextView>(
            R.id.txtNameProfile
        )


        val datos = getSharedPreferences(
            "usuario_prueba",
            MODE_PRIVATE
        )


        val nombre = datos.getString(
            "nombre",
            "Usuario"
        )


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

    private fun closeSection(){
        val close = findViewById<View>(R.id.btnLogout)
        close.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent);
        }
    }
    private fun setupOption(layoutId: Int, iconRes: Int, title: String) {
        val layout = findViewById<View>(layoutId)
        layout.findViewById<ImageView>(R.id.ivIcon).setImageResource(iconRes)
        layout.findViewById<TextView>(R.id.tvTitle).text = title
    }
}
