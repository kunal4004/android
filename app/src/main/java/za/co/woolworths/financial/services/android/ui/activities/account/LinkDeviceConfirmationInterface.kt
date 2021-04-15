package za.co.woolworths.financial.services.android.ui.activities.account

import android.view.View

interface LinkDeviceConfirmationInterface {
    fun setToolbarTitle(title: String)
    fun hideToolbarButton()
    fun showToolbarButton()
    fun getToolbar(): View
}