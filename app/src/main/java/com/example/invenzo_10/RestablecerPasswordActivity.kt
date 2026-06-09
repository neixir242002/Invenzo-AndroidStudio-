package com.example.invenzo_10

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText

class RestablecerPasswordActivity : AppCompatActivity() {

    private lateinit var etNewPassword: TextInputEditText
    private lateinit var cardReqMinChars: MaterialCardView
    private lateinit var cardReqNumber: MaterialCardView
    private lateinit var cardReqSpecial: MaterialCardView
    private lateinit var tvReqMinChars: TextView
    private lateinit var tvReqNumber: TextView
    private lateinit var tvReqSpecial: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restablecer_password)

        etNewPassword = findViewById(R.id.etNewPassword)
        cardReqMinChars = findViewById(R.id.cardReqMinChars)
        cardReqNumber = findViewById(R.id.cardReqNumber)
        cardReqSpecial = findViewById(R.id.cardReqSpecial)
        tvReqMinChars = findViewById(R.id.tvReqMinChars)
        tvReqNumber = findViewById(R.id.tvReqNumber)
        tvReqSpecial = findViewById(R.id.tvReqSpecial)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
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
        updateStatus(password.length >= 8, cardReqMinChars, tvReqMinChars)

        // 2. At least one number
        updateStatus(password.any { it.isDigit() }, cardReqNumber, tvReqNumber)

        // 3. Special character
        val specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?"
        updateStatus(password.any { it in specialChars }, cardReqSpecial, tvReqSpecial)
    }

    private fun updateStatus(isSatisfied: Boolean, card: MaterialCardView, text: TextView) {
        val color = if (isSatisfied) {
            ContextCompat.getColor(this, R.color.successColor)
        } else {
            ContextCompat.getColor(this, R.color.textSecondary)
        }

        card.setCardBackgroundColor(ColorStateList.valueOf(color))
        text.setTextColor(color)
    }
}
