package com.anggiiqna.polafit.features.profile

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.ImageView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.anggiiqna.polafit.features.login.LoginActivity
import androidx.lifecycle.lifecycleScope
import com.anggiiqna.polafit.R
import com.anggiiqna.polafit.network.ApiClient
import com.anggiiqna.polafit.network.ApiService
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewProfile : AppCompatActivity() {
    private lateinit var apiService: ApiService
    private lateinit var editProfileView: TextView
    private lateinit var aboutApp: TextView
    private lateinit var username: TextView
    private lateinit var email: TextView
    private lateinit var phone: TextView
    private lateinit var image: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        editProfileView = findViewById(R.id.editProfile)
        aboutApp = findViewById(R.id.aboutApp)
        username = findViewById(R.id.tv_name)
        email = findViewById(R.id.tv_email)
        phone = findViewById(R.id.tv_phone)
        image = findViewById(R.id.profile_image)

        apiService = ApiClient.create()

        val backButton: ImageView = findViewById(R.id.icon_back)
        backButton.setOnClickListener {
            finish()
        }

        val userId = intent.getStringExtra("id") ?: ""
        editProfileView.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("id", userId)
            startActivity(intent)
        }

        aboutApp.setOnClickListener {
            val intent = Intent(this, AboutApp::class.java)
            startActivity(intent)
        }

        if (userId.isNotEmpty()) {
            fetchUserProfile(userId)
        }

        val logoutButton: Button = findViewById(R.id.btn_logout)
        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun fetchUserProfile(userId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getUserById(userId)
                withContext(Dispatchers.Main) {
                    username.text = response.username
                    email.text = response.email
                    phone.text = response.phone
                    Glide.with(this@ViewProfile)
                        .load(if (response.image.isNullOrEmpty()) R.drawable.noprofile else response.image)
                        .apply(RequestOptions.circleCropTransform())
                        .into(image)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    username.text = "Not found"
                    email.text = "Not found"
                    phone.text = "Not found"
                }
            }
        }
    }

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
