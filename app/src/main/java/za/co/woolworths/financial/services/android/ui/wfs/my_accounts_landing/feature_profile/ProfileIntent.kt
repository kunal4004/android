package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_profile

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.shoppinglist.view.MyShoppingListFragment
import za.co.woolworths.financial.services.android.ui.activities.MessagesActivity
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.fragments.shop.MyListsFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.MyOrdersAccountFragment
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AccountLandingFirebaseManagerImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import za.co.woolworths.financial.services.android.util.ScreenManager
import javax.inject.Inject

interface IProfileIntent {
    fun createMessageIntent(
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?,
        viewModel: UserAccountLandingViewModel
    )
    fun createDetailIntent()
    fun createOrderIntent()
    fun createShoppingListIntent()
    fun getBottomNavigationActivity(): BottomNavigationActivity?
}

class ProfileIntent @Inject constructor(private val activity : Activity?,
                                        private val  analytics: AccountLandingFirebaseManagerImpl
) : IProfileIntent {

    companion object {
        const val fromNotification  : String = "fromNotification"
    }

    override fun createMessageIntent(
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?,
        viewModel: UserAccountLandingViewModel
    ) {
        activity?.apply {
            analytics.onMessagingItem()
            val openMessageActivity = Intent(this, MessagesActivity::class.java)
            openMessageActivity.putExtra(fromNotification, false)
            activityLauncher?.launch (openMessageActivity, onActivityResult = {
                viewModel.apply {
                    resetUnreadMessageCount()
                    queryUserMessagesService()
                }
            })
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    override fun createDetailIntent() {
        activity?.apply {
            ScreenManager.presentSSOUpdateProfile(this)
        }
    }

    override fun createOrderIntent() {
        activity?.apply {
            analytics.onMyOrderItem()
            when (this) {
                is BottomNavigationActivity -> getBottomNavigationActivity()?.pushFragment(
                    MyOrdersAccountFragment()
                )
                is MyAccountActivity -> replaceFragment(MyOrdersAccountFragment())
            }
        }
    }

    override fun createShoppingListIntent() {
        activity?.apply {
            analytics.onShoppingListItem()
            val fragment = MyListsFragment()
            when (this) {
                is BottomNavigationActivity -> getBottomNavigationActivity()?.pushFragment(
                    fragment
                )
                is MyAccountActivity -> replaceFragment(fragment = fragment)
            }
        }
    }

    override fun getBottomNavigationActivity(): BottomNavigationActivity? {
        return if (activity !is BottomNavigationActivity) null else activity
    }

}