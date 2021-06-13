package it.mobileflow.mfcovaxt.util

import android.content.Context
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class EzDateParser {
    companion object {
        fun parseIso8601TzUTC(dateIso8601: String, context: Context) : Date {
            val orig = dateIso8601.substring(0, 19)
            val target = orig.replace("T", " ")
            val fmt: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    context.resources.configuration.locales[0])
            fmt.timeZone = TimeZone.getTimeZone("UTC")
            return fmt.parse(target)!!
        }

        fun parseDateOnly(fmtDate: String, context: Context) : Date {
            val d : Date?

            try {
                d = SimpleDateFormat("yyyy-MM-dd",
                        context.resources.configuration.locales[0]).parse(fmtDate)
            } catch (e: ParseException) {
                return Date(0)
            }

            return d!!
        }

        fun dateTimeToStr(dt: Date, context: Context) : String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    context.resources.configuration.locales[0])
            return dateFormat.format(dt)
        }
    }
}