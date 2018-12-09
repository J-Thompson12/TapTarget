package com.justin.thompson.taptarget


import android.content.Context
import android.content.Intent
import android.content.res.Resources
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



class GameView(context: Context, attributes: AttributeSet) : SurfaceView(context, attributes), SurfaceHolder.Callback {
    private val thread: GameThread
    private val targets: MutableList<Target>
    private val explosions: MutableList<Animations>
    private val breaks: MutableList<Animations>
    private var minInterval = 100
    private var maxInterval = 175
    private var score = 0
    private var increase = false
    private val scorePaint: Paint
    private val intent = Intent(context, MainActivity::class.java)
    private val breakBmp = BitmapFactory.decodeResource(resources, R.drawable.targetbreak)
    private val explodeBmp = BitmapFactory.decodeResource(resources, R.drawable.exp)
    private val targetBmp = BitmapFactory.decodeResource(resources, R.drawable.target)
    private val bombBmp = BitmapFactory.decodeResource(resources, R.drawable.bomb)
    private val strikeBmp = BitmapFactory.decodeResource(resources, R.drawable.strike)
    private val preStrikeBmp = BitmapFactory.decodeResource(resources, R.drawable.prestrike)
    private val screenWidth = Resources.getSystem().displayMetrics.widthPixels
    private var timeAlive = 100
    private var numTargets: MutableList<Int>
    private var seed = 0
    private var consumedIntent: Boolean = false
    private var strikesArray = arrayOf(preStrikeBmp,preStrikeBmp,preStrikeBmp)
    private var SAVED_INSTANCE_STATE_CONSUMED_INTENT = "SAVED_INSTANCE_STATE_CONSUMED_INTENT"
    private var touchedBomb: Boolean = false
    private var touched = false
    private var strikes = 0
    private var buffer = 0f
    private var distance = 0f
    private var canvasPaint: Paint
    var myCanvas_w: Int = 0
    var myCanvas_h:Int = 0
    var myCanvasBitmap: Bitmap? = null
    var myCanvas: Canvas? = null
    var identityMatrix: Matrix? = null

    init{
        // add callback
        holder.addCallback(this)
        scorePaint = Paint()
        scorePaint.setColor(Color.WHITE)

        setTextSizeForWidth(40f,score.toString())

        // instantiate the game thread
        thread = GameThread(holder, this)

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
        if(score % 10 == 0 && score != 0 && increase){
            if(minInterval != 50 && maxInterval != 80) {
                minInterval = minInterval - 5
                maxInterval = maxInterval - 5
                increase = false
            }
            if(score % 20 == 0) {
                if (numTargets.size != 4) {
                    numTargets.add(getRandomInterval())
                }
            }
        }
        for(interval in numTargets){
            if(thread.getTick() % interval == 0 && thread.getTick() != 0) {
                addTarget()
                numTargets.remove(interval)
                numTargets.add(getRandomInterval())
            }
        }
    }

    /**
     * Function to draw canvas to bitmap to speed up drawing
     */
    fun drawCanvas(){
        myCanvas!!.drawColor(0, PorterDuff.Mode.CLEAR)
        myCanvas!!.drawText(score.toString(),30f,80f,scorePaint)

        buffer = preStrikeBmp.width + 10f
        distance = screenWidth - buffer * 3
        for(image in strikesArray){
            myCanvas!!.drawBitmap(image, distance, 40f, canvasPaint)
            distance = distance + buffer
        }

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
                    touched = true
                    var broke = Animations(breakBmp, 4,1)
                    broke.x = target.x
                    broke.y = target.y
                    breaks.add(broke)
                    break
                }else if(target.isTouched(event) && target.isBomb && !touched){
                    targets.remove(target)
                    touchedBomb = true
                    var exp = Animations(explodeBmp, 4,4)
                    exp.x = target.x
                    exp.y = target.y
                    explosions.add(exp)
                    break
                }
            }
            if (!touched || touchedBomb) {
                gameOver()
            }
        }
        return true
    }

    private fun setTextSizeForWidth(desiredWidth: Float, text: String) {
        val testTextSize = 48f

        scorePaint.textSize = testTextSize
        val bounds = Rect()
        scorePaint.getTextBounds(text, 0, text.length, bounds)

        val desiredTextSize = testTextSize * desiredWidth / bounds.width()

        scorePaint.textSize = desiredTextSize
    }

    private fun addTarget(){
        var time = System.currentTimeMillis()
        var randObject = Random(time + seed).nextInt(1,9)
        var target: Target? = null
        when(randObject){
            1,2,3,4,5,6 -> target = Target(targetBmp, seed)
            7,8 -> target = Target(bombBmp, seed)
        }

        if (target != null) {
            if(randObject == 7 || randObject == 8){
                target.isBomb = true
            }
            if (randObject == 2 || randObject == 4 || randObject == 6 || randObject == 8){
                target.isMoving(true)
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

    private fun gameOver(){
        strikes++
        strikesArray[strikes - 1] = strikeBmp
        if(strikes == 3) {
            consumedIntent = true
            thread.setRunning(false)
            intent.putExtra("score", score)
            intent.putExtra(SAVED_INSTANCE_STATE_CONSUMED_INTENT, consumedIntent)
            context.startActivity(intent)
        }
    }

    private fun timeOut(target: Target){
        if(target.timeAlive % timeAlive == 0 && target.timeAlive != 0 && !target.isBomb){
            targets.remove(target)
            gameOver()
        }
        if(target.timeAlive % timeAlive == 0 && target.timeAlive != 0 && target.isBomb){
            targets.remove(target)
        }
    }

}