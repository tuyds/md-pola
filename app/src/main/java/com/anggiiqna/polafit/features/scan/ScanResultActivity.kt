package com.anggiiqna.polafit.features.scan

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.anggiiqna.polafit.R

class ScanResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        // Add more visible debug messages
        Log.e("ScanResult", "=== START OF SCAN RESULT DEBUG ===")

        // Check if extras bundle exists
        if (intent.extras == null) {
            Log.e("ScanResult", "No extras found in intent!")
        } else {
            Log.e("ScanResult", "Found extras in intent - checking contents:")
            intent.extras?.keySet()?.forEach { key ->
                Log.e("ScanResult", "➡️ Key: $key, Value: ${intent.getStringExtra(key)}")
            }
        }

        val nameFoodResultTextView: TextView = findViewById(R.id.name_food_result)
        val servingSizeResultTextView: TextView = findViewById(R.id.serving_size_result)
        val caloriesTextView: TextView = findViewById(R.id.calories_text_view)
        val proteinTextView: TextView = findViewById(R.id.protein_text_view)
        val fatTextView: TextView = findViewById(R.id.fat_text_view)
        val carboTextView: TextView = findViewById(R.id.carbo_text_view)
        val fiberTextView: TextView = findViewById(R.id.fiber_text_view)
        val sugarTextView: TextView = findViewById(R.id.sugar_text_view)
        val foodImageView: ImageView = findViewById(R.id.food_image)
        val backButton: ImageView = findViewById(R.id.icon_back)



        backButton.setOnClickListener {
            val intent = Intent(this@ScanResultActivity, ScanActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Get the name from the intent
        val foodName = intent.getStringExtra("Makanan")
        val servingSize = "${intent.getStringExtra("Berat_per_Serving") ?: ""} Gram"
        val calories = "${intent.getStringExtra("Kalori") ?: ""} Kcal"
        val protein = "${intent.getStringExtra("Protein") ?: ""} Gram"
        val fat = "${intent.getStringExtra("Lemak") ?: ""} Gram"
        val carbo = "${intent.getStringExtra("Karbohidrat") ?: ""} Gram"
        val fiber = "${intent.getStringExtra("Serat") ?: ""} Gram"  // Changed key
        val sugar = "${intent.getStringExtra("Gula") ?: ""} Gram"   // Changed key

        val imageUriString = intent.getStringExtra("ImageUri")

        // Set the name to the TextView
        nameFoodResultTextView.text = foodName
        servingSizeResultTextView.text = servingSize
        caloriesTextView.text = calories
        proteinTextView.text = protein
        fatTextView.text = fat
        carboTextView.text = carbo
        fiberTextView.text = fiber
        sugarTextView.text = sugar

        Log.e("ScanResult", """
            === NUTRITION VALUES RECEIVED ===
            Food Name: $foodName
            Serving Size: $servingSize
            Calories: $calories
            Protein: $protein
            Fat: $fat
            Carbo: $carbo
            Fiber: $fiber
            Sugar: $sugar
            ==============================
        """.trimIndent())


        imageUriString?.let { uriString ->
            val imageUri = Uri.parse(uriString)
            foodImageView.setImageURI(imageUri)
        }
    }
}


