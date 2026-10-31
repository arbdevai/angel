package com.arbdev.angel.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object Formatters {

    private val idLocale = Locale("id", "ID")
    private val currencyFormat = NumberFormat.getCurrencyInstance(idLocale).apply {
        maximumFractionDigits = 0
    }

    private val dateOnlyFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", idLocale)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", idLocale)
    private val dayNameFormatter = DateTimeFormatter.ofPattern("EEE", idLocale)

    @Synchronized
    fun formatRupiah(amount: Double): String {
        return try {
            val formatted = currencyFormat.format(amount)
            if (formatted.startsWith("Rp") && !formatted.startsWith("Rp ")) {
                formatted.replaceFirst("Rp", "Rp ")
            } else {
                formatted
            }
        } catch (_: Exception) {
            "Rp " + String.format(Locale.US, "%,.0f", amount).replace(',', '.')
        }
    }

    fun formatDigitsWithDots(rawInput: String): String {
        val digits = rawInput.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        val num = digits.toLongOrNull() ?: return digits
        return String.format(Locale.US, "%,d", num).replace(',', '.')
    }

    fun cleanDigits(formattedInput: String): String {
        return formattedInput.filter { it.isDigit() }
    }

    fun formatDate(millis: Long): String {
        if (millis <= 0L) return "-"
        return try {
            val instant = Instant.ofEpochMilli(millis)
            val zonedDateTime = instant.atZone(ZoneId.systemDefault())
            zonedDateTime.format(dateOnlyFormatter)
        } catch (_: Exception) {
            "-"
        }
    }

    fun formatDateTime(millis: Long): String {
        if (millis <= 0L) return "-"
        return try {
            val instant = Instant.ofEpochMilli(millis)
            val zonedDateTime = instant.atZone(ZoneId.systemDefault())
            zonedDateTime.format(dateTimeFormatter)
        } catch (_: Exception) {
            "-"
        }
    }

    fun getDayName(date: Date): String {
        return try {
            val instant = date.toInstant()
            val zonedDateTime = instant.atZone(ZoneId.systemDefault())
            zonedDateTime.format(dayNameFormatter)
        } catch (_: Exception) {
            "-"
        }
    }
}
