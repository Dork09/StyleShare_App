/**
 * מטרת הקובץ:
 * מסך MyLooks - מציג רק לוקים של המשתמש
 */
package com.example.styleshare.ui.mylooks

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styleshare.R
import com.example.styleshare.databinding.FragmentMyLooksBinding
import com.example.styleshare.ui.home.LooksAdapter
import com.example.styleshare.utils.Result

class MyLooksFragment : Fragment(R.layout.fragment_my_looks) {

    private var _binding: FragmentMyLooksBinding? = null
    private val binding get() = _binding!!

    private val vm: MyLooksViewModel by viewModels()
    private lateinit var adapter: LooksAdapter

    /** UI */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyLooksBinding.bind(view)

        vm.init(requireContext())

        adapter = LooksAdapter(
            items = emptyList(),
            onItemClick = { look ->
                val action = MyLooksFragmentDirections.actionMyLooksFragmentToEditLookFragment(look.id)
                findNavController().navigate(action)
            },
            onFavClick = { look ->
                val action = MyLooksFragmentDirections.actionMyLooksFragmentToLookDetailsFragment(look.id)
                findNavController().navigate(action)
            }
        )

        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        binding.rv.adapter = adapter

        vm.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Result.Loading -> binding.progress.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progress.visibility = View.GONE
                    adapter.submitList(state.data)
                    binding.tvEmpty.visibility = if (state.data.isEmpty()) View.VISIBLE else View.GONE
                }
                is Result.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.tvEmpty.text = state.message
                }
            }
        }

        vm.loadMyLooks()
    }

    override fun onResume() {
        super.onResume()
        vm.loadMyLooks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
