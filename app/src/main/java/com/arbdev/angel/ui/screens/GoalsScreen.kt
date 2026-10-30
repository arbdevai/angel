package com.arbdev.angel.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arbdev.angel.data.model.Goal
import com.arbdev.angel.ui.theme.StreakGreen
import com.arbdev.angel.util.Formatters

@Composable
fun GoalsScreen(
    goals: List<Goal>,
    dailyTarget: Double,
    onCreateGoalClick: () -> Unit,
    onDepositWithdrawClick: (Goal) -> Unit,
    onEditGoalClick: (Goal) -> Unit,
    onDeleteGoalClick: (Goal) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredGoals = when (selectedFilter) {
        "ACTIVE" -> goals.filter { !it.isCompleted }
        "COMPLETED" -> goals.filter { it.isCompleted }
        else -> goals
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Natural Inline Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Celengan Impian",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${goals.size} Wadah tabungan terdaftar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onCreateGoalClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Target Baru")
                }
            }
        }

        // Filter Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("Semua (${goals.size})") }
                )
                FilterChip(
                    selected = selectedFilter == "ACTIVE",
                    onClick = { selectedFilter = "ACTIVE" },
                    label = { Text("Aktif (${goals.count { !it.isCompleted }})") }
                )
                FilterChip(
                    selected = selectedFilter == "COMPLETED",
                    onClick = { selectedFilter = "COMPLETED" },
                    label = { Text("Tercapai (${goals.count { it.isCompleted }})") }
                )
            }
        }

        if (filteredGoals.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tidak Ada Celengan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Buat target celengan untuk mulai menabung",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onCreateGoalClick) {
                            Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buat Celengan Baru")
                        }
                    }
                }
            }
        } else {
            items(filteredGoals, key = { it.id }) { goal ->
                GoalCardItem(
                    goal = goal,
                    dailyTarget = dailyTarget,
                    onDepositWithdraw = { onDepositWithdrawClick(goal) },
                    onEdit = { onEditGoalClick(goal) },
                    onDelete = { onDeleteGoalClick(goal) },
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun GoalCardItem(
    goal: Goal,
    dailyTarget: Double,
    onDepositWithdraw: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (goal.isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StreakGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = StreakGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tercapai!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StreakGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "${goal.progressPercentage}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Ubah",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Hapus",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (goal.note.isNotBlank()) {
                Text(
                    text = goal.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Terkumpul",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Formatters.formatRupiah(goal.currentAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Formatters.formatRupiah(goal.targetAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { goal.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = goal.category.color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            val estDays = goal.getEstimatedDaysRemaining(dailyTarget)
            val estDate = goal.getEstimatedCompletionDateMillis(dailyTarget)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (goal.isCompleted) "Target tercapai!" else "Estimasi: ~$estDays hari lagi (${Formatters.formatDate(estDate)})",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (goal.isCompleted) StreakGreen else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Sisa: ${Formatters.formatRupiah(goal.remainingAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDepositWithdraw,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Payments, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Setor / Tarik Saldo",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
