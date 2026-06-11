package com.construrrenta.app.ui.provider

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.construrrenta.app.data.api.RetrofitClient
import com.construrrenta.app.data.api.ToolApiService
import com.construrrenta.app.data.local.SessionManager
import com.construrrenta.app.data.model.ToolRequest
import com.construrrenta.app.databinding.ActivityCreateEditToolBinding
import kotlinx.coroutines.launch

class CreateEditToolActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEditToolBinding
    private lateinit var toolApi: ToolApiService
    private lateinit var sessionManager: SessionManager

    private var editToolId: String? = null // null = crear, non-null = editar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEditToolBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        toolApi = RetrofitClient.getInstance(this).create(ToolApiService::class.java)

        binding.toolbar.setNavigationOnClickListener { finish() }

        // Verificar si es modo edición
        editToolId = intent.getStringExtra("TOOL_ID")
        if (editToolId != null) {
            binding.toolbar.title = "Editar Herramienta"
            binding.btnSave.text = "Actualizar"
            populateForEdit()
        } else {
            binding.toolbar.title = "Nueva Herramienta"
        }

        binding.btnSave.setOnClickListener { saveTool() }
    }

    private fun populateForEdit() {
        binding.etName.setText(intent.getStringExtra("TOOL_NAME") ?: "")
        binding.etDescription.setText(intent.getStringExtra("TOOL_DESCRIPTION") ?: "")
        binding.etPrice.setText(intent.getDoubleExtra("TOOL_PRICE", 0.0).toString())
        binding.etImageUrl.setText(intent.getStringExtra("TOOL_IMAGE") ?: "")
        binding.etStock.setText(intent.getIntExtra("TOOL_STOCK", 1).toString())
    }

    private fun saveTool() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()
        val imageUrl = binding.etImageUrl.text.toString().trim()
        val stockStr = binding.etStock.text.toString().trim()

        // Validaciones
        if (name.isBlank()) {
            showError("El nombre es obligatorio"); return
        }
        if (description.isBlank()) {
            showError("La descripción es obligatoria"); return
        }
        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            showError("El precio debe ser mayor a 0"); return
        }
        val stock = stockStr.toIntOrNull() ?: 1
        if (stock < 0) {
            showError("El stock no puede ser negativo"); return
        }

        val providerId = sessionManager.getUserId() ?: run {
            showError("Error: sin sesión activa"); return
        }

        val request = ToolRequest(
            name = name,
            description = description,
            pricePerDay = price,
            imageUrl = imageUrl.ifBlank { null },
            providerId = providerId,
            stock = stock
        )

        binding.btnSave.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        binding.tvError.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = if (editToolId != null) {
                    toolApi.updateTool(editToolId!!, request)
                } else {
                    toolApi.createTool(request)
                }

                if (response.isSuccessful) {
                    val msg = if (editToolId != null) "Herramienta actualizada" else "Herramienta creada"
                    Toast.makeText(this@CreateEditToolActivity, msg, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    showError("Error del servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                showError("Error de conexión: ${e.localizedMessage}")
            } finally {
                binding.btnSave.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
}
