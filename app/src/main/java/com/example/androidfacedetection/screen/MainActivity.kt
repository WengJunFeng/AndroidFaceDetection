package com.example.androidfacedetection.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.androidfacedetection.customViews.FaceHighLightTransformer
import com.example.androidfacedetection.databinding.ActivityMainBinding
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity(), com.example.androidfacedetection.screen.View {

    companion object {
        const val MY_CAMERA_REQUEST_CODE = 200
    }

    private lateinit var binding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var presenter: Presenter

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        presenter = MainActivityPresenter(this)

        detectImages.setOnClickListener {
            if (checkPermission()
            ) {
                presenter.launchPreview()
            } else {
                requestCameraPermission()
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun checkPermission() =
        (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            MY_CAMERA_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun launchVisionPerceptor() {
        loadPreview()
    }


    override fun processWholeFace(
        boundingBox: MutableList<FirebaseVisionFace>,
        image: FirebaseVisionImage
    ) {
        rectOverlay.drawFaceBounds(boundingBox, object : FaceHighLightTransformer {
            override val widthScaleFactor: Float
                get() = (viewFinder.width / image.bitmap.width).toFloat()
            override val heightScaleFactor: Float
                get() = (viewFinder.measuredHeight / image.bitmap.height).toFloat()
            override val leftOffset: Int
                get() = viewFinder.paddingLeft
            override val topOffset: Int
                get() = viewFinder.paddingTop
        })
    }

    override fun noFaceDetected() {
        Toast.makeText(this, "No Face Detected", Toast.LENGTH_LONG).show()
    }


    private fun loadPreview() {
        initViews()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder()
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ImageAnalyzer { imageProxy ->
                        presenter.detectImages(imageProxy, imageProxy.imageInfo.rotationDegrees)
                    })
                }
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("mainActivity", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun initViews() {
        binding.viewFinder.visibility = View.VISIBLE
        binding.detectImages.visibility = View.GONE
        binding.imageView.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
