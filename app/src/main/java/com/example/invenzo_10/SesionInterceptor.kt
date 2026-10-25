
package com.example.invenzo_10

import android.content.Context
import android.os.Handler
import android.os.Looper
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class SesionInterceptor(
    context: Context
) : Interceptor {

    private val appContext = context.applicationContext

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        try {
            val response = chain.proceed(request)

            // Un error 5xx indica un problema del servidor.
            // No cerramos la sesión por errores 4xx,
            // como credenciales incorrectas o permisos insuficientes.
            if (response.code in 500..599 &&
                SesionManager.tieneSesion(appContext)
            ) {
                response.close()
                cerrarSesion()
                throw IOException("El servidor no está disponible")
            }

            return response

        } catch (e: IOException) {
            // Si falla una petición por un error de red,
            // cerramos la sesión si todavía existe.
            if (SesionManager.tieneSesion(appContext)) {
                cerrarSesion()
            }

            throw e
        }
    }

    private fun cerrarSesion() {
        Handler(Looper.getMainLooper()).post {
            SesionManager.cerrarSesion(appContext)
        }
    }
}
