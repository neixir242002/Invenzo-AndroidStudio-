
package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

object SesionManager {

    private const val PREFS_AUTH = "auth"

    /**
     * Elimina los datos de autenticación guardados.
     */
    fun cerrarSesion(context: Context) {
        context.getSharedPreferences(
            PREFS_AUTH,
            Context.MODE_PRIVATE
        ).edit().clear().commit()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        context.startActivity(intent)
    }

    /**
     * Comprueba si existe un token guardado.
     */
    fun tieneSesion(context: Context): Boolean {
        val prefs = context.getSharedPreferences(
            PREFS_AUTH,
            Context.MODE_PRIVATE
        )

        return !prefs.getString("token", null).isNullOrEmpty()
    }
}
