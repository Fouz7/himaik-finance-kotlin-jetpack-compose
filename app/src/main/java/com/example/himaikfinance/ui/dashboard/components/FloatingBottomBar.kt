package com.example.himaikfinance.ui.dashboard.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
@Suppress("UNUSED_PARAMETER")
fun FloatingBottomBar(
    items: List<Pair<ImageVector, String>>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onAddIncome: () -> Unit = {},
    onAddTransaction: () -> Unit = {},
    onUploadEvidence: () -> Unit = {},
    onShowQr: () -> Unit = {},
    onExpandedChanged: (Boolean) -> Unit = {}
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val containerHeight = 76.dp
    val controlSize = 56.dp
    val bgTargetWidth by animateDpAsState(
        targetValue = if (expanded) 320.dp else controlSize,
        label = "bgWidth"
    )
    val plusRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "plusRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .navigationBarsPadding()
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(containerHeight)
                .clip(MaterialTheme.shapes.extraLarge),
            color = MaterialTheme.colorScheme.primary,
            tonalElevation = 6.dp,
            shadowElevation = 10.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .height(controlSize)
                        .width(bgTargetWidth),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    if (expanded) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconButton(onClick = { onAddIncome() }) {
                                    Icon(
                                        imageVector = Icons.Outlined.AddCircle,
                                        contentDescription = "Add circle",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { onAddTransaction() }) {
                                    Icon(
                                        imageVector = Icons.Outlined.RemoveCircle,
                                        contentDescription = "Remove circle",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(controlSize + 16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconButton(onClick = { onUploadEvidence() }) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudUpload,
                                        contentDescription = "Cloud upload",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { onShowQr() }) {
                                    Icon(
                                        imageVector = Icons.Outlined.QrCode,
                                        contentDescription = "QR Code",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .height(controlSize)
                        .width(controlSize)
                        .align(Alignment.Center),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp,
                    onClick = {
                        val next = !expanded
                        expanded = next
                        onExpandedChanged(next)
                    }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.rotate(plusRotation)
                        )
                    }
                }
            }
        }
    }
}
