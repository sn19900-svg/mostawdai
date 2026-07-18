package com.mostawdai.feature.settings

import androidx.lifecycle.ViewModel
import com.mostawdai.data.local.AppPreferences
import com.mostawdai.data.local.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {
    val lockEnabled: StateFlow<Boolean> = appPreferences.lockEnabled
    val themeMode: StateFlow<ThemeMode> = appPreferences.themeMode

    fun setLockEnabled(enabled: Boolean) = appPreferences.setLockEnabled(enabled)
    fun setThemeMode(mode: ThemeMode) = appPreferences.setThemeMode(mode)
}
