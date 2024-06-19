package com.derosa.progettolam.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.derosa.progettolam.R
import com.derosa.progettolam.activities.LoginActivity
import com.derosa.progettolam.pojo.User
import com.derosa.progettolam.pojo.UserCorrectlySignedUp
import com.derosa.progettolam.util.DataSingleton
import com.derosa.progettolam.viewmodel.UserViewModel


class Account : Fragment() {

    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnUnsub = view.findViewById<Button>(R.id.btnUnsub)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        btnUnsub.setOnClickListener {
            val token = DataSingleton.token
            if (token != null) {
                showUnsubscribeConfirmationDialog(token)
            } else {
                goToLogin()
            }
        }

        btnLogout.setOnClickListener {
            goToLogin()
        }

        userViewModel.observeUserCorrectlyRemovedLiveData().observe(viewLifecycleOwner) {
            Toast.makeText(activity, it.detail, Toast.LENGTH_SHORT).show()
            goToLogin()
        }

        userViewModel.observeUserCorrectlyRemovedErrorLiveData().observe(viewLifecycleOwner) {
            Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
            goToLogin()
        }
    }

    private fun goToLogin() {
        DataSingleton.token = null

        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun showUnsubscribeConfirmationDialog(token: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Conferma")
            .setMessage("Sei sicuro di voler cancellare il tuo account?")
            .setPositiveButton("Si") { dialog, which ->
                userViewModel.authUnsubscribe(token)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
}