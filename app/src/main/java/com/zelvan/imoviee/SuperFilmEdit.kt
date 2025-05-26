package com.zelvan.imoviee

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.zelvan.imoviee.data.Film // Pastikan import data class Film
import com.zelvan.imoviee.data.Genre
import com.zelvan.imoviee.databinding.ActivitySuperFilmEditBinding

class SuperFilmEdit : AppCompatActivity() {
    private lateinit var binding: ActivitySuperFilmEditBinding
    private lateinit var filmId: String
    private lateinit var firestore: FirebaseFirestore

    private val previewUrls = mutableListOf<String>() // List untuk menampung URL preview saat diedit
    private var selectedGenrePath: String = "" // String untuk menampung path genre yang terpilih
    private val availableGenres = mutableListOf<Genre>() // List semua genre yang tersedia

    private lateinit var genreSpinnerAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySuperFilmEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()
        filmId = intent.getStringExtra("filmId") ?: ""

        // Inisialisasi Spinner Adapter (awal kosong / placeholder)
        genreSpinnerAdapter = ArrayAdapter(this, R.layout.spinner_item_custom, mutableListOf("Loading Genres..."))
        genreSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom)
        binding.spinnerGenre.adapter = genreSpinnerAdapter

        // Set Listener untuk Spinner
        binding.spinnerGenre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0 && availableGenres.isNotEmpty()) { // index 0 adalah "Select Genre"
                    val selectedGenre = availableGenres[position - 1]
                    selectedGenrePath = "genres/${selectedGenre.id}"
                } else {
                    selectedGenrePath = "" // Reset jika item pertama atau tidak valid
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedGenrePath = "" // Tidak ada yang dipilih
            }
        }

        // Listener untuk tombol Add Preview URL
        binding.buttonAddPreviewUrl.setOnClickListener {
            addPreviewUrl()
        }

        // Listener untuk tombol Submit (Update Film)
        binding.buttonSubmit.setOnClickListener {
            saveFilmToFirestore()
        }

        // Listener untuk tombol Hapus Film
        binding.btnHapus.setOnClickListener {
            deleteFilm()
        }

        // Listener untuk tombol Back
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Panggil fetchGenres() terlebih dahulu karena ini data dasar untuk Spinner
        fetchGenres()
        // ambilData() dipanggil setelah fetchGenres Selesai agar availableGenres terisi
        // Ini akan kita panggil di dalam callback fetchGenres() atau di fungsi terpisah setelah keduanya siap
    }

    private fun fetchGenres() {
        firestore.collection("genres")
            .get()
            .addOnSuccessListener { result ->
                availableGenres.clear()
                val genreNamesForSpinner = mutableListOf("Select Genre") // Tambahkan placeholder di awal
                for (document in result) {
                    val genre = document.toObject(Genre::class.java).copy(id = document.id)
                    availableGenres.add(genre)
                    genreNamesForSpinner.add(genre.name)
                }
                genreSpinnerAdapter.clear()
                genreSpinnerAdapter.addAll(genreNamesForSpinner)
                genreSpinnerAdapter.notifyDataSetChanged()

                // Panggil ambilData() di sini setelah genre berhasil di-fetch
                // Ini penting agar Spinner bisa diatur seleksinya dengan benar
                ambilData()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching genres: ${exception.message}", Toast.LENGTH_SHORT).show()
                genreSpinnerAdapter.clear()
                genreSpinnerAdapter.add("Error loading genres")
                genreSpinnerAdapter.notifyDataSetChanged()
            }
    }

    private fun addPreviewUrl() {
        val url = binding.editTextPreviewUrl.text.toString().trim()
        if (url.isNotEmpty() && !previewUrls.contains(url)) {
            previewUrls.add(url)
            binding.editTextPreviewUrl.setText("")
            updatePreviewUrlsUI()
        } else if (url.isEmpty()) {
            Toast.makeText(this, "URL cannot be empty", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "URL already added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePreviewUrlsUI() {
        binding.linearLayoutPreviewUrls.removeAllViews()
        if (previewUrls.isEmpty()) {
            val noPreviewText = TextView(this).apply {
                text = "No preview URLs added"
                setTextColor(ContextCompat.getColor(context, R.color.white))
                setPadding(8, 4, 8, 4)
            }
            binding.linearLayoutPreviewUrls.addView(noPreviewText)
        } else {
            previewUrls.forEachIndexed { index, url ->
                val urlTextView = TextView(this).apply {
                    text = url
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                    setBackgroundResource(R.drawable.preview_url_background)
                    setPadding(8, 4, 8, 4)
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 0, 0, 8)
                    layoutParams = params

                    setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_menu_close_clear_cancel, 0)
                    compoundDrawablePadding = 8
                    setOnClickListener {
                        previewUrls.removeAt(index)
                        updatePreviewUrlsUI()
                    }
                }
                binding.linearLayoutPreviewUrls.addView(urlTextView)
            }
        }
    }

    private fun saveFilmToFirestore() {
        val title = binding.editTextTitle.text.toString().trim()
        val synopsis = binding.editTextSynopsis.text.toString().trim()
        val ratingString = binding.editTextRating.text.toString().trim()
        val viewsString = binding.editTextviews.text.toString().trim()
        val coverPortrait = binding.editTextCoverPortrait.text.toString().trim()
        val coverLandscape = binding.editTextCoverLandscape.text.toString().trim()

        if (selectedGenrePath.isEmpty()) {
            Toast.makeText(this, "Please select a genre", Toast.LENGTH_SHORT).show()
            return
        }

        if (title.isEmpty() || synopsis.isEmpty() || ratingString.isEmpty() || viewsString.isEmpty() ||
            coverPortrait.isEmpty() || coverLandscape.isEmpty() || previewUrls.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and add preview URLs", Toast.LENGTH_LONG).show()
            return
        }

        val rating = ratingString.toDoubleOrNull()
        val views = viewsString.toIntOrNull()

        if (rating == null) {
            Toast.makeText(this, "Invalid rating format. Please enter a number.", Toast.LENGTH_SHORT).show()
            return
        }
        if (views == null) {
            Toast.makeText(this, "Invalid views format. Please enter an integer.", Toast.LENGTH_SHORT).show()
            return
        }

        // Menggunakan mapOf untuk update, karena kita tidak mengubah semua field
        val updateData = mapOf(
            "title" to title,
            "synopsis" to synopsis,
            "rating" to rating,
            "views" to views,
            "coverPortrait" to coverPortrait,
            "coverLandscape" to coverLandscape,
            "preview" to previewUrls.toList(), // Menggunakan toList() agar immutable
            "genreRef" to selectedGenrePath // Pastikan field ini namanya "genreRef" di Film.kt
        )

        firestore.collection("films").document(filmId).update(updateData)
            .addOnSuccessListener {
                Toast.makeText(this, "Film changed successfully!", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke activity sebelumnya
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating film: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun ambilData() {
        firestore.collection("films").document(filmId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val film = doc.toObject(Film::class.java) // Konversi langsung ke objek Film
                    film?.let {
                        binding.editTextTitle.setText(it.title)
                        binding.editTextSynopsis.setText(it.synopsis)
                        binding.editTextRating.setText(it.rating.toString()) // Konversi Double ke String
                        binding.editTextviews.setText(it.views.toString())   // Konversi Int ke String
                        binding.editTextCoverPortrait.setText(it.coverPortrait)
                        binding.editTextCoverLandscape.setText(it.coverLandscape)

                        // --- Mengisi Genre Spinner ---
                        selectedGenrePath = it.genre // Ambil path genre dari film
                        // Cari indeks genre ini di `availableGenres` untuk setting Spinner
                        val genreIndex = availableGenres.indexOfFirst { genre ->
                            "genres/${genre.id}" == selectedGenrePath
                        }
                        // +1 karena ada placeholder "Select Genre" di index 0
                        if (genreIndex != -1) { // Jika genre ditemukan
                            binding.spinnerGenre.setSelection(genreIndex + 1)
                        } else {
                            binding.spinnerGenre.setSelection(0) // Default ke placeholder
                        }

                        // --- Mengisi Preview URLs ---
                        previewUrls.clear()
                        // Pastikan field "preview" di Firestore itu array of strings
                        // doc.toObject(Film::class.java) sudah handle ini jika fieldnya sama
                        it.preview.forEach { url ->
                            previewUrls.add(url)
                        }
                        updatePreviewUrlsUI() // Update UI untuk preview URLs
                    }
                } else {
                    Toast.makeText(this, "Film not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching film data: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun deleteFilm() {
        // Tambahkan konfirmasi dialog sebelum menghapus (disarankan)
        AlertDialog.Builder(this)
            .setTitle("Delete Film")
            .setMessage("Are you sure you want to delete this film?")
            .setPositiveButton("Yes") { dialog, which ->
                firestore.collection("films").document(filmId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Film deleted successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error deleting film: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }
}