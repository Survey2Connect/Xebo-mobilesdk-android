package com.example.xebosurveycollectorandroidsdk

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.xebompack.XeboSurveyDelegate
import com.example.xebompack.XeboSurveyView

class VisitsActivity : AppCompatActivity(), XeboSurveyDelegate {
    private lateinit var surveyView: XeboSurveyView
    private val surveyUrl = "https://az4.xebo.ai/S/da217ffb"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_visits)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Initialize XeboSurveyView
        surveyView = XeboSurveyView(this)

        val visitCount = intent.getIntExtra("VISIT_COUNT", 0)

        val visitsText = findViewById<TextView>(R.id.visits_text)
        visitsText.text = "$visitCount"
        surveyView.loadSurveyAfter(2, this, this, surveyUrl)
    }

    override fun handleSurveyResponse(response: Map<String, Any>) {
        print(response)
    }

    override fun handleSurveyLoaded(response: Map<String, Any>) {
        print(response)
    }
}