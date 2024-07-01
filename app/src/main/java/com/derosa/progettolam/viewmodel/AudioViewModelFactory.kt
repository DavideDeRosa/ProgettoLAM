package com.derosa.progettolam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.derosa.progettolam.db.AudioDatabase

class AudioViewModelFactory(private val audioDatabase: AudioDatabase) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AudioViewModel(audioDatabase) as T
    }
}