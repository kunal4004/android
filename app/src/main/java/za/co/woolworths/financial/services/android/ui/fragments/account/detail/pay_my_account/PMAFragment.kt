package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import androidx.fragment.app.Fragment
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString

open class PMAFragment : Fragment() {

    fun configureToolbar(isBackButtonVisible: Boolean, toolbarTitle: Int) {
        (activity as? PayMyAccountActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(isBackButtonVisible)
            displayToolbarDivider(isBackButtonVisible)
            configureToolbar(bindString(toolbarTitle))
        }
    }
}