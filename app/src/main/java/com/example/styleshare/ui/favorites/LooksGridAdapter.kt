package com.example.styleshare.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.styleshare.R
import com.example.styleshare.databinding.ItemLookGridBinding
import com.example.styleshare.model.Look
import com.squareup.picasso.Picasso
import java.io.File

class LooksGridAdapter(
    private var items: List<Look>,
    private val onItemClick: (Look) -> Unit,
    private val onFavClick: (Look) -> Unit
) : RecyclerView.Adapter<LooksGridAdapter.GridLookVH>() {

    inner class GridLookVH(val binding: ItemLookGridBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridLookVH {
        val binding = ItemLookGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GridLookVH(binding)
    }

    override fun onBindViewHolder(holder: GridLookVH, position: Int) {
        val look = items[position]

        holder.binding.tvTitle.text = look.title
        holder.binding.tvUserName.text = look.authorName

        val req = if (look.imagePath.startsWith("http")) {
            Picasso.get().load(look.imagePath)
        } else {
            Picasso.get().load(File(look.imagePath))
        }

        req.fit()
            .centerInside()
            .into(holder.binding.ivLook)

        applyFavoriteTint(holder.binding, look.isFavorite)

        holder.itemView.setOnClickListener { onItemClick(look) }
        holder.binding.ivFavorite.setOnClickListener {
            val toggledLook = look.copy(
                isFavorite = !look.isFavorite,
                likesCount = if (look.isFavorite) {
                    maxOf(0, look.likesCount - 1)
                } else {
                    look.likesCount + 1
                }
            )
            val updatedItems = items.toMutableList()
            updatedItems[position] = toggledLook
            items = updatedItems
            notifyItemChanged(position)
            onFavClick(look)
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Look>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun applyFavoriteTint(binding: ItemLookGridBinding, isFavorite: Boolean) {
        val colorRes = if (isFavorite) R.color.pink_gradient_start else R.color.white
        binding.ivFavorite.setColorFilter(binding.root.context.getColor(colorRes))
    }
}
