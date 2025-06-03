package com.example.mixedreality

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Choreographer
import android.view.PixelCopy
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import com.example.mixedreality.utils.Utils
import com.example.mixedreality.utils.YuvToRgbConverter
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
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class Mr : AppCompatActivity(), Choreographer.FrameCallback {

    private lateinit var sceneView: ArSceneView
    private lateinit var modelNode: ArModelNode

    private lateinit var leftEye: ImageView
    private lateinit var rightEye: ImageView
    private lateinit var background: ImageView
    private val stereoHandler = Handler(Looper.getMainLooper())
    private val frameIntervalMillis = 5L

    private var isPlaced = false
    private var canBePlaced = false

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

    override fun doFrame(frameTimeNanos: Long) {
        val arFrame = sceneView.arSession?.update() ?: return
        val image = arFrame.acquireCameraImage()

        if (image.format == ImageFormat.YUV_420_888) {
            val converter = YuvToRgbConverter(this)
            val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
            converter.yuvToRgb(image, bitmap)
            val inputBitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, true)

            val inputImage = FloatArray(1 * 512 * 512 * 3)
            val intValues = IntArray(512 * 512)
            inputBitmap.getPixels(intValues, 0, 512, 0, 0, 512, 512)

            for (i in intValues.indices) {
                val pixel = intValues[i]
                val r = ((pixel shr 16) and 0xFF).toFloat() / 255f
                val g = ((pixel shr 8) and 0xFF).toFloat() / 255f
                val b = (pixel and 0xFF).toFloat() / 255f

                val baseIndex = i * 3
                inputImage[baseIndex] = r
                inputImage[baseIndex + 1] = g
                inputImage[baseIndex + 2] = b
            }

            val inputTensor = TensorBuffer.createFixedSize(intArrayOf(1, 512, 512, 3), DataType.FLOAT32)
            inputTensor.loadArray(inputImage)

            val byteBuffer = ByteBuffer.allocateDirect(4 * 1 * 512 * 512 * 3)
            byteBuffer.order(ByteOrder.nativeOrder())
            for (f in inputImage) {
                byteBuffer.putFloat(f)
            }

            val interpreter = Interpreter(loadModelFile(this))

            val input = arrayOf(byteBuffer) // или inputTensor.buffer
            val output = Array(1) { Array(512) { Array(512) { FloatArray(32) } } }

            interpreter.run(input[0], output)

            canBePlaced = output[0][256][256][0] == 0F
        }

        image.close()
        Choreographer.getInstance().postFrameCallback(this)
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
        if (!canBePlaced) return

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

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("unet_floors.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }
}