package com.example.himaikfinance.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.example.himaikfinance.data.local.TokenManager
import com.example.himaikfinance.data.remote.RetrofitClient
import com.example.himaikfinance.data.repositories.BalanceRepository
import com.example.himaikfinance.data.repositories.IncomeRepository
import com.example.himaikfinance.data.repositories.TransactionRepsitory
import com.example.himaikfinance.ui.dashboard.components.AddIncomeDialog
import com.example.himaikfinance.ui.dashboard.components.AddTransactionDialog
import com.example.himaikfinance.ui.dashboard.components.FloatingBottomBar
import com.example.himaikfinance.ui.dashboard.components.QrDialog
import com.example.himaikfinance.ui.dashboard.components.UploadEvidenceDialog
import com.example.himaikfinance.ui.dashboard.components.CardComponent
import com.example.himaikfinance.ui.dashboard.components.ListComponent
import com.example.himaikfinance.ui.dashboard.components.OverflowMenu
import com.example.himaikfinance.ui.login.LoginActivity
import com.example.himaikfinance.ui.enum.AppTheme
import com.example.himaikfinance.ui.enum.MenuAction
import com.example.himaikfinance.ui.theme.HIMAIKFinanceTheme
import kotlinx.coroutines.launch
import kotlin.math.min
import androidx.core.graphics.drawable.toDrawable
import androidx.core.content.edit
import com.example.himaikfinance.data.local.db.AppDatabase

class DashboardActivity : ComponentActivity() {

    private val tokenManager: TokenManager by lazy { TokenManager(applicationContext) }
    private val db: AppDatabase by lazy { AppDatabase.get(applicationContext) }
    private val vm: DashboardViewModel by viewModels {
        DashboardViewModelFactory(
            usernameProvider = { getUsername(applicationContext) },
            balanceRepository = BalanceRepository(RetrofitClient.api, db.balanceDao()),
            incomeRepository = IncomeRepository(RetrofitClient.api, tokenManager, db.incomeDao()),
            transactionRepository = TransactionRepsitory(RetrofitClient.api, tokenManager, db.transactionDao())
        )
    }
    private val themeVm: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val appTheme by themeVm.theme.collectAsState()

