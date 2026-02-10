package com.example.xebompack.helpers

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    // Nested object to manage all SharedPreferences keys in one place
    object Keys {
        const val visitNumber = "Visits:"  // append with SurveyID or URL to make key unique for a survey
        const val authToken = "authToken"
        // Add more keys here as needed
    }

    // Set a value in SharedPreferences
    fun setValue(key: String, value: Any?) {
        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, value)
            is Long -> editor.putLong(key, value)
            else -> throw IllegalArgumentException("Unsupported data type")
        }
        editor.apply() // Saves changes asynchronously
    }

    // Retrieve a value from SharedPreferences
    fun getValue(key: String): Any? {
        return sharedPreferences.all[key]
    }

    // Retrieve a specific type of value from SharedPreferences
    fun <T> getValue(key: String, type: Class<T>): T? {
        return when (type) {
            String::class.java -> sharedPreferences.getString(key, null) as? T
            Int::class.java -> sharedPreferences.getInt(key, 0) as? T
            Boolean::class.java -> sharedPreferences.getBoolean(key, false) as? T
            Float::class.java -> sharedPreferences.getFloat(key, 0f) as? T
            Long::class.java -> sharedPreferences.getLong(key, 0L) as? T
            else -> null
        }
    }

    // Delete a value from SharedPreferences
    fun deleteValue(key: String) {
        editor.remove(key)
        editor.apply() // Saves changes asynchronously
    }
}
