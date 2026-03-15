/**
 * מטרת הקובץ:
 * מסך פרטי לוק:
 * - מציג תמונה/כותרת/תיאור
 * - מועדפים
 * - מעבר לעריכה (SafeArgs)
 * - מחיקה
 */
package com.example.styleshare.ui.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.styleshare.R
import com.example.styleshare.databinding.FragmentLookDetailsBinding
import com.example.styleshare.utils.Result
import com.squareup.picasso.Picasso
import java.io.File
import androidx.recyclerview.widget.LinearLayoutManager

class LookDetailsFragment : Fragment(R.layout.fragment_look_details) {

    private var _binding: FragmentLookDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: LookDetailsFragmentArgs by navArgs()
    private val vm: LookDetailsViewModel by viewModels()
    private lateinit var commentsAdapter: CommentsAdapter

    /** חיבור UI */
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

                    binding.tvTitle.text = look.title
                    binding.tvDesc.text = look.description
                    binding.tvLikesCount.text = "${look.likesCount} Likes"

                    // Set Tags
                    binding.cgTags.removeAllViews()
                    for (tag in look.tags) {
                        val chip = com.google.android.material.chip.Chip(requireContext())
                        chip.text = "#$tag"
                        chip.isClickable = false
                        chip.isCheckable = false
                        binding.cgTags.addView(chip)
                    }

                    val req = if (look.imagePath.startsWith("http")) {
                        Picasso.get().load(look.imagePath)
                    } else {
                        Picasso.get().load(File(look.imagePath))
                    }
                    req.fit()
                        .centerCrop()
                        .into(binding.ivLook)

                    binding.btnLike.text = if (look.isFavorite) "Liked" else "Like"
                    
                    // Permission Check: only the creator can edit/delete
                    val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"
                    if (currentUid == look.createdByUid) {
                        binding.actionLayout.visibility = View.VISIBLE
                    } else {
                        binding.actionLayout.visibility = View.GONE
                    }
                }
                is Result.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.tvTitle.text = state.message
                }
            }
        }

        // Setup Comments RecyclerView
        commentsAdapter = CommentsAdapter()
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = commentsAdapter
        }

        vm.commentsState.observe(viewLifecycleOwner) { state ->
            if (state is Result.Success) {
                commentsAdapter.submitList(state.data)
            }
        }

        binding.btnLike.setOnClickListener {
            vm.toggleFavorite(args.lookId)
        }

        binding.btnSendComment.setOnClickListener {
            val text = binding.etComment.text.toString()
            if (text.isNotBlank()) {
                vm.addComment(args.lookId, text)
                binding.etComment.text.clear()
            }
        }

        binding.btnEdit.setOnClickListener {
            val action = LookDetailsFragmentDirections.actionLookDetailsFragmentToEditLookFragment(args.lookId)
            findNavController().navigate(action)
        }

        binding.btnDelete.setOnClickListener {
            vm.deleteLook(args.lookId)
            findNavController().navigate(R.id.homeFragment)
        }

        vm.loadLook(args.lookId)
    }

    /** ניקוי */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
