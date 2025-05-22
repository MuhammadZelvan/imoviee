package com.zelvan.imoviee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zelvan.imoviee.databinding.ActivityInfoFilmBinding

class InfoFilm : AppCompatActivity() {
    private lateinit var binding: ActivityInfoFilmBinding // Declare binding variable

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

        // Access the "Watch Now" button using View Binding
        binding.btnwatchnow.setOnClickListener { // Assuming R.id.btnwatchnow is the ID
            val intent = Intent(this, Playing::class.java)
            startActivity(intent)
        }

        // --- ADD BACK BUTTON FUNCTIONALITY HERE ---
        // Access the backButton using View Binding
        // Make sure your ImageButton in XML has android:id="@+id/backButton"
        binding.backButton.setOnClickListener {
            // This will finish the current activity and go back to the previous one in the stack
            onBackPressedDispatcher.onBackPressed() // Recommended way
            // Alternatively, you could use:
            // finish()
        }
    }
}