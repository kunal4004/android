package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_general.data

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.repository.AppStateRepository
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesActivity
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountsFragment
import za.co.woolworths.financial.services.android.ui.fragments.help.HelpSectionFragment
import za.co.woolworths.financial.services.android.ui.fragments.mypreferences.MyPreferencesFragment
import za.co.woolworths.financial.services.android.ui.fragments.store.StoresNearbyFragment1
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SignOutFragment
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.fragment.ContactUsFragment
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_device_security.navigation.DeviceSecurityActivityResult
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import javax.inject.Inject

interface GeneralIntent {
    fun createStoreLocatorIntent()
    fun createNeedHelpIntent()
    fun createContactUsIntent()
    fun createUpdatePasswordIntent()
    fun createMyPreferenceIntent(isNowWfsUser : Boolean)
    fun createSignOutIntent()
}

class GeneralIntentImpl @Inject constructor(private val activity: Activity?) : GeneralIntent {

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

    override fun createNeedHelpIntent() {
        activity?.apply {
            val helpSectionFragment = HelpSectionFragment()
            if (MyAccountsFragment.mAccountResponse != null) {
                val bundle = Bundle()
                bundle.putString("accounts", Utils.objectToJson(MyAccountsFragment.mAccountResponse))
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

    override fun createMyPreferenceIntent(isNowWfsUser : Boolean) {
        activity?.apply {
            val myPreferences = Intent(this, MyPreferencesActivity::class.java)
            myPreferences.putExtra(
                MyPreferencesFragment.IS_NON_WFS_USER, isNowWfsUser)
            myPreferences.putExtra(
                MyPreferencesFragment.DEVICE_LIST,
                AppStateRepository().getLinkedDevices()
            )
            startActivityForResult(
                myPreferences,
                DeviceSecurityActivityResult.RESULT_CODE_DEVICE_LINKED
            )
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