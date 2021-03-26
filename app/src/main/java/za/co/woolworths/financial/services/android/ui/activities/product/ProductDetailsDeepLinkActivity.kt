package za.co.woolworths.financial.services.android.ui.activities.product

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.StartupActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.ProductDetailsExtension
import za.co.woolworths.financial.services.android.ui.activities.product.ProductDetailsActivity.Companion.DEEP_LINK_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.Utils
import java.util.*

/**
 * Created by Kunal Uttarwar on 26/3/21.
 */
class ProductDetailsDeepLinkActivity : AppCompatActivity(), ProductDetailsExtension.ProductDetailsStatusListner {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var bundle: Any? = intent?.data
        if (bundle == null && intent?.extras != null) {
            bundle = intent!!.extras!!
            intent?.action = Intent.ACTION_VIEW
        }
        if (Intent.ACTION_VIEW == intent?.action && bundle != null && bundle.toString().contains("A-")) {
            handleAppLink(bundle)
        } else {
            goToProductDetailsActivity(bundle as Bundle?)
        }
    }

    private fun handleAppLink(appLinkData: Any?) {
        if (appLinkData != null && appLinkData is Uri) {
            val productSearchTerm = appLinkData.pathSegments?.find { it.startsWith("A-") }!!
            if (productSearchTerm == null || productSearchTerm.isEmpty()) {
                restartApp()
            }

            val productId = productSearchTerm.substring(2)
            val arguments = HashMap<String, String>()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.PRODUCT_ID] = productId
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE] = FirebaseManagerAnalyticsProperties.ACTION_PDP_DEEPLINK
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_PDP_NATIVE_SHARE_DP_LNK, arguments)

            ProductDetailsExtension.retrieveProduct(productId, productId, this, this)
        } else {
            finish()
        }
    }

    private fun restartApp() {
        val intent = Intent(this, StartupActivity::class.java)
        this.startActivity(intent)
        finishAffinity()
    }

    private fun goToProductDetailsActivity(bundle: Bundle?) {
        val intent = Intent(this, ProductDetailsActivity::class.java)
        intent.putExtras(bundle!!)
        startActivityForResult(intent,DEEP_LINK_REQUEST_CODE)
        overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
    }

    override fun onSuccess(bundle: Bundle) {
        goToProductDetailsActivity(bundle)
    }

    override fun onFailure() {
        TODO("Not yet implemented")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DEEP_LINK_REQUEST_CODE)
            finish()

    }
}