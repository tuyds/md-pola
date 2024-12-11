package com.anggiiqna.polafit.features.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.anggiiqna.polafit.R
import com.anggiiqna.polafit.network.ApiClient
import com.anggiiqna.polafit.network.ApiService
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import com.bumptech.glide.request.RequestOptions
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var profileImageView: ImageView

    private var selectedImageUri: Uri? = null

    private val pickImageRequestCode = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        val backButton: ImageView = findViewById(R.id.icon_back)
        usernameEditText = findViewById(R.id.et_username)
        emailEditText = findViewById(R.id.et_email)
        phoneEditText = findViewById(R.id.et_nohanp)
        saveButton = findViewById(R.id.btn_save)
        profileImageView = findViewById(R.id.profile_image)

        apiService = ApiClient.create()

        backButton.setOnClickListener {
            finish()
        }

        val userId = intent.getStringExtra("id") ?: ""

        if (userId.isNotEmpty()) {
            fetchUserProfile(userId)
        }

        profileImageView.setOnClickListener {
            openGallery()
        }

        saveButton.setOnClickListener {
            val updatedUsername = usernameEditText.text.toString()
            val updatedEmail = emailEditText.text.toString()
            val updatedPhone = phoneEditText.text.toString()

            if (selectedImageUri != null) {
                saveUserProfileWithImage(userId, updatedUsername, updatedEmail, updatedPhone, selectedImageUri!!)
            }
        }
    }

    private fun fetchUserProfile(userId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getUserById(userId)
                withContext(Dispatchers.Main) {
                    usernameEditText.setText(response.username)
                    emailEditText.setText(response.email)
                    phoneEditText.setText(response.phone)
                    Glide.with(this@ProfileActivity)
                        .load(if (response.image.isNullOrEmpty()) R.drawable.noprofile else response.image)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profileImageView)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    usernameEditText.setText("Error")
                    emailEditText.setText("Error")
                    phoneEditText.setText("Error")
                }
            }
        }
    }

    private fun saveUserProfileWithImage(userId: String, username: String, email: String, phone: String, imageUri: Uri?) {
        val usernameBody = username.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailBody = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val phoneBody = phone.toRequestBody("text/plain".toMediaTypeOrNull())

        val usernamePart = MultipartBody.Part.createFormData("username", null, usernameBody)
        val emailPart = MultipartBody.Part.createFormData("email", null, emailBody)
        val phonePart = MultipartBody.Part.createFormData("phone", null, phoneBody)

        if (imageUri != null) {
            val file = File(getRealPathFromURI(imageUri))
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val response = apiService.updateUserProfileWithImage(
                        userId, usernamePart, emailPart, phonePart, imagePart
                    )
                    withContext(Dispatchers.Main) {
                        if (response != null) {
                            Toast.makeText(this@ProfileActivity, "Profile updated with image!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@ProfileActivity, "Failed to update profile with image", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "Error updating profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val response = apiService.updateUserProfileWithoutImage(
                        userId, usernamePart, emailPart, phonePart
                    )
                    withContext(Dispatchers.Main) {
                        if (response != null) {
                            Toast.makeText(this@ProfileActivity, "Profile updated without image!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@ProfileActivity, "Failed to update profile without image", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "Error updating profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }



    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, pickImageRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == pickImageRequestCode) {
            selectedImageUri = data?.data
            selectedImageUri?.let {
                profileImageView.setImageURI(it)
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val index = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)
        return cursor?.getString(index ?: -1) ?: ""
    }
}
