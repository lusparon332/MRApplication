package com.example.mixedreality

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import com.example.mixedreality.utils.Utils
import com.google.android.filament.MaterialInstance
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.Config
import com.google.ar.core.Frame
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.material.setBaseColor
import io.github.sceneview.math.Position
import io.github.sceneview.utils.Color

class Mr : AppCompatActivity() {

    private lateinit var sceneView: ArSceneView
    private lateinit var modelNode: ArModelNode

    private lateinit var leftEye: ImageView
    private lateinit var rightEye: ImageView
    private lateinit var background: ImageView
    private val stereoHandler = Handler(Looper.getMainLooper())
    private val frameIntervalMillis = 5L

    private var isPlaced = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mr)

        sceneView = findViewById<ArSceneView?>(R.id.sceneView).apply {
            this.lightEstimationMode = Config.LightEstimationMode.DISABLED
        }

        modelNode = ArModelNode(sceneView.engine, PlacementMode.BEST_AVAILABLE).apply {
            loadModelGlbAsync(
                glbFileLocation = when (MainActivity.MODEL) {
                    "chicken" -> "models/chicken.glb"
                    "penguin" -> "models/penguin.glb"
                    "duck" -> "models/duck.glb"
                    "tree" -> "models/tree.glb"
                    "rabbit" -> "models/rabbit.glb"
                    else -> "models/chicken.glb"
                },
                scaleToUnits = 0.5f,
                centerOrigin = Position(-0.5f)
            ) {
                sceneView.planeRenderer.isVisible = true
            }
        }

        sceneView.addChild(modelNode)
        leftEye = findViewById(R.id.mrLeftEye)
        rightEye = findViewById(R.id.mrRightEye)
        background = findViewById(R.id.background)

        leftEye.setOnClickListener {
            placeModel()
        }
        rightEye.setOnClickListener {
            placeModel()
        }
        background.setOnClickListener {
            placeModel()
        }

        startStereoArFeed()
    }

    override fun onDestroy() {
        super.onDestroy()
        stereoHandler.removeCallbacksAndMessages(null)
    }

    private fun startStereoArFeed() {
        stereoHandler.post(object : Runnable {
            override fun run() {
                captureArBitmap { bitmap ->
                    leftEye.setImageBitmap(Utils.acceptOffset(bitmap, MainActivity.OFFSET))
                    rightEye.setImageBitmap(Utils.acceptOffset(bitmap, 0))
                }
                stereoHandler.postDelayed(this, frameIntervalMillis)
            }
        })
    }

    private fun captureArBitmap(callback: (Bitmap) -> Unit) {
        if (!::sceneView.isInitialized || sceneView.width == 0 || sceneView.height == 0) return

        val bitmap = Bitmap.createBitmap(sceneView.width, sceneView.height, Bitmap.Config.ARGB_8888)

        val surface = sceneView.holder.surface
        if (surface.isValid) PixelCopy.request(sceneView, bitmap, { result ->
            if (result == PixelCopy.SUCCESS) {
                callback(bitmap)
            } else {
                Log.e("AR", "PixelCopy failed: $result")
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun placeModel(){
        if (!isPlaced) {
            modelNode.anchor()
            sceneView.planeRenderer.isVisible = false
        } else {
            val anchor = modelNode.anchor()
            modelNode.anchor = null
            anchor?.detach()
            sceneView.planeRenderer.isVisible = true
        }
        isPlaced = !isPlaced
    }
}