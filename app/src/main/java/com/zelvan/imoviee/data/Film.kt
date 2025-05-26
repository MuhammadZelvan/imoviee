package com.zelvan.imoviee.data

import com.google.firebase.firestore.DocumentReference

data class Film(
    var id: String = "",
    val coverPortrait: String = "",
    val coverLandscape: String = "",
    val title: String = "",
    val genre: String = "",
    val views: Int = 0,
    val rating: Double = 0.0,
    val synopsis: String = "",
    val preview: List<String> = emptyList()
)
