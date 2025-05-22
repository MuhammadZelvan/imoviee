package com.zelvan.imoviee

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.zelvan.imoviee.databinding.ActivitySuperCatEditBinding

class SuperCatEdit : AppCompatActivity() {

    private lateinit var binding: ActivitySuperCatEditBinding
    private lateinit var genreId: String
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySuperCatEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        genreId = intent.getStringExtra("genreId") ?: ""
        firestore = FirebaseFirestore.getInstance()

        binding.buttonSubmit.setOnClickListener {
            val update = mapOf(
                "name" to binding.editTextTitle.text.toString()
            )
            firestore.collection("genres").document(genreId).update(update).addOnSuccessListener {
                Toast.makeText(this, "Genre changed", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        binding.btnHapus.setOnClickListener {
            firestore.collection("genres").document(genreId).delete().addOnSuccessListener {
                Toast.makeText(this, "Genre deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.backButton.setOnClickListener {
            // This will finish the current activity and go back to the previous one in the stack
            onBackPressedDispatcher.onBackPressed() // Recommended way
            // Alternatively, you could use:
            // finish()
        }

        ambilData()
    }

    private fun ambilData() {
        firestore.collection("genres").document(genreId).get().addOnSuccessListener { doc ->
            binding.editTextTitle.setText(doc.getString("name"))
        }
    }

}