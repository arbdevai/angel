package com.arbdev.angel

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arbdev.angel.data.model.Goal
import com.arbdev.angel.data.model.GoalCategory
import com.arbdev.angel.data.preferences.UserPreferencesManager
import com.arbdev.angel.data.repository.SavingsRepository
import com.arbdev.angel.ui.components.AngelCompactDock
import com.arbdev.angel.ui.components.AngelTab
import com.arbdev.angel.ui.components.CreateGoalDialog
import com.arbdev.angel.ui.components.DepositWithdrawDialog
import com.arbdev.angel.ui.components.NotificationRationaleDialog
import com.arbdev.angel.ui.components.QuickSaveSheet
import com.arbdev.angel.ui.screens.DashboardScreen
import com.arbdev.angel.ui.screens.GoalsScreen
import com.arbdev.angel.ui.screens.HistoryScreen
import com.arbdev.angel.ui.screens.SettingsScreen
import com.arbdev.angel.ui.screens.SimulatorScreen
import com.arbdev.angel.ui.theme.AngelTheme
import com.arbdev.angel.util.Formatters
import com.arbdev.angel.widget.AngelAppWidgetProvider
import com.arbdev.angel.worker.NotificationHelper
import com.arbdev.angel.worker.ReminderScheduler
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(applicationContext)

        val repository = SavingsRepository.getInstance(applicationContext)
        val prefs = UserPreferencesManager(applicationContext)

        setContent {
            AngelTheme {
                val coroutineScope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }
                val haptic = LocalHapticFeedback.current

                // Collect state
                val goals by repository.goals.collectAsStateWithLifecycle()
                val transactions by repository.transactions.collectAsStateWithLifecycle()
                val streakInfo by repository.streakInfo.collectAsStateWithLifecycle()
                val stats by repository.stats.collectAsStateWithLifecycle()

                val isReminderEnabled by prefs.isDailyReminderEnabled.collectAsStateWithLifecycle(initialValue = true)
                val reminderHour by prefs.reminderHour.collectAsStateWithLifecycle(initialValue = 20)
                val reminderMinute by prefs.reminderMinute.collectAsStateWithLifecycle(initialValue = 0)
                val dailyTarget by prefs.dailySavingTarget.collectAsStateWithLifecycle(initialValue = 20000.0)

                // Navigation State
                var currentTab by remember { mutableStateOf(AngelTab.DASHBOARD) }

                // Modals State
                var showQuickSaveSheet by remember { mutableStateOf(false) }
                val quickSaveSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                var activeDepositGoal by remember { mutableStateOf<Goal?>(null) }
                var activeEditGoal by remember { mutableStateOf<Goal?>(null) }
                var showCreateGoalDialog by remember { mutableStateOf(false) }
                var goalToDelete by remember { mutableStateOf<Goal?>(null) }

                // Prefilled data from Simulator
                var prefillTitle by remember { mutableStateOf("") }
                var prefillTargetAmount by remember { mutableStateOf(0.0) }
                var prefillDays by remember { mutableStateOf(30) }

                // Permission State (Android 13+)
                var showRationaleDialog by remember { mutableStateOf(false) }
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Izin notifikasi pengingat harian aktif.")
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasPermission) {
                            showRationaleDialog = true
                        }
                    }
                    ReminderScheduler.scheduleDailyReminder(applicationContext, reminderHour, reminderMinute)
                    AngelAppWidgetProvider.updateAllWidgets(applicationContext)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        AngelCompactDock(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                if (targetState.ordinal > initialState.ordinal) {
                                    (slideInHorizontally { width -> width } + fadeIn())
                                        .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                                } else {
                                    (slideInHorizontally { width -> -width } + fadeIn())
                                        .togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                                }
                            },
                            label = "TabNavigationAnimation"
                        ) { targetTab ->
                            when (targetTab) {
                                AngelTab.DASHBOARD -> {
                                    DashboardScreen(
                                        stats = stats,
                                        streakInfo = streakInfo,
                                        goals = goals,
                                        dailyTarget = dailyTarget,
                                        recentTransactions = transactions,
                                        onInstantDailySave = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            coroutineScope.launch {
                                                val primaryGoal = goals.firstOrNull { !it.isCompleted } ?: goals.firstOrNull()
                                                if (primaryGoal != null) {
                                                    val ok = repository.saveMoney(primaryGoal.id, dailyTarget, "Setoran target harian")
                                                    if (ok) {
                                                        NotificationHelper.playChimeSound(applicationContext)
                                                        AngelAppWidgetProvider.updateAllWidgets(applicationContext)
                                                        snackbarHostState.showSnackbar("Setoran ${Formatters.formatRupiah(dailyTarget)} berhasil dicatat.")
                                                    }
                                                } else {
                                                    showCreateGoalDialog = true
                                                }
                                            }
                                        },
                                        onCustomSaveClick = { showQuickSaveSheet = true },
                                        onGoalClick = { goal -> activeDepositGoal = goal },
                                        onNavigateToGoals = { currentTab = AngelTab.GOALS },
                                        onNavigateToHistory = { currentTab = AngelTab.HISTORY }
                                    )
                                }
                                AngelTab.GOALS -> {
                                    GoalsScreen(
                                        goals = goals,
                                        dailyTarget = dailyTarget,
                                        onCreateGoalClick = {
                                            prefillTitle = ""
                                            prefillTargetAmount = 0.0
                                            prefillDays = 30
                                            showCreateGoalDialog = true
                                        },
                                        onDepositWithdrawClick = { goal -> activeDepositGoal = goal },
                                        onEditGoalClick = { goal -> activeEditGoal = goal },
                                        onDeleteGoalClick = { goal -> goalToDelete = goal }
                                    )
                                }
                                AngelTab.SIMULATOR -> {
                                    SimulatorScreen(
                                        currentDailyTarget = dailyTarget,
                                        onCreateGoalFromMilestone = { title, targetAmount, _, days ->
                                            prefillTitle = title
                                            prefillTargetAmount = targetAmount
                                            prefillDays = days
                                            showCreateGoalDialog = true
                                        }
                                    )
                                }
                                AngelTab.HISTORY -> {
                                    HistoryScreen(
                                        stats = stats,
                                        transactions = transactions
                                    )
                                }
                                AngelTab.SETTINGS -> {
                                    SettingsScreen(
                                        isReminderEnabled = isReminderEnabled,
                                        reminderHour = reminderHour,
                                        reminderMinute = reminderMinute,
                                        dailyTarget = dailyTarget,
                                        onToggleReminder = { enabled ->
                                            coroutineScope.launch {
                                                prefs.setDailyReminderEnabled(enabled)
                                                if (enabled) {
                                                    ReminderScheduler.scheduleDailyReminder(
                                                        applicationContext,
                                                        reminderHour,
                                                        reminderMinute
                                                    )
                                                    snackbarHostState.showSnackbar("Pengingat harian diaktifkan.")
                                                } else {
                                                    ReminderScheduler.cancelReminder(applicationContext)
                                                    snackbarHostState.showSnackbar("Pengingat harian dinonaktifkan.")
                                                }
                                            }
                                        },
                                        onChangeReminderTime = { h, m ->
                                            coroutineScope.launch {
                                                prefs.setReminderTime(h, m)
                                                ReminderScheduler.scheduleDailyReminder(applicationContext, h, m)
                                                snackbarHostState.showSnackbar(
                                                    String.format("Waktu pengingat disimpan: %02d:%02d WIB", h, m)
                                                )
                                            }
                                        },
                                        onChangeDailyTarget = { target ->
                                            coroutineScope.launch {
                                                prefs.setDailySavingTarget(target)
                                                AngelAppWidgetProvider.updateAllWidgets(applicationContext)
                                                snackbarHostState.showSnackbar("Target harian berhasil diperbarui.")
                                            }
                                        },
                                        onPlayChimeSound = {
                                            NotificationHelper.playChimeSound(applicationContext)
                                        },
                                        onSendTestNotification = {
                                            NotificationHelper.sendReminderNotification(
                                                context = applicationContext,
                                                title = "Uji Coba Pengingat Angel",
                                                message = "Ini adalah simulasi pengingat tabungan harian dengan audio lonceng Angel."
                                            )
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Notifikasi uji coba berhasil dikirim.")
                                            }
                                        },
                                        onExportBackup = {
                                            coroutineScope.launch {
                                                val json = repository.exportBackupJson()
                                                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Angel Backup", json))

                                                // Share Sheet ramah pengguna awam
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_SUBJECT, "Cadangan Data Celengan Angel")
                                                    putExtra(Intent.EXTRA_TEXT, json)
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                startActivity(Intent.createChooser(shareIntent, "Bagikan Cadangan Celengan"))
                                                snackbarHostState.showSnackbar("Data cadangan berhasil disalin dan siap dibagikan.")
                                            }
                                        },
                                        onRestoreBackup = { jsonStr ->
                                            coroutineScope.launch {
                                                val result = repository.restoreBackupJson(jsonStr)
                                                result.fold(
                                                    onSuccess = { count ->
                                                        AngelAppWidgetProvider.updateAllWidgets(applicationContext)
                                                        snackbarHostState.showSnackbar("Berhasil memulihkan $count celengan.")
                                                    },
                                                    onFailure = { err ->
                                                        snackbarHostState.showSnackbar(err.message ?: "Gagal memulihkan cadangan.")
                                                    }
                                                )
                                            }
                                        },
                                        onClearAllData = {
                                            coroutineScope.launch {
                                                repository.clearAllData()
                                                AngelAppWidgetProvider.updateAllWidgets(applicationContext)
                                                snackbarHostState.showSnackbar("Seluruh data berhasil dihapus.")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Save Bottom Sheet (Nominal Kustom)
                if (showQuickSaveSheet) {
                    QuickSaveSheet(
                        sheetState = quickSaveSheetState,
                        goals = goals,
                        onDismiss = { showQuickSaveSheet = false },
                        onConfirmSave = { goalId, amount, note ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            coroutineScope.launch {
                                val ok = repository.saveMoney(goalId, amount, note)
                                showQuickSaveSheet = false
                                if (ok) {
                                    NotificationHelper.playChimeSound(applicationContext)
                                    AngelAppWidgetProvider.updateAllWidgets(applicationContext)
                                    snackbarHostState.showSnackbar("Setoran tabungan berhasil dicatat.")
                                }
                            }
                        }
                    )
                }

                // Setor / Tarik Dialog
                activeDepositGoal?.let { goal ->
                    DepositWithdrawDialog(
                        goal = goal,
                        onDismiss = { activeDepositGoal = null },
                        onConfirm = { amount, type, note ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            coroutineScope.launch {
                                val ok = if (type == com.arbdev.angel.data.model.TransactionType.DEPOSIT) {
                                    repository.saveMoney(goal.id, amount, note)
                                } else {
                                    repository.withdrawMoney(goal.id, amount, note)
                                }
                                activeDepositGoal = null
                                if (ok) {
                                    if (type == com.arbdev.angel.data.model.TransactionType.DEPOSIT) {
                                        NotificationHelper.playChimeSound(applicationContext)
                                    }
                                    AngelAppWidgetProvider.updateAllWidgets(applicationContext)
                                    snackbarHostState.showSnackbar("Transaksi berhasil dicatat.")
                                }
                            }
                        }
                    )
                }

                // Buat / Ubah Celengan Dialog
                if (showCreateGoalDialog || activeEditGoal != null) {
                    CreateGoalDialog(
                        initialGoal = activeEditGoal,
                        prefilledTitle = prefillTitle,
                        prefilledTargetAmount = prefillTargetAmount,
                        prefilledDays = prefillDays,
                        onDismiss = {
                            showCreateGoalDialog = false
                            activeEditGoal = null
                            prefillTitle = ""
                            prefillTargetAmount = 0.0
                        },
                        onConfirm = { title, targetAmount, category, daysToTarget, note ->
                            coroutineScope.launch {
                                val targetDateMillis = System.currentTimeMillis() + (daysToTarget.toLong() * 24 * 60 * 60 * 1000)
                                if (activeEditGoal != null) {
                                    val updated = activeEditGoal!!.copy(
                                        title = title,
                                        targetAmount = targetAmount,
                                        categoryName = category.name,
                                        targetDateMillis = targetDateMillis,
                                        colorHex = category.colorHex,
                                        note = note
                                    )
                                    repository.updateGoal(updated)
                                    snackbarHostState.showSnackbar("Target celengan berhasil diperbarui.")
                                } else {
                                    repository.createGoal(
                                        title = title,
                                        targetAmount = targetAmount,
                                        categoryName = category.name,
                                        targetDateMillis = targetDateMillis,
                                        colorHex = category.colorHex,
                                        note = note
                                    )
                                    snackbarHostState.showSnackbar("Celengan baru berhasil dibuat.")
                                }
                                AngelAppWidgetProvider.updateAllWidgets(applicationContext)
                                showCreateGoalDialog = false
                                activeEditGoal = null
                                prefillTitle = ""
                                prefillTargetAmount = 0.0
                            }
                        }
                    )
                }

                // Konfirmasi Hapus Celengan
                goalToDelete?.let { goal ->
                    AlertDialog(
                        onDismissRequest = { goalToDelete = null },
                        shape = RoundedCornerShape(26.dp),
                        title = { Text("Hapus Celengan") },
                        text = { Text("Hapus target '${goal.title}'? Seluruh riwayat transaksi celengan ini juga akan dihapus.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.deleteGoal(goal.id)
                                        goalToDelete = null
                                        AngelAppWidgetProvider.updateAllWidgets(applicationContext)
                                        snackbarHostState.showSnackbar("Celengan berhasil dihapus.")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Hapus", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { goalToDelete = null }, shape = RoundedCornerShape(12.dp)) {
                                Text("Batal")
                            }
                        }
                    )
                }

                // Educational Notification Permission Dialog
                if (showRationaleDialog) {
                    NotificationRationaleDialog(
                        onDismiss = { showRationaleDialog = false },
                        onGrantRequested = {
                            showRationaleDialog = false
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                }
            }
        }
    }
}
