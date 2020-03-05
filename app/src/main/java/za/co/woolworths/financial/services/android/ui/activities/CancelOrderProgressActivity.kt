package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.cancel_order_progress_activity.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.CancelOrderProgressFragment
import za.co.woolworths.financial.services.android.util.Utils

class CancelOrderProgressActivity : AppCompatActivity() {

    var orderId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cancel_order_progress_activity)
        Utils.updateStatusBarBackground(this)
        Utils.setAsVirtualTemporaryStoreCardPopupShown(true)
        configureActionBar()

        intent?.extras?.apply { orderId = getString(CancelOrderProgressFragment.ORDER_ID, "") }

        addFragment(
                fragment = CancelOrderProgressFragment.getInstance(orderId),
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

    companion object {
        fun triggerFirebaseEvent(properties: String){
            val arguments = HashMap<String, String>()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = properties
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_MY_ORDERS_CANCEL_ORDER, arguments)
        }
    }
}