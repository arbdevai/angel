package com.arbdev.angel.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.arbdev.angel.data.model.SavingsStats
import com.arbdev.angel.data.model.TransactionRecord
import com.arbdev.angel.data.model.TransactionType
import com.arbdev.angel.ui.components.TransactionRowItem
import com.arbdev.angel.ui.theme.StreakGreen
import com.arbdev.angel.util.Formatters

@Composable
fun HistoryScreen(
    stats: SavingsStats,
    transactions: List<TransactionRecord>
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredList = when (selectedFilter) {
        "DEPOSIT" -> transactions.filter { it.type == TransactionType.DEPOSIT }
        "WITHDRAW" -> transactions.filter { it.type == TransactionType.WITHDRAW }
        else -> transactions
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Natural Inline Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "Riwayat Tabungan",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Arsip setoran dan penarikan seluruh celengan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Monthly Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Ringkasan Bulan Ini",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Total Masuk (Deposit)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(StreakGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowDownward,
                                    contentDescription = null,
                                    tint = StreakGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Total Masuk",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Formatters.formatRupiah(stats.totalSavedThisMonth),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = StreakGreen
                                )
                            }
                        }

                        // Total Keluar (Withdraw)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowUpward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Total Keluar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Formatters.formatRupiah(stats.totalWithdrawnThisMonth),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        // Filters
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("Semua (${transactions.size})") }
                )
                FilterChip(
                    selected = selectedFilter == "DEPOSIT",
                    onClick = { selectedFilter = "DEPOSIT" },
                    label = { Text("Setoran (+)") }
                )
                FilterChip(
                    selected = selectedFilter == "WITHDRAW",
                    onClick = { selectedFilter = "WITHDRAW" },
                    label = { Text("Penarikan (-)") }
                )
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Text(
                        text = "Belum ada riwayat transaksi yang tersimpan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(filteredList, key = { it.id }) { tx ->
                TransactionRowItem(
                    transaction = tx,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
