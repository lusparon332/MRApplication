package com.example.mixedreality

import android.content.Intent
import android.os.Bundle
import android.view.Display.Mode
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    companion object {
        var OFFSET = 35
        var MODEL = "chicken"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val mrButton = findViewById<Button>(R.id.startMRButton)
        mrButton.setOnClickListener {
            val intent = Intent(this, Mr::class.java)
            startActivity(intent)
        }

        val settingsButton = findViewById<Button>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        val modelChooseButton = findViewById<Button>(R.id.modelChooseButton)
        modelChooseButton.setOnClickListener {
            val intent = Intent(this, ModelChoose::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}