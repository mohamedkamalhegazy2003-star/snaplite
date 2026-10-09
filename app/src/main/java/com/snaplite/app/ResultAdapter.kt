package com.snaplite.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.snaplite.app.databinding.ItemResultBinding
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class ResultAdapter(
    private val onClick: (StreamInfoItem) -> Unit
) : RecyclerView.Adapter<ResultAdapter.VH>() {

    private val items = mutableListOf<StreamInfoItem>()

    fun submit(list: List<StreamInfoItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class VH(val b: ItemResultBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemResultBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.title.text = item.name
        holder.b.sub.text = item.uploaderName ?: ""
        holder.b.thumb.load(item.thumbnails.firstOrNull()?.url)
        holder.b.root.setOnClickListener { onClick(item) }
    }
}
