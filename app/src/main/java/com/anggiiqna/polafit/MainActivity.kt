package com.anggiiqna.polafit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anggiiqna.polafit.databinding.ActivityHomeBinding
import com.anggiiqna.polafit.databinding.ActivityWelcomeBinding
import com.anggiiqna.polafit.features.inputdata.InputActivity
import com.anggiiqna.polafit.features.login.LoginActivity
import com.anggiiqna.polafit.features.register.SignUpActivity
import com.anggiiqna.polafit.features.scan.ScanActivity


class MainActivity : AppCompatActivity() {

    private lateinit var homeBinding: ActivityHomeBinding
    private lateinit var welcomeBinding: ActivityWelcomeBinding
    private var isWelcomeScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isWelcomeScreen) {
            welcomeBinding = ActivityWelcomeBinding.inflate(layoutInflater)
            setContentView(welcomeBinding.root)

            welcomeBinding.btnSignup.setOnClickListener {
                val intent = Intent(this, SignUpActivity::class.java)
                startActivity(intent)
            }

            welcomeBinding.btnLogin.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
