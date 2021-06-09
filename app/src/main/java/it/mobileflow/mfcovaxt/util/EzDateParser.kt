package it.mobileflow.mfcovaxt.util

import android.content.Context
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class EzDateParser {
    companion object {
        fun parse(dateIso8601: String) : Date {
            val localDateTime = LocalDateTime.parse(dateIso8601.substring(0, 19))
            return GregorianCalendar(
                    localDateTime.year,
                    localDateTime.monthValue,
                    localDateTime.dayOfMonth,
                    localDateTime.hour,
                    localDateTime.minute,
                    localDateTime.second).time
        }

        fun parseDateOnlyGetTime(fmtDate: String, context: Context) : Long {
            val d : Date?

            try {
                d = SimpleDateFormat("yyyy-MM-dd",
                        context.resources.configuration.locales[0]).parse(fmtDate)
            } catch(e: ParseException) {
                return 0
            }

            return d!!.time
        }
    }
}