package com.justin.thompson.taptarget

import android.content.res.Resources
import android.graphics.*

class Animations(private var bmp: Bitmap, private var numColumns: Int, private var numRows: Int) {
    var x = 0
    var y = 0
    private var currentFrame = 0
    private var currentColumn = 0
    private var currentRow = 0
    private var width: Int = 0
    private var height: Int = 0
    private var lastFrameChangeTime: Long = 0
    private val frameLengthInMillisecond = 70
    private val screenWidth = Resources.getSystem().displayMetrics.widthPixels
    var finished = false
    private var canvasPaint: Paint

    init {
        scale()
        width = bmp.width / numColumns
        height = bmp.height / numRows
        canvasPaint = Paint()
        canvasPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
    }

    /**
     * Draws the object on to the canvas.
     */
    fun draw(canvas: Canvas) {
        update()
        val srcX = currentColumn * width
        val srcY = currentRow* height
        val src = Rect(srcX, srcY, srcX + width, srcY + height)
        val dst = Rect(x, y, x + width, y + height)
        canvas.drawBitmap(bmp, src, dst, canvasPaint)

    }

    private fun update() {
        val time = System.currentTimeMillis()
        if (currentFrame < numColumns * numRows) {
            if (time > lastFrameChangeTime + frameLengthInMillisecond) {
                println("Time " + time + " lastFrame" + lastFrameChangeTime+ 100)
                lastFrameChangeTime = time
                currentFrame++
                currentColumn++
                if(currentColumn >= numColumns){
                    currentRow++
                    currentColumn = 0
                }
            }
        }else{
            finished = true
        }
    }
    private fun scale(){
        var bitmapWidth = screenWidth / 11 * numColumns
        var bitmapHeight = screenWidth / 11 * numRows
        bmp = Bitmap.createScaledBitmap(bmp,bitmapWidth, bitmapHeight, false)
    }
}