package com.zelvan.imoviee

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.zelvan.imoviee.data.Film
import com.zelvan.imoviee.databinding.ActivityInfoFilmBinding

class InfoFilm : AppCompatActivity() {
    private lateinit var binding: ActivityInfoFilmBinding // Declare binding variable
    private var isWatchlisted = false // Track watchlist status

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Keep this if you need edge-to-edge

        // Inflate the layout using View Binding
        binding = ActivityInfoFilmBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set the content view to the root of the binding

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets -> // Use binding.main
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get film ID from intent
        val filmId = intent.getStringExtra("FILM_DATA") // -1 is a default if not found

        // Access the "Watch Now" button using View Binding
        binding.btnwatchnow.setOnClickListener { // Assuming R.id.btnwatchnow is the ID
            val intent = Intent(this, Playing::class.java)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Check initial watchlist status
        checkWatchlistStatus()

        binding.btnaddtowatchlist.setOnClickListener {
            val currentFilmId = intent.getStringExtra("FILM_DATA")
            if (currentFilmId != null) {
                val filmDocumentReference = "films/$currentFilmId"
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val db = FirebaseFirestore.getInstance()
                    val userRef = db.collection("users").document(userId)

                    if (isWatchlisted) {
                        // Remove from watchlist
                        userRef.update("watchlist", FieldValue.arrayRemove(filmDocumentReference))
                            .addOnSuccessListener {
                                Toast.makeText(this, "Removed from watchlist", Toast.LENGTH_SHORT).show()
                                isWatchlisted = false
                                updateWatchlistButton()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to remove from watchlist: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.e("InfoFilm", "Error removing from watchlist", e)
                            }
                    } else {
                        // Add to watchlist
                        userRef.update("watchlist", FieldValue.arrayUnion(filmDocumentReference))
                            .addOnSuccessListener {
                                Toast.makeText(this, "Added to watchlist", Toast.LENGTH_SHORT).show()
                                isWatchlisted = true
                                updateWatchlistButton()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to add to watchlist: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.e("InfoFilm", "Error adding to watchlist", e)
                            }
                    }
                } else {
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Film ID not available", Toast.LENGTH_SHORT).show()
            }
        }

        if (filmId != null) {
            fetchFilmData(filmId)
        } else {
            Toast.makeText(this, "Film ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateWatchlistButton() {
        binding.btnaddtowatchlist.text = if (isWatchlisted) "Added to watchlist" else "Add to watchlist"
    }

    private fun checkWatchlistStatus() {
        val currentFilmId = intent.getStringExtra("FILM_DATA")
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentFilmId != null && userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)
            val filmDocumentReference = "films/$currentFilmId"

            userRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val watchlist = documentSnapshot.get("watchlist") as? List<String>
                    isWatchlisted = watchlist?.contains(filmDocumentReference) == true
                    updateWatchlistButton()
                }
            }.addOnFailureListener { e ->
                Log.e("InfoFilm", "Error checking watchlist status", e)
            }
        }
    }

    private fun fetchFilmData(filmId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("films").document(filmId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val film = document.toObject(Film::class.java)
                    film?.let { currentFilm -> // Ganti 'it' menjadi 'currentFilm' agar lebih jelas
                        // Update UI with film data (kecuali genre sementara)
                        binding.tvInfoFilmTitle.text = currentFilm.title
                        binding.tvInfoViews.text = "${currentFilm.views} views"
                        binding.tvInfoRating.text = currentFilm.rating.toString()
                        binding.tvInfoSynopsis.text = currentFilm.synopsis

                        // Load cover landscape
                        Glide.with(this)
                            .load(currentFilm.coverLandscape)
                            .into(binding.coverLandscape)

                        // TODO: Handle preview images (e.g., in a RecyclerView or ImageSwitcher)
                        // For now, logging the preview URLs
                        binding.linInfoFilmPreview.removeAllViews()

                        val previewUrls = currentFilm.preview // Asumsi `preview` di kelas Film adalah List<String>
                        if (previewUrls != null && previewUrls.isNotEmpty()) {
                            previewUrls.forEach { imageUrl ->
                                val imageView = ImageView(this).apply {
                                    layoutParams = LinearLayout.LayoutParams(
                                        dpToPx(150), // Lebar 150dp
                                        dpToPx(100)  // Tinggi 100dp
                                    ).apply {
                                        // Margin horizontal 8dp
                                        marginStart = dpToPx(8)
                                        marginEnd = dpToPx(8)
                                    }
                                    scaleType = ImageView.ScaleType.CENTER_CROP // Sesuaikan dengan layout XML Anda
                                    // Optional: Set placeholder atau error image
                                    // setImageResource(R.drawable.placeholder_image)
                                }

                                // Load gambar menggunakan Glide
                                Glide.with(this)
                                    .load(imageUrl)
                                    .apply(RequestOptions.bitmapTransform(RoundedCorners(16)))
                                    .error(R.drawable.filmhome_rounded_bg) // Load this if imageUrl fails
                                    .into(imageView)

                                binding.linInfoFilmPreview.addView(imageView)
                            }
                        } else {
                            Log.d("InfoFilm", "No preview images available for this film.")
                            // Opsional: Tambahkan TextView atau pesan lain jika tidak ada gambar preview
                        }

                        // --- BAGIAN BARU UNTUK MENGAMBIL NAMA GENRE ---
                        if (currentFilm.genre.startsWith("genres/")) {
                            val genreDocPath = currentFilm.genre
                            val genreDocRef = db.document(genreDocPath) // Buat DocumentReference dari string path

                            genreDocRef.get()
                                .addOnSuccessListener { genreDocument ->
                                    if (genreDocument != null && genreDocument.exists()) {
                                        val genreName = genreDocument.getString("name") // Asumsikan field nama genre adalah "name"
                                        if (genreName != null) {
                                            binding.tvInfoGenre.text = genreName
                                        } else {
                                            binding.tvInfoGenre.text = "Unknown Genre (Name Field Missing)"
                                            Log.e("InfoFilm", "Genre document found but 'name' field is missing: ${genreDocPath}")
                                        }
                                    } else {
                                        binding.tvInfoGenre.text = "Unknown Genre (Document Not Found)"
                                        Log.e("InfoFilm", "Genre document not found for path: ${genreDocPath}")
                                    }
                                }
                                .addOnFailureListener { genreException ->
                                    binding.tvInfoGenre.text = "Error Loading Genre"
                                    Log.e("InfoFilm", "Error getting genre data for path ${genreDocPath}: ${genreException.message}")
                                }
                        } else {
                            // Jika format genre tidak seperti DocumentReference string
                            binding.tvInfoGenre.text = currentFilm.genre
                            Log.w("InfoFilm", "Genre field does not seem to be a document path: ${currentFilm.genre}")
                        }
                        // --- AKHIR BAGIAN BARU ---

                    }
                } else {
                    Toast.makeText(this, "Film not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting film data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Helper function to convert dp to pixels
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}