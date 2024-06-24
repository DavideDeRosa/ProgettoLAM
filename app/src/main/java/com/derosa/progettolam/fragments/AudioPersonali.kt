package com.derosa.progettolam.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.derosa.progettolam.R
import com.derosa.progettolam.activities.RecordActivity
import com.derosa.progettolam.adapters.MyAudioAdapter
import com.derosa.progettolam.databinding.FragmentAudioPersonaliBinding
import com.derosa.progettolam.dialogs.MyAudioMetadataDialog
import com.derosa.progettolam.util.DataSingleton
import com.derosa.progettolam.viewmodel.AudioViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AudioPersonali : Fragment() {

    private lateinit var binding: FragmentAudioPersonaliBinding
    private lateinit var myAudioAdapter: MyAudioAdapter
    private lateinit var audioViewModel: AudioViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioViewModel = ViewModelProvider(this)[AudioViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAudioPersonaliBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        myAudioAdapter = MyAudioAdapter(context)
        binding.recyclerView.adapter = myAudioAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioViewModel.observeAudioMyLiveData().observe(viewLifecycleOwner) {
            myAudioAdapter.setMyAudioList(ArrayList(it))
        }

        audioViewModel.observeAudioMyErrorLiveData().observe(viewLifecycleOwner) {
            Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
        }

        val token = DataSingleton.token
        if (token != null) {
            audioViewModel.myAudio(token)
        }

        onMyAudioClick()

        val btnAddAudio = view.findViewById<FloatingActionButton>(R.id.addAudio)
        btnAddAudio.setOnClickListener {
            val intent = Intent(activity, RecordActivity::class.java)
            startActivity(intent)
            //activity?.finish()    in questo modo tornando indietro possiamo tornare alla pagina principale
        }

        audioViewModel.observeAudioByIdLiveData().observe(viewLifecycleOwner) {
            val customDialog = MyAudioMetadataDialog(it)
            customDialog.show(parentFragmentManager, "AudioMetaDataDialog")
        }

    }

    private fun onMyAudioClick() {
        myAudioAdapter.onItemClick = {
            val token = DataSingleton.token
            if (token != null) {
                audioViewModel.getAudioById(token, it.id)
            }
        }
    }
}