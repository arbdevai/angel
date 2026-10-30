package com.arbdev.angel.data.model

import androidx.compose.ui.graphics.Color
import com.arbdev.angel.ui.theme.CatEducation
import com.arbdev.angel.ui.theme.CatEmergency
import com.arbdev.angel.ui.theme.CatGadget
import com.arbdev.angel.ui.theme.CatGeneral
import com.arbdev.angel.ui.theme.CatInvestment
import com.arbdev.angel.ui.theme.CatVacation
import com.arbdev.angel.ui.theme.CatVehicle
import com.arbdev.angel.ui.theme.CatWorship

enum class GoalCategory(val displayName: String, val color: Color, val colorHex: String) {
    EMERGENCY("Dana Darurat", CatEmergency, "#EF4444"),
    GADGET("Gadget & Elektronik", CatGadget, "#8B5CF6"),
    VACATION("Liburan & Wisata", CatVacation, "#06B6D4"),
    VEHICLE("Kendaraan", CatVehicle, "#F97316"),
    EDUCATION("Pendidikan & Kursus", CatEducation, "#3B82F6"),
    INVESTMENT("Modal & Investasi", CatInvestment, "#10B981"),
    WORSHIP("Ibadah & Kebaikan", CatWorship, "#EC4899"),
    GENERAL("Tabungan Umum", CatGeneral, "#64748B");

    companion object {
        fun fromName(name: String?): GoalCategory {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: GENERAL
        }
    }
}
