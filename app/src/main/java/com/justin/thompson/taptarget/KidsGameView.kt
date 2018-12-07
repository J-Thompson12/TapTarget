package com.justin.thompson.taptarget


import android.content.Context
import android.graphics.*
import kotlin.random.Random
import android.util.AttributeSet
import android.view.*
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.view.SurfaceHolder
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Bitmap

class KidsGameView(context: Context, attributes: AttributeSet) : SurfaceView(context, attributes), SurfaceHolder.Callback {
    private val thread: KidsGameThread
    private val targets: MutableList<Target>
    private val explosions: MutableList<Animations>
    private val breaks: MutableList<Animations>
    private var minInterval = 100
    private var maxInterval = 175
    private var score = 0
    private var increase = false
    private val breakBmp = BitmapFactory.decodeResource(resources, R.drawable.targetbreak)
    private val explodeBmp = BitmapFactory.decodeResource(resources, R.drawable.exp)
    private val targetBmp = BitmapFactory.decodeResource(resources, R.drawable.target)
    private val bombBmp = BitmapFactory.decodeResource(resources, R.drawable.bomb)
    private var timeAlive = 200
    private var numTargets: MutableList<Int>
    private var seed = 0
    private var touchedBomb: Boolean = false
    private var touched = false
    private var canvasPaint: Paint
    var myCanvas_w: Int = 0
    var myCanvas_h:Int = 0
    var myCanvasBitmap: Bitmap? = null
    var myCanvas: Canvas? = null
    var identityMatrix: Matrix? = null

    init{
        // add callback
        holder.addCallback(this)

        // instantiate the game thread
        thread = KidsGameThread(holder, this)

        targets = mutableListOf()
        explosions = mutableListOf()
        breaks = mutableListOf()
        numTargets = mutableListOf()
        numTargets.add(getRandomInterval())
        numTargets.add(getRandomInterval())
        canvasPaint = Paint()
        canvasPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        var retry = true
        while (retry) {
            try {
                thread.setRunning(false)
                thread.join()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            retry = false
        }
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        this.setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSPARENT)

        thread.setRunning(true)
        thread.start()
        myCanvas_w = width
        myCanvas_h = height
        myCanvasBitmap = Bitmap.createBitmap(myCanvas_w, myCanvas_h, Bitmap.Config.ARGB_8888)
        myCanvas = Canvas()
        myCanvas!!.setBitmap(myCanvasBitmap)

        identityMatrix = Matrix()
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

    }

    /**
     * Function to update the positions of player and game objects
     */
    fun update() {
        for(interval in numTargets){
            if(thread.getTick() % interval == 0 && thread.getTick() != 0) {
                addTarget()
                numTargets.remove(interval)
                numTargets.add(getRandomInterval())
            }
        }
    }
    fun drawCanvas(){
        myCanvas!!.drawColor(0, PorterDuff.Mode.CLEAR)

        for(target in targets) {
            target.update()
            timeOut(target)
            target.draw(myCanvas!!)
        }

        for(tar in breaks){
            tar.draw(myCanvas!!)
            if(tar.finished){
                breaks.remove(tar)
            }
        }

        for(exp in explosions){
            exp.draw(myCanvas!!)
            if(exp.finished){
                explosions.remove(exp)
                touchedBomb = false
            }
        }
    }


    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
        canvas.drawBitmap(myCanvasBitmap, identityMatrix, null)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event!!.actionMasked == MotionEvent.ACTION_DOWN) {
            touched = false
            for (target in targets) {
                if (target.isTouched(event) && !target.isBomb) {
                    targets.remove(target)
                    score++
                    increase = true
                    var broke = Animations(breakBmp, 4,1)
                    broke.x = target.x
                    broke.y = target.y
                    breaks.add(broke)
                    break
                }else if(target.isTouched(event) && target.isBomb && !touched){
                    targets.remove(target)
                    var exp = Animations(explodeBmp, 4,4)
                    exp.x = target.x
                    exp.y = target.y
                    explosions.add(exp)
                    break
                }
            }
        }
        return true
    }

    private fun addTarget(){
        var time = System.currentTimeMillis()
        var randObject = Random(time).nextInt(1,9)
        var target: Target? = null
        when(randObject){
            1,2,3,4 -> target = Target(targetBmp, seed)
            5,6,7,8 -> target = Target(bombBmp, seed)
        }

        if (target != null) {
            if(randObject == 5 || randObject == 6 || randObject == 7 || randObject == 8){
                target.isBomb = true
            }
            var movement = Random(time).nextInt(1, 3)
            when (movement) {
                1 -> target.isMoving(false)
                2 -> target.isMoving(true)
            }
            targets.add(target)
        }
    }

    private fun getRandomInterval(): Int{
        seed++
        var time = System.currentTimeMillis()
        var randomInterval= Random(time + seed).nextInt(minInterval, maxInterval)
        return randomInterval
    }

    private fun timeOut(target: Target){
        if(target.timeAlive % timeAlive == 0 && target.timeAlive != 0 && !target.isBomb){
            targets.remove(target)
        }
        if(target.timeAlive % timeAlive == 0 && target.timeAlive != 0 && target.isBomb){
            targets.remove(target)
        }
    }

}