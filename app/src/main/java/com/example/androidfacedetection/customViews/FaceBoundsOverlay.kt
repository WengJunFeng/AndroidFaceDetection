package com.example.androidfacedetection.customViews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import kotlin.math.abs
import kotlin.math.ceil


class FaceBoundsOverlay @JvmOverloads constructor(context: Context?, attributeSet: AttributeSet?) :
    View(context, attributeSet) {

    private val faceBounds: MutableList<FirebaseVisionFace> = mutableListOf()
    private val faceRect: MutableList<Rect> = mutableListOf()

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 4f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        faceRect.forEach {
            canvas.drawRect(
                it, paint
            )
        }
    }

    fun drawFaceBounds(boundingBox: MutableList<FirebaseVisionFace>,transformer: FaceHighLightTransformer) {
        this.faceBounds.clear()
        this.faceRect.clear()
        this.faceBounds.addAll(boundingBox)
        transform(transformer)
        invalidate()
    }

    private fun transform(transformer : FaceHighLightTransformer) {
        faceBounds.forEach {
            val boxFace = it.boundingBox
            val originalBoxWidth = abs(boxFace.left - boxFace.right)
            val originalBoxHeight = abs(boxFace.top - boxFace.bottom)

            val newBoxWidth = ceil(originalBoxWidth * transformer.widthScaleFactor).toInt()
            val newBoxHeight = ceil(originalBoxHeight * transformer.heightScaleFactor).toInt()

            boxFace.left =
                (boxFace.left * transformer.widthScaleFactor).toInt() + transformer.leftOffset
            boxFace.top =
                (boxFace.top * transformer.heightScaleFactor).toInt() + transformer.topOffset

            boxFace.right = boxFace.right + newBoxWidth
            boxFace.bottom = boxFace.bottom + newBoxHeight
            faceRect.add(boxFace)
        }
    }
}
