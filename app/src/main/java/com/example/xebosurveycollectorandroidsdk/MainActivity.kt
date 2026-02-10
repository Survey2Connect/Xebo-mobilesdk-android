package com.example.xebosurveycollectorandroidsdk

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.xebompack.XeboSurveyView
import com.example.xebompack.XeboSurveyDelegate

class MainActivity : AppCompatActivity(), XeboSurveyDelegate{

    private lateinit var surveyView: XeboSurveyView
    private val surveyUrl = "https://az4.xebo.ai/S/0b9414b4"
    private var visitCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Pre-warm the survey engine for faster loading
        XeboSurveyView.preWarmSurvey(this)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val main: View = findViewById<View>(R.id.main)

        val timerBased = findViewById<Button>(R.id.survey_btn)
        val afterVisits = findViewById<Button>(R.id.full_survey)
        val cartBtn = findViewById<Button>(R.id.cart_btn)
        val surveyContainer: FrameLayout = findViewById(R.id.survey_container)

        // Initialize XeboSurveyView
        surveyView = XeboSurveyView(this)
        surveyContainer.addView(surveyView)

        timerBased.setOnClickListener {
            val intent = Intent(this, TimerActivity::class.java)
            startActivity(intent)
        }

        afterVisits.setOnClickListener {
            if (visitCount >= 3) {
                visitCount = 0
            }
            visitCount++
            val intent = Intent(this, VisitsActivity::class.java)
            intent.putExtra("VISIT_COUNT", visitCount)
            startActivity(intent)
        }

        cartBtn.setOnClickListener {
            showAlert(this, "Added Successfully", "Let us know about your experience.") {
                surveyView.loadFullscreenSurvey(this, this, surveyUrl)
            }
        }

        // --- VERIFICATION START ---
        val testUrls = listOf("http://test.com/1", "http://test.com/2")
        // Simulate 1 visit for url1 (requires 10 to show, so it just increments count to 1)
        surveyView.loadSurveyAfter(10, this, this, testUrls[0])
        
        val bestUrl = surveyView.getSurveyWithLowestVisits(testUrls)
        android.util.Log.d("XeboVerification", "Expected: http://test.com/2, Actual: $bestUrl")
        // --- VERIFICATION END ---

    }

    override fun handleSurveyResponse(response: Map<String, Any>) {
        print(response)
    }

    override fun handleSurveyLoaded(response: Map<String, Any>) {
        print(response)
    }

    fun showAlert(context: Context, title: String, message: String, okAction: () -> Unit) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)

        // OK Button
        alertDialog.setPositiveButton("OK") { _, _ ->
            okAction()
        }

        // Cancel Button
        alertDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        // Show the AlertDialog
        alertDialog.show()
    }

}