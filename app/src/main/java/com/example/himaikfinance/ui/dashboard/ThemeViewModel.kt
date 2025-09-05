package com.example.himaikfinance.ui.dashboard

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.himaikfinance.ui.enum.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.core.content.edit

class ThemeViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs by lazy {
        app.getSharedPreferences("session", Context.MODE_PRIVATE)
    }

    private val _theme = MutableStateFlow(loadTheme())
    val theme: StateFlow<AppTheme> = _theme

    fun setTheme(t: AppTheme) {
        if (_theme.value == t) return
        _theme.value = t
        viewModelScope.launch(Dispatchers.IO) {
            prefs.edit { putString("appTheme", t.name) }
        }
    }

    private fun loadTheme(): AppTheme {
        val saved = prefs.getString("appTheme", AppTheme.HIMAIK.name)
        return if (saved == AppTheme.BASIC.name) AppTheme.BASIC else AppTheme.HIMAIK
    }
}