            HIMAIKFinanceTheme(theme = appTheme, darkTheme = false, dynamicColor = false) {
                val view = LocalView.current
                val navColor = MaterialTheme.colorScheme.background
                val notifColor = MaterialTheme.colorScheme.secondary
                val lightIcons = navColor.luminance() > 0.5f
                SideEffect {
                    window.setBackgroundDrawable(navColor.toArgb().toDrawable())
                    if (Build.VERSION.SDK_INT < 35) {
                        window.statusBarColor = notifColor.toArgb()
                        window.navigationBarColor = navColor.toArgb()
                    }
                    WindowCompat.getInsetsController(window, view)
                        .isAppearanceLightNavigationBars = lightIcons
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.secondary
                ) {
                    DashboardScreen(
                        vm = vm,
                        tokenManager = tokenManager,
                        onLogout = { performLogout() },
                        currentTheme = appTheme,
                        onChangeTheme = { t -> themeVm.setTheme(t) }
                    )
                }
            }
        }
    }

    private fun performLogout() {
        lifecycleScope.launch {
            tokenManager.clear()
            getSharedPreferences("session", Context.MODE_PRIVATE)
                .edit {
                    clear()
                }
            startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreen(
    vm: DashboardViewModel,
    tokenManager: TokenManager,
    onLogout: () -> Unit,
    currentTheme: AppTheme,
    onChangeTheme: (AppTheme) -> Unit
) {
    LaunchedEffect(Unit) {
        vm.loadBalanceEvidence()
        vm.loadTotalIncome()
        vm.loadTotalOutcome()
    }
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val items: List<Pair<ImageVector, String>> = listOf(
        Icons.Outlined.Home to "Income",
        Icons.Outlined.Wallet to "Evidence",
        Icons.Outlined.NotificationsNone to "Transactions"
    )
    var selected by rememberSaveable { mutableStateOf(0) }
    val username by vm.username.collectAsState()
    val totalBalance by vm.totalBalanceText.collectAsState()
    val totalIncome by vm.totalIncomeText.collectAsState()
    val totalOutcome by vm.totalOutcomeText.collectAsState()
    val evidenceUrl by vm.balanceEvidenceUrl.collectAsState()

    var activeDialog by rememberSaveable { mutableStateOf<String?>(null) }
    var dismissSignal by remember { mutableStateOf(0) }
    var pivotX by remember { mutableStateOf(0.5f) }
    var pendingDialog by remember { mutableStateOf<String?>(null) }
    var pendingPivotX by remember { mutableStateOf(0.5f) }

    fun requestDismiss() {
        dismissSignal++
    }

    fun openOrToggle(target: String, x: Float) {
        if (activeDialog == null) {
            pivotX = x
            activeDialog = target
        } else if (activeDialog == target) {
            requestDismiss()
        } else {
            pendingDialog = target
            pendingPivotX = x
            requestDismiss()
        }
    }

    LaunchedEffect(activeDialog) {
        if (activeDialog == null && pendingDialog != null) {
            pivotX = pendingPivotX
            activeDialog = pendingDialog
            pendingDialog = null
        }
    }

    var showLogoutConfirm by rememberSaveable { mutableStateOf(false) }
    var showThemeChooser by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.secondary,
        bottomBar = {
            FloatingBottomBar(
                items = items,
                selectedIndex = selected,
                onSelect = {
                    selected = it
                    if (activeDialog != null) requestDismiss()
                },
                onAddIncome = { openOrToggle("income", 0.25f) },
                onAddTransaction = { openOrToggle("transaction", 0.40f) },
                onUploadEvidence = { openOrToggle("upload", 0.60f) },
                onShowQr = { openOrToggle("qr", 0.75f) },
                onExpandedChanged = { expanded ->
                    if (!expanded && activeDialog != null) requestDismiss()
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    vm.refreshAll()
                    isRefreshing = false
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                DashboardBody(
                    vm = vm,
                    selected = selected,
                    items = items,
                    username = username,
                    totalBalance = totalBalance,
                    totalIncome = totalIncome,
                    totalOutcome = totalOutcome,
                    evidenceUrl = evidenceUrl,
                    onRequestMenu = { action ->
                        when (action) {
                            MenuAction.Theme -> showThemeChooser = true
                            MenuAction.Logout -> showLogoutConfirm = true
                        }
                    }
                )
            }

            when (activeDialog) {
                "income" -> AddIncomeDialog(
                    onDismiss = { activeDialog = null },
                    vm = vm,
                    onDone = { requestDismiss() },
                    dismissSignal = dismissSignal,
                    pivotX = pivotX
                )

                "transaction" -> AddTransactionDialog(
                    onDismiss = { activeDialog = null },
                    vm = vm,
                    onDone = { requestDismiss() },
                    dismissSignal = dismissSignal,
                    pivotX = pivotX
                )

                "upload" -> UploadEvidenceDialog(
                    onDismiss = {
                        vm.loadBalanceEvidence()
                        activeDialog = null
                    },
                    tokenManager = tokenManager,
                    onUploaded = {
                        vm.forceRefreshBalanceEvidence()
                        requestDismiss()
                    },
                    dismissSignal = dismissSignal,
                    pivotX = pivotX
                )

                "qr" -> QrDialog(
                    imageModel = qrImageModel(LocalContext.current),
                    onDismiss = { activeDialog = null },
                    dismissSignal = dismissSignal,
                    pivotX = pivotX
                )
            }

            var selectedTheme by rememberSaveable { mutableStateOf(currentTheme) }
            if (showThemeChooser) {
                AlertDialog(
                    onDismissRequest = { showThemeChooser = false },
                    title = { Text("Select Theme", color = MaterialTheme.colorScheme.tertiary) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedTheme == AppTheme.HIMAIK,
                                    onClick = { selectedTheme = AppTheme.HIMAIK },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("HIMAIK", color = MaterialTheme.colorScheme.tertiary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedTheme == AppTheme.BASIC,
                                    onClick = { selectedTheme = AppTheme.BASIC },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Basic", color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onChangeTheme(selectedTheme)
                                showThemeChooser = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
                        ) { Text("Apply") }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showThemeChooser = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
                        ) { Text("Cancel") }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }

            if (showLogoutConfirm) {
                AlertDialog(
                    onDismissRequest = { showLogoutConfirm = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showLogoutConfirm = false
                                onLogout()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
                        ) { Text("Yes") }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showLogoutConfirm = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
                        ) { Text("No") }
                    },
                    title = { Text("Confirm", color = MaterialTheme.colorScheme.tertiary) },
                    text = {
                        Text(
                            "Are you sure you want to logout?",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun safeQrUrl(): String? = try {
    val clazz = Class.forName("com.example.himaikfinance.BuildConfig")
    val field = clazz.getField("QR_IMAGE_URL")
    val value = field.get(null) as? String
    value?.takeIf { it.isNotBlank() }
} catch (_: Exception) {
    null
}

private fun qrImageModel(ctx: Context): Any? {
    val resId = ctx.resources.getIdentifier("qrdana", "drawable", ctx.packageName)
    if (resId != 0) return resId
    return safeQrUrl()
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun DashboardBody(
    vm: DashboardViewModel,
    selected: Int,
    items: List<Pair<ImageVector, String>>,
    username: String,
    totalBalance: String,
    totalIncome: String,
    totalOutcome: String,
    evidenceUrl: String?,
    onRequestMenu: (MenuAction) -> Unit
) {
    when (selected) {
        0 -> Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hello, $username",
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    OverflowMenu(
                        expanded = expanded,
                        onDismiss = { expanded = false },
                        onSelect = onRequestMenu
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            val pagerState = rememberPagerState(pageCount = { 3 })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                pageSpacing = 12.dp
            ) { page ->
                when (page) {
                    0 -> CardComponent(
                        title = "Total Balance",
                        bottomRightText = totalBalance,
                        expandable = true,
                        bottomRightColor = MaterialTheme.colorScheme.tertiary,
                        expandContent = {
                            if (!evidenceUrl.isNullOrBlank()) {
                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                    val density = LocalDensity.current
                                    var targetRatio by remember { mutableStateOf(1f) }
                                    var intrinsicWidthPx by remember { mutableStateOf<Int?>(null) }

                                    val screenWidthPx = with(density) { maxWidth.toPx() }
                                    val maxWidthPx =
                                        intrinsicWidthPx?.let { min(it.toFloat(), screenWidthPx) }
                                            ?: screenWidthPx
                                    val targetWidthDp = with(density) { maxWidthPx.toDp() }

                                    AsyncImage(
                                        model = evidenceUrl,
                                        contentDescription = "Evidence",
                                        contentScale = ContentScale.Inside,
                                        filterQuality = FilterQuality.High,
                                        modifier = Modifier
                                            .width(targetWidthDp)
                                            .aspectRatio(targetRatio)
                                            .align(Alignment.Center)
                                            .clip(MaterialTheme.shapes.medium),
                                        onSuccess = { success ->
                                            val d = success.result.drawable
                                            val w = d.intrinsicWidth.coerceAtLeast(1)
                                            val h = d.intrinsicHeight.coerceAtLeast(1)
                                            intrinsicWidthPx = w
                                            targetRatio = w.toFloat() / h.toFloat()
                                        }
                                    )
                                }
                            } else {
                                Text(
                                    "Evidence not available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    1 -> CardComponent(
                        title = "Total Income",
                        bottomRightText = totalIncome,
                        expandable = false,
                        isDebit = false
                    )

                    2 -> CardComponent(
                        title = "Total Outcome",
                        bottomRightText = totalOutcome,
                        expandable = false,
                        isDebit = true
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pageCount = pagerState.pageCount
                repeat(pageCount) { index ->
                    val selected = pagerState.currentPage == index
                    val dotWidth by animateDpAsState(
                        targetValue = if (selected) 20.dp else 8.dp,
                        label = "dotWidth"
                    )
                    val dotColor = if (selected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(dotColor)
                    )

                    if (index != pageCount - 1) {
                        Spacer(Modifier.width(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ListComponent(
                    vm = vm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "List not supported on this Android version",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        else -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = items[selected].second,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

private fun getUsername(ctx: Context): String {
    val prefs = ctx.getSharedPreferences("session", Context.MODE_PRIVATE)
    return prefs.getString("username", "User") ?: "User"
}
