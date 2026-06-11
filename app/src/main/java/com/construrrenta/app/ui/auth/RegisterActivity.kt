package com.construrrenta.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.construrrenta.app.data.api.RetrofitClient
import com.construrrenta.app.data.api.AuthApiService
import com.construrrenta.app.data.local.SessionManager
import com.construrrenta.app.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = SessionManager(this)
        val authApi = RetrofitClient.getInstance(this).create(AuthApiService::class.java)
        viewModel = AuthViewModel(authApi, sessionManager)

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.register(firstName, lastName, email, password)
        }

        binding.tvGoToLogin.setOnClickListener {
            finish() // Vuelve al LoginActivity
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AuthUiState.Idle -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnRegister.isEnabled = true
                        }
                        is AuthUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnRegister.isEnabled = false
                            binding.tvError.visibility = View.GONE
                        }
                        is AuthUiState.RegisterSuccess -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@RegisterActivity, "¡Registro exitoso! Ahora inicia sesión.", Toast.LENGTH_LONG).show()
                            // Volver al login para que inicie sesión
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                            finish()
                        }
                        is AuthUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnRegister.isEnabled = true
                            binding.tvError.text = state.message
                            binding.tvError.visibility = View.VISIBLE
                        }
                        is AuthUiState.LoginSuccess -> { /* No aplica aquí */ }
                    }
                }
            }
        }
    }
}
