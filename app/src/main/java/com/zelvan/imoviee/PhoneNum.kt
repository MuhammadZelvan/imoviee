package com.zelvan.imoviee

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneNum : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var phoneNumberEditText: EditText
    private lateinit var sendCodeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_phone_num)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()

        // Mengambil referensi ke tombol kirim nomor telepon
        val sendCodeButton = findViewById<Button>(R.id.btnLogin)
        val phoneNumberEditText = findViewById<EditText>(R.id.editTextPhoneNumber)
        val backtologin = findViewById<TextView>(R.id.textlogin)

        sendCodeButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString()
            if (phoneNumber.isNotEmpty()) {
                startPhoneNumberVerification(phoneNumber)
            } else {
                Toast.makeText(this, "Masukkan nomor telepon", Toast.LENGTH_SHORT).show()
            }
        }

        backtologin.setOnClickListener {
            finish()
        }
    }
    private fun startPhoneNumberVerification(phoneNumber: String) {
        // Tampilkan loading
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Mengirim kode verifikasi...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    progressDialog.dismiss()
                    // Auto-verification tidak digunakan karena kita pindah ke activity lain
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    progressDialog.dismiss()
                    Log.d("PhoneNum", "Verifikasi gagal: ${e.message}")
                    Toast.makeText(this@PhoneNum,
                        "Verifikasi gagal: ${e.message}",
                        Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    progressDialog.dismiss()
                    // Pindah ke OTP Activity dengan membawa verificationId
                    val intent = Intent(this@PhoneNum, VerifyPhone::class.java).apply {
                        putExtra("VERIFICATION_ID", verificationId)
                        putExtra("PHONE_NUMBER", phoneNumber)
                    }
                    startActivity(intent)
                    finish()
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}