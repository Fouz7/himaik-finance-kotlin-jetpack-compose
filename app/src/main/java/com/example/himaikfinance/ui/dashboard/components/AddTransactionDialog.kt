package com.example.himaikfinance.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.himaikfinance.ui.dashboard.DashboardViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    vm: DashboardViewModel,
    onDone: () -> Unit,
    dismissSignal: Int = 0,
    pivotX: Float = 0.5f
) {
    var notes by rememberSaveable { mutableStateOf("") }
    var nominalText by rememberSaveable { mutableStateOf("") }
    var showConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun isValid(): Boolean = notes.isNotBlank() && nominalText.toIntOrNull()?.let { it > 0 } == true

    val transitionState = remember { MutableTransitionState(false).apply { targetState = true } }
    LaunchedEffect(dismissSignal) { if (transitionState.currentState && transitionState.targetState) transitionState.targetState = false }
    LaunchedEffect(transitionState.isIdle, transitionState.currentState) { if (transitionState.isIdle && !transitionState.currentState) onDismiss() }

    Box(modifier = Modifier.fillMaxSize().imePadding(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(visibleState = transitionState, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                        transitionState.targetState = false
                    }
            )
        }
        AnimatedVisibility(
            visibleState = transitionState,
            enter = slideInVertically { it } + fadeIn() + scaleIn(),
            exit = slideOutVertically { it } + fadeOut() + scaleOut()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .graphicsLayer { transformOrigin = TransformOrigin(pivotX.coerceIn(0f, 1f), 1f) }
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add Transaction (Outcome)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    TextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.secondary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                            focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                            focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                    TextField(
                        value = nominalText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) nominalText = it },
                        label = { Text("Nominal") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.secondary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                            focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                            focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.secondary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { transitionState.targetState = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = { showConfirm = true },
                            enabled = isValid(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        val nominal = nominalText.toIntOrNull() ?: return@TextButton
                        scope.launch {
                            val ok = vm.postTransaction(nominal, notes)
                            if (ok) onDone() else onDismiss()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
                ) { Text("Yes") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
                ) { Text("No") }
            },
            title = { Text("Confirm", color = MaterialTheme.colorScheme.tertiary) },
            text = { Text("Submit this transaction?", color = MaterialTheme.colorScheme.tertiary) },
            containerColor = MaterialTheme.colorScheme.primary
        )
    }
}
