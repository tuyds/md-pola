package com.anggiiqna.polafit.features.inputdata

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.anggiiqna.polafit.R

class InputActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_isidata)

        val backButton: ImageView = findViewById(R.id.icon_back)
        backButton.setOnClickListener {
            finish()
        }
    }
}