package com.anggiiqna.polafit.features.scan

import android.Manifest
import android.app.Activity
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
import com.anggiiqna.polafit.R
import com.anggiiqna.polafit.network.ApiService
import com.anggiiqna.polafit.network.RetrofitClient
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
        apiService = RetrofitClient.create()

        // Initialize views
        foodImageView = findViewById(R.id.food_image)

        val btnTakePicture: ImageButton = findViewById(R.id.btnTakePicture)
        val btnOpenFile: ImageButton = findViewById(R.id.btnOpenFile)

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
                    uploadImage(photoUri)
                }
                REQUEST_GALLERY -> {
                    val selectedImage: Uri? = data?.data
                    foodImageView.setImageURI(selectedImage)
                    uploadImage(selectedImage)
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

        val file = File(getRealPathFromURI(uri))
        if (!file.exists()) {
            Toast.makeText(this, "File not found: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
            return
        }

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.predictFood(imagePart)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val predictionResult = response.body()
                        val resultIntent = Intent(this@ScanActivity, ScanResultActivity::class.java)
                        resultIntent.putExtra("Makanan", predictionResult?.Makanan)
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
                Log.e("ScanActivity", "Error updating profile", e) // Log the error
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ScanActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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
