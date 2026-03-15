/**
 * מטרת הקובץ:
 * מסך יצירת לוק:
 * - בחירת תמונה (Gallery)
 * - שמירה ל-Room
 */
package com.example.styleshare.ui.create

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.styleshare.R
import com.example.styleshare.databinding.FragmentCreateLookBinding
import com.example.styleshare.utils.ImageStorage
import com.example.styleshare.utils.Result
import com.google.firebase.auth.FirebaseAuth
import kotlin.String

class CreateLookFragment : Fragment(R.layout.fragment_create_look) {

    private var _binding: FragmentCreateLookBinding? = null
    private val binding get() = _binding!!

    private val vm: CreateLookViewModel by viewModels()

    private var selectedImageUri: Uri? = null
    private var savedImagePath: String? = null

    /** בוחר תמונה מהגלריה */
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.ivPreview.visibility = View.VISIBLE
                binding.llPickerButtons.visibility = View.GONE
                binding.ivPreview.setImageURI(uri)

                // שמירה מקומית של התמונה
                savedImagePath = ImageStorage.saveImageToInternalStorage(requireContext(), uri)
            }
        }

    /** חיבור UI */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateLookBinding.bind(view)

        binding.btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        
        binding.btnCamera.setOnClickListener {
            // For now, redirect to gallery
            pickImageLauncher.launch("image/*")
        }

        binding.btnSaveLook.setOnClickListener {
            val title = binding.etLookTitle.text?.toString()?.trim().orEmpty()
            val desc = binding.etLookDesc.text?.toString()?.trim().orEmpty()

            if (title.isBlank()) {
                Toast.makeText(requireContext(), "חייב כותרת", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (savedImagePath == null) {
                Toast.makeText(requireContext(), "חייב לבחור תמונה", Toast.LENGTH_SHORT).show()
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
                is Result.Loading -> Toast.makeText(requireContext(), "שומר...", Toast.LENGTH_SHORT).show()
                is Result.Success -> {
                    Toast.makeText(requireContext(), "הלוק נשמר ✅", Toast.LENGTH_SHORT).show()
                    clearForm()
                }
                is Result.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** מנקה שדות אחרי שמירה */
    private fun clearForm() {
        binding.etLookTitle.setText("")
        binding.etLookDesc.setText("")
        binding.etLookTags.setText("")
        binding.ivPreview.setImageResource(android.R.drawable.ic_menu_gallery)
        binding.ivPreview.visibility = View.GONE
        binding.llPickerButtons.visibility = View.VISIBLE
        selectedImageUri = null
        savedImagePath = null
    }

    /** ניקוי Binding */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
