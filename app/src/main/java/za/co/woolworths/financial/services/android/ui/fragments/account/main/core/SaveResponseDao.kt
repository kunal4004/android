package za.co.woolworths.financial.services.android.ui.fragments.account.main.core

import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

class SaveResponseDao {

    companion object {

        inline fun<reified T> getValue(key: SessionDao.KEY, clazz: Class<T> = T::class.java): T {
            val gSon = Gson()
            val payload = SessionDao.getByKey(key).value
            return gSon.fromJson(payload, clazz)
        }

        fun<T> setValue(key: SessionDao.KEY,value : T) {
            val gSon = Gson()
            val sessionDao = SessionDao.getByKey(key)
            sessionDao.value = gSon.toJson(value)
            try {
                sessionDao.save()
            } catch (e: Exception) {
                FirebaseManager.logException(e)
            }
        }
    }
}