package za.co.woolworths.financial.services.android.startup.service.repository

import android.content.Context
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.network.convertToResource
import za.co.woolworths.financial.services.android.startup.service.network.StartupApiHelper
import za.co.woolworths.financial.services.android.util.Utils

/**
 * Created by Kunal Uttarwar on 23/2/21.
 */
class StartUpRepository(private val apiHelper: StartupApiHelper) {
    suspend fun queryServiceGetConfig() = apiHelper.getConfig()

    suspend fun queryCartSummary() = convertToResource {
        apiHelper.getCartSummary()
    }


    fun setSessionDao(key: SessionDao.KEY, value: String) = Utils.sessionDaoSave(key, value)
    fun getSessionDao(key: SessionDao.KEY) = Utils.getSessionDaoValue(key) == null
    fun clearSharedPreference(context: Context) = Utils.clearSharedPreferences(context)
}