package com.zelvan.imoviee

import android.os.Bundle
import android.widget.ArrayAdapter // Import ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.view.View // Untuk OnItemSelectedListener
import android.widget.AdapterView // Untuk OnItemSelectedListener

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.zelvan.imoviee.data.Film
import com.zelvan.imoviee.data.Genre
import com.zelvan.imoviee.databinding.ActivitySuperFilmInputBinding

class SuperFilmInput : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val availableGenres = mutableListOf<Genre>() // Untuk menyimpan semua genre yang tersedia
    private var selectedGenrePath: String = "" // Untuk menyimpan path genre yang dipilih film ini
    private val previewUrls = mutableListOf<String>()

    private lateinit var binding: ActivitySuperFilmInputBinding
    private lateinit var genreSpinnerAdapter: ArrayAdapter<String> // Adapter untuk Spinner Genre

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySuperFilmInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi Spinner Adapter (awal kosong)
        genreSpinnerAdapter = ArrayAdapter(this, R.layout.spinner_item_custom, mutableListOf("Loading Genres..."))
        genreSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom) // Tampilan dropdown
        binding.spinnerGenre.adapter = genreSpinnerAdapter

        // Set Listener untuk Spinner
        binding.spinnerGenre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Jangan lakukan apa-apa jika "Loading Genres..." terpilih atau jika availableGenres kosong
                if (position > 0 && availableGenres.isNotEmpty()) { // index 0 adalah "Select Genre"
                    val selectedGenre = availableGenres[position - 1] // -1 karena ada item "Select Genre" di awal
                    selectedGenrePath = "genres/${selectedGenre.id}"
                } else {
                    selectedGenrePath = "" // Reset jika item pertama atau tidak valid
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedGenrePath = "" // Tidak ada yang dipilih
            }
        }

        binding.buttonAddPreviewUrl.setOnClickListener {
            addPreviewUrl()
        }

        binding.backButton.setOnClickListener {
            // This will finish the current activity and go back to the previous one in the stack
            onBackPressedDispatcher.onBackPressed() // Recommended way
            // Alternatively, you could use:
            // finish()
        }

        binding.buttonSubmit.setOnClickListener {
            saveFilmToFirestore()
        }

        fetchGenres()
    }

    private fun fetchGenres() {
        db.collection("genres")
            .get()
            .addOnSuccessListener { result ->
                availableGenres.clear()
                val genreNamesForSpinner = mutableListOf("Select Genre") // Tambahkan placeholder di awal
                for (document in result) {
                    val genre = document.toObject(Genre::class.java).copy(id = document.id)
                    availableGenres.add(genre)
                    genreNamesForSpinner.add(genre.name) // Tambahkan nama genre ke list untuk spinner
                }
                genreSpinnerAdapter.clear()
                genreSpinnerAdapter.addAll(genreNamesForSpinner)
                genreSpinnerAdapter.notifyDataSetChanged()

                // Jika sedang dalam mode edit film, set seleksi spinner di sini
                // Misalnya: if (isEditing) setSpinnerSelectionForFilm(existingFilmData.genreRef)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching genres: ${exception.message}", Toast.LENGTH_SHORT).show()
                genreSpinnerAdapter.clear()
                genreSpinnerAdapter.add("Error loading genres")
                genreSpinnerAdapter.notifyDataSetChanged()
            }
    }

    // Fungsi ini diganti dengan Spinner, jadi tidak perlu dialog lagi
    // private fun showGenreSelectionDialog() { /* ... */ }
    // private fun updateSelectedGenreUI() { /* ... */ } // Tidak lagi dibutuhkan karena Spinner mengelola tampilannya

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

        // Validasi Spinner Genre
        if (selectedGenrePath.isEmpty()) { // Jika item "Select Genre" atau belum memilih
            Toast.makeText(this, "Please select a genre", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi input lainnya
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

        val newFilm = Film(
            title = title,
            genre = selectedGenrePath, // Gunakan path genre yang dipilih dari Spinner
            synopsis = synopsis,
            rating = rating,
            views = views,
            coverPortrait = coverPortrait,
            coverLandscape = coverLandscape,
            preview = previewUrls.toList()
        )

        db.collection("films")
            .add(newFilm)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Film '${newFilm.title}' added successfully!", Toast.LENGTH_SHORT).show()
                clearForm()

                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding film: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun clearForm() {
        binding.editTextTitle.setText("")
        binding.editTextSynopsis.setText("")
        binding.editTextRating.setText("")
        binding.editTextviews.setText("")
        binding.editTextCoverPortrait.setText("")
        binding.editTextCoverLandscape.setText("")
        binding.editTextPreviewUrl.setText("")

        // Reset Spinner
        binding.spinnerGenre.setSelection(0) // Pilih item pertama ("Select Genre")
        selectedGenrePath = ""

        previewUrls.clear()
        updatePreviewUrlsUI()
    }
}