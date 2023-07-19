package za.co.woolworths.financial.services.android.startup.utils

import android.util.Log
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse
import za.co.woolworths.financial.services.android.models.repository.AppConfigRepository
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.startup.viewmodel.StartupViewModel

/**
 * Created by Kunal Uttarwar on 23/2/21.
 */

/**
 * Returns Status with stream of data.
 */
data class ConfigResource(val responseStatus: ResponseStatus, val data: ConfigResponse?, val message: String?) {

    companion object {

        fun success(data: ConfigResponse): ConfigResource {
            Log.d("TAG_DATA_APP", "SUCCESS_SUCCESS_SUCCESS_SUCCESS_SUCCESS_SUCCESS_SUCCESS_SUCCESS_SUCCESS")
            return ConfigResource(ResponseStatus.SUCCESS, data, null)
        }

        fun  error(msg: String, data: ConfigResponse?): ConfigResource {
            Log.d("TAG_DATA_APP", "ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_")
            return ConfigResource(ResponseStatus.ERROR, data, msg)
        }

        fun loading(data: ConfigResponse?): ConfigResource {
            Log.d("TAG_DATA_APP", "LOADING_LOADING_LOADING_LOADING_LOADING_LOADING_LOADING_LOADING_LOADING_LOADING_LOADING")
            return ConfigResource(ResponseStatus.LOADING, data, null)
        }

        fun persistGlobalConfig(response: ConfigResponse?, startupViewModel: StartupViewModel) {
            Log.d("TAG_DATA_APP", "PERSISTED PERSISTED PERSISTED PERSISTED")
            response?.configs?.apply {
                AppConfigRepository().saveAppConfigData(this)
                // Reset the singleton object in case it was already initialized before the cache was saved/updated
                AppConfigSingleton.initialiseFromCache()
            }
        }
    }
}