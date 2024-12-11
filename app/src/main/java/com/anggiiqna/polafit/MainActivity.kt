package com.anggiiqna.polafit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anggiiqna.polafit.databinding.ActivityHomeBinding
import com.anggiiqna.polafit.databinding.ActivityWelcomeBinding
import com.anggiiqna.polafit.features.inputdata.InputActivity
import com.anggiiqna.polafit.features.login.LoginActivity
import com.anggiiqna.polafit.features.register.SignUpActivity
import com.anggiiqna.polafit.features.scan.ScanActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


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
        } else {
            homeBinding = ActivityHomeBinding.inflate(layoutInflater)
            setContentView(homeBinding.root)

            val bottomNavigationView: BottomNavigationView = homeBinding.bottomNavigation

            bottomNavigationView.setOnItemSelectedListener { menuItem ->
                Log.d("MainActivity", "Menu item selected: ${menuItem.itemId}")
                when (menuItem.itemId) {
                    R.id.nav_home -> {
                        Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.nav_input -> {
                        val intent = Intent(this, InputActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.nav_scan -> {
                        val intent = Intent(this, ScanActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
        }
    }
}
