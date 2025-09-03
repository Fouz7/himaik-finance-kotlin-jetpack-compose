package com.example.himaikfinance.ui.dashboard.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.himaikfinance.data.local.TokenManager
import com.example.himaikfinance.data.repositories.EvidenceRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border

@Composable
fun UploadEvidenceDialog(
    onDismiss: () -> Unit,
    tokenManager: TokenManager,
    onUploaded: () -> Unit,
    dismissSignal: Int = 0,
    pivotX: Float = 0.5f
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showConfirm by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> selectedUri = uri }
    )

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
                    Text(
                        "Upload Evidence",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    val areaShape = MaterialTheme.shapes.medium
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(areaShape)
                            .background(MaterialTheme.colorScheme.secondary)
                            .border(1.dp, MaterialTheme.colorScheme.outline, areaShape)
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedUri != null) {
                            AsyncImage(
                                model = selectedUri,
                                contentDescription = "Selected image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tap to change",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Tap to choose image",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "PNG/JPG",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { transitionState.targetState = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF765E41),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = { showConfirm = true },
                            enabled = selectedUri != null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF765E41),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Upload")
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
                TextButton(onClick = {
                    showConfirm = false
                    val uri = selectedUri ?: return@TextButton
                    scope.launch {
                        val repo = EvidenceRepository(tokenManager)
                        val file = copyUriToCache(context, uri)
                        val res = repo.uploadEvidence(file)
                        if (res.isSuccess) onUploaded() else onDismiss()
                    }
                },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)) { Text("Yes") }
            },
            dismissButton = { TextButton(
                onClick = { showConfirm = false },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
                ) { Text("No") } },
            title = { Text("Confirm", color = MaterialTheme.colorScheme.tertiary) },
            text = { Text("Upload this evidence?", color = MaterialTheme.colorScheme.tertiary) },
            containerColor = MaterialTheme.colorScheme.primary
        )
    }
}

private fun copyUriToCache(context: Context, uri: Uri): File {
    val input = context.contentResolver.openInputStream(uri) ?: error("Cannot open input stream")
    val ext = when (context.contentResolver.getType(uri)) {
        "image/png" -> "png"
        "image/jpeg" -> "jpg"
        else -> "jpg"
    }
    val outFile = File(context.cacheDir, "evidence_${System.currentTimeMillis()}.$ext")
    FileOutputStream(outFile).use { out -> input.use { it.copyTo(out) } }
    return outFile
}
