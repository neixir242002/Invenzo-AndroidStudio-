
package com.example.invenzo_10

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Cambia esta dirección por la que estés utilizando realmente.
    //const val BASE_URL = "http://127.0.0.1:8000/"

    // Para utilizar Tailscale, usa tu IP de Tailscale:
     const val BASE_URL = "http://100.110.205.13:8000/"

    @Volatile
    private var retrofitInstance: ApiService? = null

    fun inicializar(context: Context) {
        if (retrofitInstance != null) return

        synchronized(this) {
            if (retrofitInstance == null) {
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }

                val client = OkHttpClient.Builder()
                    .addInterceptor(SesionInterceptor(context))
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

                retrofitInstance = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(
                        GsonConverterFactory.create()
                    )
                    .build()
                    .create(ApiService::class.java)
            }
        }
    }

    val instance: ApiService
        get() = checkNotNull(retrofitInstance) {
            "RetrofitClient no está inicializado. " +
                    "Llama a RetrofitClient.inicializar(context) primero."
        }

    fun obtenerUrlRealtime(path: String?): String? {
        if (path.isNullOrEmpty()) return null

        val cleanBase = BASE_URL.trimEnd('/')
        val cleanPath = path.trimStart('/')

        val fullUrl = when {
            path.startsWith("http") -> path
            cleanPath.startsWith("storage/") ->
                "$cleanBase/$cleanPath"
            else -> "$cleanBase/storage/$cleanPath"
        }

        return "$fullUrl?t=${System.currentTimeMillis()}"
    }
}
