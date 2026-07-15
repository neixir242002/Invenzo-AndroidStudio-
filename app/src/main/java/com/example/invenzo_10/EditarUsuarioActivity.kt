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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
    private lateinit var layoutPhoto: View
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
        setupRolesSpinner()
        cargarDatos()
        setupListeners()
    }

    private fun initViews() {
        imgProfile = findViewById(R.id.imgProfile)
        layoutPhoto = findViewById(R.id.layoutPhoto)
        etNombre = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        spinnerRol = findViewById(R.id.etRolUsuario) 
        btnSave = findViewById(R.id.btnSave)
    }

    private fun setupRolesSpinner() {
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
            
            val rolRaw = intent.getStringExtra("user_role")?.lowercase() ?: "auxiliar"
            val rolVisual = when {
                rolRaw.contains("principal") -> "Administrador Principal"
                rolRaw == "administrador" -> "Administrador"
                else -> "Auxiliar"
            }
            spinnerRol.setText(rolVisual, false)
            cargarImagen(intent.getStringExtra("user_photo"))
        } else {
            val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
            editingUserId = prefs.getInt("user_id", -1)
            etNombre.setText(prefs.getString("user_name", ""))
            etEmail.setText(prefs.getString("user_email", ""))
            spinnerRol.setText(prefs.getString("user_role", "Administrador Principal"), false)
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
        val nuevoRolVisual = spinnerRol.text.toString()

        val nuevoRolApi = when (nuevoRolVisual) {
            "Administrador Principal" -> "administrador_principal"
            "Administrador" -> "administrador"
            else -> "auxiliar"
        }

        if (nuevoNombre.isEmpty() || nuevoEmail.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val responseDatos = if (isEditingOther) {

                    RetrofitClient.instance.actualizarUsuario(

                        "Bearer $token",

                        editingUserId,

                        ProfileUpdateRequest(

                            nombre = nuevoNombre,

                            email = nuevoEmail,

                            rol = nuevoRolApi

                        )

                    )

                } else {

                    RetrofitClient.instance.actualizarPerfil(

                        "Bearer $token",

                        ProfileUpdateRequest(

                            nombre = nuevoNombre,

                            email = nuevoEmail,

                            rol = null

                        )

                    )

                }
                if (!responseDatos.isSuccessful) {
                    val code = responseDatos.code()
                    val error = responseDatos.errorBody()?.string()
                    Log.e("EDITAR_USUARIO", "HTTP $code -> $error")
                    Toast.makeText(
                        this@EditarUsuarioActivity,
                        "HTTP $code: ${error ?: "sin detalle"}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val userActualizado = responseDatos.body()?.user
                if (userActualizado != null) {
                    prefs.edit().apply {
                        putString("user_name", userActualizado.nombre)
                        putString("user_email", userActualizado.email)
                        putString("user_role", nuevoRolVisual)
                        putString("user_photo", userActualizado.foto ?: "")
                        apply()
                    }
                }

                selectedImageUri?.let { uri ->
                    val file = uriToFile(uri)
                    if (file != null) {
                        val contentType = contentResolver.getType(uri) ?: "image/jpeg"
                        val requestFile = file.asRequestBody(contentType.toMediaTypeOrNull())
                        val fotoPart = MultipartBody.Part.createFormData("foto", file.name, requestFile)

                        val responseFoto = RetrofitClient.instance.uploadPhoto(
                            "Bearer $token",
                            fotoPart
                        )

                        if (!responseFoto.isSuccessful) {
                            val error = responseFoto.errorBody()?.string()
                            Log.e("EDITAR_USUARIO", "Error foto: $error")
                            Toast.makeText(this@EditarUsuarioActivity, "Datos guardados, pero falló la foto: $error", Toast.LENGTH_LONG).show()
                            return@launch
                        }

                        responseFoto.body()?.user?.let { userFoto ->
                            prefs.edit().putString("user_photo", userFoto.foto ?: "").apply()
                        }
                    }
                }

                Toast.makeText(this@EditarUsuarioActivity, "Usuario actualizado con éxito", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e("EDITAR_USUARIO", "Exception", e)
                Toast.makeText(this@EditarUsuarioActivity, e.message ?: "Error desconocido", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun uriToFile(uri: Uri): File? {
        val fileName = getFileName(uri) ?: "profile_update_${System.currentTimeMillis()}.jpg"
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
