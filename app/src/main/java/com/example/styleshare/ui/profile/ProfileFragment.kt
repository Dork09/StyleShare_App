/**
 * מטרת הקובץ:
 * מסך פרופיל:
 * - מציג שם מלא + BIO + תמונת פרופיל
 * - מעבר לעריכה
 * - כפתור Logout
 */
package com.example.styleshare.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.styleshare.R
import com.example.styleshare.databinding.FragmentProfileBinding
import com.example.styleshare.utils.Result
import com.squareup.picasso.Picasso
import java.io.File

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val vm: ProfileViewModel by viewModels()

    /**
     * חיבור ה-UI + Observers.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)
        binding.btnSettings.visibility = View.GONE

        vm.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Result.Loading -> binding.progress.visibility = View.VISIBLE

                is Result.Success -> {
                    binding.progress.visibility = View.GONE
                    val profile = state.data

                    binding.tvFullName.text = profile?.fullName ?: "משתמש/ת חדש/ה"
                    binding.tvBio.text = profile?.bio ?: "כאן יהיה ה-BIO שלך ✨"

                    val path = profile?.imagePath
                    if (!path.isNullOrEmpty()) {
                        val req = if (path.startsWith("http")) {
                            Picasso.get().load(path)
                        } else {
                            Picasso.get().load(File(path))
                        }
                        req.fit()
                            .centerCrop()
                            .into(binding.ivProfile)
                    } else {
                        binding.ivProfile.setImageResource(R.drawable.ic_launcher_foreground)
                    }
                }

                is Result.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.tvFullName.text = "שגיאה"
                    binding.tvBio.text = state.message
                    binding.ivProfile.setImageResource(R.drawable.ic_launcher_foreground)
                }
            }
        }

        vm.stats.observe(viewLifecycleOwner) { (looks, likes) ->
            binding.tvLooksCount.text = looks.toString()
            
            // Format large numbers using K for thousands
            val formattedLikes = if (likes >= 1000) {
                String.format("%.1fK", likes / 1000.0)
            } else {
                likes.toString()
            }
            binding.tvLikesTotal.text = formattedLikes
        }

        binding.btnEditProfile.setOnClickListener {
            // עכשיו זה יעבוד כי תיקנו את האקשן ב-nav_graph
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
        
        binding.btnMyLooks.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_myLooksFragment)
        }
        
        binding.btnLogout.setOnClickListener {
            vm.logout()

            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()

            findNavController().navigate(R.id.loginFragment, null, navOptions)
        }

        vm.loadProfile()
    }

    /**
     * ניקוי Binding.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
