package com.zelvan.imoviee

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.zelvan.imoviee.databinding.ActivityMainBinding
import com.zelvan.imoviee.databinding.FragmentProfileBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Profile.newInstance] factory method to
 * create an instance of this fragment.
 */
class Profile : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentProfileBinding
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        val currentUser = auth.currentUser
        if (currentUser !== null) {
            // Jika sudah login, perbarui UI dengan informasi pengguna
            updateUI(currentUser)
        }

        // Find the button and set its click listener
        val backButton: Button = view.findViewById(R.id.logout)
        backButton.setOnClickListener {
            auth.signOut()
            requireActivity().finish() // Ends the hosting activity
        }

        val verifyButton: Button = view.findViewById(R.id.Verify)
        verifyButton.setOnClickListener {
            sendEmailVerification()
        }

        return view

//        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onResume() {
        super.onResume()

        // Change the status bar color when this fragment is visible
        activity?.window?.statusBarColor = resources.getColor(R.color.toolbar_color, activity?.theme)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Profile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Profile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Verification email sent to ${user.email}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        user?.reload()?.addOnSuccessListener {
            // Setelah reload, ambil user terbaru
            val updatedUser = auth.currentUser
            updatedUser?.let {
                // Tampilkan nama pengguna
                val name = it.displayName ?: "No Name"
                binding.tvName.text = name
                // Tampilkan email pengguna
                val email = it.email ?: "No Email"
                binding.tvUserId.text = email
                // Sembunyikan tombol verifikasi email jika email sudah terverifikasi
                if (it.isEmailVerified) {
                    binding.Verify.visibility = View.GONE
                } else {
                    binding.Verify.visibility = View.VISIBLE
                }
            }
        }?.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
        }
    }
}