package za.co.woolworths.financial.services.android.common

import android.app.Activity


interface SingleMessageCommonToast {

    fun showMessage(context: Activity, message: String, yOffset: Int)
}