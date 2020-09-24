package com.example.androidfacedetection.customViews

interface FaceHighLightTransformer {
    val widthScaleFactor: Float
    val heightScaleFactor: Float

    val leftOffset: Int
    val topOffset: Int
}
