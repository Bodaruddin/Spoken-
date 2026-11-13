package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ExamTimeFormatter {

    fun formatShortDateTime(millis: Long): String {
        if (millis <= 0L) return "Not set"
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val nowCal = Calendar.getInstance()
        val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())
        val timeStr = timeFmt.format(Date(millis))

        val isSameDay = cal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)
        if (isSameDay) return "Today, $timeStr"

        val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val isTomorrow = cal.get(Calendar.YEAR) == tomorrowCal.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == tomorrowCal.get(Calendar.DAY_OF_YEAR)
        if (isTomorrow) return "Tomorrow, $timeStr"

        val fullFmt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        return fullFmt.format(Date(millis))
    }

    fun formatTimeOnly(millis: Long): String {
        if (millis <= 0L) return "--:--"
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))
    }

    fun formatCountdown(remainingMillis: Long): String {
        if (remainingMillis <= 0L) return "0s"
        val totalSeconds = remainingMillis / 1000L
        val days = totalSeconds / 86400L
        val hours = (totalSeconds % 86400L) / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        return when {
            days > 0L -> "${days}d ${hours}h"
            hours > 0L -> "${hours}h ${minutes}m"
            minutes > 0L -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }
}
