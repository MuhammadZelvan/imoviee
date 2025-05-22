package com.zelvan.imoviee

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.zelvan.imoviee.adapter.GenreAdapter
import com.zelvan.imoviee.data.Genre
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.LinearLayoutManager
import com.zelvan.imoviee.databinding.ActivitySuperCatListViewBinding

class SuperCatListView : AppCompatActivity() {

    private lateinit var binding: ActivitySuperCatListViewBinding
    private val genreList = mutableListOf<Genre>()
    private lateinit var genreAdapter: GenreAdapter // Declare adapter as a class member
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        // Inflate the layout using View Binding
        binding = ActivitySuperCatListViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firestore = FirebaseFirestore.getInstance()

        // Use binding.main for the root view ID
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Ambil Referensi RecyclerView - Now accessed via binding
        // recyclerViewGenres = findViewById(R.id.recyclerView_genres) // Old way

        // 3. Buat Objek Adapter Genre
        genreAdapter = GenreAdapter(genreList) { genre ->
            val intent = Intent(this, SuperCatEdit::class.java)
            intent.putExtra("genreId", genre.id)
            startActivity(intent)
        }

        // 4. Set LayoutManager (LinearLayoutManager buat tampilan vertikal)
        // Use binding.recyclerViewGenres
        binding.recyclerViewGenres.layoutManager = LinearLayoutManager(this)

        // 5. Set Adapter ke RecyclerView
        // Use binding.recyclerViewGenres
        binding.recyclerViewGenres.adapter = genreAdapter

        // Access the backButton using View Binding
        // Make sure your Button/ImageButton in XML has android:id="@+id/backButton"
        binding.backButton.setOnClickListener {
            // This will finish the current activity and go back to the previous one in the stack
            onBackPressedDispatcher.onBackPressed() // Recommended way
            // Alternatively, you could use:
            // finish()
        }

        binding.fabAddgenres.setOnClickListener {
            val intent = Intent(this, SuperCatInput::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_MENU)
        }

        loadGenreData()
    }

    private fun loadGenreData() {
        firestore.collection("genres").get().addOnSuccessListener { documents ->
            genreList.clear()
            for (document in documents) {
                val menu = document.toObject(Genre::class.java)
                menu.id = document.id
                genreList.add(menu)
            }
            genreAdapter.notifyDataSetChanged() // Call notifyDataSetChanged() on the instance
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_MENU && resultCode == RESULT_OK) {
            loadGenreData()
        }
    }
    companion object {
        const val REQUEST_CODE_ADD_MENU = 1
    }
    override fun onResume() {
        super.onResume()
        loadGenreData()
    }
}