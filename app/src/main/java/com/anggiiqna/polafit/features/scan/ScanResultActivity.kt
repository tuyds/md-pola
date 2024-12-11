package com.anggiiqna.polafit.features.scan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.anggiiqna.polafit.R

class ScanResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        val nameFoodResultTextView: TextView = findViewById(R.id.name_food_result)
        val foodImageView: ImageView = findViewById(R.id.food_image)
        val backButton: ImageView = findViewById(R.id.icon_back)

        backButton.setOnClickListener {
            val intent = Intent(this@ScanResultActivity, ScanActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Get the name from the intent
        val foodName = intent.getStringExtra("Makanan")
        val imageUriString = intent.getStringExtra("ImageUri")

        // Set the name to the TextView
        nameFoodResultTextView.text = foodName

        imageUriString?.let { uriString ->
            val imageUri = Uri.parse(uriString)
            foodImageView.setImageURI(imageUri)
        }
    }
}


