package com.justin.thompson.taptarget

import android.graphics.Canvas
import android.view.SurfaceHolder

class KidsGameThread(private val surfaceHolder: SurfaceHolder, private val gameView: KidsGameView) : Thread() {
    private var running: Boolean = false

    private val targetFPS = 60 // frames per second, the rate at which you would like to refresh the Canvas

    private var tick = 0

    fun setRunning(isRunning: Boolean) {
        this.running = isRunning
    }

    override fun run() {
        var startTime: Long
        var timeMillis: Long
        var waitTime: Long
        var totalTime: Long = 0
        var frameCount = 0
        val targetTime = 1000 / targetFPS

        while (running) {
            startTime = System.nanoTime()
            canvas = null

            try {
                this.gameView.update()
                this.gameView.drawCanvas()
                canvas = this.surfaceHolder.lockCanvas()
                synchronized(surfaceHolder) {
                    this.gameView.draw(canvas!!)
                }
            } catch (e: Exception) {
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }

            timeMillis = (System.nanoTime() - startTime) / 1000000
            waitTime = targetTime - timeMillis

            try {
                sleep(waitTime)
            } catch (e: Exception) {
            }

            totalTime += System.nanoTime() - startTime
            frameCount++
            tick++
            if (frameCount == targetFPS) {
                var averageFPS = 1000 / (totalTime / frameCount / 1000000)
                frameCount = 0
                totalTime = 0
            }
        }

    }

    fun getTick(): Int{
        return tick
    }

    companion object {
        private var canvas: Canvas? = null
    }

}