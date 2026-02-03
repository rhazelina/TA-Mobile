package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ritamesa.LoginLanjut
import kotlin.math.abs

class LoginAwal : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_awal)

        val mainView = findViewById<android.view.View>(R.id.motionLayout)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        gestureDetector = GestureDetector(
            this,
            object : GestureDetector.SimpleOnGestureListener() {

                private val SWIPE_THRESHOLD = 100
                private val SWIPE_VELOCITY_THRESHOLD = 100

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {

                    if (e1 == null) return false

                    val diffX = e2.x - e1.x
                    val diffY = e2.y - e1.y

                    if (abs(diffX) > abs(diffY)) {
                        // Horizontal
                        if (abs(diffX) > SWIPE_THRESHOLD &&
                            abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
                        ) {
                            if (diffX < 0) {
                                navigateToNext()
                            }
                            return true
                        }
                    } else {
                        // Vertical
                        if (abs(diffY) > SWIPE_THRESHOLD &&
                            abs(velocityY) > SWIPE_VELOCITY_THRESHOLD
                        ) {
                            if (diffY < 0) {
                                navigateToNext()
                            }
                            return true
                        }
                    }
                    return false
                }
            }
        )

        mainView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun navigateToNext() {
        val intent = Intent(this, LoginLanjut::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right
        )
    }
}
