package com.justin.thompson.taptarget

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.view.View
import android.content.Context


class MainActivity : Activity() {
    private var score = 0
    private var consumed = false
    private var highScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        textView.setVisibility(View.INVISIBLE)
        val prefs = this.getSharedPreferences("TapTarget", Context.MODE_PRIVATE)
        highScore = prefs.getInt("highScore", 0)
        highScoreTxt.setText(highScore.toString())

        val extras = intent.extras

        if(extras != null){
            textView.setVisibility(View.VISIBLE)
            score = extras.getInt("score")
            textView.setText("Score  " + score.toString())
            textView.setVisibility(View.VISIBLE)
            if(score > highScore){
                highScore = score
                highScoreTxt.setText(highScore.toString())
            }
            val prefs = this.getSharedPreferences("TapTarget", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putInt("highScore", highScore)
            editor.commit()
            intent.removeExtra("score")
        }

        playButton.setOnClickListener {
            var gameIntent = Intent(this, GameActivity::class.java)
            startActivity(gameIntent)
        }
        kidsPlayButton.setOnClickListener {
            var gameIntent = Intent(this, KidsGameActivity::class.java)
            startActivity(gameIntent)
        }

    }

    override fun onResume() {
        super.onResume()
        if(intent.extras != null) {
            val extras = intent.extras
            consumed = extras.getBoolean("SAVED_INSTANCE_STATE_CONSUMED_INTENT")
            if (consumed == false) {
                textView.setVisibility(View.INVISIBLE)
            }
        }
    }
}
