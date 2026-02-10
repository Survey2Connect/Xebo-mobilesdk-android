package com.example.xebompack

interface XeboSurveyDelegate {
    fun handleSurveyResponse(response: Map<String, Any>)
    fun handleSurveyLoaded(response: Map<String, Any>)
}