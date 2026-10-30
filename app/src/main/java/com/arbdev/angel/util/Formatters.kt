package com.arbdev.angel.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    private val idLocale = Locale("id", "ID")
    private val currencyFormat = NumberFormat.getCurrencyInstance(idLocale).apply {
        maximumFractionDigits = 0
    }

    private val dateOnlyFormat = SimpleDateFormat("dd MMM yyyy", idLocale)
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", idLocale)
    private val dayNameFormat = SimpleDateFormat("EEE", idLocale)

    fun formatRupiah(amount: Double): String {
        return try {
            val formatted = currencyFormat.format(amount)
            // Replace "Rp" with "Rp " for standard spacing
            if (formatted.startsWith("Rp") && !formatted.startsWith("Rp ")) {
                formatted.replaceFirst("Rp", "Rp ")
            } else {
                formatted
            }
        } catch (_: Exception) {
            "Rp " + String.format(Locale.US, "%,.0f", amount).replace(',', '.')
        }
    }

    fun formatDate(millis: Long): String {
        if (millis <= 0L) return "-"
        return dateOnlyFormat.format(Date(millis))
    }

    fun formatDateTime(millis: Long): String {
        if (millis <= 0L) return "-"
        return dateTimeFormat.format(Date(millis))
    }

    fun getDayName(date: Date): String {
        return dayNameFormat.format(date)
    }
}
