package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.cancel_order_progress_activity.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.CancelOrderProgressFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils

class CancelOrderProgressActivity : AppCompatActivity() {

    var orderId: String = ""
    var isNavigatedFromMyAccounts: Boolean  = false
    var commarceOrderItemList: ArrayList<CommerceItem>? = null
    var orderItemTotal: Double = 0.0
    var orderShippingTotal: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cancel_order_progress_activity)
        Utils.updateStatusBarBackground(this)
        Utils.updateUserVirtualTempCardState(true)
        configureActionBar()
        intent?.extras?.apply {
            orderId = getString(CancelOrderProgressFragment.ORDER_ID, "")
            isNavigatedFromMyAccounts = getBoolean(AppConstant.NAVIGATED_FROM_MY_ACCOUNTS, false)
            commarceOrderItemList = getSerializable(AppConstant.ORDER_ITEM_LIST) as ArrayList<CommerceItem>?
            orderItemTotal = getDouble(AppConstant.ORDER_ITEM_TOTAL, 0.0)
            orderShippingTotal = getDouble(AppConstant.ORDER_SHIPPING_TOTAL, 0.0)
        }
        addFragment(
                fragment = CancelOrderProgressFragment.getInstance(orderId, commarceOrderItemList, orderItemTotal, orderShippingTotal),
                tag = CancelOrderProgressFragment::class.java.simpleName,
                containerViewId = R.id.fragmentContainer)
    }

    private fun configureActionBar() {
        setSupportActionBar(tbMyCard)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }
     fun triggerFirebaseEvent(properties: String) {
            val arguments = HashMap<String, String>()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = properties
            Utils.triggerFireBaseEvents(if (isNavigatedFromMyAccounts) FirebaseManagerAnalyticsProperties.Acc_My_Orders_Cancel_Order else FirebaseManagerAnalyticsProperties.SHOP_MY_ORDERS_CANCEL_ORDER, arguments, this)
        }
}