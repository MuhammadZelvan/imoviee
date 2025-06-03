package com.zelvan.imoviee

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.zelvan.imoviee.data.Film
import com.zelvan.imoviee.data.FilmHome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Home.newInstance] factory method to
 * create an instance of this fragment.
 */
class Home : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_home, container, false)

        // Find the button and set its click listener
        val backButton: LinearLayout = view.findViewById(R.id.btntoinfofilm)
        backButton.setOnClickListener {
            val intent = Intent(requireContext(), InfoFilm::class.java)
            startActivity(intent)
        }

        val flexboxLayout = view.findViewById<FlexboxLayout>(R.id.genreFlexbox)
        val db = FirebaseFirestore.getInstance()

        db.collection("genres")
            .get()
            .addOnSuccessListener { result ->
                val genres = mutableListOf<String>()
                for (document in result) {
                    document.getString("name")?.let {
                        genres.add(it)
                    }
                }
                genres.forEach { genre ->
                    val chip = Chip(requireContext()).apply {
                        text = genre
                        isCheckable = true
                        isClickable = true
                        chipCornerRadius = 24f
                        setTextColor(Color.WHITE)
                        chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#404040"))
                        layoutParams = FlexboxLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            marginEnd = 16
                            bottomMargin = 0
                        }
                    }
                    flexboxLayout.addView(chip)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("HomeFragment", "Error getting documents: ", exception)
            }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val containerGenre = view.findViewById<LinearLayout>(R.id.containerGenre)

        lifecycleScope.launch {
            val genreWithFilms = fetchGenresWithFilms()

            genreWithFilms.forEach { genre ->
                // Buat TextView untuk nama genre
                val genreTitle = TextView(requireContext()).apply {
                    text = genre.genre
                    setTextColor(Color.WHITE)
                    textSize = 18f
                    setTypeface(null, Typeface.BOLD)
                    setPadding(10, 20, 10, 10)
                }

                containerGenre.addView(genreTitle)

                // Buat FlexboxLayout buat film-nya
                val flexboxLayoutFilms = com.google.android.flexbox.FlexboxLayout(requireContext()).apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.FLEX_START
                    layoutParams = LinearLayout.LayoutParams(
                        MATCH_PARENT,
                        WRAP_CONTENT
                    )
                }

                genre.films.forEachIndexed { index, film ->
                    val filmItem = LayoutInflater.from(context).inflate(R.layout.item_filmhome, flexboxLayoutFilms, false)

                    val titleView = filmItem.findViewById<TextView>(R.id.tvFilmTitle)
                    val coverView = filmItem.findViewById<ImageView>(R.id.ivFilmCover)
                    val viewsView = filmItem.findViewById<TextView>(R.id.tvViews)

                    titleView.text = film.title
                    Glide.with(requireContext()).load(film.coverPortrait)
                        .transform(RoundedCorners(16))
                        .into(coverView)
                    viewsView.text = film.views.toString()
                    val params = FlexboxLayout.LayoutParams(
                        WRAP_CONTENT,
                        WRAP_CONTENT
                    ).apply {
                        setMargins(10, 10, 0, 10)
                    }

                    filmItem.layoutParams = params
                    filmItem.setOnClickListener {
                        val intent = Intent(requireContext(), InfoFilm::class.java)
                        Log.d("HomeActivity", "Sending FILM_DATA with ID: ${film.id}")
                        if (film.id.isNullOrEmpty()) {
                            Log.e("HomeActivity", "Film ID is null or empty. Cannot start InfoFilm activity.")
                            // Handle the error appropriately, e.g., show a toast or a dialog
                        } else {
                            intent.putExtra("FILM_DATA", film.id)
                        }
                        startActivity(intent)
                    }
                    flexboxLayoutFilms.addView(filmItem)
                }

                containerGenre.addView(flexboxLayoutFilms)
            }
        }
    }

    suspend fun fetchGenresWithFilms(): List<FilmHome> = withContext(Dispatchers.IO) {
        val firestore = FirebaseFirestore.getInstance()

        val genresSnapshot = firestore.collection("genres").get().await()
        val filmsSnapshot = firestore.collection("films").get().await()

        val filmList = filmsSnapshot.documents.mapNotNull { doc ->
            val filmId = doc.id
            val filmData = doc.toObject(Film::class.java)

            if (filmData != null) {
                filmData.id = filmId
            }
            filmData
        }

        val result = mutableListOf<FilmHome>()

        for (genreDoc in genresSnapshot.documents) {
            val genreName = genreDoc.getString("name") ?: continue
            val genrePath = "genres/${genreDoc.id}" // Construct the path string
            val filmsForGenre = filmList.filter { film ->
                film.genre == genrePath // Compare with the string path
            }
            result.add(FilmHome(genreName, filmsForGenre))
        }

        return@withContext result
    }

    override fun onResume() {
        super.onResume()

        // Change the status bar color when this fragment is visible
        activity?.window?.statusBarColor = resources.getColor(R.color.toolbar_colordefault, activity?.theme)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Home.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Home().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}