package com.example.ubicompapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class WelcomeActivity : AppCompatActivity() {

    private lateinit var btnGetStarted: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        btnGetStarted = findViewById(R.id.btn_get_started)

        btnGetStarted.setOnClickListener {
            startActivity(Intent(this@WelcomeActivity, HomeActivity::class.java))
        }
    }
}