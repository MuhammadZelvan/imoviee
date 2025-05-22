package com.zelvan.imoviee

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.TextView
import android.widget.Toast

class SuperCatInput : AppCompatActivity() {

    private lateinit var editTextGenreName: TextInputEditText
    private lateinit var buttonSubmit: Button

    // Inisialisasi Firestore
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_super_cat_input)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Ambil referensi View dari layout
        editTextGenreName = findViewById(R.id.editTextTitle) // ID di layout kamu namanya editTextTitle
        buttonSubmit = findViewById(R.id.buttonSubmit)

        // Setup Toolbar title (opsional, sesuai layout kamu)
        val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
        toolbarTitle.text = "Create Genre" // Set judul toolbar

        // Setup Button Submit Click Listener
        buttonSubmit.setOnClickListener {
            saveGenreToFirestore() // Panggil fungsi buat simpan data
        }

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            // This will finish the current activity and go back to the previous one in the stack
            onBackPressedDispatcher.onBackPressed() // Recommended way
            // Alternatively, you could use:
            // finish()
        }
    }
    private fun saveGenreToFirestore() {
        // Ambil teks dari input field nama genre
        val genreName = editTextGenreName.text.toString().trim() // Trim buat hapus spasi di awal/akhir

        // Validasi input (pastikan nggak kosong)
        if (genreName.isEmpty()) {
            editTextGenreName.error = "Genre name cannot be empty" // Tampilkan error di input field
            return // Berhenti di sini kalo input kosong
        }

        // Buat data genre dalam bentuk Map (karena cuma 1 field, Map lebih simpel)
        val genreData = hashMapOf(
            "name" to genreName
            // Firestore akan otomatis nambahin field ID dokumen
        )

        // Dapatkan referensi ke collection "genres" di Firestore
        val genresCollection = db.collection("genres") // Ganti "genres" kalo nama collection kamu beda

        // Tambahkan dokumen baru ke collection
        genresCollection
            .add(genreData) // .add() akan otomatis generate ID dokumen baru
            .addOnSuccessListener { documentReference ->
                // Berhasil menyimpan data
                Toast.makeText(this, "Genre added with ID: ${documentReference.id}", Toast.LENGTH_SHORT).show()

                // Opsional: Bersihkan input field setelah berhasil
                editTextGenreName.text?.clear()

                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                // Gagal menyimpan data
                Toast.makeText(this, "Error adding genre: ${e.message}", Toast.LENGTH_SHORT).show()
                // Log error untuk debugging
                // Log.w("FirestoreError", "Error adding document", e)
            }
    }
}