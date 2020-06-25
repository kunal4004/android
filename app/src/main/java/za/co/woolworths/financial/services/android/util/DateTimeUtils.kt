package za.co.woolworths.financial.services.android.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


open class DateTimeUtils {

    val currentDayZoneTime: Calendar
        get() = GregorianCalendar(TimeZone.getTimeZone("Africa/Johannesburg"))

    fun parseDate(date: String): Date? {
        val inputFormat = "HH:mm"
        val inputParser = SimpleDateFormat(inputFormat, Locale.getDefault())
        return try {
            inputParser.parse(date)
        } catch (e: ParseException) {
            Date(0)
        }
    }
}