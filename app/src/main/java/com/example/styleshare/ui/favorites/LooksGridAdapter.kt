package com.example.styleshare.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
        
        // Fetch real username instead of hardcoding, but we'll try to find authorName if present.
        // Look model has createdByUid. In a real app we'd fetch the user's name. For now let's show "User".
        holder.binding.tvUserName.text = "User" // Or fetch from somewhere

        val req = if (look.imagePath.startsWith("http")) {
            Picasso.get().load(look.imagePath)
        } else {
            Picasso.get().load(File(look.imagePath))
        }
        
        req.fit()
            .centerCrop()
            .into(holder.binding.ivLook)

        if (look.isFavorite) {
            holder.binding.ivFavorite.setColorFilter(holder.itemView.context.getColor(com.example.styleshare.R.color.pink_gradient_start))
        } else {
            holder.binding.ivFavorite.setColorFilter(holder.itemView.context.getColor(com.example.styleshare.R.color.white))
        }

        holder.itemView.setOnClickListener { onItemClick(look) }
        holder.binding.ivFavorite.setOnClickListener { onFavClick(look) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Look>) {
        items = newItems
        notifyDataSetChanged()
    }
}
