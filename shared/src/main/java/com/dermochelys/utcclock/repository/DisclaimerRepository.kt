package com.dermochelys.utcclock.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

private const val SHARED_PREFS_DISCLAIMER_FILE_NAME = "disclaimer"

private const val SHARED_PREFS_DISCLAIMER_VALUE_NAME = "agreed"

class SharedPreferencesRepository(applicationContext: Context): FlagStore {
    private val sharedPrefs: SharedPreferences =
        applicationContext.getSharedPreferences(SHARED_PREFS_DISCLAIMER_FILE_NAME, Context.MODE_PRIVATE)

    override fun isFlagSet(): Boolean {
        return sharedPrefs.getBoolean(SHARED_PREFS_DISCLAIMER_VALUE_NAME, false)
    }

    override fun setBoolean() {
        sharedPrefs.edit { putBoolean(SHARED_PREFS_DISCLAIMER_VALUE_NAME, true) }
    }
}

interface FlagStore {
    fun isFlagSet(): Boolean

    fun setBoolean()
}
