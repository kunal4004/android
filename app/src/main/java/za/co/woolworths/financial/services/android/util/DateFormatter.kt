package za.co.woolworths.financial.services.android.util

import android.text.TextUtils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {

    private const val UNDEFINED = "N/A"

    fun isDateExpired(validToDate: String?): Boolean {
        if (TextUtils.isEmpty(validToDate))
            return false
        try {
            val formattedDate = formatDateTOddMMMYYYY(validToDate)
            if (!TextUtils.isEmpty(formattedDate)) {
                val parsedValidDate: Date? = DateFormat.getDateInstance().parse(formattedDate ?: "")
                if (parsedValidDate?.before(Date()) == true) {
                    return true
                }
            }
        } catch (ex: Exception) {
            FirebaseManager.logException("DateFormatter isDateExpired:: $ex")
            return false
        }
        return false
    }

    fun formatDateTOddMMMYYYY(validFromDate: String?): String? {
        val toPattern = "dd MMM yyyy"
        val fromPattern = "yyyy-MM-dd"
        if (TextUtils.isEmpty(validFromDate)) {
            return UNDEFINED
        }
        return try {
            val mISO8601Local: DateFormat = SimpleDateFormat(fromPattern, Locale.US)
            val date = mISO8601Local.parse(validFromDate ?: "")
            SimpleDateFormat(toPattern, Locale.US).format(date ?: "")
        } catch (ex: Exception) {
            FirebaseManager.logException("formatDateTOddMMMYYYY $ex")
            UNDEFINED
        }
    }

    fun formatDate(validFromDate: String?): String? {
        val fromPattern = "yyyy-MM-dd'T'HH:mm:ssZ"
        val toPattern = "dd/MM/yyyy"
        if (validFromDate == null) {
            return UNDEFINED
        }
        return try {
            val mISO8601Local: DateFormat = SimpleDateFormat(fromPattern, Locale.US)
            val date: Date? = mISO8601Local.parse(validFromDate)
            SimpleDateFormat(toPattern, Locale.US).format(date ?: "")
        } catch (ex: Exception) {
            FirebaseManager.logException("formatDate $ex")
            UNDEFINED
        }
    }
}