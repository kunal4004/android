package za.co.woolworths.financial.services.android.ui.views.actionsheet.vouchersBottomDialog

import android.content.Context

interface VouchersBottomDialog {
    fun showCashBackVouchersInfo(
        context: Context,
        title:String,
        desc : String,
        isVouchersInfo:Boolean
    )
}