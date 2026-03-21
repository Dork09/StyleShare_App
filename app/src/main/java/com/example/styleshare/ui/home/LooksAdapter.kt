package com.example.styleshare.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.styleshare.R
import com.example.styleshare.databinding.ItemLookBinding
import com.example.styleshare.model.Look
import com.squareup.picasso.Picasso
import java.io.File

class LooksAdapter(
    private var items: List<Look>,
    private val onItemClick: (Look) -> Unit,
    private val onFavClick: (Look) -> Unit
) : RecyclerView.Adapter<LooksAdapter.LookVH>() {

    inner class LookVH(val binding: ItemLookBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LookVH {
        val binding = ItemLookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LookVH(binding)
    }

    override fun onBindViewHolder(holder: LookVH, position: Int) {
        val look = items[position]

        holder.binding.tvTitle.text = look.title
        holder.binding.tvDesc.text = look.description
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
        holder.binding.tvCommentsCount.text = "${look.commentsCount} Comments"
        holder.binding.tvLikesCount.text = "${look.likesCount} Likes"

        holder.binding.cgTags.removeAllViews()
        for (tag in look.tags) {
            val chip = com.google.android.material.chip.Chip(holder.itemView.context)
            chip.text = "#$tag"
            chip.isClickable = false
            chip.isCheckable = false
            holder.binding.cgTags.addView(chip)
        }

        holder.binding.cardLook.setOnClickListener { onItemClick(look) }
        holder.binding.btnLike.setOnClickListener {
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

    private fun applyFavoriteTint(binding: ItemLookBinding, isFavorite: Boolean) {
        val colorRes = if (isFavorite) R.color.pink_gradient_start else R.color.text_primary
        binding.btnLike.setColorFilter(binding.root.context.getColor(colorRes))
    }
}
