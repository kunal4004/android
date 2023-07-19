package za.co.woolworths.financial.services.android.ui.fragments.account.device_security

import android.app.Activity
import android.content.Intent
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.linkdevice.ViewAllLinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.repository.AppStateRepository
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeUnfreezeCardItemFragment.Companion.DEVICE_SECURITY_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

fun verifyAppInstanceId(isDeviceLinked: Boolean = true): Boolean {
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
    return !isLinked && isDeviceLinked
}
fun verifyAppInstanceId(): Boolean {
    var isLinked = false
    val deviceList = AppStateRepository().getLinkedDevices()
    if (!deviceList.isNullOrEmpty()) {
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

fun updateLinkedDevices() {
    if (SessionUtilities.getInstance().isUserAuthenticated) {

        OneAppService().getAllLinkedDevices(true).enqueue(CompletionHandler (object :
            IResponseListener<ViewAllLinkedDeviceResponse> {
            override fun onSuccess(response: ViewAllLinkedDeviceResponse?) {
                response?.userDevices?.let  {
                    AppStateRepository().saveLinkedDevices(response.userDevices)
                }
            }
        }, ViewAllLinkedDeviceResponse::class.java))
    }
}


fun linkMyDeviceIfNecessary(
    activity: Activity,
    isDeviceLinked: Boolean,
    state: ApplyNowState,
    doJob: () -> Unit,
    elseJob: () -> Any?) {
    when (verifyAppInstanceId(isDeviceLinked) &&
            (Utils.isGooglePlayOrHuaweiMobileServicesAvailable())
    ) {
        true -> {
            doJob()
            activity.let {
                val intent = Intent(it, LinkDeviceConfirmationActivity::class.java)
                intent.putExtra(AccountSignedInPresenterImpl.APPLY_NOW_STATE, state)
                it.startActivityForResult(intent, DEVICE_SECURITY_REQUEST_CODE)
                it.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay)
            }
        }
        else -> elseJob()

    }
}