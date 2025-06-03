package com.example.mixedreality

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mixedreality.utils.Utils
import com.example.mixedreality.utils.Utils.Companion.acceptOffset
import kotlin.math.abs
import kotlin.math.roundToInt

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        val rightEye = findViewById<ImageView>(R.id.rightEye)
        val leftEye = findViewById<ImageView>(R.id.leftEye)
        rightEye.scaleType = ImageView.ScaleType.FIT_CENTER
        leftEye.scaleType = ImageView.ScaleType.FIT_CENTER

        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        seekBar.progress = MainActivity.OFFSET
        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    MainActivity.OFFSET = progress
                    updateStereoImages()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) { }
                override fun onStopTrackingTouch(seekBar: SeekBar?) { }
            }
        )

        val testPicture = BitmapFactory.decodeResource(resources, R.drawable.penguin)

        rightEye.setImageBitmap(acceptOffset(testPicture, 0))
        leftEye.setImageBitmap(acceptOffset(testPicture, MainActivity.OFFSET))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    private fun updateStereoImages() {
        val rightEye = findViewById<ImageView>(R.id.rightEye)
        val leftEye = findViewById<ImageView>(R.id.leftEye)
        rightEye.scaleType = ImageView.ScaleType.FIT_CENTER
        leftEye.scaleType = ImageView.ScaleType.FIT_CENTER

        val testPicture = BitmapFactory.decodeResource(resources, R.drawable.penguin)

        rightEye.setImageBitmap(acceptOffset(testPicture, 0))
        leftEye.setImageBitmap(acceptOffset(testPicture, MainActivity.OFFSET))
    }

}