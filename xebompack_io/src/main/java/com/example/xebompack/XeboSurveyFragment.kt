package com.example.xebompack

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class XeboSurveyFragment : Fragment(), XeboSurveyDelegate {

    // Properties
    var surveyDelegate: XeboSurveyDelegate? = null
    var getSurveyLoadedResponse: Boolean = false
    var surveyUrl: String? = null
    var thankyouTimeout: Double = 3.0

    companion object {
        private const val ARG_SURVEY_URL = "survey_url"
        private const val ARG_GET_SURVEY_LOADED_RESPONSE = "get_survey_loaded_response"

        // Creating a new instance with arguments
        fun newInstance(surveyUrl: String?, delegate: XeboSurveyDelegate): XeboSurveyFragment {
            val fragment = XeboSurveyFragment()
            fragment.surveyDelegate = delegate
            val args = Bundle().apply {
                putString(ARG_SURVEY_URL, surveyUrl)
                putBoolean(ARG_GET_SURVEY_LOADED_RESPONSE, true)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {
            surveyUrl = it.getString(ARG_SURVEY_URL)
            getSurveyLoadedResponse = it.getBoolean(ARG_GET_SURVEY_LOADED_RESPONSE)
        }

        val xsSurveyView = XeboSurveyView(requireContext())
        xsSurveyView.surveyDelegate = this
        xsSurveyView.getSurveyLoadedResponse = getSurveyLoadedResponse

        // Loading the survey
        surveyUrl?.let { xsSurveyView.loadSurvey(it) }

        // Setting layout parameters for survey view
        xsSurveyView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        return xsSurveyView
    }

    // Delegate Methods
    override fun handleSurveyResponse(response: Map<String, Any>) {
        surveyDelegate?.handleSurveyResponse(response)

        // Dismissing the fragment after the thank you timeout
        Handler(Looper.getMainLooper()).postDelayed({
            parentFragmentManager.popBackStack() // Dismissing the fragment
        }, (thankyouTimeout * 1000).toLong())
    }

    override fun handleSurveyLoaded(response: Map<String, Any>) {
        surveyDelegate?.handleSurveyLoaded(response)
    }
}
