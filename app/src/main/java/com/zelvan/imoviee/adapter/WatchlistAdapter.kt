package com.zelvan.imoviee.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.zelvan.imoviee.InfoFilm
import com.zelvan.imoviee.R
import com.zelvan.imoviee.data.Film

class WatchlistFilmAdapter(
    private var films: MutableList<Film>
) : RecyclerView.Adapter<WatchlistFilmAdapter.FilmViewHolder>() {

    class FilmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val poster: ImageView = itemView.findViewById(R.id.imageView_poster)
        val title: TextView = itemView.findViewById(R.id.textView_title)
        val genre: TextView = itemView.findViewById(R.id.textView_genre)
        val rating: TextView = itemView.findViewById(R.id.tvInfoRating)
        val views: TextView = itemView.findViewById(R.id.tvInfoViews)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_filmlist, parent, false)
        return FilmViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        val film = films[position]
        holder.title.text = film.title

        // Untuk genre, karena it.genre adalah path seperti "genres/ID", kita perlu mengambil nama genre sebenarnya
        // Untuk saat ini, kita akan tampilkan path-nya atau kosongkan.
        // Nanti akan kita perbaiki di Watchlist fragment untuk fetch nama genre
        holder.genre.text = film.genre.substringAfterLast("/") // Hanya tampilkan ID genre untuk sementara

        holder.rating.text = film.rating.toString()
        holder.views.text = film.views.toString()

        // Load poster
        Glide.with(holder.itemView.context)
            .load(film.coverPortrait) // Asumsi coverPortrait adalah yang digunakan untuk poster
            .transform(RoundedCorners(12)) // Opsional: sudut membulat 12dp
            .placeholder(R.drawable.filmhome_rounded_bg) // Placeholder saat loading
            .error(R.drawable.filmhome_rounded_bg) // Gambar error
            .into(holder.poster)

        holder.itemView.setOnClickListener {
            if (film.id.isNullOrEmpty()) {
                Log.e("WatchlistAdapter", "Film ID is null or empty. Cannot start InfoFilm activity.")
                // Opsional: Toast.makeText(holder.itemView.context, "Film ID not available.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(holder.itemView.context, InfoFilm::class.java)
                intent.putExtra("FILM_DATA", film.id)
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = films.size

    fun updateFilms(newFilms: List<Film>) {
        films.clear()
        films.addAll(newFilms)
        notifyDataSetChanged()
    }
}