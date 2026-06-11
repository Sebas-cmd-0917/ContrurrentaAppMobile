package com.construrrenta.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.construrrenta.app.data.api.RetrofitClient
import com.construrrenta.app.data.api.ToolApiService
import com.construrrenta.app.data.local.SessionManager
import com.construrrenta.app.databinding.ActivityMainBinding
import com.construrrenta.app.ui.ToolAdapter
import com.construrrenta.app.ui.ToolDetailActivity
import com.construrrenta.app.ui.ToolUiState
import com.construrrenta.app.ui.ToolViewModel
import com.construrrenta.app.ui.auth.LoginActivity
import com.construrrenta.app.ui.bookings.MyBookingsActivity
import com.construrrenta.app.ui.profile.ProfileActivity
import com.construrrenta.app.ui.provider.ProviderInventoryActivity
import com.construrrenta.app.ui.provider.RentalManagementActivity
import com.construrrenta.app.ui.admin.AdminUsersActivity
import com.construrrenta.app.ui.admin.AdminReportsActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var toolViewModel: ToolViewModel
    private lateinit var toolAdapter: ToolAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // No enviamos al login por defecto, MainActivity es público

        setupToolbar()
        setupNavigationDrawer()
        setupRecyclerView()
        setupSearch()
        setupSwipeRefresh()

        // Crear ViewModel
        val toolApi = RetrofitClient.getInstance(this).create(ToolApiService::class.java)
        toolViewModel = ToolViewModel(toolApi)

        observeToolState()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            android.R.string.ok, android.R.string.cancel
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupNavigationDrawer() {
        val isLoggedIn = sessionManager.isLoggedIn()
        val role = sessionManager.getRole() ?: "CUSTOMER"

        // Configurar header con datos del usuario
        val headerView = binding.navView.getHeaderView(0)
        val tvName = headerView.findViewById<TextView>(R.id.tvNavHeaderName)
        val tvEmail = headerView.findViewById<TextView>(R.id.tvNavHeaderEmail)
        val tvRole = headerView.findViewById<TextView>(R.id.tvNavHeaderRole)

        tvName.text = if (isLoggedIn) (sessionManager.getFirstName() ?: "Usuario") else "Invitado"
        tvEmail.text = if (isLoggedIn) (sessionManager.getEmail() ?: "") else "Inicia sesión para alquilar"
        tvRole.text = if (isLoggedIn) role else "GUEST"

        // Ocultar opciones según rol y login
        val menu = binding.navView.menu

        menu.setGroupVisible(R.id.group_customer, isLoggedIn)
        menu.setGroupVisible(R.id.group_provider, isLoggedIn && (role == "PROVIDER" || role == "ADMIN"))
        menu.setGroupVisible(R.id.group_admin, isLoggedIn && role == "ADMIN")

        val loginItem = menu.findItem(R.id.nav_logout)
        if (isLoggedIn) {
            loginItem.title = "Cerrar Sesión"
        } else {
            loginItem.title = "Iniciar Sesión"
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            when (menuItem.itemId) {
                R.id.nav_home -> { /* Ya estamos aquí */ }
                R.id.nav_my_bookings -> startActivity(Intent(this, MyBookingsActivity::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_my_inventory -> startActivity(Intent(this, ProviderInventoryActivity::class.java))
                R.id.nav_my_rentals -> startActivity(Intent(this, RentalManagementActivity::class.java))
                R.id.nav_admin_users -> startActivity(Intent(this, AdminUsersActivity::class.java))
                R.id.nav_admin_reports -> startActivity(Intent(this, AdminReportsActivity::class.java))
                R.id.nav_logout -> {
                    if (isLoggedIn) {
                        sessionManager.clearSession()
                        // Recargar la actividad para que aplique los cambios
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        navigateToLogin()
                    }
                }
            }
            true
        }
    }

    private fun setupRecyclerView() {
        toolAdapter = ToolAdapter(emptyList()) { tool ->
            val intent = Intent(this, ToolDetailActivity::class.java).apply {
                putExtra("TOOL_ID", tool.id)
                putExtra("TOOL_NAME", tool.name)
                putExtra("TOOL_PRICE", tool.pricePerDay)
                putExtra("TOOL_DESCRIPTION", tool.description)
                putExtra("TOOL_IMAGE", tool.imageUrl)
                putExtra("TOOL_STOCK", tool.stock)
            }
            startActivity(intent)
        }
        binding.rvTools.layoutManager = LinearLayoutManager(this)
        binding.rvTools.adapter = toolAdapter
    }

    private fun setupSearch() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                toolViewModel.searchTools(query)
                true
            } else false
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            androidx.core.content.ContextCompat.getColor(this, com.construrrenta.app.R.color.primaryColor)
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.etSearch.text?.clear()
            toolViewModel.fetchTools()
        }
    }

    private fun observeToolState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                toolViewModel.uiState.collect { state ->
                    when (state) {
                        is ToolUiState.Loading -> {
                            if (!binding.swipeRefresh.isRefreshing) {
                                binding.pbLoading.visibility = View.VISIBLE
                            }
                            binding.rvTools.visibility = View.GONE
                            binding.tvEmpty.visibility = View.GONE
                            binding.layoutError.visibility = View.GONE
                        }
                        is ToolUiState.Success -> {
                            binding.pbLoading.visibility = View.GONE
                            binding.swipeRefresh.isRefreshing = false
                            binding.rvTools.visibility = View.VISIBLE
                            binding.tvEmpty.visibility = View.GONE
                            binding.layoutError.visibility = View.GONE
                            toolAdapter.updateData(state.tools)
                        }
                        is ToolUiState.Empty -> {
                            binding.pbLoading.visibility = View.GONE
                            binding.swipeRefresh.isRefreshing = false
                            binding.rvTools.visibility = View.GONE
                            binding.tvEmpty.visibility = View.VISIBLE
                            binding.layoutError.visibility = View.GONE
                        }
                        is ToolUiState.Error -> {
                            binding.pbLoading.visibility = View.GONE
                            binding.swipeRefresh.isRefreshing = false
                            binding.rvTools.visibility = View.GONE
                            binding.tvEmpty.visibility = View.GONE
                            binding.layoutError.visibility = View.VISIBLE
                            binding.tvErrorMessage.text = state.message
                            binding.btnRetry.setOnClickListener { toolViewModel.fetchTools() }
                        }
                    }
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Refrescar lista al volver de otras pantallas
        if (::toolViewModel.isInitialized) {
            toolViewModel.fetchTools()
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}