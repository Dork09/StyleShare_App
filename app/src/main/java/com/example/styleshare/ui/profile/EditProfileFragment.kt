/**
 * מטרת הקובץ:
 * מסך עריכת פרופיל:
 * - עריכת fullName
 * - עריכת bio
 * - החלפת תמונת פרופיל (נשמרת מקומית)
 */
package com.example.styleshare.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.styleshare.R
import com.example.styleshare.databinding.FragmentEditProfileBinding
import com.example.styleshare.utils.ImageStorage
import com.example.styleshare.utils.Result
import com.squareup.picasso.Picasso
import java.io.File

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val vm: ProfileViewModel by viewModels()

    private var selectedImagePath: String? = null

    /**
     * בוחר תמונה מהגלריה.
     */
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImagePath = ImageStorage.saveImageToInternalStorage(
                    context = requireContext(),
                    uri = uri
                )

                Picasso.get()
                    .load(File(ImageStorage.resolveImagePathForDisplay(requireContext(), selectedImagePath) ?: selectedImagePath!!))
                    .fit()
                    .centerCrop()
                    .into(binding.ivEditProfile)
            }
        }

    /**
     * חיבור UI + טעינת נתונים.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditProfileBinding.bind(view)

        vm.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Result.Loading -> binding.progress.visibility = View.VISIBLE

                is Result.Success -> {
                    binding.progress.visibility = View.GONE
                    val profile = state.data

                    binding.etFullName.setText(profile?.fullName ?: "")
                    binding.etBio.setText(profile?.bio ?: "")

                    selectedImagePath = profile?.imagePath
                    if (!selectedImagePath.isNullOrEmpty()) {
                        val displayPath = ImageStorage.resolveImagePathForDisplay(requireContext(), selectedImagePath)
                        val req = if (displayPath?.startsWith("http") == true) {
                            Picasso.get().load(displayPath)
                        } else {
                            Picasso.get().load(File(displayPath ?: selectedImagePath!!))
                        }
                        req.fit()
                            .centerCrop()
                            .into(binding.ivEditProfile)
                    } else {
                        binding.ivEditProfile.setImageResource(R.drawable.ic_launcher_foreground)
                    }
                }

                is Result.Error -> {
                    binding.progress.visibility = View.GONE
                }
            }
        }

        binding.btnPickImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnSaveProfile.setOnClickListener {
            val fullName = binding.etFullName.text?.toString()?.trim().orEmpty()
            val bio = binding.etBio.text?.toString()?.trim().orEmpty()

            vm.saveProfile(fullName = fullName, bio = bio, imagePath = selectedImagePath)
            findNavController().navigateUp()
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
