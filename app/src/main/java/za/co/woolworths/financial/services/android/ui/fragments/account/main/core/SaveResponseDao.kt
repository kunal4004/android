package za.co.woolworths.financial.services.android.ui.fragments.account.main.core

import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.util.FirebaseManager
import kotlin.reflect.KProperty

class SaveResponseDao<T>(private val key: SessionDao.KEY, private val clazz: Class<T>) {

    private val gSon = Gson()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val payload = SessionDao.getByKey(key).value
        return gSon.fromJson(payload, clazz)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val sessionDao = SessionDao.getByKey(key)
        sessionDao.value = gSon.toJson(value)
        try {
            sessionDao.save()
        } catch (e: Exception) {
            FirebaseManager.logException(e)
        }
    }
}