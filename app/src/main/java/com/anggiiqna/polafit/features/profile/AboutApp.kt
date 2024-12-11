package com.anggiiqna.polafit.features.profile

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.anggiiqna.polafit.R
import android.widget.TextView
import androidx.annotation.RequiresApi

class AboutApp : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val backButton: ImageView = findViewById(R.id.icon_back)
        backButton.setOnClickListener {
            finish()
        }

        val aboutText = """
            <b>About</b><br><br>
            PolaFit is an app that utilizes Convolutional Neural Network (CNN) technology to detect food and provide exercise recommendations according to user inputs. The app helps you choose a healthy diet and effective exercise routine based on your fitness goals.<br><br>
            <b>Key Features</b><br><br>
            - Automatic detection of food using CNN.<br>
            - Exercise recommendations tailored to food choices and fitness goals.<br>
            - Monitoring of user's diet and exercise progress.<br><br>
            <b>Development Team</b><br><br>
            (ML) M120B4KY3283 – Naufal El Kamil Aditya Pratama Rahman<br>
            (ML) M120B4KY1123 – Didin Roy Chafihudin<br>
            (ML) M120B4KX4043 – Salsabila Septi Sukmayanti<br>
            (CC) C120B4KY2009 – Irsyam Okta Pratama Riyadi<br>
            (CC) C012B4KY0161 – Afnan Yusuf<br>
            (MD) A120B4KX3549 – R. Auliya Catur Silvia<br>
            (MD) A120B4KX0552 – Anggi Iqna Aryahyah<br><br>
            <b>Version 1.0.0</b><br><br>
            Copyright © 2024 PolaFit
        """.trimIndent()

        val aboutTextView: TextView = findViewById(R.id.about_description)
        aboutTextView.text = Html.fromHtml(aboutText, Html.FROM_HTML_MODE_LEGACY)

    }
}