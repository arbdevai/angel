package com.arbdev.angel.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arbdev.angel.data.model.Goal
import com.arbdev.angel.data.model.TransactionType
import com.arbdev.angel.util.Formatters

@Composable
fun DepositWithdrawDialog(
    goal: Goal,
    initialType: TransactionType = TransactionType.DEPOSIT,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, type: TransactionType, note: String) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val quickPills = listOf(10000.0, 20000.0, 50000.0, 100000.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        title = {
            Text(
                text = if (selectedType == TransactionType.DEPOSIT) "Setor Saldo Celengan" else "Tarik Saldo Celengan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Saldo saat ini: ${Formatters.formatRupiah(goal.currentAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row {
                    FilterChip(
                        selected = selectedType == TransactionType.DEPOSIT,
                        onClick = {
                            selectedType = TransactionType.DEPOSIT
                            errorMessage = null
                        },
                        label = { Text("Setoran (+)") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = selectedType == TransactionType.WITHDRAW,
                        onClick = {
                            selectedType = TransactionType.WITHDRAW
                            errorMessage = null
                        },
                        label = { Text("Penarikan (-)") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickPills.forEach { pill ->
                        val isSelected = amountText == pill.toInt().toString()
                        FilterChip(
                            selected = isSelected,
                            onClick = { amountText = pill.toInt().toString() },
                            label = {
                                Text(
                                    text = Formatters.formatRupiah(pill),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { ch -> ch.isDigit() }
                        errorMessage = null
                    },
                    label = { Text("Nominal (Rp)") },
                    placeholder = { Text("Contoh: 50000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Catatan Transaksi") },
                    placeholder = { Text("Contoh: Tabungan harian / Keperluan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0) {
                        errorMessage = "Masukkan nominal yang valid"
                        return@Button
                    }
                    if (selectedType == TransactionType.WITHDRAW && amount > goal.currentAmount) {
                        errorMessage = "Saldo tidak cukup (maksimal ${Formatters.formatRupiah(goal.currentAmount)})"
                        return@Button
                    }
                    onConfirm(amount, selectedType, noteText)
                },
                shape = RoundedCornerShape(12.dp),
                colors = if (selectedType == TransactionType.WITHDRAW) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(if (selectedType == TransactionType.DEPOSIT) "Setor Saldo" else "Tarik Saldo", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Batal")
            }
        }
    )
}
