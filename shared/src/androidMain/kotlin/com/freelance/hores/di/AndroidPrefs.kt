package com.freelance.hores.di

import android.content.SharedPreferences

class AndroidPrefs(private val prefs: SharedPreferences) : AppPrefs {
    override fun getBoolean(key: String, defaultValue: Boolean) =
        prefs.getBoolean(key, defaultValue)

    override fun getString(key: String, defaultValue: String) =
        prefs.getString(key, defaultValue) ?: defaultValue

    override fun putBoolean(key: String, value: Boolean) =
        prefs.edit().putBoolean(key, value).apply()

    override fun putString(key: String, value: String) =
        prefs.edit().putString(key, value).apply()
}
