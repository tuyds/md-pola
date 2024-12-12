package com.anggiiqna.polafit.features.scan

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.anggiiqna.polafit.HomeActivity
import com.anggiiqna.polafit.R
import com.anggiiqna.polafit.network.ApiClient
import com.anggiiqna.polafit.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var foodImageView: ImageView
    private var photoUri: Uri? = null

    companion object {
        const val REQUEST_CAMERA = 100
        const val REQUEST_GALLERY = 200
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        // Initialize Retrofit API service
        apiService = ApiClient.createSecondary()

        // Initialize views
        foodImageView = findViewById(R.id.food_image)
        val btnTakePicture: ImageButton = findViewById(R.id.btnTakePicture)
        val btnOpenFile: ImageButton = findViewById(R.id.btnOpenFile)
        val backButton: ImageView = findViewById(R.id.icon_back)

        backButton.setOnClickListener {
            val intent = Intent(this@ScanActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnTakePicture.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestPermissions.launch(arrayOf(Manifest.permission.CAMERA))
            }
        }

        btnOpenFile.setOnClickListener {
            if (checkGalleryPermission()) {
                openGallery()
            } else {
                requestGalleryPermission()
            }
        }
    }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions[Manifest.permission.CAMERA] == true -> openCamera()
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true ||
                        permissions[Manifest.permission.READ_MEDIA_IMAGES] == true -> openGallery()
                else -> {
                    Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkGalleryPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
        } else {
            requestPermissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show()
            null
        }

        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(cameraIntent, REQUEST_CAMERA)
        } else {
            Toast.makeText(this, "Unable to open camera due to file creation issue", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        // Use getExternalFilesDir() to ensure the directory exists and is accessible
        val storageDir = File(getExternalFilesDir(null), "my_images")

        // Create the directory if it doesn't exist
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        return File.createTempFile(
            "JPEG_${timestamp}_",
            ".jpg",
            storageDir
        ).apply {
            // Ensure the file is readable
            setReadable(true, false)
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    // Use photoUri that was set during camera intent
                    if (photoUri != null) {
                        try {
                            Log.d("ScanActivity", "Camera URI: $photoUri")
                            Log.d("ScanActivity", "URI Path: ${photoUri?.path}")
                            Log.d("ScanActivity", "File exists: ${File(photoUri?.path ?: "").exists()}")

                            foodImageView.setImageURI(null)
                            foodImageView.setImageURI(photoUri)
                            uploadImage(photoUri)
                        } catch (e: Exception) {
                            Log.e("ScanActivity", "Error setting camera image", e)
                            Toast.makeText(this, "Failed to process image: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this, "No photo captured", Toast.LENGTH_SHORT).show()
                    }
                }
                REQUEST_GALLERY -> {
                    val selectedImage: Uri? = data?.data
                    if (selectedImage != null) {
                        foodImageView.setImageURI(selectedImage)
                        uploadImage(selectedImage)
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Action canceled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImage(uri: Uri?) {
        if (uri == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
            return
        }

        val filePath = getRealPathFromURI(uri)
        Log.d("ScanActivity", "Attempting to upload file: $filePath")

        val file = File(filePath)

        if (!file.exists()) {
            Log.e("ScanActivity", "File does not exist: $filePath")

            // Additional debugging - list files in the directory
            val parentDir = file.parentFile
            if (parentDir?.exists() == true) {
                Log.d("ScanActivity", "Files in directory:")
                parentDir.listFiles()?.forEach {
                    Log.d("ScanActivity", "Found file: ${it.absolutePath}")
                }
            }

            Toast.makeText(this, "File not found: $filePath", Toast.LENGTH_LONG).show()
            return
        }

        // Show loading indicator before API call
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading image...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.predictFood(imagePart)
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    if (response.isSuccessful) {
                        val predictionResult = response.body()
                        val resultIntent = Intent(this@ScanActivity, ScanResultActivity::class.java)



                        resultIntent.putExtra("Makanan", predictionResult?.Makanan)
                        resultIntent.putExtra("Berat_per_Serving", predictionResult?.Berat_per_Serving)
                        resultIntent.putExtra("Kalori", predictionResult?.Kalori)
                        resultIntent.putExtra("Protein", predictionResult?.Protein)
                        resultIntent.putExtra("Lemak", predictionResult?.Lemak)
                        resultIntent.putExtra("Karbohidrat", predictionResult?.Karbohidrat)
                        resultIntent.putExtra("Serat", predictionResult?.Serat)
                        resultIntent.putExtra("Gula", predictionResult?.Gula)
                        resultIntent.putExtra("ImageUri", uri.toString())
                        startActivity(resultIntent)
                    } else {
                        Toast.makeText(
                            this@ScanActivity,
                            "Failed to get prediction: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Log.e("ScanActivity", "Error uploading image", e)
                    Toast.makeText(this@ScanActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String {
        // For camera captures, prioritize the exact path
        val path = uri.path ?: ""
        Log.d("ScanActivity", "Attempting to get path: $path")

        // If the path starts with /my_images, try to find the full absolute path
        if (path.contains("/my_images/")) {
            val fileName = path.substringAfterLast("/")
            val storageDir = File(getExternalFilesDir(null), "my_images")
            val file = File(storageDir, fileName)

            Log.d("ScanActivity", "Constructed file path: ${file.absolutePath}")
            Log.d("ScanActivity", "File exists: ${file.exists()}")

            return file.absolutePath
        }

        // Fallback to content resolver method
        try {
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    return it.getString(columnIndex)
                }
            }
        } catch (e: Exception) {
            Log.e("ScanActivity", "Error getting path from URI", e)
        }

        // If all else fails, return the original path
        return path
    }
}
