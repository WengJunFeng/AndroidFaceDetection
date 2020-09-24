package com.example.androidfacedetection.screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions

class MainActivityPresenter(private val view: View) : Presenter {
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun detectImages(imageProxy: ImageProxy, rotation: Int) {
        val mediaImage = imageProxy.image ?: return
        val options = FirebaseVisionFaceDetectorOptions.Builder().apply {
            setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
            enableTracking()
        }.build()

        val imageRotation = when (rotation) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
        }

        val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)
        val image = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
        detector.detectInImage(image)
            .addOnSuccessListener { faces -> view.processWholeFace(faces, image) }
            .addOnFailureListener { imageLost() }
    }

    override fun launchPreview() {
        view.launchVisionPerceptor()
    }

    override fun imageLost() {
        view.noFaceDetected()
    }

}