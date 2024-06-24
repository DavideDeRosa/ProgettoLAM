package com.derosa.progettolam.fragments

import android.content.Intent
import android.location.Address
import android.location.Geocoder
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
import com.derosa.progettolam.pojo.MyAudio
import com.derosa.progettolam.util.DataSingleton
import com.derosa.progettolam.viewmodel.AudioViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.IOException
import java.util.Locale

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
        myAudioAdapter = MyAudioAdapter()
        binding.recyclerView.adapter = myAudioAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioViewModel.observeAudioMyLiveData().observe(viewLifecycleOwner) {
            loadMyAudio(it)
        }

        audioViewModel.observeAudioMyErrorLiveData().observe(viewLifecycleOwner) {
            Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
        }

        val token = DataSingleton.token
        if (token != null) {
            audioViewModel.myAudio(token)
        }

        val btnAddAudio = view.findViewById<FloatingActionButton>(R.id.addAudio)
        btnAddAudio.setOnClickListener {
            val intent = Intent(activity, RecordActivity::class.java)
            startActivity(intent)
            //activity?.finish()    in questo modo tornando indietro possiamo tornare alla pagina principale
        }
    }

    private fun loadMyAudio(myAudioList: List<MyAudio>) {
        var myAudioListString: ArrayList<String> = ArrayList()

        for (audio in myAudioList) {
            var location = getLocationName(audio.longitude, audio.latitude)

            if (location == "") {
                myAudioListString.add("Luogo sconosciuto! Longitudine: " + audio.longitude + " Latitudine: " + audio.latitude)
            } else {
                myAudioListString.add(location)
            }
        }

        myAudioAdapter.setMyAudioList(myAudioListString)
    }

    private fun getLocationName(longitude: Double, latitude: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        var locationName = ""

        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                locationName = addresses[0].getAddressLine(0)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return locationName
    }
}