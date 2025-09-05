package com.example.himaikfinance.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.himaikfinance.ui.enum.MenuAction

@Composable
fun OverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSelect: (MenuAction) -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(backgroundColor)
    ) {
        DropdownMenuItem(
            text = { Text("Theme", color = MaterialTheme.colorScheme.onBackground) },
            onClick = { onDismiss(); onSelect(MenuAction.Theme) }
        )
        DropdownMenuItem(
            text = { Text("Logout", color = MaterialTheme.colorScheme.onBackground) },
            onClick = { onDismiss(); onSelect(MenuAction.Logout) }
        )
    }
}