package com.zelvan.imoviee

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.zelvan.imoviee.adapter.WatchlistFilmAdapter
import com.zelvan.imoviee.data.Film // Pastikan ini path yang benar ke kelas Film Anda
import com.zelvan.imoviee.databinding.FragmentWatchlistBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Watchlist : Fragment() {
    private var _binding: FragmentWatchlistBinding? = null
    private val binding get() = _binding!!
    private lateinit var watchlistFilmAdapter: WatchlistFilmAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWatchlistBinding.inflate(inflater, container, false)
        val view = binding.root

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        watchlistFilmAdapter = WatchlistFilmAdapter(mutableListOf())
        binding.recyclerViewWatchlist.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = watchlistFilmAdapter
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchWatchlist()
    }

    private fun fetchWatchlist() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // --- PERBAIKAN DI SINI ---
            // Mengambil dokumen pengguna itu sendiri untuk mendapatkan field 'watchlist'
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { userDocument ->
                    if (userDocument.exists()) {
                        // Ambil field 'watchlist' yang merupakan Array<String>
                        val watchlistFilmPaths = userDocument.get("watchlist") as? List<String>
                        val filmIds = mutableListOf<String>()

                        if (watchlistFilmPaths != null) {
                            for (path in watchlistFilmPaths) {
                                // Ekstrak ID film dari path: "films/ID_FILM_ABC" -> "ID_FILM_ABC"
                                val filmId = path.substringAfter("films/")
                                if (filmId.isNotEmpty()) {
                                    filmIds.add(filmId)
                                }
                            }
                        }

                        if (filmIds.isNotEmpty()) {
                            fetchFilmDetails(filmIds)
                        } else {
                            binding.tvEmptyWatchlist.visibility = View.VISIBLE
                            watchlistFilmAdapter.updateFilms(emptyList())
                        }
                    } else {
                        binding.tvEmptyWatchlist.visibility = View.VISIBLE
                        watchlistFilmAdapter.updateFilms(emptyList())
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("WatchlistFragment", "Error getting user document", exception)
                }
        } else {
            binding.tvEmptyWatchlist.visibility = View.VISIBLE
            watchlistFilmAdapter.updateFilms(emptyList())
        }
    }

    private fun fetchFilmDetails(filmIds: List<String>) {
        lifecycleScope.launch(Dispatchers.IO) {
            val films = mutableListOf<Film>()
            val genreNames = mutableMapOf<String, String>()

            for (filmId in filmIds) {
                try {
                    val filmDoc = firestore.collection("films").document(filmId).get().await()
                    if (filmDoc.exists()) {
                        val film = filmDoc.toObject(Film::class.java)
                        film?.let {
                            it.id = filmDoc.id
                            val genrePath = it.genre
                            if (genrePath.startsWith("genres/")) {
                                if (!genreNames.containsKey(genrePath)) {
                                    val genreDoc = firestore.document(genrePath).get().await()
                                    val name = genreDoc.getString("name")
                                    if (name != null) {
                                        genreNames[genrePath] = name
                                    } else {
                                        genreNames[genrePath] = "Unknown Genre"
                                        Log.w("WatchlistFragment", "Genre document found but 'name' field is missing for path: $genrePath")
                                    }
                                }
                                it.genre = genreNames[genrePath] ?: "Unknown Genre"
                            } else {
                                it.genre = "Unknown Genre"
                                Log.w("WatchlistFragment", "Genre field does not seem to be a document path: ${it.genre} for film ID: $filmId")
                            }
                            films.add(it)
                        }
                    } else {
                        Log.w("WatchlistFragment", "Film document not found for ID: $filmId. It might have been deleted.")
                    }
                } catch (e: Exception) {
                    Log.e("WatchlistFragment", "Error fetching film details for ID $filmId: ${e.message}", e)
                }
            }

            launch(Dispatchers.Main) {
                if (films.isNotEmpty()) {
                    binding.tvEmptyWatchlist.visibility = View.GONE
                    watchlistFilmAdapter.updateFilms(films)
                } else {
                    // Hanya tampilkan pesan ini jika ada ID film tapi detailnya tidak ditemukan
                    // Jika filmIds kosong, pesan "Your watchlist is empty" sudah ditangani di fetchWatchlist().
                    if (filmIds.isNotEmpty()) {
                        Toast.makeText(context, "No film details could be retrieved from your watchlist.", Toast.LENGTH_SHORT).show()
                        binding.tvEmptyWatchlist.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.statusBarColor = resources.getColor(R.color.toolbar_color, activity?.theme)
        fetchWatchlist() // Panggil fetchWatchlist lagi untuk refresh data
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}