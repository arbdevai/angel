package com.arbdev.angel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arbdev.angel.data.model.DailyStreakInfo
import com.arbdev.angel.data.model.Goal
import com.arbdev.angel.data.model.SavingsStats
import com.arbdev.angel.data.model.TransactionRecord
import com.arbdev.angel.ui.components.TransactionRowItem
import com.arbdev.angel.ui.components.WeeklyStreakIndicator
import com.arbdev.angel.ui.theme.AngelicGoldSecondary
import com.arbdev.angel.ui.theme.HeavenlyBluePrimary
import com.arbdev.angel.ui.theme.ReminderAmber
import com.arbdev.angel.ui.theme.StreakGreen
import com.arbdev.angel.util.Formatters

@Composable
fun DashboardScreen(
    stats: SavingsStats,
    streakInfo: DailyStreakInfo,
    goals: List<Goal>,
    dailyTarget: Double,
    recentTransactions: List<TransactionRecord>,
    onInstantDailySave: () -> Unit,
    onCustomSaveClick: () -> Unit,
    onGoalClick: (Goal) -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // App Header Title
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Angel Celengan",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Target Tabungan Mandiri",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${streakInfo.currentStreakDays} Hari",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Hero Card Total Saldo
        item {
            TotalBalanceCard(
                stats = stats,
                dailyTarget = dailyTarget,
                hasSavedToday = streakInfo.hasSavedToday,
                onInstantDailySave = onInstantDailySave,
                onCustomSaveClick = onCustomSaveClick,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Daily Streak Status
        item {
            DailyStreakBanner(
                streakInfo = streakInfo,
                dailyTarget = dailyTarget,
                onInstantDailySave = onInstantDailySave,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Weekly Activity Indicator
        item {
            WeeklyStreakIndicator(
                daysSaved = streakInfo.recentWeeklyDaysSaved,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Target Celengan Aktif
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Target Celengan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lihat Semua",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToGoals() }
                )
            }

            if (goals.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Belum Ada Target Celengan",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mulai impian Anda dengan membuat target celengan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = onNavigateToGoals) {
                            Text("Buat Celengan")
                        }
                    }
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(goals, key = { it.id }) { goal ->
                        GoalMiniCard(
                            goal = goal,
                            dailyTarget = dailyTarget,
                            onClick = { onGoalClick(goal) }
                        )
                    }
                }
            }
        }

        // Recent Transactions Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktivitas Terkini",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Riwayat Penuh",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToHistory() }
                )
            }

            if (recentTransactions.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = "Belum ada transaksi tersimpan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }

        items(recentTransactions.take(5), key = { it.id }) { tx ->
            TransactionRowItem(
                transaction = tx,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun TotalBalanceCard(
    stats: SavingsStats,
    dailyTarget: Double,
    hasSavedToday: Boolean,
    onInstantDailySave: () -> Unit,
    onCustomSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            HeavenlyBluePrimary,
                            Color(0xFF1E40AF),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Tabungan Terkumpul",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = AngelicGoldSecondary
                    ) {
                        Text(
                            text = "${stats.activeGoalsCount} Target Aktif",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Formatters.formatRupiah(stats.totalBalance),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar Target
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Target: ${Formatters.formatRupiah(stats.totalTarget)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Text(
                        text = "${stats.overallProgressPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { stats.overallProgressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(CircleShape),
                    color = AngelicGoldSecondary,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 1-Tap Instant Savings Button
                if (!hasSavedToday) {
                    Button(
                        onClick = onInstantDailySave,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = HeavenlyBluePrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Rounded.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Nabung Hari Ini — ${Formatters.formatRupiah(dailyTarget)}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = StreakGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sudah Menabung Hari Ini (${Formatters.formatRupiah(dailyTarget)})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Opsi Masukkan Nominal Manual
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = onCustomSaveClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.85f))
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Masukkan Nominal Lain",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyStreakBanner(
    streakInfo: DailyStreakInfo,
    dailyTarget: Double,
    onInstantDailySave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSaved = streakInfo.hasSavedToday
    val containerColor = if (isSaved) StreakGreen.copy(alpha = 0.12f) else ReminderAmber.copy(alpha = 0.12f)
    val accentColor = if (isSaved) StreakGreen else ReminderAmber

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Rounded.CheckCircle else Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isSaved) "Tercatat Hari Ini" else "Belum Tercatat Hari Ini",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isSaved) {
                            "Total masuk: ${Formatters.formatRupiah(streakInfo.todaySavedAmount)}"
                        } else {
                            "Target harian Anda: ${Formatters.formatRupiah(dailyTarget)}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isSaved) {
                Button(
                    onClick = onInstantDailySave,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ReminderAmber),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Nabung", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GoalMiniCard(
    goal: Goal,
    dailyTarget: Double,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(230.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = goal.category.color.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = goal.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = goal.category.color,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = "${goal.progressPercentage}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = Formatters.formatRupiah(goal.currentAmount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Target: ${Formatters.formatRupiah(goal.targetAmount)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { goal.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = goal.category.color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            val estDays = goal.getEstimatedDaysRemaining(dailyTarget)
            Text(
                text = if (goal.isCompleted) "Target tercapai!" else "Estimasi: ~$estDays hari lagi",
                style = MaterialTheme.typography.labelSmall,
                color = if (goal.isCompleted) StreakGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (goal.isCompleted) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
