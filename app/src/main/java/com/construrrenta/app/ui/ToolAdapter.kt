package com.construrrenta.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.construrrenta.app.R
import com.construrrenta.app.data.model.ToolResponse

class ToolAdapter(
    private var tools: List<ToolResponse>,
    private val onToolClick: (ToolResponse) -> Unit
) : RecyclerView.Adapter<ToolAdapter.ToolViewHolder>() {

    class ToolViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvToolName)
        val tvPrice: TextView = view.findViewById(R.id.tvToolPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tool, parent, false)
        return ToolViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        val tool = tools[position]
        holder.tvName.text = tool.name
        holder.tvPrice.text = "$ ${tool.pricePerDay} / día"

        holder.itemView.setOnClickListener { onToolClick(tool) }
    }

    override fun getItemCount(): Int = tools.size

    fun updateData(newTools: List<ToolResponse>) {
        this.tools = newTools
        notifyDataSetChanged()
    }
}