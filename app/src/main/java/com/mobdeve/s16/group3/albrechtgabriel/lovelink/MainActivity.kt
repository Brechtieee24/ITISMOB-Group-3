package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLoginPage()
    }

    private fun showLoginPage() {
        setContentView(R.layout.landing_page)

        val signIn = findViewById<ImageButton>(R.id.sign_in_btn_imgbtn)
        signIn.setOnClickListener {
            showHomePage()
        }
    }

    private fun showHomePage() {
        setContentView(R.layout.home_page)

        val backButton = findViewById<ImageButton>(R.id.return_btn)
        backButton.setOnClickListener {
            showLoginPage()
        }
    }
}
