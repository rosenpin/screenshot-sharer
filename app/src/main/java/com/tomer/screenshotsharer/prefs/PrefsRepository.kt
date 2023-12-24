package com.tomer.screenshotsharer.prefs

import android.content.SharedPreferences

interface PrefsRepository {
    fun getString(key: String, defaultValue: String): String
    fun getInt(key: String, defaultValue: Int): Int
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun getFloat(key: String, defaultValue: Float): Float
    fun getLong(key: String, defaultValue: Long): Long
    fun <T> setSetting(key: String, value: T)
}

class PrefsRepositoryImpl(private val sharedPreferences: SharedPreferences) :PrefsRepository{
    override fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    override fun <T> setSetting(key: String, value: T) {
        with(sharedPreferences.edit()) {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                else -> throw IllegalArgumentException("This type can't be saved into SharedPreferences")
            }.apply()
        }
    }
}

class PrefsRepositoryMock : PrefsRepository {
    override fun getString(key: String, defaultValue: String): String {
        return defaultValue
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return defaultValue
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return defaultValue
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return defaultValue
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return defaultValue
    }

    override fun <T> setSetting(key: String, value: T) {
    }
}