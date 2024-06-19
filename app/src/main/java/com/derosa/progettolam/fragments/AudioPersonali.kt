package com.derosa.progettolam.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.derosa.progettolam.R
import com.derosa.progettolam.activities.LoginActivity
import com.derosa.progettolam.activities.RecordActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AudioPersonali : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audio_personali, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAddAudio = view.findViewById<FloatingActionButton>(R.id.addAudio)

        btnAddAudio.setOnClickListener{
            val intent = Intent(activity, RecordActivity::class.java)
            startActivity(intent)
            //activity?.finish()    in questo modo tornando indietro possiamo tornare alla pagina principale
        }
    }
}