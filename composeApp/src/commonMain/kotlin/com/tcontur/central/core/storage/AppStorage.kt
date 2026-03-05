package com.tcontur.central.core.storage

import com.russhwolf.settings.Settings

class AppStorage(private val settings: Settings) {

    fun getString(key: String, default: String = ""): String =
        settings.getString(key, default)

    fun putString(key: String, value: String) =
        settings.putString(key, value)

    fun getBoolean(key: String, default: Boolean = false): Boolean =
        settings.getBoolean(key, default)

    fun putBoolean(key: String, value: Boolean) =
        settings.putBoolean(key, value)

    fun remove(key: String) = settings.remove(key)

    fun clear() = settings.clear()
}
