package com.example.himaikfinance.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeDialog(
    onDismiss: () -> Unit,
    vm: DashboardViewModel,
    onDone: () -> Unit,
    dismissSignal: Int = 0,
    pivotX: Float = 0.5f
) {
    var name by rememberSaveable { mutableStateOf("") }
    var nominalText by rememberSaveable { mutableStateOf("") }
    var dateText by rememberSaveable { mutableStateOf("") }
    var showConfirm by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun isValid(): Boolean = name.isNotBlank() && nominalText.toIntOrNull()
        ?.let { it > 0 } == true && dateText.isNotBlank()

    val transitionState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }

    LaunchedEffect(dismissSignal) {
        if (transitionState.currentState && transitionState.targetState) {
            transitionState.targetState = false
        }
    }

    LaunchedEffect(transitionState.isIdle, transitionState.currentState) {
        if (transitionState.isIdle && !transitionState.currentState) {
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(), contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visibleState = transitionState,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
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
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(pivotX.coerceIn(0f, 1f), 1f)
                    }
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Add Income",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    Box {
                        TextField(
                            value = dateText,
                            onValueChange = {},
                            label = { Text("Transfer Date") },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { testTagsAsResourceId = true },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.secondary,
                                unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                                focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.secondary.copy(
                                    alpha = 0.4f
                                ),
                                focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.tertiary,
                                cursorColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { showDatePicker = true }
                        )
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
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

    if (showDatePicker) {
        var tempSelected by remember { mutableStateOf<Long?>(null) }
        val nowMillis = remember { System.currentTimeMillis() }
        val state = rememberDatePickerState(
            initialSelectedDateMillis = nowMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= nowMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = state.selectedDateMillis ?: tempSelected
                        if (millis != null) {
                            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            dateText = fmt.format(Date(millis))
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
                ) { Text("Cancel") }
            },
        ) {
            DatePicker(state = state)
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
                            val ok = vm.postIncome(name, nominal, dateText)
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
            text = { Text("Submit this income?", color = MaterialTheme.colorScheme.tertiary) },
            containerColor = MaterialTheme.colorScheme.primary
        )
    }
}
