package com.sti.nalirescue

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sti.nalirescue.R

class Settings : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SettingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewSetting)

        val settingsList = listOf(SettingsItem("Logout"))
        // Add other items as needed

        val sessionManager = SessionManager(requireContext()) // assuming SessionManager takes a context parameter

        adapter = SettingsAdapter(settingsList) { item ->
            // Handle click action for each item
            if (item.title == "Logout") {
                showLogoutConfirmationDialog()
            }
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        return view
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to log out?")
        builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
            // User clicked Yes, perform logout
            val sessionManager = SessionManager(requireContext())
            sessionManager.logoutUser()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)

            requireActivity().finish()
        }
        builder.setNegativeButton("No") { dialog: DialogInterface, _: Int ->
            // User clicked No, dismiss the dialog
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}