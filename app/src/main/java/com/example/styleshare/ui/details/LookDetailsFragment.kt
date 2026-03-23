package com.example.styleshare.ui.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.styleshare.R
import com.example.styleshare.databinding.FragmentLookDetailsBinding
import com.example.styleshare.utils.Result

class LookDetailsFragment : Fragment(R.layout.fragment_look_details) {

    private var _binding: FragmentLookDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: LookDetailsFragmentArgs by navArgs()
    private val vm: LookDetailsViewModel by viewModels()
    private lateinit var commentsAdapter: CommentsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLookDetailsBinding.bind(view)

        vm.init(requireContext())

        vm.lookState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Result.Loading -> binding.progress.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progress.visibility = View.GONE
                    val look = state.data

                    binding.tvTitle.text      = look.title
                    binding.tvDesc.text       = look.description
                    binding.tvLikesCount.text = "${look.likesCount} Likes"

                    binding.cgTags.removeAllViews()
                    for (tag in look.tags) {
                        val chip = com.google.android.material.chip.Chip(requireContext())
                        chip.text        = "#$tag"
                        chip.isClickable = false
                        chip.isCheckable = false
                        binding.cgTags.addView(chip)
                    }

                    binding.ivLook.load(look.imageUrl) {
                        crossfade(true)
                    }

                    binding.btnLike.text = if (look.isFavorite) "Liked" else "Like"

                    val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance()
                        .currentUser?.uid ?: "local_user"
                    binding.actionLayout.visibility =
                        if (currentUid == look.createdByUid) View.VISIBLE else View.GONE
                }
                is Result.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.tvTitle.text = state.message
                }
            }
        }

        commentsAdapter = CommentsAdapter()
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = commentsAdapter
        }

        vm.commentsState.observe(viewLifecycleOwner) { state ->
            if (state is Result.Success) commentsAdapter.submitList(state.data)
        }

        binding.btnLike.setOnClickListener { vm.toggleFavorite(args.lookId) }

        binding.btnSendComment.setOnClickListener {
            val text = binding.etComment.text.toString()
            if (text.isNotBlank()) {
                vm.addComment(args.lookId, text)
                binding.etComment.text.clear()
            }
        }

        binding.btnEdit.setOnClickListener {
            val action = LookDetailsFragmentDirections
                .actionLookDetailsFragmentToEditLookFragment(args.lookId)
            findNavController().navigate(action)
        }

        binding.btnDelete.setOnClickListener {
            vm.deleteLook(args.lookId)
            findNavController().navigate(R.id.homeFragment)
        }

        vm.loadLook(args.lookId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
