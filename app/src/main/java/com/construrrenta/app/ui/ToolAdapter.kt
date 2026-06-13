package com.construrrenta.app.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.construrrenta.app.BuildConfig
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
            // 1. Textos sincronizados con tu XML
            tvToolName.text = tool.name
            tvToolDescription.text = tool.description
            tvToolPrice.text = "$ ${tool.pricePerDay} / día"

            // Mostramos el stock en el campo tvToolBrand de tu XML
            tvToolBrand.text = if (tool.stock > 0) "Stock disponible: ${tool.stock}" else "Agotado"

            // 2. Imágenes
            if (!tool.imageUrl.isNullOrBlank()) {
                val imageUrl = if (tool.imageUrl.startsWith("http")) {
                    tool.imageUrl
                } else {
                    val serverRootUrl = BuildConfig.BASE_URL.substringBefore("api/v1")
                    "${serverRootUrl.removeSuffix("/")}/${tool.imageUrl.removePrefix("/")}"
                }

                Log.d("IMAGEN_DEBUG", "Cargando foto en: $imageUrl")

                ivToolImage.load(imageUrl) {
                    crossfade(true)
                    placeholder(android.R.color.darker_gray)
                    error(android.R.color.holo_red_light)
                }
            } else {
                ivToolImage.setImageResource(android.R.color.darker_gray)
            }

            // 3. Clics
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