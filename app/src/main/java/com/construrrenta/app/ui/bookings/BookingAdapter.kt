package com.construrrenta.app.ui.bookings

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.construrrenta.app.data.model.BookingResponse
import com.construrrenta.app.databinding.ItemBookingBinding

class BookingAdapter(
    private var bookings: List<BookingResponse>,
    private val onPay: (BookingResponse) -> Unit,
    private val onCancel: (BookingResponse) -> Unit,
    private val onReportIssue: (BookingResponse) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(val binding: ItemBookingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        with(holder.binding) {
            tvBookingToolName.text = booking.tool?.name ?: "Herramienta"
            tvBookingDates.text = "Del ${formatDate(booking.startDate)} al ${formatDate(booking.endDate)}"
            tvBookingTotal.text = "Total: $ ${"%.2f".format(booking.totalPrice)}"

            // Badge de estado con color
            tvBookingStatus.text = translateStatus(booking.status)
            val bg = GradientDrawable().apply {
                cornerRadius = 12f
                setColor(getStatusColor(booking.status))
            }
            tvBookingStatus.background = bg

            // Mostrar botones según estado
            btnPay.visibility = if (booking.status == "PENDING") View.VISIBLE else View.GONE
            btnCancel.visibility = if (booking.status == "PENDING" || booking.status == "CONFIRMED") View.VISIBLE else View.GONE
            btnReportIssue.visibility = if (booking.status == "CONFIRMED") View.VISIBLE else View.GONE

            btnPay.setOnClickListener { onPay(booking) }
            btnCancel.setOnClickListener { onCancel(booking) }
            btnReportIssue.setOnClickListener { onReportIssue(booking) }
        }
    }

    override fun getItemCount(): Int = bookings.size

    fun updateData(newBookings: List<BookingResponse>) {
        this.bookings = newBookings
        notifyDataSetChanged()
    }

    private fun formatDate(isoDate: String): String {
        return try {
            // Convierte "2025-06-15T12:00:00" a "15/06/2025"
            val parts = isoDate.substring(0, 10).split("-")
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } catch (_: Exception) { isoDate }
    }

    private fun translateStatus(status: String): String = when (status) {
        "PENDING" -> "PENDIENTE"
        "CONFIRMED" -> "CONFIRMADA"
        "CANCELLED" -> "CANCELADA"
        "COMPLETED" -> "COMPLETADA"
        else -> status
    }

    private fun getStatusColor(status: String): Int = when (status) {
        "PENDING" -> Color.parseColor("#FF9800")
        "CONFIRMED" -> Color.parseColor("#28A745")
        "CANCELLED" -> Color.parseColor("#DC3545")
        "COMPLETED" -> Color.parseColor("#00509D")
        else -> Color.parseColor("#6C757D")
    }
}
