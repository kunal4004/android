package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_general.data

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import za.co.woolworths.financial.services.android.models.AppConfigSingleton.isBadgesRequired
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.repository.AppStateRepository
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesActivity
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.fragments.help.HelpSectionFragment
import za.co.woolworths.financial.services.android.ui.fragments.mypreferences.DeletedSuccessBottomSheetDialog.Companion.newInstance
import za.co.woolworths.financial.services.android.ui.fragments.mypreferences.MyPreferencesFragment
import za.co.woolworths.financial.services.android.ui.fragments.store.StoresNearbyFragment1
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SignOutFragment
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.fragment.ContactUsFragment
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.UserAccountResponse
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.RESULT_CODE_DELETE_ACCOUNT
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.ServiceTools.Companion.stop
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import javax.inject.Inject

interface GeneralIntent {
    fun createStoreLocatorIntent()
    fun createNeedHelpIntent(userAccountResponse: UserAccountResponse?)
    fun createContactUsIntent()
    fun createUpdatePasswordIntent()
    fun UserAccountLandingViewModel.createMyPreferenceIntent(
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?
    )
    fun createSignOutIntent()
}

class GeneralIntentImpl @Inject constructor(private val activity: Activity?) : GeneralIntent {

    private val GENERAL_CLASS_NAME = this.javaClass.simpleName
    override fun createStoreLocatorIntent() {
        activity?.apply {
            if (this is BottomNavigationActivity) {
                pushFragment(StoresNearbyFragment1())
                return
            }

            if (this is MyAccountActivity) {
                replaceFragment(StoresNearbyFragment1())
            }
        }
    }

    override fun createNeedHelpIntent(userAccountResponse: UserAccountResponse?) {
        activity?.apply {
            val helpSectionFragment = HelpSectionFragment()
            userAccountResponse?.let {
                val bundle = Bundle()
                bundle.putString("accounts", Utils.objectToJson(it))
                helpSectionFragment.arguments = bundle
            }
            if (this is BottomNavigationActivity) {
                pushFragment(helpSectionFragment)
                return
            }

            if (this is MyAccountActivity) {
                replaceFragment(helpSectionFragment)
            }
        }
    }

    override fun createContactUsIntent() {
        activity?.apply {
            if (this is BottomNavigationActivity) {
               pushFragment(ContactUsFragment())
                return
            }
            if (this is MyAccountActivity) {
                replaceFragment(ContactUsFragment())
            }
        }
    }

    override fun createUpdatePasswordIntent() {
        ScreenManager.presentSSOUpdatePassword(activity)
    }

    override fun UserAccountLandingViewModel.createMyPreferenceIntent(
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?
    ) {
        //RESULT_CODE_DELETE_ACCOUNT
        activity?.apply {
            val myPreferencesIntent = Intent(this, MyPreferencesActivity::class.java)
            myPreferencesIntent.putExtra(
                MyPreferencesFragment.IS_NON_WFS_USER, isNowWfsUser())
            myPreferencesIntent.putExtra(
                MyPreferencesFragment.DEVICE_LIST,
                AppStateRepository().getLinkedDevices()
            )

            activityLauncher?.launch(myPreferencesIntent) { result ->
                when (result.resultCode) {
                    RESULT_CODE_DELETE_ACCOUNT -> {
                        SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                        stop(this, LiveChatService::class.java)
                        isBadgesRequired = true
                        CookieManager.getInstance().removeAllCookies(null)
                        CookieManager.getInstance().flush()
                        setUserUnAuthenticated(SSOActivity.SSOActivityResult.SIGNED_OUT.rawValue())
                        Utils.clearPreferredDeliveryLocation()
                        val deleteSuccessFulFragment: BottomSheetDialogFragment = newInstance()
                        (activity as? AppCompatActivity)?.supportFragmentManager?.let { fragmentManager ->
                            deleteSuccessFulFragment.show(fragmentManager, GENERAL_CLASS_NAME)
                        }
                    }
                }
            }
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        }
    }

    override fun createSignOutIntent() {
        activity ?: return
        val signOutFragment = SignOutFragment()
        try {
            val supportFragmentManager = when (activity) {
                is BottomNavigationActivity -> activity.supportFragmentManager
                is MyAccountActivity -> activity.supportFragmentManager
                else -> null
            }
            supportFragmentManager?.let {
                signOutFragment.show(
                    it,
                    SignOutFragment::class.java.simpleName
                )
            }
        } catch (ex: IllegalStateException) {
            FirebaseManager.logException(ex)
        }
    }

}