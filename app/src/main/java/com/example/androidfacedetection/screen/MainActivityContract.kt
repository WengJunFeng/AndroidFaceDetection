package com.example.androidfacedetection.screen

import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace

interface View {

    fun launchVisionPerceptor()

    fun processWholeFace(boundingBox: MutableList<FirebaseVisionFace>, image: FirebaseVisionImage)

    fun noFaceDetected()
}


interface Presenter {

    fun detectImages(imageProxy: ImageProxy, rotation: Int)

    fun launchPreview()

    fun imageLost()
}