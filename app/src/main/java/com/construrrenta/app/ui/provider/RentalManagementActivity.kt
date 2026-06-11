package com.construrrenta.app.ui.provider

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.construrrenta.app.data.api.BookingApiService
import com.construrrenta.app.data.api.RetrofitClient
import com.construrrenta.app.data.local.SessionManager
import com.construrrenta.app.data.model.BookingResponse
import com.construrrenta.app.data.model.ReturnRequest
import com.construrrenta.app.databinding.ActivityRentalManagementBinding
import com.construrrenta.app.databinding.ItemRentalBinding
import kotlinx.coroutines.launch

class RentalManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRentalManagementBinding
    private lateinit var bookingApi: BookingApiService
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: RentalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRentalManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        bookingApi = RetrofitClient.getInstance(this).create(BookingApiService::class.java)

        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = RentalAdapter(
            emptyList(),
            onApprove = { booking -> approveBooking(booking.id) },
            onReject = { booking -> rejectBooking(booking.id) },
            onReturn = { booking -> showReturnDialog(booking.id) }
        )
        binding.rvRentals.layoutManager = LinearLayoutManager(this)
        binding.rvRentals.adapter = adapter

        binding.swipeRefresh.setColorSchemeColors(
            androidx.core.content.ContextCompat.getColor(this, com.construrrenta.app.R.color.primaryColor)
        )
        binding.swipeRefresh.setOnRefreshListener { loadRentals() }

        loadRentals()
    }

    private fun loadRentals() {
        val providerId = sessionManager.getUserId() ?: return
        binding.swipeRefresh.isRefreshing = true

        lifecycleScope.launch {
            try {
                val response = bookingApi.getProviderBookings(providerId)
                binding.swipeRefresh.isRefreshing = false
                if (response.isSuccessful && response.body() != null) {
                    val bookings = response.body()!!
                    if (bookings.isEmpty()) {
                        binding.rvRentals.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        binding.rvRentals.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
                        adapter.updateData(bookings)
                    }
                }
            } catch (e: Exception) {
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@RentalManagementActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun approveBooking(bookingId: String) {
        lifecycleScope.launch {
            try {
                val response = bookingApi.approveBooking(bookingId)
                if (response.isSuccessful) {
                    Toast.makeText(this@RentalManagementActivity, "Reserva aprobada", Toast.LENGTH_SHORT).show()
                    loadRentals()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RentalManagementActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun rejectBooking(bookingId: String) {
        AlertDialog.Builder(this)
            .setMessage("¿Rechazar esta reserva?")
            .setPositiveButton("Sí") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val response = bookingApi.rejectBooking(bookingId)
                        if (response.isSuccessful) {
                            Toast.makeText(this@RentalManagementActivity, "Reserva rechazada", Toast.LENGTH_SHORT).show()
                            loadRentals()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@RentalManagementActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showReturnDialog(bookingId: String) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }
        val cbDamage = CheckBox(this).apply { text = "¿Tiene daños?" }
        val etDescription = EditText(this).apply {
            hint = "Descripción del daño"
            visibility = View.GONE
        }
        val etCost = EditText(this).apply {
            hint = "Costo de reparación"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            visibility = View.GONE
        }

        cbDamage.setOnCheckedChangeListener { _, checked ->
            etDescription.visibility = if (checked) View.VISIBLE else View.GONE
            etCost.visibility = if (checked) View.VISIBLE else View.GONE
        }

        layout.addView(cbDamage)
        layout.addView(etDescription)
        layout.addView(etCost)

        AlertDialog.Builder(this)
            .setTitle("Registrar devolución")
            .setView(layout)
            .setPositiveButton("Confirmar") { _, _ ->
                val request = ReturnRequest(
                    withDamage = cbDamage.isChecked,
                    damageDescription = etDescription.text.toString().trim().ifBlank { null },
                    repairCost = etCost.text.toString().toDoubleOrNull()
                )
                lifecycleScope.launch {
                    try {
                        val response = bookingApi.returnTool(bookingId, request)
                        if (response.isSuccessful) {
                            Toast.makeText(this@RentalManagementActivity, "Devolución registrada", Toast.LENGTH_SHORT).show()
                            loadRentals()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@RentalManagementActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Adapter interno
    class RentalAdapter(
        private var bookings: List<BookingResponse>,
        private val onApprove: (BookingResponse) -> Unit,
        private val onReject: (BookingResponse) -> Unit,
        private val onReturn: (BookingResponse) -> Unit
    ) : RecyclerView.Adapter<RentalAdapter.VH>() {

        class VH(val binding: ItemRentalBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val binding = ItemRentalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(binding)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val booking = bookings[position]
            with(holder.binding) {
                tvRentalToolName.text = booking.tool?.name ?: "Herramienta"
                tvRentalDates.text = "Del ${formatDate(booking.startDate)} al ${formatDate(booking.endDate)}"
                tvRentalTotal.text = "Total: $ ${"%.2f".format(booking.totalPrice)}"

                tvRentalStatus.text = translateStatus(booking.status)
                val bg = GradientDrawable().apply {
                    cornerRadius = 12f
                    setColor(getStatusColor(booking.status))
                }
                tvRentalStatus.background = bg

                btnApprove.visibility = if (booking.status == "PENDING") View.VISIBLE else View.GONE
                btnReject.visibility = if (booking.status == "PENDING") View.VISIBLE else View.GONE
                btnReturn.visibility = if (booking.status == "CONFIRMED") View.VISIBLE else View.GONE

                btnApprove.setOnClickListener { onApprove(booking) }
                btnReject.setOnClickListener { onReject(booking) }
                btnReturn.setOnClickListener { onReturn(booking) }
            }
        }

        override fun getItemCount() = bookings.size

        fun updateData(newBookings: List<BookingResponse>) {
            bookings = newBookings
            notifyDataSetChanged()
        }

        private fun formatDate(iso: String): String = try {
            val parts = iso.substring(0, 10).split("-")
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } catch (_: Exception) { iso }

        private fun translateStatus(s: String) = when (s) {
            "PENDING" -> "PENDIENTE"; "CONFIRMED" -> "CONFIRMADA"
            "CANCELLED" -> "CANCELADA"; "COMPLETED" -> "COMPLETADA"
            else -> s
        }

        private fun getStatusColor(s: String) = when (s) {
            "PENDING" -> Color.parseColor("#FF9800"); "CONFIRMED" -> Color.parseColor("#28A745")
            "CANCELLED" -> Color.parseColor("#DC3545"); "COMPLETED" -> Color.parseColor("#00509D")
            else -> Color.parseColor("#6C757D")
        }
    }
}
