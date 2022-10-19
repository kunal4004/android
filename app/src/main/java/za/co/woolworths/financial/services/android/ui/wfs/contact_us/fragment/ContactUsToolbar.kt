package za.co.woolworths.financial.services.android.ui.wfs.contact_us.fragment

import android.app.Activity
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import javax.inject.Inject

interface IContactUsToolbar {
    fun setToolbar(title : String)
}

class ContactUsToolbar @Inject constructor(private val activity: Activity) : IContactUsToolbar  {

    override fun setToolbar(title: String) {
        with(activity) {
            when (this) {
                is BottomNavigationActivity -> {
                    setTitle(title)
                    displayToolbar()
                    showBackNavigationIcon(true)
                }
                is MyAccountActivity -> setToolbarTitle(title)
            }
        }
    }

}