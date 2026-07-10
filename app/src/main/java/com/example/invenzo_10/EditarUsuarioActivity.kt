package com.example.invenzo_10

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EditarUsuarioActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSave: MaterialButton
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            imgProfile.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editarusuario)

        initViews()
        cargarDatosActuales()
        setupListeners()
    }

    private fun initViews() {
        imgProfile = findViewById(R.id.profileImage)
        etNombre = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun cargarDatosActuales() {
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        etNombre.setText(prefs.getString("user_name", ""))
        etEmail.setText(prefs.getString("user_email", ""))
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnChangePhoto).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        btnSave.setOnClickListener {
            guardarCambios()
        }
    }

    private fun guardarCambios() {
        val nuevoNombre = etNombre.text.toString().trim()
        val nuevoEmail = etEmail.text.toString().trim()

        if (nuevoNombre.isEmpty() || nuevoEmail.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        val userId = prefs.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val nombrePart = nuevoNombre.toRequestBody("text/plain".toMediaTypeOrNull())
                val emailPart = nuevoEmail.toRequestBody("text/plain".toMediaTypeOrNull())
                val methodPart = "PUT".toRequestBody("text/plain".toMediaTypeOrNull())

                var fotoPart: MultipartBody.Part? = null
                selectedImageUri?.let { uri ->
                    val file = uriToFile(uri)
                    if (file != null) {
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        fotoPart = MultipartBody.Part.createFormData("foto", file.name, requestFile)
                    }
                }

                val response = RetrofitClient.instance.actualizarPerfil(
                    "Bearer $token",
                    userId,
                    methodPart,
                    nombrePart,
                    emailPart,
                    fotoPart
                )

                if (response.isSuccessful) {
                    prefs.edit().apply {
                        putString("user_name", nuevoNombre)
                        putString("user_email", nuevoEmail)
                        apply()
                    }
                    Toast.makeText(this@EditarUsuarioActivity, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("EditarUsuario", "Error API: $errorMsg")
                    Toast.makeText(this@EditarUsuarioActivity, "Error al actualizar", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("EditarUsuario", "Excepción: ${e.message}")
                Toast.makeText(this@EditarUsuarioActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        val fileName = getFileName(uri) ?: "temp_image"
        val tempFile = File(cacheDir, fileName)
        
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }
}
