package com.construrrenta.app.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.construrrenta.app.data.api.RetrofitClient
import com.construrrenta.app.data.api.UserApiService
import com.construrrenta.app.data.model.CreateUserRequest
import com.construrrenta.app.data.model.UserResponse
import com.construrrenta.app.databinding.ActivityAdminUsersBinding
import com.construrrenta.app.databinding.ItemUserBinding
import kotlinx.coroutines.launch

class AdminUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminUsersBinding
    private lateinit var userApi: UserApiService
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userApi = RetrofitClient.getInstance(this).create(UserApiService::class.java)

        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = UserAdapter(emptyList()) { user -> confirmDelete(user) }
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter

        binding.swipeRefresh.setColorSchemeColors(
            androidx.core.content.ContextCompat.getColor(this, com.construrrenta.app.R.color.primaryColor)
        )
        binding.swipeRefresh.setOnRefreshListener { loadUsers() }

        binding.fabAdd.setOnClickListener { showCreateUserDialog() }

        loadUsers()
    }

    private fun loadUsers() {
        binding.swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            try {
                val response = userApi.getAllUsers()
                binding.swipeRefresh.isRefreshing = false
                if (response.isSuccessful && response.body() != null) {
                    adapter.updateData(response.body()!!)
                }
            } catch (e: Exception) {
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@AdminUsersActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCreateUserDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }
        val etFirst = EditText(this).apply { hint = "Nombre" }
        val etLast = EditText(this).apply { hint = "Apellido" }
        val etEmail = EditText(this).apply { hint = "Email" }
        val etPass = EditText(this).apply { hint = "Contraseña" }
        val spinner = Spinner(this)
        val roles = arrayOf("CUSTOMER", "PROVIDER", "ADMIN")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        layout.addView(etFirst)
        layout.addView(etLast)
        layout.addView(etEmail)
        layout.addView(etPass)
        layout.addView(spinner)

        AlertDialog.Builder(this)
            .setTitle("Crear Usuario")
            .setView(layout)
            .setPositiveButton("Crear") { _, _ ->
                val request = CreateUserRequest(
                    firstName = etFirst.text.toString().trim(),
                    lastName = etLast.text.toString().trim(),
                    email = etEmail.text.toString().trim(),
                    password = etPass.text.toString().trim(),
                    role = spinner.selectedItem.toString()
                )
                lifecycleScope.launch {
                    try {
                        val response = userApi.createUser(request)
                        if (response.isSuccessful) {
                            Toast.makeText(this@AdminUsersActivity, "Usuario creado", Toast.LENGTH_SHORT).show()
                            loadUsers()
                        } else {
                            Toast.makeText(this@AdminUsersActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@AdminUsersActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDelete(user: UserResponse) {
        AlertDialog.Builder(this)
            .setMessage("¿Eliminar a '${user.firstName} ${user.lastName}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val response = userApi.deleteUser(user.id)
                        if (response.isSuccessful) {
                            Toast.makeText(this@AdminUsersActivity, "Eliminado", Toast.LENGTH_SHORT).show()
                            loadUsers()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@AdminUsersActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Adapter interno
    class UserAdapter(
        private var users: List<UserResponse>,
        private val onDelete: (UserResponse) -> Unit
    ) : RecyclerView.Adapter<UserAdapter.VH>() {

        class VH(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(binding)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val user = users[position]
            with(holder.binding) {
                tvUserName.text = "${user.firstName} ${user.lastName}"
                tvUserEmail.text = user.email
                tvUserRole.text = user.role
                btnDeleteUser.setOnClickListener { onDelete(user) }
            }
        }

        override fun getItemCount() = users.size

        fun updateData(newUsers: List<UserResponse>) {
            users = newUsers
            notifyDataSetChanged()
        }
    }
}
