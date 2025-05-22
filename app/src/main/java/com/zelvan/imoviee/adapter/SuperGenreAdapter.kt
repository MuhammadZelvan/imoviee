package com.zelvan.imoviee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zelvan.imoviee.R // Import R file (buat akses resource ID)
import com.zelvan.imoviee.data.Genre

class GenreAdapter(
    private val genreList: List<Genre>,
    private val onItemClick: (Genre) -> Unit
) :
    RecyclerView.Adapter<GenreAdapter.ViewHolder>() {

    // Class ViewHolder di dalam Adapter
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Simpan referensi TextView dari layout item
        val textViewGenreName: TextView = itemView.findViewById(R.id.textView_genreName)
    }

    // Dipanggil pas RecyclerView butuh ViewHolder baru
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate layout item dari XML (super_cat_list_view.xml)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_supergenre, parent, false)
        // Buat objek ViewHolder baru
        return ViewHolder(view)
    }

    // Dipanggil buat nampilin data di posisi tertentu
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Ambil data Genre dari list sesuai posisi
        val genre = genreList[position]

        // Set nama genre ke TextView di ViewHolder
        holder.textViewGenreName.text = genre.name

        // Opsional: Tambahin OnClickListener buat item genre
        holder.itemView.setOnClickListener {
            // TODO: Lakukan aksi pas item genre diklik (misal: edit genre)
            // Kamu bisa akses data genre yang diklik pake variabel 'genre'
            onItemClick(genre)
        }
    }

    // Balikin jumlah total item di list
    override fun getItemCount(): Int {
        return genreList.size // Balikin ukuran list data genre
    }
}