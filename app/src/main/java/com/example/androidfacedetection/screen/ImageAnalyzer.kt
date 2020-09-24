package com.example.androidfacedetection.screen

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class ImageAnalyzer(private val imageListener: (imageProxy: ImageProxy) -> Unit) :
    ImageAnalysis.Analyzer {


    override fun analyze(image: ImageProxy) {
        imageListener(image)
        image.close()
    }

}