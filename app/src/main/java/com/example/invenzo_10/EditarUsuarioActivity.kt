package com.example.invenzo_10

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class EditarUsuarioActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var layoutPhoto: View
    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var tilRol: TextInputLayout
    private lateinit var spinnerRol: AutoCompleteTextView
    private lateinit var btnSave: MaterialButton
    
    private var selectedImageUri: Uri? = null
    private var editingUserId: Int = -1
    private var isEditingOther: Boolean = false
    private var canEditRol: Boolean = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(imgProfile)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editarusuario)

        initViews()
        cargarDatos()
        setupListeners()
    }

    private fun initViews() {
        imgProfile = findViewById(R.id.imgProfile)
        layoutPhoto = findViewById(R.id.layoutPhoto)
        etNombre = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        tilRol = findViewById(R.id.tilRol)
        spinnerRol = findViewById(R.id.etRolUsuario) 
        btnSave = findViewById(R.id.btnSave)
    }

    private fun setupRolesSpinner() {
        val roles = arrayOf("Administrador Principal", "Administrador", "Auxiliar")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        spinnerRol.setAdapter(adapter)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun cargarDatos() {
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val currentLoggedInRole = prefs.getString("user_role", "")
        
        isEditingOther = intent.getBooleanExtra("is_editing_other", false)

        // RESTRICCIÓN: 
        // 1. Nadie puede editar su propio rol (!isEditingOther)
        // 2. Solo el Administrador Principal puede editar el rol de otros usuarios
        canEditRol = isEditingOther && currentLoggedInRole == "Administrador Principal"

        if (!canEditRol) {
            val msjRestriccion = "NO tienes permiso para cambiarte de rol"
            
            spinnerRol.apply {
                keyListener = null
                isFocusable = false
                isFocusableInTouchMode = false
                isClickable = true
                setAdapter(null)
                
                // Respuesta inmediata al toque
                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        Toast.makeText(this@EditarUsuarioActivity, msjRestriccion, Toast.LENGTH_SHORT).show()
                    }
                    true
                }
            }
            
            tilRol.apply {
                endIconMode = TextInputLayout.END_ICON_NONE
                isClickable = true
                setOnClickListener {
                    Toast.makeText(this@EditarUsuarioActivity, msjRestriccion, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            setupRolesSpinner()
        }
        
        if (isEditingOther) {
            editingUserId = intent.getIntExtra("user_id", -1)
            etNombre.setText(intent.getStringExtra("user_name"))
            etEmail.setText(intent.getStringExtra("user_email"))
            
            val rolRaw = intent.getStringExtra("user_role")?.lowercase() ?: "auxiliar"
            val rolVisual = when {
                rolRaw.contains("principal") -> "Administrador Principal"
                rolRaw == "administrador" -> "Administrador"
                else -> "Auxiliar"
            }
            spinnerRol.setText(rolVisual, false)
            cargarImagen(intent.getStringExtra("user_photo"))
        } else {
            editingUserId = prefs.getInt("user_id", -1)
            etNombre.setText(prefs.getString("user_name", ""))
            etEmail.setText(prefs.getString("user_email", ""))
            val miRol = prefs.getString("user_role", "Auxiliar")
            spinnerRol.setText(miRol, false)
            cargarImagen(prefs.getString("user_photo", ""))
        }
    }

    private fun cargarImagen(path: String?) {
        if (path.isNullOrEmpty()) {
            imgProfile.setImageResource(R.drawable.ic_profile)
            return
        }

        val fullUrl = RetrofitClient.obtenerUrlRealtime(path)
        if (fullUrl != null) {
            Glide.with(this)
                .load(fullUrl)
                .signature(com.bumptech.glide.signature.ObjectKey(System.currentTimeMillis().toString()))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(imgProfile)
        } else {
            imgProfile.setImageResource(R.drawable.ic_profile)
        }
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        layoutPhoto.setOnClickListener { pickImageLauncher.launch("image/*") }
        btnSave.setOnClickListener { guardarCambios() }
    }

    private fun guardarCambios() {
        val nuevoNombre = etNombre.text.toString().trim()
        val nuevoEmail = etEmail.text.toString().trim()
        
        if (nuevoNombre.isEmpty() || nuevoEmail.isEmpty()) {
            Toast.makeText(this, "Nombre y Email son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        // Solo enviamos el rol si se permitió su edición
        val nuevoRolApi = if (canEditRol) {
            when (spinnerRol.text.toString()) {
                "Administrador Principal" -> "administrador_principal"
                "Administrador" -> "administrador"
                else -> "auxiliar"
            }
        } else null

        lifecycleScope.launch {
            try {
                val request = ProfileUpdateRequest(
                    nombre = nuevoNombre,
                    email = nuevoEmail,
                    rol = nuevoRolApi
                )

                val response = if (isEditingOther) {
                    RetrofitClient.instance.actualizarUsuario("Bearer $token", editingUserId, request)
                } else {
                    RetrofitClient.instance.actualizarPerfil("Bearer $token", request)
                }
                
                if (response.isSuccessful) {
                    val userActualizado = response.body()?.user
                    if (userActualizado != null && !isEditingOther) {
                        prefs.edit().apply {
                            putString("user_name", userActualizado.nombre)
                            putString("user_email", userActualizado.email)
                            // Actualizamos el rol localmente solo si se permitió cambiarlo
                            if (nuevoRolApi != null) putString("user_role", spinnerRol.text.toString())
                            putString("user_photo", userActualizado.foto ?: "")
                            apply()
                        }
                    }

                    subirFotoSiExiste(token, prefs)

                    Toast.makeText(this@EditarUsuarioActivity, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e("EDITAR_USUARIO", "Error: ${response.code()}")
                    Toast.makeText(this@EditarUsuarioActivity, "Error al guardar cambios", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("EDITAR_USUARIO", "Excepción: ${e.message}")
                Toast.makeText(this@EditarUsuarioActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun subirFotoSiExiste(token: String, prefs: android.content.SharedPreferences) {
        selectedImageUri?.let { uri ->
            lifecycleScope.launch {
                val file = uriToFile(uri)
                if (file != null) {
                    val contentType = contentResolver.getType(uri) ?: "image/jpeg"
                    val requestFile = file.asRequestBody(contentType.toMediaTypeOrNull())
                    val fotoPart = MultipartBody.Part.createFormData("foto", file.name, requestFile)

                    val responseFoto = RetrofitClient.instance.uploadPhoto("Bearer $token", fotoPart)
                    if (responseFoto.isSuccessful) {
                        responseFoto.body()?.user?.let { userFoto ->
                            if (!isEditingOther) {
                                prefs.edit().putString("user_photo", userFoto.foto ?: "").apply()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        val fileName = getFileName(uri) ?: "profile_${System.currentTimeMillis()}.jpg"
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
