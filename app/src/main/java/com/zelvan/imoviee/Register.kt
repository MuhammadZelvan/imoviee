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
import com.google.firebase.firestore.FirebaseFirestore
import com.zelvan.imoviee.databinding.ActivityRegisterBinding

class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
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
        db = FirebaseFirestore.getInstance()

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
                    ?.addOnCompleteListener { updateProfileTask -> // Ganti nama task agar tidak sama
                        if (updateProfileTask.isSuccessful) {
                            // --- BAGIAN BARU: BUAT DOKUMEN USER DI FIRESTORE ---
                            user?.let { firebaseUser -> // Pastikan firebaseUser tidak null
                                val userDocData = hashMapOf(
                                    "email" to firebaseUser.email,
                                    "username" to fullname, // Menggunakan fullname sebagai username
                                    "watchlist" to listOf<String>(), // Watchlist kosong saat pertama dibuat
                                )

                                db.collection("users").document(firebaseUser.uid)
                                    .set(userDocData) // Gunakan .set() untuk membuat atau menimpa dokumen
                                    .addOnSuccessListener {
                                        // Sukses membuat dokumen user di Firestore
                                        Toast.makeText(this, "Account created and user data saved!", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, AccCreated::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        // Gagal membuat dokumen user di Firestore, tapi akun Auth sudah dibuat
                                        Toast.makeText(this, "Account created, but failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
                                        // Tetap navigasi meskipun Firestore gagal, atau berikan opsi lain
                                        val intent = Intent(this, AccCreated::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                            } ?: run {
                                // Jika user object somehow null setelah registrasi berhasil
                                Toast.makeText(this, "User object is null after successful registration.", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, AccCreated::class.java) // Tetap navigasi
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            // Gagal update profile di Firebase Auth
                            Toast.makeText(this, "Gagal update profile: ${updateProfileTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            // Karena gagal update profile, tidak lanjut ke Firestore
                        }
                    }
            } else {
                // Gagal membuat akun di Firebase Auth
                Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }}