/**
 * מטרת הקובץ:
 * Adapter ל-RecyclerView שמציג כרטיס לוק יפה.
 */
package com.example.styleshare.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.styleshare.databinding.ItemLookBinding
import com.example.styleshare.model.Look
import com.squareup.picasso.Picasso
import java.io.File

class LooksAdapter(
    private var items: List<Look>,
    private val onItemClick: (Look) -> Unit,
    private val onFavClick: (Look) -> Unit
) : RecyclerView.Adapter<LooksAdapter.LookVH>() {

    /** ViewHolder עם ViewBinding */
    inner class LookVH(val binding: ItemLookBinding) : RecyclerView.ViewHolder(binding.root)

    /** יצירת שורה */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LookVH {
        val binding = ItemLookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LookVH(binding)
    }

    /** קישור נתונים לשורה */
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

        // ✅ MaterialButton -> IconButton. Change icon tint based on isFavorite
        if (look.isFavorite) {
            holder.binding.btnLike.setColorFilter(holder.itemView.context.getColor(com.example.styleshare.R.color.pink_gradient_start))
        } else {
            holder.binding.btnLike.setColorFilter(holder.itemView.context.getColor(com.example.styleshare.R.color.text_primary))
        }
        
        holder.binding.tvCommentsCount.text = "${look.commentsCount} Comments"
        holder.binding.tvLikesCount.text = "${look.likesCount} Likes"

        // Tags
        holder.binding.cgTags.removeAllViews()
        for (tag in look.tags) {
            val chip = com.google.android.material.chip.Chip(holder.itemView.context)
            chip.text = "#$tag"
            chip.isClickable = false
            chip.isCheckable = false
            holder.binding.cgTags.addView(chip)
        }

        holder.binding.cardLook.setOnClickListener { onItemClick(look) }
        holder.binding.btnLike.setOnClickListener { onFavClick(look) }
    }

    /** כמות פריטים */
    override fun getItemCount(): Int = items.size

    /** עדכון רשימה */
    fun submitList(newItems: List<Look>) {
        items = newItems
        notifyDataSetChanged()
    }
}
