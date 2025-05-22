package com.zelvan.imoviee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zelvan.imoviee.R // Import file R buat akses resource ID
import com.bumptech.glide.Glide // Contoh library buat load gambar dari URL
import com.zelvan.imoviee.data.Film

// Import library lain yang kamu butuhin (misal: context, dll)

class FilmAdapter(private val filmList: List<Film>) :
    RecyclerView.Adapter<FilmAdapter.ViewHolder>() {

    // Class ViewHolder di dalam Adapter
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewPoster: ImageView = itemView.findViewById(R.id.imageView_poster)
        val textViewTitle: TextView = itemView.findViewById(R.id.textView_title)
        val textViewGenre: TextView = itemView.findViewById(R.id.textView_genre)
        val textViewStats: TextView = itemView.findViewById(R.id.textView_stats)
        val textViewSynopsis: TextView = itemView.findViewById(R.id.textView_synopsis)
    }

    // Method-method wajib dari RecyclerView.Adapter
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_superfilm, parent, false) // Inflate layout item
        return ViewHolder(view) // Kembalikan objek ViewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val film = filmList[position] // Ambil data film

        // Bind data ke View di ViewHolder
        holder.textViewTitle.text = film.title
        holder.textViewGenre.text = film.genre.joinToString(", ")
        holder.textViewStats.text = "${film.views} Views | ${film.rating} Rating"
        holder.textViewSynopsis.text = film.synopsis

        // Load gambar pake Glide
        Glide.with(holder.itemView.context)
            .load(film.coverPortrait) // Ganti kalo mau pake coverLandscape
            .placeholder(R.drawable.placeholder_poster)
            .error(R.drawable.error_poster)
            .into(holder.imageViewPoster)

        // OnClickListener buat item
        holder.itemView.setOnClickListener {
            // TODO: Lakukan aksi pas item diklik
        }
    }

    override fun getItemCount(): Int {
        return filmList.size // Balikin jumlah item
    }
}