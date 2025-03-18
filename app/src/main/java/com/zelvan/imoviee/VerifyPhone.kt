package com.zelvan.imoviee

import android.app.ProgressDialog
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
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class VerifyPhone : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var verificationCodeEditText: EditText
    private lateinit var verifyButton: Button
    private lateinit var resendButton: TextView

    private var verificationId: String = ""
    private var phoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify_phone)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        // Ambil verificationId dari Intent
        verificationId = intent.getStringExtra("VERIFICATION_ID") ?: ""
        phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: ""

        if (verificationId.isEmpty()) {
            Toast.makeText(this, "Error: Tidak ada verification ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        verificationCodeEditText = findViewById(R.id.editTextOTP)
        verifyButton = findViewById(R.id.btnConfirm)
        resendButton = findViewById(R.id.textResend)

        verifyButton.setOnClickListener {
            val code = verificationCodeEditText.text.toString().trim()
            if (code.isNotEmpty()) {
                verifyPhoneNumberWithCode(code)
            } else {
                Toast.makeText(this, "Masukkan kode verifikasi", Toast.LENGTH_SHORT).show()
            }
        }

        resendButton.setOnClickListener {
            resendVerificationCode()
        }
    }

    private fun verifyPhoneNumberWithCode(code: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Memverifikasi kode...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    // Verifikasi berhasil
                    Toast.makeText(this, "Verifikasi berhasil", Toast.LENGTH_SHORT).show()

                    // Pindah ke Main Activity atau activity lainnya
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Verifikasi gagal
                    Toast.makeText(this, "Kode verifikasi salah", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun resendVerificationCode() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Mengirim ulang kode...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    progressDialog.dismiss()
                    // Auto-verification biasanya tidak digunakan di sini
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    progressDialog.dismiss()
                    Toast.makeText(this@VerifyPhone,
                        "Gagal mengirim ulang: ${e.message}",
                        Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(
                    newVerificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    progressDialog.dismiss()
                    verificationId = newVerificationId
                    Toast.makeText(this@VerifyPhone,
                        "Kode verifikasi telah dikirim ulang",
                        Toast.LENGTH_SHORT).show()
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}