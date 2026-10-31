package com.arbdev.angel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arbdev.angel.data.model.Goal
import com.arbdev.angel.data.model.GoalCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateGoalDialog(
    initialGoal: Goal? = null,
    prefilledTitle: String = "",
    prefilledTargetAmount: Double = 0.0,
    prefilledDays: Int = 30,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        targetAmount: Double,
        category: GoalCategory,
        daysToTarget: Int,
        note: String
    ) -> Unit
) {
    var title by remember {
        mutableStateOf(
            initialGoal?.title ?: prefilledTitle.ifBlank { "" }
        )
    }
    var targetAmountText by remember {
        mutableStateOf(
            if (initialGoal != null) {
                com.arbdev.angel.util.Formatters.formatDigitsWithDots(initialGoal.targetAmount.toLong().toString())
            } else if (prefilledTargetAmount > 0.0) {
                com.arbdev.angel.util.Formatters.formatDigitsWithDots(prefilledTargetAmount.toLong().toString())
            } else {
                "1.000.000"
            }
        )
    }
    var selectedCategory by remember { mutableStateOf(initialGoal?.category ?: GoalCategory.GENERAL) }
    var daysText by remember { mutableStateOf(if (prefilledDays > 0) prefilledDays.toString() else "30") }
    var note by remember { mutableStateOf(initialGoal?.note ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        title = {
            Text(
                text = if (initialGoal == null) "Target Celengan Baru" else "Ubah Target Celengan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Nama Target Celengan") },
                    placeholder = { Text("Contoh: Beli Laptop Baru / Umroh") },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = com.arbdev.angel.util.Formatters.formatDigitsWithDots(it) },
                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    label = { Text("Target Nominal") },
                    placeholder = { Text("5.000.000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Kategori Celengan:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GoalCategory.entries.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = cat },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) cat.color else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else cat.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Estimasi Waktu (Hari)") },
                    placeholder = { Text("Contoh: 30 / 90") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan Motivasi (Opsional)") },
                    placeholder = { Text("Alasan atau tujuan menabung...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isSubmitting) return@Button
                    val target = com.arbdev.angel.util.Formatters.cleanDigits(targetAmountText).toDoubleOrNull() ?: 0.0
                    val days = daysText.toIntOrNull() ?: 30
                    if (title.isBlank()) {
                        errorMessage = "Nama target tidak boleh kosong"
                        return@Button
                    }
                    if (target <= 0.0) {
                        errorMessage = "Nominal target harus lebih dari 0"
                        return@Button
                    }
                    isSubmitting = true
                    onConfirm(title.trim(), target, selectedCategory, days, note.trim())
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Menyimpan...")
                } else {
                    Text(if (initialGoal == null) "Simpan Target" else "Simpan Perubahan", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Batal")
            }
        }
    )
}
