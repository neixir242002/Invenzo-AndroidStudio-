package com.example.invenzo_10

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText

class RestablecerPasswordActivity : AppCompatActivity() {

    private lateinit var etNewPassword: TextInputEditText
    private lateinit var tvReqMinChars: TextView
    private lateinit var tvReqNumber: TextView
    private lateinit var tvReqSpecial: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restablecer_password)

        etNewPassword = findViewById(R.id.etNewPassword)
        tvReqMinChars = findViewById(R.id.tvReqMinChars)
        tvReqNumber = findViewById(R.id.tvReqNumber)
        tvReqSpecial = findViewById(R.id.tvReqSpecial)

        // Using btnCancel instead of btnBack if btnBack is missing in XML
        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            finish()
        }

        // Optional: If you add btnBack to XML later, this won't crash
        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        etNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()
                updateRequirements(password)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateRequirements(password: String) {
        // 1. Min 8 characters
        updateStatus(password.length >= 8, tvReqMinChars)

        // 2. At least one number
        updateStatus(password.any { it.isDigit() }, tvReqNumber)

        // 3. Special character
        val specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?"
        updateStatus(password.any { it in specialChars }, tvReqSpecial)
    }

    private fun updateStatus(isSatisfied: Boolean, text: TextView) {
        val color = if (isSatisfied) {
            ContextCompat.getColor(this, R.color.successColor)
        } else {
            ContextCompat.getColor(this, R.color.textSecondary)
        }
        text.setTextColor(color)
    }
}
