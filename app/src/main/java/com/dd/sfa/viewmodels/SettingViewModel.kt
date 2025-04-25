package com.dd.sfa.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dd.sfa.ui.theme.ThemeMode

class SettingsViewModel : ViewModel() {
    var themeMode = mutableStateOf(ThemeMode.SYSTEM)
}