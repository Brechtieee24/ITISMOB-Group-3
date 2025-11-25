package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller.UserController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.LandingPageBinding
import com.jakewharton.threetenabp.AndroidThreeTen

import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class MainActivity : AppCompatActivity() {
    private lateinit var binding: LandingPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        binding = LandingPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            UserSeeder.migrateUsersToEmailId()
        }

        binding.signInBtnImgbtn.setOnClickListener {
            lifecycleScope.launch {
                // Example: check if user exists (replace with email input if needed)
                val userEmail = "john.doe@example.com"
                val user = UserController.getUserByEmail(userEmail)

                if (user != null) {
                    // User exists → go to HomeActivity
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    // User doesn't exist → maybe show a login/register screen
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}
