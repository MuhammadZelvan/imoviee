package com.zelvan.imoviee

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
// Hapus import FacebookAuthProvider jika tidak digunakan
// import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore // Import Firestore
import com.zelvan.imoviee.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var db: FirebaseFirestore // Tambahkan variabel untuk Firestore

    // Hapus variabel yang tidak terpakai jika Phone Auth belum diimplementasikan sepenuhnya
    // private var storedVerificationId: String = ""
    // private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()
        // Inisialisasi Firestore
        db = FirebaseFirestore.getInstance()

        // Inisialisasi Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        Log.d("MainActivity", "GoogleSignInClient initialized")

        // Listener untuk tombol login email/password
        binding.btnLogin.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString()
            val password = binding.editTextTextPassword.text.toString()
            Log.d("MainActivity", "Email/Password login clicked: $email")
            signIn(email, password)
        }

        // Listener untuk signup
        binding.textsignup.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        // Listener untuk Google login
        binding.btnLoginGoogle.setOnClickListener {
            signInWithGoogle()
        }

        // Listener untuk tombol login Telp (jika ada)
        binding.btnLoginTelp.setOnClickListener {
            val intent = Intent(this, PhoneNum::class.java)
            startActivity(intent)
        }
    }

    private fun signIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password ga boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity", "Email/Password login successful")
                    navigateToLayoutApp() // Panggil fungsi navigasi
                } else {
                    Log.e("MainActivity", "Email/Password login failed", task.exception)
                    Toast.makeText(this, "Username atau Password salah", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity", "Google login successful")
                    val firebaseUser: FirebaseUser? = auth.currentUser // Pengguna Firebase saat ini

                    firebaseUser?.let { user ->
                        // Cek apakah pengguna baru atau sudah ada di Firestore
                        db.collection("users").document(user.uid).get()
                            .addOnSuccessListener { document ->
                                if (!document.exists()) {
                                    // Pengguna baru, buat dokumen di Firestore
                                    Log.d("MainActivity", "New Google user, creating document in Firestore")
                                    val userDocData = hashMapOf(
                                        "email" to user.email,
                                        "username" to user.displayName, // Ambil nama dari profil Google
                                        "watchlist" to listOf<String>() // Watchlist kosong saat pertama dibuat
                                        // Anda bisa menambahkan field lain seperti photoUrl: user.photoUrl?.toString()
                                    )

                                    db.collection("users").document(user.uid)
                                        .set(userDocData)
                                        .addOnSuccessListener {
                                            Log.d("MainActivity", "New Google user document created in Firestore")
                                            navigateToLayoutApp()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("MainActivity", "Error creating Google user document in Firestore", e)
                                            Toast.makeText(this, "Login successful, but failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
                                            navigateToLayoutApp() // Tetap navigasi meskipun Firestore gagal
                                        }
                                } else {
                                    // Pengguna lama, langsung navigasi
                                    Log.d("MainActivity", "Existing Google user signed in, document found in Firestore")
                                    navigateToLayoutApp()
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("MainActivity", "Error checking Firestore for Google user", e)
                                Toast.makeText(this, "Login successful, but error checking user data: ${e.message}", Toast.LENGTH_LONG).show()
                                navigateToLayoutApp() // Tetap navigasi meskipun ada error saat cek Firestore
                            }
                    }
                } else {
                    Log.e("MainActivity", "Google login failed", task.exception)
                    Toast.makeText(this, "Google login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("MainActivity", "User already logged in: ${currentUser.email}")
            navigateToLayoutApp() // Panggil fungsi navigasi
        } else {
            Log.d("MainActivity", "No user logged in")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d("MainActivity", "Google Sign-In success, idToken: ${account.idToken}")
                if (account.idToken != null) {
                    firebaseAuthWithGoogle(account.idToken!!)
                } else {
                    Log.e("MainActivity", "Google Sign-In idToken is null")
                    Toast.makeText(this, "Google login gagal: Tidak mendapatkan token.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.e("MainActivity", "Google Sign-In failed", e)
                Toast.makeText(this, "Google login gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi helper untuk navigasi
    private fun navigateToLayoutApp() {
        val intent = Intent(this, LayoutApp::class.java)
        startActivity(intent)
        finish() // Tutup MainActivity agar tidak bisa kembali ke halaman login
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}