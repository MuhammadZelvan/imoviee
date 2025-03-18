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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.zelvan.imoviee.databinding.ActivityRegisterBinding

class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()

        binding.btnSignUp.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString()
            val password = binding.editTextTextPassword.text.toString()
            val firstname = binding.editTextTextFirstName.text.toString()
            val lastname = binding.editTextTextLastName.text.toString()

            val fullName = "$firstname $lastname"

            if (email.isBlank() || password.isBlank() || firstname.isBlank() || lastname.isBlank()) {
                Toast.makeText(this, "Field should be filled!", Toast.LENGTH_SHORT).show()
            } else {
                createAccount(email, password, fullName)
            }
        }

        val logIn = findViewById<TextView>(R.id.textLogIn)
        logIn.setOnClickListener {
            finish()
        }

    }

    private fun createAccount(email: String, password: String, fullname: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user: FirebaseUser? = auth.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullname)
                    .build()

                user?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            val intent = Intent(this, AccCreated::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Gagal update profile: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // Gagal membuat akun, tampilkan pesan error
                Toast.makeText(this, "Authentication failed: ${task.exception?.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}