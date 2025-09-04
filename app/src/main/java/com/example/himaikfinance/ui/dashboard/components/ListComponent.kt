package com.example.himaikfinance.ui.dashboard.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.himaikfinance.data.model.IncomeData
import com.example.himaikfinance.data.model.TransactionData
import com.example.himaikfinance.ui.dashboard.DashboardViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.runtime.LaunchedEffect
import com.example.himaikfinance.ui.theme.himaikSurface
import com.example.himaikfinance.ui.theme.himaikIncomeColor
import com.example.himaikfinance.ui.theme.himaikOutcomeColor

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListComponent(
    vm: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val incomeLazy = vm.incomePaging.collectAsLazyPagingItems()
    val trxLazy = vm.transactionPaging.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        vm.refreshLists.collect {
            incomeLazy.refresh()
            trxLazy.refresh()
        }
    }

    val mainBackground = MaterialTheme.colorScheme.surface

    Surface(
        modifier = modifier.fillMaxSize(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.large,
        color = mainBackground
    ) {
        val pagerState = rememberPagerState(pageCount = { 2 })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val title = if (pagerState.currentPage == 0) "Income" else "Transaction"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    if (page == 0) {
                        PagedListIncome(incomeLazy)
                    } else {
                        PagedListTransaction(trxLazy)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            PagerDots(
                count = 2,
                current = pagerState.currentPage,
                activeColor = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PagerDots(count: Int, current: Int, activeColor: Color) {
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { i ->
            val isActive = i == current
            val targetWidth = if (isActive) 24.dp else 8.dp
            val targetHeight = 8.dp

            val animWidth = animateDpAsState(
                targetValue = targetWidth,
                label = "pagerDotWidth"
            ).value
            val animHeight = animateDpAsState(
                targetValue = targetHeight,
                label = "pagerDotHeight"
            ).value

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(animWidth)
                    .height(animHeight)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (isActive) activeColor else inactiveColor)
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun PagedListIncome(
    items: LazyPagingItems<IncomeData>
) {
    val isInitialLoading = items.loadState.refresh is LoadState.Loading
    if (items.itemCount == 0 && isInitialLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            RoundedPolygonLoader(
                diameter = 64.dp,
                sides = 6,
                color = MaterialTheme.colorScheme.surface,
                cornerRadius = 12.dp
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        val count = items.itemCount
        items(count) { index ->
            val item = items[index]
            if (item != null) {
                IncomeItem(
                    name = item.name,
                    transferDate = formatDateDdMMyyyy(item.transfer_date),
                    nominalText = formatRupiah(parseAmountToLong(item.nominal))
                )
            }
        }
        when (items.loadState.append) {
            is LoadState.Loading -> item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp), contentAlignment = Alignment.Center
                ) {
                    Text("Loading more...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            is LoadState.Error -> item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp), contentAlignment = Alignment.Center
                ) {
                    Text("Failed to load more", color = MaterialTheme.colorScheme.error)
                }
            }

            else -> {}
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun PagedListTransaction(
    items: LazyPagingItems<TransactionData>
) {
    val isInitialLoading = items.loadState.refresh is LoadState.Loading
    if (items.itemCount == 0 && isInitialLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            RoundedPolygonLoader(
                diameter = 64.dp,
                sides = 6,
                color = MaterialTheme.colorScheme.surface,
                cornerRadius = 12.dp
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        val count = items.itemCount
        items(count) { index ->
            val item = items[index]
            if (item != null) {
                val debit = parseAmountToLong(item.debit)
                val credit = parseAmountToLong(item.credit)
                val chosen = if (debit > 0L) debit else if (credit > 0L) credit else 0L
                TransactionItem(
                    leftTitle = item.notes,
                    leftSub = formatDateDdMMyyyy(item.createdAt),
                    amountText = formatRupiah(chosen),
                    isDebit = debit > 0L
                )
            }
        }
        when (items.loadState.append) {
            is LoadState.Loading -> item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp), contentAlignment = Alignment.Center
                ) {
                    Text("Loading more...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            is LoadState.Error -> item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp), contentAlignment = Alignment.Center
                ) {
                    Text("Failed to load more", color = MaterialTheme.colorScheme.error)
                }
            }

            else -> {}
        }
    }
}

@Composable
private fun IncomeItem(name: String, transferDate: String, nominalText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = transferDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = nominalText,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = himaikIncomeColor,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
private fun TransactionItem(
    leftTitle: String,
    leftSub: String,
    amountText: String,
    isDebit: Boolean
) {
    val amountColor = if (isDebit) himaikOutcomeColor else himaikIncomeColor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = leftTitle,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = leftSub,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = amountText,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = amountColor,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDateDdMMyyyy(raw: String?): String {
    if (raw.isNullOrBlank()) return "-"
    val out = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val candidates = listOf(
        DateTimeFormatter.ISO_OFFSET_DATE_TIME,
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        null,
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    )
    for (fmt in candidates) {
        try {
            val date: LocalDate = when (fmt) {
                null -> {
                    val inst = Instant.parse(raw)
                    inst.atZone(ZoneId.systemDefault()).toLocalDate()
                }

                DateTimeFormatter.ISO_LOCAL_DATE -> LocalDate.parse(raw, fmt)
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") -> {
                    val ldt = LocalDateTime.parse(raw, fmt)
                    ldt.toLocalDate()
                }

                else -> LocalDate.parse(raw, fmt)
            }
            return date.format(out)
        } catch (_: Exception) {
        }
    }
    return raw
}

private fun parseAmountToLong(value: String?): Long {
    if (value.isNullOrBlank()) return 0L
    val s = value.trim()
    val lastComma = s.lastIndexOf(',')
    val lastDot = s.lastIndexOf('.')
    val sepIndex = maxOf(lastComma, lastDot)
    val integerPart = if (sepIndex != -1 && s.length - sepIndex - 1 in 1..2) {
        s.substring(0, sepIndex)
    } else {
        s
    }

    val digits = integerPart.filter { it.isDigit() }
    return digits.toLongOrNull() ?: 0L
}

private fun formatRupiah(amount: Long): String {
    val symbols = DecimalFormatSymbols(Locale.forLanguageTag("id-ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val formatter = DecimalFormat("#,###", symbols)
    return "Rp ${formatter.format(amount)}"
}

@Composable
private fun RoundedPolygonLoader(
    modifier: Modifier = Modifier,
    diameter: Dp = 64.dp,
    sides: Int = 6,
    color: Color = himaikSurface,
    cornerRadius: Dp = 12.dp,
    durationMillis: Int = 1200
) {
    val transition = rememberInfiniteTransition(label = "roundedPolygonRotation")
    val angle = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing)
        ),
        label = "angle"
    ).value

    Canvas(modifier = modifier.size(diameter)) {
        val w = size.width
        val h = size.height
        val cornerPx = cornerRadius.toPx()
        val r = min(w, h) / 2f - 2f
        val c = Offset(w / 2f, h / 2f)
        val n = if (sides < 3) 3 else sides

        val path = Path()
        for (i in 0 until n) {
            val theta = (2f * PI.toFloat() * i / n) - (PI.toFloat() / 2f)
            val x = c.x + cos(theta) * r
            val y = c.y + sin(theta) * r
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        rotate(degrees = angle, pivot = c) {
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    this.color = color
                    this.pathEffect = PathEffect.cornerPathEffect(cornerPx)
                    this.isAntiAlias = true
                }
                canvas.drawPath(path, paint)
            }
        }
    }
}
