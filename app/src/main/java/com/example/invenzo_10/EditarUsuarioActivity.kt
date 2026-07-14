package com.example.invenzo_10

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class EditarUsuarioActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var spinnerRol: AutoCompleteTextView
    private lateinit var btnSave: MaterialButton
    
    private var selectedImageUri: Uri? = null
    private var editingUserId: Int = -1
    private var isEditingOther: Boolean = false

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

        // Manejo de Insets para evitar que la TopBar choque con la barra de estado
        val topBar = findViewById<View>(R.id.topBar)
        ViewCompat.setOnApplyWindowInsetsListener(topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        initViews()
        setupRolesSpinner()
        cargarDatos()
        setupListeners()
    }

    private fun initViews() {
        etNombre = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        spinnerRol = findViewById(R.id.etRolUsuario) 
        btnSave = findViewById(R.id.btnSave)
    }

    private fun setupRolesSpinner() {
        // Incluimos los 3 roles para que el mapeo sea correcto
        val roles = arrayOf("Administrador Principal", "Administrador", "Auxiliar")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        spinnerRol.setAdapter(adapter)
    }

    private fun cargarDatos() {
        isEditingOther = intent.getBooleanExtra("is_editing_other", false)
        
        if (isEditingOther) {
            editingUserId = intent.getIntExtra("user_id", -1)
            etNombre.setText(intent.getStringExtra("user_name"))
            etEmail.setText(intent.getStringExtra("user_email"))
            
            // Normalizar el rol que viene del intent
            val rolRaw = intent.getStringExtra("user_role")?.lowercase() ?: "auxiliar"
            val rolVisual = when {
                rolRaw.contains("principal") -> "Administrador Principal"
                rolRaw == "administrador" -> "Administrador"
                else -> "Auxiliar"
            }
            spinnerRol.setText(rolVisual, false)
        } else {
            val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
            editingUserId = prefs.getInt("user_id", -1)
            etNombre.setText(prefs.getString("user_name", ""))
            etEmail.setText(prefs.getString("user_email", ""))
            val rol = prefs.getString("user_role", "Administrador Principal")
            spinnerRol.setText(rol, false)
        }
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        btnSave.setOnClickListener {
            guardarCambios()
        }
    }

    private fun guardarCambios() {
        val nuevoNombre = etNombre.text.toString().trim()
        val nuevoEmail = etEmail.text.toString().trim()
        val nuevoRolVisual = spinnerRol.text.toString()
        
        // Mapeo exacto para el servidor
        val nuevoRolApi = when (nuevoRolVisual) {
            "Administrador Principal" -> "administrador_principal"
            "Administrador" -> "administrador"
            else -> "auxiliar"
        }

        if (nuevoNombre.isEmpty() || nuevoEmail.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val nombrePart = nuevoNombre.toRequestBody("text/plain".toMediaTypeOrNull())
                val emailPart = nuevoEmail.toRequestBody("text/plain".toMediaTypeOrNull())
                val rolPart = nuevoRolApi.toRequestBody("text/plain".toMediaTypeOrNull())
                val methodPart = "PUT".toRequestBody("text/plain".toMediaTypeOrNull())

                var fotoPart: MultipartBody.Part? = null
                selectedImageUri?.let { uri ->
                    val file = uriToFile(uri)
                    file?.let {
                        val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                        fotoPart = MultipartBody.Part.createFormData("foto", it.name, requestFile)
                    }
                }

                val response = RetrofitClient.instance.actualizarPerfil(
                    "Bearer $token",
                    editingUserId,
                    methodPart,
                    nombrePart,
                    emailPart,
                    rolPart,
                    fotoPart
                )

                if (response.isSuccessful) {
                    if (!isEditingOther) {
                        // Si edito mi propio perfil, actualizo mis datos locales con el nombre visual exacto
                        prefs.edit().apply {
                            putString("user_name", nuevoNombre)
                            putString("user_email", nuevoEmail)
                            putString("user_role", nuevoRolVisual) // "Administrador" o "Administrador Principal"
                            apply()
                        }
                    }
                    Toast.makeText(this@EditarUsuarioActivity, "Cambios guardados", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditarUsuarioActivity, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditarUsuarioActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        val fileName = getFileName(uri) ?: "temp_image"
        val tempFile = File(cacheDir, fileName)
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output -> input.copyTo(output) }
            }
            tempFile
        } catch (e: Exception) { null }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) name = it.getString(index)
            }
        }
        return name
    }
}
