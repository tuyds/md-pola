package com.anggiiqna.polafit.features.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.anggiiqna.polafit.HomeActivity
import com.anggiiqna.polafit.databinding.ActivityLoginBinding
import com.anggiiqna.polafit.network.ApiClient
import com.anggiiqna.polafit.network.ApiService
import com.anggiiqna.polafit.network.datamodel.LoginRequest
import com.anggiiqna.polafit.pref.AppPreferences
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var apiService: ApiService
    private lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appPreferences = AppPreferences(this)

        apiService = ApiClient.create()

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            loginWithUsername(username, password)
        }
    }

    private fun loginWithUsername(username: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = apiService.login(
                    LoginRequest(
                        username = username,
                        password = password
                    )
                )
                appPreferences.saveToken(response.token)
                val id = response.user.id
                goToHome(id)
                Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
            } catch (ex: Exception) {
                Toast.makeText(this@LoginActivity, "Login error: ${ex.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun goToHome(id: String) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("id", id)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}