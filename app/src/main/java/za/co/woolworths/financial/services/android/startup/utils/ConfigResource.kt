package za.co.woolworths.financial.services.android.startup.utils

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
            return ConfigResource(ResponseStatus.SUCCESS, data, null)
        }

        fun  error(msg: String, data: ConfigResponse?): ConfigResource {
            return ConfigResource(ResponseStatus.ERROR, data, msg)
        }

        fun loading(data: ConfigResponse?): ConfigResource {
            return ConfigResource(ResponseStatus.LOADING, data, null)
        }

        fun persistGlobalConfig(response: ConfigResponse?, startupViewModel: StartupViewModel) {
            response?.configs?.apply {
                AppConfigRepository().saveAppConfigData(this)
            }
        }
    }
}