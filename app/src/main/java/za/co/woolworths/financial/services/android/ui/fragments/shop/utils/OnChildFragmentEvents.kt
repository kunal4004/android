package za.co.woolworths.financial.services.android.ui.fragments.shop.utils

import java.io.Serializable

interface OnChildFragmentEvents : Serializable {
    fun onStartShopping()
    fun isSendDeliveryDetails(): Boolean
}