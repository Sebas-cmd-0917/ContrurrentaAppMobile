package com.construrrenta.app.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.construrrenta.app.R
import com.construrrenta.app.data.api.BookingApiService
import com.construrrenta.app.data.api.RetrofitClient
import com.construrrenta.app.data.local.SessionManager
import com.construrrenta.app.data.model.BookingRequest
import com.construrrenta.app.databinding.ActivityToolDetailBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ToolDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityToolDetailBinding
    private var startDate: Date? = null
    private var endDate: Date? = null
    private lateinit var toolId: String
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToolDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        toolId = intent.getStringExtra("TOOL_ID") ?: ""
        val toolName = intent.getStringExtra("TOOL_NAME") ?: ""
        val toolPrice = intent.getDoubleExtra("TOOL_PRICE", 0.0)
        val toolDescription = intent.getStringExtra("TOOL_DESCRIPTION") ?: ""
        val toolImage = intent.getStringExtra("TOOL_IMAGE")
        val toolStock = intent.getIntExtra("TOOL_STOCK", 0)

        binding.tvDetailName.text = toolName
        binding.tvDetailPrice.text = "$ $toolPrice / día"
        binding.tvDetailDescription.text = toolDescription
        binding.tvDetailStock.text = "Stock disponible: $toolStock"

        if (!toolImage.isNullOrBlank()) {
            binding.ivDetailImage.load(toolImage) {
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_background)
                crossfade(true)
            }
        }

        binding.btnSelectDates.setOnClickListener {
            if (!sessionManager.isLoggedIn()) {
                navigateToLogin()
                return@setOnClickListener
            }
            showDatePicker { start ->
                startDate = start
                showDatePicker { end ->
                    endDate = end
                    if (end.before(start)) {
                        Toast.makeText(this, "La fecha fin debe ser posterior al inicio", Toast.LENGTH_SHORT).show()
                        endDate = null
                    } else {
                        binding.btnSelectDates.text = "Fechas: ${formatToDisplay(start)} - ${formatToDisplay(end)}"
                        // Calcular costo estimado
                        val days = ((end.time - start.time) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
                        val total = toolPrice * days
                        binding.tvEstimatedCost.text = "Costo estimado: $ ${"%.2f".format(total)} ($days días)"
                        binding.tvEstimatedCost.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.btnBookNow.setOnClickListener {
            if (!sessionManager.isLoggedIn()) {
                navigateToLogin()
                return@setOnClickListener
            }
            if (startDate != null && endDate != null && toolId.isNotEmpty()) {
                executeBooking()
            } else {
                Toast.makeText(this, "Por favor, selecciona las fechas primero", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun navigateToLogin() {
        val intent = android.content.Intent(this, com.construrrenta.app.ui.auth.LoginActivity::class.java)
        startActivity(intent)
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance()
                selected.set(year, month, dayOfMonth, 12, 0, 0)
                onDateSelected(selected.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.minDate = System.currentTimeMillis()
        dialog.show()
    }

    private fun formatToDisplay(date: Date): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }

    private fun executeBooking() {
        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        val request = BookingRequest(
            userId = sessionManager.getUserId() ?: "",
            toolId = toolId,
            startDate = isoFormatter.format(startDate!!),
            endDate = isoFormatter.format(endDate!!)
        )

        binding.btnBookNow.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val bookingApi = RetrofitClient.getInstance(this@ToolDetailActivity)
                    .create(BookingApiService::class.java)
                val response = bookingApi.createBooking(request)
                if (response.isSuccessful) {
                    Toast.makeText(this@ToolDetailActivity, "¡Reserva creada exitosamente!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Datos inválidos. Verifica las fechas."
                        401 -> "Sesión expirada. Inicia sesión de nuevo."
                        409 -> "La herramienta no está disponible en esas fechas."
                        else -> "Error del servidor: ${response.code()}"
                    }
                    Toast.makeText(this@ToolDetailActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ToolDetailActivity, "Error de conexión: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnBookNow.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}