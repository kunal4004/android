package za.co.woolworths.financial.services.android.startup.service.repository

import za.co.woolworths.financial.services.android.startup.service.network.StartupApiHelper

/**
 * Created by Kunal Uttarwar on 23/2/21.
 */
class StartUpRepository(private val apiHelper: StartupApiHelper) {
    suspend fun queryServiceGetConfig() = apiHelper.getConfig()
}