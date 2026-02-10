package com.example.xebompack

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView

/**
 * Singleton class to manage pre-warmed WebViews for Xebo surveys.
 */
object XeboSurveyPreloader {
    private const val TAG = "XeboSurveyPreloader"
    private var preWarmedWebView: WebView? = null

    /**
     * Creates and initializes a WebView instance in the background.
     * This prepares the WebView engine so it's ready when needed.
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun preWarm(context: Context) {
        if (preWarmedWebView != null) {
            Log.d(TAG, "WebView already pre-warmed")
            return
        }

        Log.d(TAG, "Pre-warming WebView...")
        try {
            preWarmedWebView = createWebView(context.applicationContext)
            // Load a blank page to force immediate engine initialization
            preWarmedWebView?.loadData("<html><body></body></html>", "text/html", "UTF-8")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pre-warm WebView: ${e.message}")
        }
    }

    /**
     * Retrieves the pre-warmed WebView if available, or creates a new one.
     * The caller takes ownership of the WebView.
     */
    fun getPreWarmedWebView(context: Context): WebView {
        val webView = preWarmedWebView
        if (webView != null) {
            Log.d(TAG, "Using pre-warmed WebView")
            preWarmedWebView = null // Reset so next pre-warm creates a fresh one
            
            // Re-parenting check if necessary, but usually the caller adds it to their layout
            val parent = webView.parent as? ViewGroup
            parent?.removeView(webView)
            
            return webView
        }

        Log.d(TAG, "No pre-warmed WebView available, creating new one")
        return createWebView(context)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(context: Context): WebView {
        return WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            // Optimize caching
            settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            setBackgroundColor(Color.GRAY)
        }
    }
}
