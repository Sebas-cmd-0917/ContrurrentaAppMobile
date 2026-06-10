package com.construrrenta.app.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.construrrenta.app.R
import com.construrrenta.app.data.model.BookingRequest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ToolDetailActivity : AppCompatActivity() {

    private var startDate: Date? = null
    private var endDate: Date? = null
    private lateinit var toolId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tool_detail)

        toolId = intent.getStringExtra("TOOL_ID") ?: ""
        val toolName = intent.getStringExtra("TOOL_NAME")
        val toolPrice = intent.getDoubleExtra("TOOL_PRICE", 0.0)

        val tvDetailName = findViewById<TextView>(R.id.tvDetailName)
        val tvDetailPrice = findViewById<TextView>(R.id.tvDetailPrice)
        val btnSelectDates = findViewById<Button>(R.id.btnSelectDates)
        val btnBookNow = findViewById<Button>(R.id.btnBookNow)

        tvDetailName.text = toolName
        tvDetailPrice.text = "$ $toolPrice / día"

        btnSelectDates.setOnClickListener {
            showDatePicker { start ->
                startDate = start
                showDatePicker { end ->
                    endDate = end
                    btnSelectDates.text = "Fechas: ${formatToDisplay(start)} - ${formatToDisplay(end)}"
                }
            }
        }

        btnBookNow.setOnClickListener {
            if (startDate != null && endDate != null && toolId.isNotEmpty()) {
                executeBooking()
            } else {
                Toast.makeText(this, "Por favor, selecciona las fechas primero", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance()
                // Se setea a mediodía para evitar problemas de zona horaria con el backend
                selected.set(year, month, dayOfMonth, 12, 0, 0)
                onDateSelected(selected.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun formatToDisplay(date: Date): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }

    private fun executeBooking() {
        // Mapeo exacto del LocalDateTime de Spring Boot
        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        // Aquí extraes el ID del usuario del SessionManager que hizo tu compañero
        val request = BookingRequest(
            userId = "ID_DEL_USUARIO_AQUI",
            toolId = toolId,
            startDate = isoFormatter.format(startDate!!),
            endDate = isoFormatter.format(endDate!!)
        )

        // TODO: Llamar al ViewModel o ApiService con el request
        Toast.makeText(this, "Simulando envío a la API...", Toast.LENGTH_SHORT).show()
    }
}