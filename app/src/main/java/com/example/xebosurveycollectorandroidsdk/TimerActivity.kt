package com.example.xebosurveycollectorandroidsdk

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.CountDownTimer
import android.widget.TextView
import com.example.xebompack.XeboSurveyDelegate
import com.example.xebompack.XeboSurveyView


class TimerActivity : AppCompatActivity(), XeboSurveyDelegate {
    private var countdownTimer: CountDownTimer? = null
    private lateinit var countdownLbl: TextView
    private lateinit var surveyView: XeboSurveyView
    private val surveyUrl = "https://az4.xebo.ai/S/a7ba9c64"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_timer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        surveyView = XeboSurveyView(this)

        countdownLbl = findViewById<TextView>(R.id.timer_txt)
        val initialTime = 30L

        countdownTimer = object : CountDownTimer(initialTime * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                countdownLbl.text = seconds.toString()
            }

            override fun onFinish() {
                // Timer has finished, call your function here
                timerFinished()
                countdownTimer = null
            }

        }
        countdownTimer?.start()
    }

    private fun timerFinished() {
        // This function will be called when the timer completes
        // Add your desired actions here
        countdownLbl.text = "Timer finished!"
        surveyView.loadFullscreenSurvey(this, this, surveyUrl)
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
    }

    override fun handleSurveyResponse(response: Map<String, Any>) {
        print(response)
    }

    override fun handleSurveyLoaded(response: Map<String, Any>) {
        print(response)
    }
}