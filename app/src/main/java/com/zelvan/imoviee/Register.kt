package com.zelvan.imoviee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val edtxtEmail = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val edtxtPassword = findViewById<EditText>(R.id.editTextTextPassword)
        val edtxtFirstname = findViewById<EditText>(R.id.editTextTextFirstName)
        val edtxtLastname = findViewById<EditText>(R.id.editTextTextLastName)

        val btnSignup = findViewById<Button>(R.id.btnSignUp)

        btnSignup.setOnClickListener {
            val email = edtxtEmail.text.toString()
            val password = edtxtPassword.text.toString()
            val firstname = edtxtFirstname.text.toString()
            val lastname = edtxtLastname.text.toString()

            if (email.isBlank() || password.isBlank() || firstname.isBlank() || lastname.isBlank()) {
                Toast.makeText(this, "Field should be filled!", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, AccCreated::class.java)
                startActivity(intent)
            }
        }

        val logIn = findViewById<TextView>(R.id.textLogIn)
        logIn.setOnClickListener {
            finish()
        }

    }
}