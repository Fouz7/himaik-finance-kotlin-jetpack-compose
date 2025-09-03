package com.example.himaikfinance.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CardComponent(
    bottomRightText: String,
    modifier: Modifier = Modifier,
    title: String = "Total",
    height: Dp = 120.dp,
    expandedHeight: Dp = height + 380.dp,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    titleStyle: TextStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
    bottomRightStyle: TextStyle =  MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
    contentPadding: Dp = 16.dp,
    titleMaxLines: Int = 1,
    bottomRightMaxLines: Int = 1,
    expandable: Boolean = false,
    expandedInitially: Boolean = false,
    isDebit: Boolean = false,
    incomeColor: Color = Color(0xFF267A0A),
    outcomeColor: Color = Color(0xFFA42222),
    bottomRightColor: Color? = null,
    onExpandChanged: (Boolean) -> Unit = {},
    expandContent: (@Composable () -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(expandedInitially) }
    val finalBottomRightColor = bottomRightColor ?: if (isDebit) outcomeColor else incomeColor

    val animatedHeight by animateDpAsState(
        targetValue = if (expandable && expanded) expandedHeight else height,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "cardHeight"
    )

    val rotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "chevron"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(animatedHeight)
            .clip(MaterialTheme.shapes.large)
            .clickable(
                indication = LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                expanded = !expanded
                onExpandChanged(expanded)
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = titleStyle,
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = titleMaxLines,
                    overflow = TextOverflow.Ellipsis
                )
                if (expandable) {
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = "Expand",
                        modifier = Modifier.rotate(rotation),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = bottomRightText,
                    style = bottomRightStyle,
                    color = finalBottomRightColor,
                    maxLines = bottomRightMaxLines,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (expandable && expandContent != null) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(
                        animationSpec = tween(
                            320,
                            easing = FastOutSlowInEasing
                        )
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(
                            220,
                            easing = FastOutSlowInEasing
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        expandContent()
                    }
                }
            }
        }
    }
}