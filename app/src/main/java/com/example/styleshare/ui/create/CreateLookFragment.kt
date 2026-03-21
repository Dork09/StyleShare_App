package com.example.styleshare.ui.create

import android.net.Uri
import android.os.LocaleList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.styleshare.R
import com.example.styleshare.databinding.FragmentCreateLookBinding
import com.example.styleshare.utils.ImageStorage
import com.example.styleshare.utils.Result
import com.google.firebase.auth.FirebaseAuth

class CreateLookFragment : Fragment(R.layout.fragment_create_look) {

    private var _binding: FragmentCreateLookBinding? = null
    private val binding get() = _binding!!

    private val vm: CreateLookViewModel by viewModels()

    private var selectedImageUri: Uri? = null
    private var savedImagePath: String? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.ivPreview.visibility = View.VISIBLE
                binding.llPickerButtons.visibility = View.GONE
                binding.ivPreview.setImageURI(uri)
                savedImagePath = ImageStorage.saveImageToInternalStorage(requireContext(), uri)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateLookBinding.bind(view)
        val imeLocales = LocaleList.forLanguageTags("he,en")
        binding.etLookTitle.imeHintLocales = imeLocales
        binding.etLookDesc.imeHintLocales = imeLocales
        binding.etLookTags.imeHintLocales = imeLocales

        binding.btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnCamera.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSaveLook.setOnClickListener {
            val title = binding.etLookTitle.text?.toString()?.trim().orEmpty()
            val desc = binding.etLookDesc.text?.toString()?.trim().orEmpty()

            if (title.isBlank()) {
                Toast.makeText(requireContext(), "חייבים למלא כותרת", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (savedImagePath == null) {
                Toast.makeText(requireContext(), "חייבים לבחור תמונה", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"
            val rawTags = binding.etLookTags.text?.toString()?.trim().orEmpty()
            val tags = if (rawTags.isNotBlank()) {
                rawTags.split(",")
                    .map { it.trim().removePrefix("#") }
                    .filter { it.isNotBlank() }
            } else {
                emptyList()
            }

            vm.saveLook(
                title = title,
                desc = desc,
                imagePath = savedImagePath!!,
                createdByUid = uid,
                tags = tags
            )
        }

        vm.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Result.Loading -> setSavingState(true)
                is Result.Success -> {
                    setSavingState(false)
                    Toast.makeText(requireContext(), "יצירת הלוק בוצעה בהצלחה!", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (isAdded) {
                            findNavController().navigate(R.id.homeFragment)
                        }
                    }, 900)
                }
                is Result.Error -> {
                    setSavingState(false)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setSavingState(isSaving: Boolean) {
        binding.loadingOverlay.visibility = if (isSaving) View.VISIBLE else View.GONE
        binding.btnSaveLook.isEnabled = !isSaving
        binding.btnGallery.isEnabled = !isSaving
        binding.btnCamera.isEnabled = !isSaving
        binding.etLookTitle.isEnabled = !isSaving
        binding.etLookDesc.isEnabled = !isSaving
        binding.etLookTags.isEnabled = !isSaving
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
