package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils

import za.co.woolworths.financial.services.android.models.repository.AppStateRepository
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface IMyAccountsUtils {
    fun isAppInstanceIdLinked(): Boolean
}

class MyAccountsUtils @Inject constructor() : IMyAccountsUtils {

    override fun isAppInstanceIdLinked(): Boolean {
        var isLinked = false
        val deviceList = AppStateRepository().getLinkedDevices()
        if (deviceList != null && deviceList.isNotEmpty()) {
            val uniqueDeviceId = Utils.getUniqueDeviceID()
            for (device in deviceList) {
                if (device.appInstanceId == uniqueDeviceId) {
                    isLinked = true
                    break
                }
            }
        }
        return !isLinked
    }

}