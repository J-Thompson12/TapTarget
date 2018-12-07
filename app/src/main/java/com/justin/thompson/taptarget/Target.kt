package com.justin.thompson.taptarget

import android.content.res.Resources
import android.graphics.*
import android.view.MotionEvent
import kotlin.random.Random
import android.transition.Explode
import com.justin.thompson.taptarget.R.drawable.target


class Target(var image: Bitmap, var seed: Int) {
    var x: Int = 0
    var y: Int = 0
    var w: Int = 0
    var h: Int = 0
    var isBomb = false
    var timeAlive = 0
    private var xVelocity = 0
    private var yVelocity = 0
    private val screenWidth = Resources.getSystem().displayMetrics.widthPixels
    private val screenHeight = Resources.getSystem().displayMetrics.heightPixels
    private var canvasPaint: Paint

    init {
        var randX = randomInt(0,screenWidth - image.width)
        var randY = randomInt(100,screenHeight - image.height)
        scale()

        w = image.width
        h = image.height


        x = randX
        y = randY

        canvasPaint = Paint()
        canvasPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
    }

    /**
     * Draws the object on to the canvas.
     */
    fun draw(canvas: Canvas) {
        canvas.drawBitmap(image, x.toFloat(), y.toFloat(), canvasPaint)
        timeAlive++
    }

    fun isTouched(event: MotionEvent) :Boolean{
        val targetRect = Rect(x, y, x + w, y + h)
        return targetRect.contains(event!!.x.toInt(), event.y.toInt())
    }

    fun isMoving(moving : Boolean){
        if(moving){
            xVelocity = randomInt(-5, 5)
            yVelocity = randomInt(-5, 5)
        }
    }

    fun update() {

        if (x > screenWidth  - image.width || x < 0) {
            xVelocity = xVelocity * -1
        }
        if (y > screenHeight  - image.height || y < 100) {
            yVelocity = yVelocity * -1
        }

        x += (xVelocity)
        y += (yVelocity)

    }

    private fun randomInt(min: Int, max: Int): Int{
        var time = System.currentTimeMillis()
        var rand= Random(time + seed).nextInt(min, max)
        seed++
        return rand
    }

    private fun scale(){
        var bitmapWidth = screenWidth / 11
        var bitmapHeight = bitmapWidth
        image = Bitmap.createScaledBitmap(image,bitmapWidth, bitmapHeight, false)
    }

}