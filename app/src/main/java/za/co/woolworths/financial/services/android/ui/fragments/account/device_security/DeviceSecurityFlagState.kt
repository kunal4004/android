package za.co.woolworths.financial.services.android.ui.fragments.account.device_security

import androidx.lifecycle.MutableLiveData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import javax.inject.Inject

interface DeviceSecurityFlag {
    fun clearRevertSwitcher()
    fun fillRevertSwitcher()
    fun disableDeviceSecurityPopupWhenRevertSwitcherNotEmpty(): Boolean
}

class DeviceSecurityFlagState @Inject constructor() : DeviceSecurityFlag {

    /**
     * Activate in activityResult with request code TemporaryFreezeUnfreezeCardItemFragment.DEVICE_SECURITY_REQUEST_CODE
     * when skip button is pressed on device security popup
     */
    val revertSwitcherStateOnSkippedButtonTapped: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    override fun clearRevertSwitcher() {
        revertSwitcherStateOnSkippedButtonTapped.value = ""
    }

    override fun fillRevertSwitcher() {
        revertSwitcherStateOnSkippedButtonTapped.value =
            TemporaryFreezeCardViewModel::class.java.simpleName
    }

    override fun disableDeviceSecurityPopupWhenRevertSwitcherNotEmpty(): Boolean =
        when (revertSwitcherStateOnSkippedButtonTapped.value?.isNotEmpty() == true) {
            true -> {
                clearRevertSwitcher()
                true
            }
            false -> false
        }


}