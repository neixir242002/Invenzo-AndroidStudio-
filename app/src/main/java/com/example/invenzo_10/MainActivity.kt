package com.example.invenzo_10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)



        val inputCorreo = findViewById<TextInputLayout>(
            R.id.inputCorreo
        )


        val inputPassword = findViewById<TextInputLayout>(
            R.id.inputPassword
        )


        val btnLogin = findViewById<Button>(
            R.id.inicioSecion
        )


        val tvRegister = findViewById<TextView>(
            R.id.register
        )



        btnLogin.setOnClickListener {



            val correo =
                inputCorreo.editText?.text.toString().trim()


            val password =
                inputPassword.editText?.text.toString().trim()



            // VALIDAR CAMPOS VACÍOS

            if(correo.isEmpty() || password.isEmpty()){


                Toast.makeText(
                    this,
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT
                ).show()


                return@setOnClickListener

            }




            // OBTENER DATOS REGISTRADOS

            val datos = getSharedPreferences(
                "usuario_prueba",
                Context.MODE_PRIVATE
            )


            val correoGuardado =
                datos.getString("correo","")


            val passwordGuardada =
                datos.getString("password","")





            // COMPARAR DATOS


            if(
                correo == correoGuardado &&
                password == passwordGuardada
            ){



                Toast.makeText(
                    this,
                    "Bienvenido",
                    Toast.LENGTH_SHORT
                ).show()



                val intent = Intent(
                    this,
                    ActivityInicio::class.java
                )


                startActivity(intent)

                finish()



            }else{


                Toast.makeText(
                    this,
                    "Correo o contraseña incorrectos",
                    Toast.LENGTH_SHORT
                ).show()


            }



        }




        // IR A REGISTRO


        tvRegister.setOnClickListener {


            val intent = Intent(
                this,
                RegistroActivity::class.java
            )


            startActivity(intent)


        }



    }



//    private fun setupClickListeners() {
//        // IDs corregidos según tu XML original
//        val btnInicio: Button = findViewById(R.id.inicioSecion)
//
//        // CAMBIO AQUÍ: El ID en tu XML era textView4, no "register"
//        val tvRegister: TextView = findViewById(R.id.register)
//
//        val inputCorreo: TextInputLayout = findViewById(R.id.inputCorreo)
//        val inputPass: TextInputLayout = findViewById(R.id.inputPassword)
//
//        btnInicio.setOnClickListener {
//            val email = inputCorreo.editText?.text.toString().trim()
//            val pass = inputPass.editText?.text.toString().trim()
//
//            if (email.isNotEmpty() && pass.isNotEmpty()) {
//                ejecutarLogin(email, pass)
//            } else {
//                Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        // Al hacer clic en "Regístrate" (textView4)
//        tvRegister.setOnClickListener {
//            navigateTo(RegistroActivity::class.java)
//        }
//    }


//    private fun ejecutarLogin(email: String, pass: String) {
////        val request = LoginRequest(email, pass)
////
////        RetrofitClient.instance.login(request).enqueue(object : Callback<LoginResponse> {
////            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
////                if (response.isSuccessful) {
////                    val token = response.body()?.accessToken
////                    Toast.makeText(this@MainActivity, "Bienvenido", Toast.LENGTH_SHORT).show()
////                    navigateTo(ActivityInicio::class.java, true)
////                } else {
////                    Toast.makeText(this@MainActivity, "Error: Credenciales inválidas", Toast.LENGTH_SHORT).show()
////                }
////            }
////
////            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
////                Toast.makeText(this@MainActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
////            }
////        })
//    }

    private fun navigateTo(destination: Class<*>, finishCurrent: Boolean = false) {
        val intent = Intent(this, destination)
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        if (finishCurrent) finish()
    }
}