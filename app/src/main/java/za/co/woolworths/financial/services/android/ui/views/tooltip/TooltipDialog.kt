package za.co.woolworths.financial.services.android.ui.views.tooltip

import android.app.Activity

interface TooltipDialog {

    enum class Feature(val value: Int) {
        BARCODE_SCAN(1), FIND_IN_STORE(2), DELIVERY_LOCATION(3), VOUCHERS(4), REFINE(5), ACCOUNTS(6), SHOPPING_LIST(
            7
        ),
        STATEMENTS(8), CART_REDEEM_VOUCHERS(9), CREDIT_SCORE(9), VTO_TRY_IT(10), SHOPPING(11), DASH(
            12
        ),
        DELIVERY_DETAILS(13), MY_LIST(14), PARGO_STORE(15), NEW_FBH_CNC(16), SHOP_FULFILMENT(17), SHOP_LOCATION(18)
    }

    fun show(activity: Activity): Boolean

    fun isDismissed(): Boolean

    fun hide()

    fun removeFromWindow()
}