/**
 * מטרת הקובץ:
 * מסך מועדפים:
 * RecyclerView שמציג רק מועדפים
 */
package com.example.styleshare.ui.favorites

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styleshare.R
import com.example.styleshare.databinding.FragmentFavoritesBinding
import com.example.styleshare.ui.home.LooksAdapter
import com.example.styleshare.utils.Result

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val vm: FavoritesViewModel by viewModels()
    private lateinit var adapter: LooksGridAdapter

    /** חיבור UI */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavoritesBinding.bind(view)
        binding.cvTabs.visibility = View.GONE

        vm.init(requireContext())

        adapter = LooksGridAdapter(
            items = emptyList(),
            onItemClick = { look ->
                val action = FavoritesFragmentDirections
                    .actionFavoritesFragmentToLookDetailsFragment(look.id)
                findNavController().navigate(action)
            },
            onFavClick = { look ->
                // במועדפים נשלח לפרטים כדי לשנות אם רוצים
                val action = FavoritesFragmentDirections
                    .actionFavoritesFragmentToLookDetailsFragment(look.id)
                findNavController().navigate(action)
            }
        )

        binding.rv.layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)
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

        vm.loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        vm.loadFavorites()
    }

    /** ניקוי */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
