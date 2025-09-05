package com.example.himaikfinance.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.himaikfinance.ui.enum.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = himaikPrimary,
    secondary = himaikSecondaryBackground,
    tertiary = himaikPrimaryText,
    surface = himaikSurface
)

private val LightColorScheme = lightColorScheme(
    primary = himaikPrimary,
    secondary = himaikSecondaryBackground,
    tertiary = himaikPrimaryText,
    background = himaikBackground,
    surface = himaikSurface,
)

private val BasicLightColorScheme = lightColorScheme(
    primary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFFFF),
    secondary = Color(0xFFE5E5E5),
    tertiary = Color(0xFF000000),
    surface = Color(0xFF14213D)
)

@Composable
fun HIMAIKFinanceTheme(
    theme: AppTheme = AppTheme.HIMAIK,
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> when (theme) {
            AppTheme.HIMAIK -> if (darkTheme) DarkColorScheme else LightColorScheme
            AppTheme.BASIC -> if (darkTheme) DarkColorScheme else BasicLightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}