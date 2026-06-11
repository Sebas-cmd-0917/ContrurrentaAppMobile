package com.construrrenta.app.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.construrrenta.app.data.api.RetrofitClient
import com.construrrenta.app.data.api.UserApiService
import com.construrrenta.app.data.local.SessionManager
import com.construrrenta.app.data.model.UpdateUserRequest
import com.construrrenta.app.databinding.ActivityProfileBinding
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var userApi: UserApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        userApi = RetrofitClient.getInstance(this).create(UserApiService::class.java)

        binding.toolbar.setNavigationOnClickListener { finish() }

        loadProfile()
        setupSaveButton()
    }

    private fun loadProfile() {
        binding.tvEmail.text = "Email: ${sessionManager.getEmail() ?: "N/A"}"
        binding.tvRole.text = sessionManager.getRole() ?: "CUSTOMER"
        binding.etFirstName.setText(sessionManager.getFirstName() ?: "")

        // Cargar datos completos del servidor
        val userId = sessionManager.getUserId()
        if (userId != null) {
            lifecycleScope.launch {
                try {
                    val response = userApi.getUserById(userId)
                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!
                        binding.etFirstName.setText(user.firstName)
                        binding.etLastName.setText(user.lastName)
                    }
                } catch (_: Exception) {
                    // Usar datos locales como fallback
                }
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (firstName.isBlank() || lastName.isBlank()) {
                binding.tvError.text = "Nombre y apellido son obligatorios"
                binding.tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            binding.btnSave.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            binding.tvError.visibility = View.GONE

            val userId = sessionManager.getUserId() ?: return@setOnClickListener
            val request = UpdateUserRequest(
                firstName = firstName,
                lastName = lastName,
                password = if (password.isNotEmpty()) password else null
            )

            lifecycleScope.launch {
                try {
                    val response = userApi.updateUser(userId, request)
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProfileActivity, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        binding.tvError.text = "Error al actualizar: ${response.code()}"
                        binding.tvError.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    binding.tvError.text = "Error de conexión: ${e.localizedMessage}"
                    binding.tvError.visibility = View.VISIBLE
                } finally {
                    binding.btnSave.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }
}
