package com.example.invenzo_10

import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Configura la actividad para que se extienda de borde a borde y ajusta el padding de la TopBar
 * para evitar que se superponga con la barra de estado del sistema.
 */
fun ComponentActivity.applyEdgeToEdgeWithInsets(topBar: View?) {
    // 1. Habilitar el modo Edge-to-Edge
    enableEdgeToEdge()

    // 2. Hacer que los iconos de la barra de estado sean oscuros (para fondos claros como el blanco)
    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    windowInsetsController.isAppearanceLightStatusBars = true

    // 3. Aplicar el ajuste de padding a la TopBar si existe
    topBar?.let { view ->
        // Guardamos el padding superior inicial definido en el XML (ej. 16dp)
        val initialPaddingTop = view.paddingTop
        val initialPaddingBottom = view.paddingBottom
        val initialPaddingLeft = view.paddingLeft
        val initialPaddingRight = view.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Ajustamos el padding superior sumando el tamaño de la barra de estado
            v.setPadding(
                initialPaddingLeft,
                systemBars.top + initialPaddingTop,
                initialPaddingRight,
                initialPaddingBottom
            )
            insets
        }
    }
}