package com.construrrenta.app.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.construrrenta.app.R
import com.construrrenta.app.data.api.AdminApiService
import com.construrrenta.app.data.api.RetrofitClient
import com.construrrenta.app.data.model.ToolResponse
import com.construrrenta.app.data.model.UserResponse
import com.construrrenta.app.databinding.ActivityAdminReportsBinding
import kotlinx.coroutines.launch

class AdminReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminReportsBinding
    private lateinit var adminApi: AdminApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adminApi = RetrofitClient.getInstance(this).create(AdminApiService::class.java)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.rvTopTools.layoutManager = LinearLayoutManager(this)
        binding.rvTopUsers.layoutManager = LinearLayoutManager(this)

        loadDashboard()
    }

    private fun loadDashboard() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            // Dashboard Stats
            try {
                val statsResp = adminApi.getDashboardStats()
                if (statsResp.isSuccessful && statsResp.body() != null) {
                    val stats = statsResp.body()!!
                    binding.tvTotalUsers.text = stats.totalUsers.toString()
                    binding.tvActiveBookings.text = stats.activeBookings.toString()
                    binding.tvTotalRevenue.text = "$ ${"%.2f".format(stats.totalRevenue)}"
                }
            } catch (_: Exception) {}

            // Top Tools
            try {
                val toolsResp = adminApi.getTopTools()
                if (toolsResp.isSuccessful && toolsResp.body() != null) {
                    binding.rvTopTools.adapter = SimpleToolAdapter(toolsResp.body()!!)
                }
            } catch (_: Exception) {}

            // Top Users
            try {
                val usersResp = adminApi.getTopUsers()
                if (usersResp.isSuccessful && usersResp.body() != null) {
                    binding.rvTopUsers.adapter = SimpleUserAdapter(usersResp.body()!!)
                }
            } catch (_: Exception) {}

            // Damage Reports
            try {
                val damageResp = adminApi.getAllDamageReports()
                if (damageResp.isSuccessful && damageResp.body() != null) {
                    binding.rvDamageReports.adapter = SimpleDamageAdapter(damageResp.body()!!)
                }
            } catch (_: Exception) {}

            // Payments
            try {
                val paymentsResp = adminApi.getAllPayments()
                if (paymentsResp.isSuccessful && paymentsResp.body() != null) {
                    binding.rvPayments.adapter = SimplePaymentAdapter(paymentsResp.body()!!)
                }
            } catch (_: Exception) {}

            binding.progressBar.visibility = View.GONE
        }
    }

    // Adapter simple para top herramientas
    class SimpleToolAdapter(private val tools: List<ToolResponse>) :
        RecyclerView.Adapter<SimpleToolAdapter.VH>() {

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val tool = tools[position]
            holder.tvName.text = "${position + 1}. ${tool.name} - $ ${tool.pricePerDay}/día"
            holder.tvName.textSize = 14f
        }

        override fun getItemCount() = tools.size
    }

    // Adapter simple para top usuarios
    class SimpleUserAdapter(private val users: List<UserResponse>) :
        RecyclerView.Adapter<SimpleUserAdapter.VH>() {

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val user = users[position]
            holder.tvName.text = "${position + 1}. ${user.firstName} ${user.lastName} (${user.role})"
            holder.tvName.textSize = 14f
        }

        override fun getItemCount() = users.size
    }

    // Adapter para Reportes de Daños
    class SimpleDamageAdapter(private val reports: List<com.construrrenta.app.data.model.DamageReportResponse>) :
        RecyclerView.Adapter<SimpleDamageAdapter.VH>() {

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val report = reports[position]
            val costStr = if ((report.repairCost ?: 0.0) > 0) "-$${report.repairCost}" else "Garantía"
            holder.tvName.text = "Reserva: ${report.bookingId.take(8)} | $costStr\n${report.description}"
            holder.tvName.textSize = 14f
        }

        override fun getItemCount() = reports.size
    }

    // Adapter para Pagos
    class SimplePaymentAdapter(private val payments: List<com.construrrenta.app.data.model.PaymentResponse>) :
        RecyclerView.Adapter<SimplePaymentAdapter.VH>() {

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val p = payments[position]
            holder.tvName.text = "$${p.amount} | ${p.method} | ${p.status}\nFecha: ${p.paymentDate.take(10)}"
            holder.tvName.textSize = 14f
        }

        override fun getItemCount() = payments.size
    }
}
