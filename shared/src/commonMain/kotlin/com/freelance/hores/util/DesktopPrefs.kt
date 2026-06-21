package com.freelance.hores.util
import java.io.File
import java.util.Properties
class DesktopPrefs {
    private val props = Properties()
    private val prefFile = File(System.getProperty("user.home"), ".horesfreelance/settings.properties")
    init { if (prefFile.exists()) prefFile.inputStream().use { props.load(it) } }
    fun getBoolean(key: String, defaultValue: Boolean): Boolean = props.getProperty(key)?.toBoolean() ?: defaultValue
    fun getString(key: String, defaultValue: String): String = props.getProperty(key) ?: defaultValue
    fun edit(): Editor = Editor(this)
    inner class Editor(private val desktopPrefs: DesktopPrefs) {
        fun putBoolean(key: String, value: Boolean) { desktopPrefs.props.setProperty(key, value.toString()) }
        fun putString(key: String, value: String) { desktopPrefs.props.setProperty(key, value) }
        fun apply() { if (!prefFile.parentFile.exists()) prefFile.parentFile.mkdirs(); prefFile.outputStream().use { desktopPrefs.props.store(it, null) } }
    }
}
