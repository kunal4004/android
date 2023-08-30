package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_device_security.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject


interface IDeviceSecurityActivityResult {
    fun createDeviceSecurityIntent(deepLinkParams: String?,
                                   applyNowState: ApplyNowState): Intent?
    fun createDeviceSecurityActivityResult(
        deepLinkParams: String?,
        applyNowState: ApplyNowState,
        viewModel: UserAccountLandingViewModel?,
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?
    )
    fun setDeviceSecurityResultCode(applyNowState: ApplyNowState,
                                    viewModel: UserAccountLandingViewModel?,
                                    resultCode: Int)
}

class DeviceSecurityActivityResult @Inject constructor(private val activity : Activity?) : IDeviceSecurityActivityResult {

    override fun createDeviceSecurityIntent(
        deepLinkParams: String?,
        applyNowState: ApplyNowState
    ): Intent? {
        activity ?: return null
        return Intent(activity, LinkDeviceConfirmationActivity::class.java).apply {
            putExtra(AccountSignedInPresenterImpl.APPLY_NOW_STATE, applyNowState)
            deepLinkParams?.let {
                putExtra(
                    AccountSignedInPresenterImpl.DEEP_LINKING_PARAMS,
                    Utils.objectToJson(deepLinkParams)
                )
            }
        }
    }

    override fun createDeviceSecurityActivityResult(
        deepLinkParams: String?,
        applyNowState: ApplyNowState,
        viewModel: UserAccountLandingViewModel?,
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?
    ) {
        val deviceSecurityIntent =
            createDeviceSecurityIntent(deepLinkParams = deepLinkParams, applyNowState = applyNowState)
        deviceSecurityIntent ?: return
        activityLauncher?.launch(deviceSecurityIntent, onActivityResult = { result ->
            setDeviceSecurityResultCode(applyNowState = applyNowState,viewModel = viewModel, resultCode = result.resultCode)
        })
    }

    override fun setDeviceSecurityResultCode(
        applyNowState: ApplyNowState,
        viewModel: UserAccountLandingViewModel?,
        resultCode: Int
    ) {
        viewModel ?: return
        CoroutineScope(Dispatchers.IO).launch {
            when (resultCode) {
                RESULT_CODE_LINK_DEVICE -> viewModel.remote.getAllLinkedDevices(true)
                RESULT_CODE_DEVICE_LINKED -> viewModel.remote.getAllLinkedDevices(false)
            }
        }
        viewModel.performClick()
    }

    companion object {
        const val RESULT_CODE_LINK_DEVICE : Int = 5431
        const val RESULT_CODE_DEVICE_LINKED : Int = 5432
    }
}