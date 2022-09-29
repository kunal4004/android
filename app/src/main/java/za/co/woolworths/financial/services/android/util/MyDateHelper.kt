package za.co.woolworths.financial.services.android.util

import android.text.TextUtils
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

interface DateHelper {
    fun setLocalDateTime(sessionKey: SessionDao.KEY?)
    fun hasDaysPassed(dateString: String?, maxPeriod: Int, sessionKey: SessionDao.KEY): Boolean
}

class MyDateHelper @Inject constructor(): DateHelper {

    companion object {
        const val datePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
        const val datePattern2 = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
    }

    override fun setLocalDateTime(sessionKey: SessionDao.KEY?) {
        val dateTime = LocalDateTime.now().toString()
        val sessionValue = Utils.getSessionDaoValue(sessionKey)
        if (sessionValue.isNullOrEmpty()) {
            Utils.sessionDaoSave(sessionKey, dateTime)
        }
    }

    override fun hasDaysPassed(dateString: String?, maxPeriod: Int, sessionKey: SessionDao.KEY): Boolean {
        // when dateString = null it means it's the first time to call api
        if (TextUtils.isEmpty(dateString)) return true
        val from = try {
            LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(datePattern))
        } catch (e: Exception) {
            LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(datePattern2))
        }
        val today = LocalDateTime.now()
        val period = ChronoUnit.DAYS.between(from, today)
        return when (period >= maxPeriod) {
            true -> {
                Utils.sessionDaoSave(sessionKey, null)
                true
            }
            false -> false
        }
    }

}