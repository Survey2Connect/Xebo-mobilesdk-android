package com.example.xebompack

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowInsets
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.fragment.app.FragmentActivity
import com.example.xebompack.helpers.SharedPreferencesHelper

class XeboSurveyView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    companion object {
        private const val TAG = "XeboSurveyView"
        private const val DEFAULT_API_URL = "https://uat-az2-api.xebo.ai/v3/survey-management/surveys/responses/response-count"

        /**
         * Pre-warms the survey engine to reduce the loading time when a survey is actually displayed.
         * Recommended to be called early in the app lifecycle (e.g., Application.onCreate).
         */
        @JvmStatic
        fun preWarmSurvey(context: Context) {
            XeboSurveyPreloader.preWarm(context)
        }
    }

    // Properties
    private var xsWebView: WebView = XeboSurveyPreloader.getPreWarmedWebView(context)
    private var loader: ProgressBar = ProgressBar(context)
    private var surveyLoaded: String = "surveyLoadStarted"
    private var surveyCompleted: String = "surveyCompleted"

    var getSurveyLoadedResponse: Boolean = false
    var surveyDelegate: XeboSurveyDelegate? = null

    private var closeButton: Button = Button(context).apply {
        text = "X"
        setTextColor(Color.BLACK)
        textSize = 16f
        setOnClickListener { closeButtonTapped() }
    }

    // Initialization
    init {
        addFeedbackView()
    }

    // Private methods
    private fun addFeedbackView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Use the new WindowInsets API for Android R and above
            setOnApplyWindowInsetsListener { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsets.Type.systemBars())

                // Add padding to avoid overlap with the top cutout (notch) and status bar
                view.setPadding(insets.left, insets.top, insets.right, insets.bottom)

                // Return the insets that have been consumed
                windowInsets
            }
        } else {
            // Fallback for older versions (Android P and below)
            setOnApplyWindowInsetsListener { view, windowInsets ->
                val insets = windowInsets.systemWindowInsetTop
                // Apply top padding to avoid notch or status bar overlap
                view.setPadding(0, insets, 0, 0)
                windowInsets
            }
        }

        xsWebView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        // Ensure settings are applied even if not pre-warmed
        xsWebView.settings.javaScriptEnabled = true
        xsWebView.settings.domStorageEnabled = true
        xsWebView.webViewClient = SurveyWebViewClient()
        // If it was pre-warmed, it might already have a background color, but we set it here for consistency
        xsWebView.setBackgroundColor(Color.GRAY)
        
        // Remove from current parent if any (in case of pre-warmed WebView)
        (xsWebView.parent as? android.view.ViewGroup)?.removeView(xsWebView)
        addView(xsWebView)

        closeButton.layoutParams = LayoutParams(100, 100).apply {
            gravity = Gravity.END
            setMargins(20, 20, 20, 20)
        }
        addView(closeButton)

        loader.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
        addView(loader)

        closeButton.visibility = View.VISIBLE
        loader.visibility = View.GONE
    }

    private fun closeButtonTapped() {
        xsWebView.loadData("<html><body></body></html>", "text/html", "UTF-8")
        closeButton.visibility = View.GONE

        (context as? FragmentActivity)?.supportFragmentManager?.popBackStack()
    }

    // WebViewClient to handle navigation and loading
    inner class SurveyWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            loader.visibility = View.GONE
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            loader.visibility = View.GONE
            Log.e("WebViewError", "Error loading page: ${error?.description}")
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString()
            if (url != null && url.startsWith("http")) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                return true
            }
            return false
        }
    }

    // Public methods
    /**
     * Loads a survey URL into a WebView.
     *
     * @param surveyUrl Optional URL of the survey. If no URL is provided, the function returns without loading anything.
     */
    fun loadSurvey(surveyUrl: String? = null) {
        loader.visibility = View.VISIBLE
        val url = surveyUrl ?: return
        Log.d("WebView", "Loading URL: $url")
        xsWebView.loadUrl(url)
    }

    /**
     * Loads a survey in fullscreen mode as a fragment.
     *
     * @param parent The parent activity in which the fragment will be added.
     * @param delegate A delegate to handle survey events and callbacks.
     * @param surveyURL Optional URL of the survey. If no URL is provided, the fragment is created without a URL.
     */
    fun loadFullscreenSurvey(parent: FragmentActivity, delegate: XeboSurveyDelegate, surveyURL: String? = null) {
        val surveyFragment = XeboSurveyFragment.newInstance(surveyURL, delegate)
        parent.supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, surveyFragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Loads an embedded survey inside the current view.
     *
     * @param surveyURL Optional URL of the survey. If no URL is provided, it attempts to load a default or fallback URL.
     * Additionally, shows a close button for the embedded survey.
     */
    fun loadEmbedSurvey(surveyURL: String? = null) {
        loadSurvey(surveyURL)
        closeButton.visibility = View.VISIBLE
    }

    /**
     * Loads a survey after a specified number of visits.
     *
     * @param visits The number of visits required before the survey is displayed.
     * @param parent The parent activity where the survey will be shown.
     * @param delegate A delegate to handle survey events and callbacks.
     * @param surveyURL Optional URL of the survey. If no URL is provided, the function manages visits but doesn't show the survey.
     *
     * The method keeps track of how many times the user has visited, and when the number of visits matches the specified `visits`,
     * it will load and display the survey in fullscreen. The visit count is stored in shared preferences.
     */
    fun loadSurveyAfter(visits: Int, parent: FragmentActivity, delegate: XeboSurveyDelegate, surveyURL: String? = null) {
        val key = SharedPreferencesHelper.Keys.visitNumber + surveyURL
        val sharedPreferencesHelper = SharedPreferencesHelper(parent)
        val savedVisits = sharedPreferencesHelper.getValue(key, Int::class.java)

        if (savedVisits != null) {
            if (visits == savedVisits) {
                // Show the survey
                sharedPreferencesHelper.deleteValue(key)
                loadFullscreenSurvey(parent, delegate, surveyURL)
            } else {
                // Update the visit number
                sharedPreferencesHelper.setValue(key, savedVisits + 1)
            }
        } else {
            // 1st visit, set the initial value in Shared Preferences
            sharedPreferencesHelper.setValue(key, 1)
        }
    }

    /**
     * Determines which survey from a list has the lowest visit count.
     *
     * @param surveyUrls A list of survey URLs to check.
     * @return The URL with the lowest visit count, or null if the list is empty.
     */
    fun getSurveyWithLowestVisits(surveyUrls: List<String>): String? {
        if (surveyUrls.isEmpty()) return null

        val sharedPreferencesHelper = SharedPreferencesHelper(context)
        var lowestVisitUrl: String? = null
        var lowestVisitCount = Int.MAX_VALUE

        for (url in surveyUrls) {
            val key = SharedPreferencesHelper.Keys.visitNumber + url
            // Default to 0 visits if not found
            val visits = sharedPreferencesHelper.getValue(key, Int::class.java) ?: 0
            
            if (visits < lowestVisitCount) {
                lowestVisitCount = visits
                lowestVisitUrl = url
            }
        }
        return lowestVisitUrl
    }

    /**
     * Loads the survey with the lowest response count from the provided list.
     * It calls an external API to determine the survey with the lowest response count.
     *
     * @param parent The parent activity in which the fragment will be added.
     * @param delegate A delegate to handle survey events and callbacks.
     * @param surveyUUIDs A list of survey UUIDs to check.
     * @param apiKey The API key for authentication.
     */
    fun loadLowestResponseSurvey(
        parent: FragmentActivity,
        delegate: XeboSurveyDelegate,
        surveyUUIDs: List<String>,
        apiKey: String
    ) {
        if (surveyUUIDs.isEmpty()) return

        Thread {
            var conn: java.net.HttpURLConnection? = null
            try {
                val url = java.net.URL(DEFAULT_API_URL)
                conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("x-api-key", apiKey)
                conn.doOutput = true
                conn.doInput = true

                // Build request body
                val jsonBody = org.json.JSONObject()
                val jsonArray = org.json.JSONArray()
                for (uuid in surveyUUIDs) {
                    jsonArray.put(uuid)
                }
                jsonBody.put("surveyUUIDs", jsonArray)

                // Write request
                conn.outputStream.use {
                    it.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
                }

                val responseCode = conn.responseCode
                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {

                    val response = conn.inputStream
                        .bufferedReader()
                        .use { it.readText() }

                    val jsonResponse = org.json.JSONObject(response)
                    val dataObject = jsonResponse.optJSONObject("data")
                    val optimalSurveyUrl = dataObject?.optString("surveyURL")

                    if (!optimalSurveyUrl.isNullOrEmpty()) {
                        parent.runOnUiThread {
                            loadFullscreenSurvey(parent, delegate, optimalSurveyUrl)
                        }
                    }
                } else {
                    Log.e("XeboSurveyView", "API Error: $responseCode")
                }

            } catch (e: Exception) {
                Log.e(
                    "XeboSurveyView",
                    "Exception in loadLowestResponseSurvey: ${e.message}",
                    e
                )
            } finally {
                conn?.disconnect()
            }
        }.start()
    }
}
