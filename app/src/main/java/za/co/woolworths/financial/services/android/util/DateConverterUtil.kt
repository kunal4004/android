package za.co.woolworths.financial.services.android.util

import android.annotation.SuppressLint
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DateConverterUtil {

    companion object {
        // TimeZone Ids: https://docs.oracle.com/middleware/12212/wcs/tag-ref/MISC/TimeZones.html
        private const val INPUT_FORMAT_TIME: String = "yyyy-MM-dd'T'HH:mm:ssZ"
        private const val OUTPUT_FORMAT_TIME: String = "dd/MM/yy"
        private const val ZONE_DATE_TIME = "Africa/Johannesburg"

        @SuppressLint("NewApi")
        fun dateConversion(inputDateString: String): String? {
            val inputFormatter = DateTimeFormatter.ofPattern(INPUT_FORMAT_TIME)
            val localDate = LocalDate.parse(inputDateString, inputFormatter)
            val outputFormatter = DateTimeFormatter.ofPattern(OUTPUT_FORMAT_TIME)
            val localDateTime = localDate.atStartOfDay()
            val zonedDateTime = localDateTime.atZone(ZoneId.of(ZONE_DATE_TIME))
            return outputFormatter.format(zonedDateTime)
        }

        fun formatDate(inputDateString: String): String? {
            val convertedDate = dateConversion(inputDateString)
            return convertedDate?.replace("/", " / ")
        }
    }
}