package com.example.invenzo_10

import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Configura la actividad para que se extienda de borde a borde y ajusta el padding de la vista superior.
 * [isLightStatusBar] true para iconos oscuros (fondo claro), false para iconos blancos (fondo oscuro).
 */
fun ComponentActivity.applyEdgeToEdgeWithInsets(topBar: View?, isLightStatusBar: Boolean = true) {
    enableEdgeToEdge()

    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    windowInsetsController.isAppearanceLightStatusBars = isLightStatusBar

    topBar?.let { view ->
        val initialPaddingTop = view.paddingTop
        val initialPaddingBottom = view.paddingBottom
        val initialPaddingLeft = view.paddingLeft
        val initialPaddingRight = view.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
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