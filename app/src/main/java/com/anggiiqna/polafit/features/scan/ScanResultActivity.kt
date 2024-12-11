package com.anggiiqna.polafit.features.scan

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.anggiiqna.polafit.R

class ScanResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_result)

        val nameFoodResultTextView: TextView = findViewById(R.id.name_food_result)

        // Get the name from the intent
        val foodName = intent.getStringExtra("Makanan")

        // Set the name to the TextView
        nameFoodResultTextView.text = foodName
    }
}


