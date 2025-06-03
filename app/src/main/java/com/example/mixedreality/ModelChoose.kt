package com.example.mixedreality

import android.os.Bundle
import android.widget.RadioButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ModelChoose : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_model_choose)

        val chickenButton = findViewById<RadioButton>(R.id.chicken)
        val treeButton = findViewById<RadioButton>(R.id.tree)
        val duckButton = findViewById<RadioButton>(R.id.duck)
        val penguinButton = findViewById<RadioButton>(R.id.penguin)
        val rabbitButton = findViewById<RadioButton>(R.id.rabbit)

        when (MainActivity.MODEL) {
            "chicken" -> chickenButton.isChecked = true
            "tree" -> treeButton.isChecked = true
            "duck" -> duckButton.isChecked = true
            "penguin" -> penguinButton.isChecked = true
            "rabbit" -> rabbitButton.isChecked = true
            else -> chickenButton.isChecked = true
        }

        chickenButton.setOnClickListener {
            MainActivity.MODEL = "chicken"
        }
        treeButton.setOnClickListener {
            MainActivity.MODEL = "tree"
        }
        duckButton.setOnClickListener {
            MainActivity.MODEL = "duck"
        }
        penguinButton.setOnClickListener {
            MainActivity.MODEL = "penguin"
        }
        rabbitButton.setOnClickListener {
            MainActivity.MODEL = "rabbit"
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}