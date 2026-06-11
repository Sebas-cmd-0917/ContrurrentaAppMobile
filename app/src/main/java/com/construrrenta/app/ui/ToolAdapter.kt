package com.construrrenta.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.construrrenta.app.R
import com.construrrenta.app.data.model.ToolResponse
import com.construrrenta.app.databinding.ItemToolBinding

class ToolAdapter(
    private var tools: List<ToolResponse>,
    private val onToolClick: (ToolResponse) -> Unit
) : RecyclerView.Adapter<ToolAdapter.ToolViewHolder>() {

    class ToolViewHolder(val binding: ItemToolBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val binding = ItemToolBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToolViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        val tool = tools[position]
        with(holder.binding) {
            tvToolName.text = tool.name
            tvToolDescription.text = tool.description
            tvToolPrice.text = "$ ${tool.pricePerDay} / día"
            tvToolStock.text = "Stock: ${tool.stock}"

            // Cargar imagen con Coil
            if (!tool.imageUrl.isNullOrBlank()) {
                ivToolImage.load(tool.imageUrl) {
                    placeholder(R.drawable.ic_launcher_background)
                    error(R.drawable.ic_launcher_background)
                    crossfade(true)
                }
            } else {
                ivToolImage.setImageResource(R.drawable.ic_launcher_background)
            }

            root.setOnClickListener { onToolClick(tool) }
        }
    }

    override fun getItemCount(): Int = tools.size

    fun updateData(newTools: List<ToolResponse>) {
        this.tools = newTools
        notifyDataSetChanged()
    }
}