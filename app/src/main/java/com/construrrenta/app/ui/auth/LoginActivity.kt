package com.construrrenta.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.construrrenta.app.MainActivity
import com.construrrenta.app.data.api.RetrofitClient
import com.construrrenta.app.data.api.AuthApiService
import com.construrrenta.app.data.local.SessionManager
import com.construrrenta.app.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Si ya tiene sesión activa, redirigir al Home
        val sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        val authApi = RetrofitClient.getInstance(this).create(AuthApiService::class.java)
        viewModel = AuthViewModel(authApi, sessionManager)

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AuthUiState.Idle -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnLogin.isEnabled = true
                        }
                        is AuthUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnLogin.isEnabled = false
                            binding.tvError.visibility = View.GONE
                        }
                        is AuthUiState.LoginSuccess -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@LoginActivity, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                            navigateToMain()
                        }
                        is AuthUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnLogin.isEnabled = true
                            binding.tvError.text = state.message
                            binding.tvError.visibility = View.VISIBLE
                        }
                        is AuthUiState.RegisterSuccess -> { /* No aplica aquí */ }
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
