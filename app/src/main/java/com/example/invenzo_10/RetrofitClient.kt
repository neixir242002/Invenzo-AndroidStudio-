
package com.example.invenzo_10

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Cambia esta dirección si la IP de tu servidor es diferente.
    // Usa la IP de tu PC accesible desde el celular.
    const val BASE_URL = "http://100.110.205.13:8000/"

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }

    fun obtenerUrlRealtime(path: String?): String? {
        if (path.isNullOrBlank()) return null

        val cleanBase = BASE_URL.trimEnd('/')
        val cleanPath = path.trimStart('/')

        val fullUrl = when {
            path.startsWith("http://", ignoreCase = true) ||
                    path.startsWith("https://", ignoreCase = true) -> path

            cleanPath.startsWith("storage/") ->
                "$cleanBase/$cleanPath"

            else ->
                "$cleanBase/storage/$cleanPath"
        }

        val separador = if (fullUrl.contains("?")) "&" else "?"

        return "$fullUrl${separador}t=${System.currentTimeMillis()}"
    }
}
