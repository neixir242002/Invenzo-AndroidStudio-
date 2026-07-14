package com.example.invenzo_10

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificacionManager {
    private var listener: (() -> Unit)? = null

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("invenzo_notifications", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun triggerChange() {
        listener?.invoke()
    }

    fun setOnNotificationChangedListener(l: () -> Unit) {
        listener = l
    }

    /**
     * Envía la notificación al servidor.
     * Usamos redundancia en los nombres de los campos (español e inglés) para que
     * el backend de Laravel y el modelo de Android siempre encuentren la información.
     */
    fun addNotification(context: Context, titulo: String, mensaje: String, type: String = "STOCK") {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        if (token.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // EVITAR DUPLICADOS: Si es una alerta de STOCK, verificamos si ya existe una idéntica sin leer
                if (type == "STOCK") {
                    val current = RetrofitClient.instance.getNotificaciones("Bearer $token")
                    if (current.isSuccessful) {
                        val yaExiste = current.body()?.any { 
                            it.titulo.equals(titulo, true) && it.mensaje.equals(mensaje, true) && !it.leida 
                        } ?: false
                        if (yaExiste) return@launch
                    }
                }

                // Crear la petición con redundancia para asegurar el mapeo en el objeto 'data' de Laravel
                val request = NotificacionRequest(
                    titulo = titulo,
                    title = titulo,
                    mensaje = mensaje,
                    message = mensaje,
                    tipo = type,
                    type = type
                )
                
                val response = RetrofitClient.instance.crearNotificacion("Bearer $token", request)
                
                if (response.isSuccessful) {
                    triggerChange()
                    Log.d("NotifManager", "Notificación registrada: $titulo")
                } else {
                    Log.e("NotifManager", "Error API: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("NotifManager", "Excepción al registrar notificación: ${e.message}")
            }
        }
    }

    fun clearAll(context: Context) {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.instance.limpiarNotificaciones("Bearer $token")
                triggerChange()
            } catch (e: Exception) {
                Log.e("NotifManager", "Error al limpiar")
            }
        }
    }

    fun clearNotificationsByType(context: Context, type: String) {}
    fun markAllAsRead(context: Context) { triggerChange() }
    fun getUnreadCount(context: Context): Int = 0
}
