package com.construrrenta.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.construrrenta.app.BuildConfig
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
            tvToolBrand.text = "Stock: ${tool.stock}"
            tvToolName.text = tool.name
            tvToolDescription.text = tool.description
            tvToolPrice.text = "$ ${tool.pricePerDay} / día"

            // Cargar imagen con Coil, manejando rutas relativas y absolutas
            if (!tool.imageUrl.isNullOrBlank()) {
                val imageUrl = if (tool.imageUrl.startsWith("http")) {
                    tool.imageUrl
                } else {
                    "${BuildConfig.BASE_URL.removeSuffix("/")}/${tool.imageUrl.removePrefix("/")}"
                }

                ivToolImage.load(imageUrl) {
                    placeholder(R.drawable.ic_launcher_background)
                    error(R.drawable.ic_launcher_background)
                    crossfade(true)
                }
            } else {
                ivToolImage.setImageResource(R.drawable.ic_launcher_background)
            }

            // Click listener para la tarjeta entera y el botón Añadir
            root.setOnClickListener { onToolClick(tool) }
            btnAdd.setOnClickListener { onToolClick(tool) }
        }
    }

    override fun getItemCount(): Int = tools.size

    fun updateData(newTools: List<ToolResponse>) {
        this.tools = newTools
        notifyDataSetChanged()
    }
}