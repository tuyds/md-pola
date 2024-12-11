package com.anggiiqna.polafit.features.scan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.anggiiqna.polafit.R
import com.anggiiqna.polafit.network.ScanResponse
import com.anggiiqna.polafit.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanActivity : AppCompatActivity() {

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

        // Initialize views
        foodImageView = findViewById(R.id.food_image)

        val btnTakePicture: ImageButton = findViewById(R.id.btnTakePicture)
        val btnOpenFile: ImageButton = findViewById(R.id.btnOpenFile)

        // Set click listeners using registerForActivityResult
        btnTakePicture.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestPermissions.launch(arrayOf(Manifest.permission.CAMERA))
            }
        }

        btnOpenFile.setOnClickListener {
            if (checkStoragePermission()) {
                openGallery()
            } else {
                requestPermissions.launch(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                )
            }
        }
    }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions[Manifest.permission.CAMERA] == true -> openCamera()
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true -> openGallery()
                else -> {
                    Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
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
        }
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
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
                    foodImageView.setImageURI(photoUri)
                    // Upload the image
                    uploadImage(photoUri)
                }
                REQUEST_GALLERY -> {
                    val selectedImage: Uri? = data?.data
                    foodImageView.setImageURI(selectedImage)
                    // Upload the image
                    uploadImage(selectedImage)
                }
            }
        } else {
            Toast.makeText(this, "Action canceled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImage(uri: Uri?) {
        if (uri == null) return

        try {
            // Convert Uri to File
            val file = File(uri.path!!)
            if (!file.exists()) {
                Toast.makeText(this, "File not found: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                return
            }

            val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull() ?: "application/octet-stream".toMediaTypeOrNull(), file)
            val imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

            // Create API service
            val apiService = RetrofitClient.apiService
            apiService.predictFood(imagePart).enqueue(object : Callback<ScanResponse> {
                override fun onResponse(call: Call<ScanResponse>, response: Response<ScanResponse>) {
                    if (response.isSuccessful) {
                        // Handle successful response
                        val predictionResult = response.body()
                        Toast.makeText(this@ScanActivity, "Prediction: ${predictionResult?.Makanan}", Toast.LENGTH_SHORT).show()
                        // Display other fields as needed
                    } else {
                        Toast.makeText(this@ScanActivity, "Failed to get prediction", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ScanResponse>, t: Throwable) {
                    Toast.makeText(this@ScanActivity, "API Call Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: IOException) {
            Toast.makeText(this, "File operation failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_GALLERY -> {
                if (grantResults.isNotEmpty() &&
                    grantResults.contains(PackageManager.PERMISSION_GRANTED)
                ) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Storage permissions denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
