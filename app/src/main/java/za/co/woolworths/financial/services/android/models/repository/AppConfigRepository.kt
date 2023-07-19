package za.co.woolworths.financial.services.android.models.repository

import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.app_config.AppConfig
import za.co.woolworths.financial.services.android.util.db.DatabaseManager

class AppConfigRepository : DatabaseManager() {

    companion object {
        private val KEY_APP_CONFIG_DB = SessionDao.KEY.APP_CONFIG
    }

    fun saveAppConfigData(appConfig: AppConfig?) {
        return saveToDB(KEY_APP_CONFIG_DB, appConfig)
    }

    fun getAppConfigData(): AppConfig? {
        return getDataFromDB(KEY_APP_CONFIG_DB, AppConfig::class.java)
    }
}
