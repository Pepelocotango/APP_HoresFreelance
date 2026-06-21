package com.freelance.hores.di

interface AppPrefs {
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun getString(key: String, defaultValue: String): String
    fun putBoolean(key: String, value: Boolean)
    fun putString(key: String, value: String)
}
