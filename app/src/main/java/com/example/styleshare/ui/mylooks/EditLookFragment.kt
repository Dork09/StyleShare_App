package com.example.styleshare.ui.mylooks

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.styleshare.R
import com.example.styleshare.databinding.FragmentEditLookBinding
import com.example.styleshare.utils.Result
import com.example.styleshare.utils.Validators

class EditLookFragment : Fragment(R.layout.fragment_edit_look) {

    private var _binding: FragmentEditLookBinding? = null
    private val binding get() = _binding!!

    private val args: EditLookFragmentArgs by navArgs()
    private val vm: EditLookViewModel by viewModels()

    private var newImageUri: Uri? = null
    private var currentImageUrl: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            newImageUri = uri
            binding.ivPreview.load(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditLookBinding.bind(view)

        vm.lookState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Result.Loading -> binding.progress.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progress.visibility = View.GONE
                    val look = state.data
                    binding.etTitle.setText(look.title)
                    binding.etDesc.setText(look.description)
                    binding.etTags.setText(look.tags.joinToString(", "))
                    currentImageUrl = look.imageUrl
                    binding.ivPreview.load(look.imageUrl)
                }
                is Result.Error -> binding.progress.visibility = View.GONE
            }
        }

        vm.saveState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Result.Loading -> binding.progress.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progress.visibility = View.GONE
                    findNavController().navigate(R.id.myLooksFragment)
                }
                is Result.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.tvHint.text = state.message
                }
            }
        }

        binding.btnPickImage.setOnClickListener { pickImage.launch("image/*") }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text?.toString().orEmpty()
            val desc  = binding.etDesc.text?.toString().orEmpty()

            if (!Validators.isNotBlank(title)) {
                binding.etTitle.error = "כותרת חובה"
                return@setOnClickListener
            } else binding.etTitle.error = null

            if (!Validators.isNotBlank(desc)) {
                binding.etDesc.error = "תיאור חובה"
                return@setOnClickListener
            } else binding.etDesc.error = null

            if (newImageUri == null && currentImageUrl.isNullOrEmpty()) {
                binding.tvHint.text = "חסרה תמונה"
                return@setOnClickListener
            }

            val rawTags = binding.etTags.text?.toString()?.trim().orEmpty()
            val tags = if (rawTags.isNotBlank()) {
                rawTags.split(",").map { it.trim().removePrefix("#") }.filter { it.isNotBlank() }
            } else emptyList()

            vm.updateLook(
                lookId = args.lookId,
                title = title,
                desc = desc,
                newImageUri = newImageUri,
                currentImageUrl = currentImageUrl,
                tags = tags
            )
        }

        binding.btnDelete.setOnClickListener {
            vm.deleteLook(args.lookId)
            findNavController().navigate(R.id.myLooksFragment)
        }

        vm.loadLook(args.lookId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
