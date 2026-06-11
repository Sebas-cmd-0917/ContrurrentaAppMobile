package com.construrrenta.app.ui.provider

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.construrrenta.app.data.api.RetrofitClient
import com.construrrenta.app.data.api.ToolApiService
import com.construrrenta.app.data.local.SessionManager
import com.construrrenta.app.data.model.ToolResponse
import com.construrrenta.app.databinding.ActivityProviderInventoryBinding
import com.construrrenta.app.ui.ToolAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ProviderInventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderInventoryBinding
    private lateinit var toolApi: ToolApiService
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: ToolAdapter

    private val _tools = MutableStateFlow<List<ToolResponse>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        toolApi = RetrofitClient.getInstance(this).create(ToolApiService::class.java)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupFab()
        setupSwipeRefresh()

        loadMyTools()
    }

    private fun setupRecyclerView() {
        adapter = ToolAdapter(emptyList()) { tool ->
            // Clic en herramienta -> opciones: Editar / Eliminar
            showToolOptions(tool)
        }
        binding.rvTools.layoutManager = LinearLayoutManager(this)
        binding.rvTools.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, CreateEditToolActivity::class.java))
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            androidx.core.content.ContextCompat.getColor(this, com.construrrenta.app.R.color.primaryColor)
        )
        binding.swipeRefresh.setOnRefreshListener { loadMyTools() }
    }

    private fun loadMyTools() {
        val providerId = sessionManager.getUserId() ?: return
        binding.swipeRefresh.isRefreshing = true

        lifecycleScope.launch {
            try {
                val response = toolApi.getToolsByProvider(providerId)
                binding.swipeRefresh.isRefreshing = false
                if (response.isSuccessful && response.body() != null) {
                    val tools = response.body()!!
                    if (tools.isEmpty()) {
                        binding.rvTools.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        binding.rvTools.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
                        adapter.updateData(tools)
                    }
                } else {
                    Toast.makeText(this@ProviderInventoryActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@ProviderInventoryActivity, "Error de red: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showToolOptions(tool: ToolResponse) {
        AlertDialog.Builder(this)
            .setTitle(tool.name)
            .setItems(arrayOf("Editar", "Eliminar")) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, CreateEditToolActivity::class.java).apply {
                            putExtra("TOOL_ID", tool.id)
                            putExtra("TOOL_NAME", tool.name)
                            putExtra("TOOL_DESCRIPTION", tool.description)
                            putExtra("TOOL_PRICE", tool.pricePerDay)
                            putExtra("TOOL_IMAGE", tool.imageUrl)
                            putExtra("TOOL_STOCK", tool.stock)
                        }
                        startActivity(intent)
                    }
                    1 -> confirmDelete(tool)
                }
            }
            .show()
    }

    private fun confirmDelete(tool: ToolResponse) {
        AlertDialog.Builder(this)
            .setMessage("¿Eliminar '${tool.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val response = toolApi.deleteTool(tool.id)
                        if (response.isSuccessful) {
                            Toast.makeText(this@ProviderInventoryActivity, "Eliminada", Toast.LENGTH_SHORT).show()
                            loadMyTools()
                        } else {
                            Toast.makeText(this@ProviderInventoryActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@ProviderInventoryActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadMyTools()
    }
}
