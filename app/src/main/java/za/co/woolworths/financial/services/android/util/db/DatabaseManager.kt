package za.co.woolworths.financial.services.android.util.db

import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dao.SessionDao

open class DatabaseManager {
    companion object {
        fun saveToDB(key: SessionDao.KEY, value: Any?) {
            val sessionDao = SessionDao.getByKey(key)
            sessionDao?.value = Gson().toJson(value)
            sessionDao?.save()
        }

        fun saveToDB(key: SessionDao.KEY, value: String?) {
            val sessionDao = SessionDao.getByKey(key)
            sessionDao?.value = value
            sessionDao?.save()
        }

        fun getDataFromDB(key: SessionDao.KEY): String? {
            return SessionDao.getByKey(key)?.value
        }

        inline fun <reified T> getDataFromDB(key: SessionDao.KEY, clazz: Class<T>?): T? {
            return Gson().fromJson(SessionDao.getByKey(key).value, clazz)
        }
    }
}