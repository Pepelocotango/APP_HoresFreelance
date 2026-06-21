package com.freelance.hores.di

import java.io.File
import java.util.Properties

class DesktopPrefs : AppPrefs {
    private val props = Properties()
    private val prefFile = File(System.getProperty("user.home"), ".horesfreelance/settings.properties")

    init {
        if (prefFile.exists()) prefFile.inputStream().use { props.load(it) }
    }

    override fun getBoolean(key: String, defaultValue: Boolean) =
        props.getProperty(key)?.toBoolean() ?: defaultValue

    override fun getString(key: String, defaultValue: String) =
        props.getProperty(key) ?: defaultValue

    override fun putBoolean(key: String, value: Boolean) {
        props.setProperty(key, value.toString())
        save()
    }

    override fun putString(key: String, value: String) {
        props.setProperty(key, value)
        save()
    }

    private fun save() {
        prefFile.parentFile?.mkdirs()
        prefFile.outputStream().use { props.store(it, null) }
    }
}
