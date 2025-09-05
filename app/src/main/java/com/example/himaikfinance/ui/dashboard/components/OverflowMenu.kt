package com.example.himaikfinance.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.himaikfinance.ui.enum.MenuAction

@Composable
fun OverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSelect: (MenuAction) -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
) {
    val scheme = MaterialTheme.colorScheme
    MaterialTheme(
        colorScheme = scheme.copy(surface = backgroundColor),
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes
    ) {
        CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss,
                modifier = Modifier.background(backgroundColor)
            ) {
                DropdownMenuItem(
                    text = { Text("Theme") },
                    onClick = { onDismiss(); onSelect(MenuAction.Theme) }
                )
                DropdownMenuItem(
                    text = { Text("Logout") },
                    onClick = { onDismiss(); onSelect(MenuAction.Logout) }
                )
            }
        }
    }
}

