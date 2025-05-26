package com.zelvan.imoviee

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.zelvan.imoviee.SuperCatListView.Companion.REQUEST_CODE_ADD_MENU
import com.zelvan.imoviee.adapter.FilmAdapter
import com.zelvan.imoviee.data.Film
import com.zelvan.imoviee.databinding.ActivitySuperFilmListViewBinding

class SuperFilmListView : AppCompatActivity() {

    private lateinit var binding: ActivitySuperFilmListViewBinding
    private val filmList = mutableListOf<Film>()
    private lateinit var filmAdapter: FilmAdapter // Declare adapter as a class member
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        // Inflate the layout using View Binding
        binding = ActivitySuperFilmListViewBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set the content view to the root of the binding
        firestore = FirebaseFirestore.getInstance()

        // Use binding.main for the root view ID (assuming the root layout ID in XML is 'main')
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 3. Buat Objek Adapter
        filmAdapter = FilmAdapter(filmList) { film ->
            val intent = Intent(this, SuperFilmEdit::class.java)
            intent.putExtra("filmId", film.id)
            startActivity(intent)
        }

        // 4. Set LayoutManager (buat tampilan vertikal) using binding
        binding.recyclerViewFilms.layoutManager = LinearLayoutManager(this) // Use binding.recyclerViewFilms

        // 5. Set Adapter ke RecyclerView using binding
        binding.recyclerViewFilms.adapter = filmAdapter // Use binding.recyclerViewFilms

        // Access the backButton using View Binding
        // Make sure your Button/ImageButton in XML has android:id="@+id/backButton"
        binding.backButton.setOnClickListener {
            // This will finish the current activity and go back to the previous one in the stack
            onBackPressedDispatcher.onBackPressed() // Recommended way
            // Alternatively, you could use:
            // finish()
        }

        binding.fabAddFilms.setOnClickListener {
            val intent = Intent(this, SuperFilmInput::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_MENU)
        }

        loadFilmData()
    }

    private fun loadFilmData() {
        firestore.collection("films").get().addOnSuccessListener { documents ->
            filmList.clear()
            for (document in documents) {
                val menu = document.toObject(Film::class.java)
                menu.id = document.id
                filmList.add(menu)
            }
            filmAdapter.notifyDataSetChanged() // Call notifyDataSetChanged() on the instance
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_MENU && resultCode == RESULT_OK) {
            loadFilmData()
        }
    }
    companion object {
        const val REQUEST_CODE_ADD_MENU = 1
    }
    override fun onResume() {
        super.onResume()
        loadFilmData()
    }
}