package it.mobileflow.mfcovaxt.util

import java.time.LocalDateTime
import java.util.*

class EzDateParser {
    companion object {
        fun parse(dateIso8601: String) : Date {
            val localDateTime = LocalDateTime.parse(dateIso8601.substring(0,19))
            return GregorianCalendar(
                localDateTime.year,
                localDateTime.monthValue,
                localDateTime.dayOfMonth,
                localDateTime.hour,
                localDateTime.minute,
                localDateTime.second).time
        }
    }
}