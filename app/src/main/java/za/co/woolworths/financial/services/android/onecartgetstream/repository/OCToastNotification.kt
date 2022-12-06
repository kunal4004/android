package za.co.woolworths.financial.services.android.onecartgetstream.repository

import android.app.Activity

interface OCToastNotification {
    fun showOCToastNotification(context: Activity, messageCount: String, yOffset: Int, orderId:String)
}