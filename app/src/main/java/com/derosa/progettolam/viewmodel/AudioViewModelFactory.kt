package com.derosa.progettolam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.derosa.progettolam.db.AudioDataDatabase

class AudioViewModelFactory(private val audioDataDatabase: AudioDataDatabase) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AudioViewModel(audioDataDatabase) as T
    }
}