package com.derosa.progettolam.pojo

data class UploadData(
    val bpm: Int,
    val danceability: Double,
    val genre: Genre,
    val instrument: Instrument,
    val loudness: Double,
    val mood: Mood
)