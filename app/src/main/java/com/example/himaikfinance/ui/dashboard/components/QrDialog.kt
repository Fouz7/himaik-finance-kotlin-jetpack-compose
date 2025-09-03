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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight

@Composable
fun QrDialog(
    imageModel: Any?,
    onDismiss: () -> Unit,
    dismissSignal: Int = 0,
    pivotX: Float = 0.5f
) {
    // Start hidden -> animate to visible on first composition
    val transitionState = remember { MutableTransitionState(false).apply { targetState = true } }

    LaunchedEffect(dismissSignal) {
        if (transitionState.currentState && transitionState.targetState) transitionState.targetState = false
    }
    LaunchedEffect(transitionState.isIdle, transitionState.currentState) {
        if (transitionState.isIdle && !transitionState.currentState) onDismiss()
    }

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
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "QR Code",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.height(12.dp))
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = "QR code",
                            contentScale = ContentScale.Fit,
                            filterQuality = FilterQuality.High,
                            modifier = Modifier
                                .size(320.dp)
                        )
                    } else {
                        Text("QR image is not configured")
                    }
                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick = { transitionState.targetState = false },) { Text("Close", color = MaterialTheme.colorScheme.tertiary) }
                }
            }
        }
    }
}
