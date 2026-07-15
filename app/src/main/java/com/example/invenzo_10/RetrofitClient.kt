package com.example.invenzo_10

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Cambiamos 127.0.0.1 por tu IP real para que el celular encuentre el servidor
    const val BASE_URL = "http://127.0.0.1:8000/"

//    Puerto VPN Exlusivo de TAILSCALE, permite conectar la api de laravel a cualqier dispocitivo que use tailscale
//    const val BASE_URL = "http://100.110.205.13:8000/"

//    =========================================
//    NO SE IMPLEMENTO EN LA SUSTENTACION FINAL
//    =========================================
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }

    fun obtenerUrlRealtime(path: String?): String? {
        if (path.isNullOrEmpty()) return null
        
        val cleanBase = BASE_URL.trimEnd('/')
        val cleanPath = path.trimStart('/')
        
        val fullUrl = when {
            path.startsWith("http") -> path
            cleanPath.startsWith("storage/") -> "$cleanBase/$cleanPath"
            else -> "$cleanBase/storage/$cleanPath"
        }
        
        return "$fullUrl?t=${System.currentTimeMillis()}"
    }
}