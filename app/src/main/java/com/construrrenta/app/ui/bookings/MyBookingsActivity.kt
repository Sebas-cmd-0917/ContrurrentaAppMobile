package com.construrrenta.app.ui.bookings

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.construrrenta.app.data.api.BookingApiService
import com.construrrenta.app.data.api.RetrofitClient
import com.construrrenta.app.databinding.ActivityMyBookingsBinding
import kotlinx.coroutines.launch

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyBookingsBinding
    private lateinit var viewModel: BookingViewModel
    private lateinit var adapter: BookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        val bookingApi = RetrofitClient.getInstance(this).create(BookingApiService::class.java)
        viewModel = BookingViewModel(bookingApi)

        setupRecyclerView()
        setupSwipeRefresh()
        observeState()

        viewModel.fetchMyHistory()
    }

    private fun setupRecyclerView() {
        adapter = BookingAdapter(
            emptyList(),
            onPay = { booking -> showPaymentDialog(booking.id) },
            onCancel = { booking -> showConfirmDialog("¿Cancelar esta reserva?") { viewModel.cancelBooking(booking.id) } },
            onReportIssue = { booking -> showReportDialog(booking.id) }
        )
        binding.rvBookings.layoutManager = LinearLayoutManager(this)
        binding.rvBookings.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            androidx.core.content.ContextCompat.getColor(this, com.construrrenta.app.R.color.primaryColor)
        )
        binding.swipeRefresh.setOnRefreshListener { viewModel.fetchMyHistory() }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.swipeRefresh.isRefreshing = false
                        when (state) {
                            is BookingUiState.Loading -> binding.swipeRefresh.isRefreshing = true
                            is BookingUiState.Success -> {
                                binding.rvBookings.visibility = View.VISIBLE
                                binding.tvEmpty.visibility = View.GONE
                                binding.layoutError.visibility = View.GONE
                                adapter.updateData(state.bookings)
                            }
                            is BookingUiState.Empty -> {
                                binding.rvBookings.visibility = View.GONE
                                binding.tvEmpty.visibility = View.VISIBLE
                                binding.layoutError.visibility = View.GONE
                            }
                            is BookingUiState.Error -> {
                                binding.rvBookings.visibility = View.GONE
                                binding.tvEmpty.visibility = View.GONE
                                binding.layoutError.visibility = View.VISIBLE
                                binding.tvErrorMessage.text = state.message
                                binding.btnRetry.setOnClickListener { viewModel.fetchMyHistory() }
                            }
                        }
                    }
                }
                launch {
                    viewModel.actionState.collect { state ->
                        when (state) {
                            is BookingActionState.Success -> {
                                Toast.makeText(this@MyBookingsActivity, state.message, Toast.LENGTH_SHORT).show()
                                viewModel.resetActionState()
                            }
                            is BookingActionState.Error -> {
                                Toast.makeText(this@MyBookingsActivity, state.message, Toast.LENGTH_SHORT).show()
                                viewModel.resetActionState()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun showPaymentDialog(bookingId: String) {
        val methods = arrayOf("CREDIT_CARD", "PAYPAL", "TRANSFER", "CASH")
        val displayNames = arrayOf("Tarjeta de Crédito", "PayPal", "Transferencia", "Efectivo")
        AlertDialog.Builder(this)
            .setTitle("Método de pago")
            .setItems(displayNames) { _, which ->
                viewModel.confirmPayment(bookingId, methods[which])
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showReportDialog(bookingId: String) {
        val input = EditText(this).apply {
            hint = "Describe el problema..."
            setPadding(48, 32, 48, 32)
        }
        AlertDialog.Builder(this)
            .setTitle("Reportar problema")
            .setView(input)
            .setPositiveButton("Enviar") { _, _ ->
                val description = input.text.toString().trim()
                if (description.isNotEmpty()) {
                    viewModel.reportArrivalIssue(bookingId, description)
                } else {
                    Toast.makeText(this, "La descripción es obligatoria", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showConfirmDialog(message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("Sí") { _, _ -> onConfirm() }
            .setNegativeButton("No", null)
            .show()
    }
}
