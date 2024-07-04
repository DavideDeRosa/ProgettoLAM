package com.derosa.progettolam.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.derosa.progettolam.R
import com.derosa.progettolam.activities.AppActivity
import com.derosa.progettolam.activities.LoginActivity
import com.derosa.progettolam.activities.MyAudioActivity
import com.derosa.progettolam.activities.MyAudioOfflineActivity
import com.derosa.progettolam.activities.RecordActivity
import com.derosa.progettolam.activities.UploadActivity
import com.derosa.progettolam.adapters.AudioOfflineAdapter
import com.derosa.progettolam.adapters.MyAudioAdapter
import com.derosa.progettolam.adapters.SpacingItemDecoration
import com.derosa.progettolam.databinding.FragmentAudioPersonaliBinding
import com.derosa.progettolam.util.DataSingleton
import com.derosa.progettolam.util.ExtraUtil
import com.derosa.progettolam.viewmodel.AudioViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AudioPersonali : Fragment() {

    private lateinit var binding: FragmentAudioPersonaliBinding
    private lateinit var myAudioAdapter: MyAudioAdapter
    private lateinit var audioOfflineAdapter: AudioOfflineAdapter
    private lateinit var audioViewModel: AudioViewModel
    private var audio_id: Int = 0
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        audioViewModel = (activity as AppActivity).audioViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAudioPersonaliBinding.inflate(inflater, container, false)

        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isNetworkAvailable = sharedPref.getBoolean("network_state", false)

        if (!isNetworkAvailable) {
            audioOfflineAdapter = AudioOfflineAdapter(context)
            binding.recyclerView.adapter = audioOfflineAdapter
        } else {
            myAudioAdapter = MyAudioAdapter(context)
            binding.recyclerView.adapter = myAudioAdapter
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.addItemDecoration(SpacingItemDecoration(20))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isNetworkAvailable = sharedPref.getBoolean("network_state", false)

        if (!isNetworkAvailable) {
            view.findViewById<FloatingActionButton>(R.id.addAudio).visibility = View.GONE
            view.findViewById<FloatingActionButton>(R.id.viewUpload).visibility = View.GONE

            audioViewModel.getAllAudioDb()

            audioViewModel.observeListAudioDbLiveData().observe(viewLifecycleOwner) {
                audioOfflineAdapter.setAudioList(ArrayList(it))
            }

            audioOfflineAdapter.onItemClick = {
                val intent = Intent(activity, MyAudioOfflineActivity::class.java)
                intent.putExtra("audio_id_offline", it.id)
                startActivity(intent)
                activity?.finish()
            }
        } else {
            audioViewModel.observeAudioMyLiveData().observe(viewLifecycleOwner) {
                myAudioAdapter.setMyAudioList(ArrayList(it))
            }

            audioViewModel.observeAudioMyErrorLiveData().observe(viewLifecycleOwner) {
                Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
                goToLogin()
            }

            audioViewModel.observeAudioShowLiveData().observe(viewLifecycleOwner) {
                val intent = Intent(activity, MyAudioActivity::class.java)
                intent.putExtra("audio_id", audio_id)
                intent.putExtra("longitude", longitude)
                intent.putExtra("latitude", latitude)
                startActivity(intent)
                activity?.finish()
            }

            audioViewModel.observeAudioShowErrorLiveData().observe(viewLifecycleOwner) {
                Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
                goToLogin()
            }

            val token = DataSingleton.token
            if (token != null) {
                audioViewModel.myAudio(token)
            }

            onMyAudioClick()

            view.findViewById<FloatingActionButton>(R.id.viewUpload).setOnClickListener {
                val intent = Intent(activity, UploadActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }

            view.findViewById<FloatingActionButton>(R.id.addAudio).setOnClickListener {
                val intent = Intent(activity, RecordActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
        }
    }

    private fun onMyAudioClick() {
        myAudioAdapter.onItemClick = {
            val token = DataSingleton.token
            if (token != null) {
                if (it.hidden) {
                    showShowConfirmationDialog(token, it.id, it.longitude, it.latitude)
                } else {
                    val intent = Intent(activity, MyAudioActivity::class.java)
                    intent.putExtra("audio_id", it.id)
                    intent.putExtra("longitude", it.longitude)
                    intent.putExtra("latitude", it.latitude)
                    startActivity(intent)
                    activity?.finish()
                }
            }
        }
    }

    private fun showShowConfirmationDialog(token: String, id: Int, lon: Double, lat: Double) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Conferma")
            .setMessage("L'audio è nascosto. Per visualizzare i suoi dati devi renderlo visibile. Procedere?")
            .setPositiveButton("Si") { dialog, which ->
                audioViewModel.showAudio(token, id)
                audio_id = id
                longitude = lon
                latitude = lat
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun goToLogin() {
        DataSingleton.token = null
        DataSingleton.username = null

        ExtraUtil.clearTokenAndUsername(requireContext())

        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}