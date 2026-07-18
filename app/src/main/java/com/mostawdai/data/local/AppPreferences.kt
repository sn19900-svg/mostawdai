package com.mostawdai.data.local

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { LIGHT, DARK, SYSTEM }

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("mostawdai_prefs", Context.MODE_PRIVATE)

    private val _lockEnabled = MutableStateFlow(prefs.getBoolean(KEY_LOCK_ENABLED, true))
    val lockEnabled: StateFlow<Boolean> = _lockEnabled

    private val _themeMode = MutableStateFlow(
        ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode

    fun setLockEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_LOCK_ENABLED, enabled) }
        _lockEnabled.value = enabled
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString(KEY_THEME_MODE, mode.name) }
        _themeMode.value = mode
    }

    companion object {
        private const val KEY_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
