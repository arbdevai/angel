package com.arbdev.angel.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Cyber-Fintech Stealth Dark Palette (Lime Cards × Dark Canvas)
// ============================================================================

// 1. Root Canvas & Surface Hierarchy
val DarkCanvas = Color(0xFF141414)        // Canvas background utama (Deep Carbon OLED)
val DarkSurface = Color(0xFF1B1B1B)       // Latar kartu reguler (Deep Charcoal)
val DarkElevated = Color(0xFF242424)      // Latar dialog & surface variant elevated
val DarkBorder = Color(0xFF434343)        // Garis tepi tipis 1dp (Hairline Stroke)
val DarkBorderSubtle = Color(0xFF2C2C2C)  // Border sekunder kartu

// 2. Primary Hero Accent (Electric Lime)
val CyberLime = Color(0xFFA2FF01)         // Hero Accent, Progress, Action Buttons
val CyberLimeGlow = Color(0xFFB5FF33)     // Lighter neon highlight
val CyberLimeDark = Color(0xFF8CE600)     // Gradient end shade for hero card
val OnCyberLime = Color(0xFF141414)       // Teks hitam pekat di atas Electric Lime (Kontras 14:1)

// 3. Typography & Monochromatic Accents
val TextWhite = Color(0xFFFFFFFF)         // Teks utama, judul, saldo besar
val TextMuted = Color(0xFF9E9E9E)         // Teks sekunder, deskripsi, label
val TextDim = Color(0xFF6B7280)           // Teks tersier, timestamp

// 4. Semantic Status Colors (Fintech Tuned)
val StatusSuccess = Color(0xFFA2FF01)      // Sukses & Streak aktif (selaras dengan Lime)
val StatusWarning = Color(0xFFFBBF24)      // Amber warning
val StatusError = Color(0xFFEF4444)        // Crimson error penarikan
val StatusInfo = Color(0xFF60A5FA)         // Soft sky blue untuk info

// 5. Kategori Celengan Accent Colors (Vibrant against #141414)
val CatEmergency = Color(0xFFEF4444)
val CatVacation = Color(0xFF06B6D4)
val CatGadget = Color(0xFFA2FF01)
val CatVehicle = Color(0xFFF97316)
val CatEducation = Color(0xFF38BDF8)
val CatInvestment = Color(0xFF10B981)
val CatWorship = Color(0xFFEC4899)
val CatGeneral = Color(0xFFA2FF01)

// Backward compatibility aliases
val HeavenlyBluePrimary = CyberLime
val HeavenlyBlueOnPrimary = OnCyberLime
val HeavenlyBlueContainer = DarkElevated
val HeavenlyBlueOnContainer = CyberLime
val AngelicGoldSecondary = CyberLime
val AngelicGoldOnSecondary = OnCyberLime
val StreakGreen = CyberLime
val StreakGreenContainer = DarkElevated
val ReminderAmber = StatusWarning
