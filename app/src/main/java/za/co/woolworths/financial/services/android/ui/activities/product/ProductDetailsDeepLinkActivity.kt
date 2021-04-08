package za.co.woolworths.financial.services.android.ui.activities.product

import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_deeplink_pdp.*
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
        setContentView(R.layout.activity_deeplink_pdp)
        startProgressBar()
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
        if (productDetailsprogressBar.isVisible)
            productDetailsprogressBar.visibility = View.GONE
        val intent = Intent(this, ProductDetailsActivity::class.java)
        intent.putExtras(bundle!!)
        startActivityForResult(intent, DEEP_LINK_REQUEST_CODE)
        overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
    }

    override fun onSuccess(bundle: Bundle) {
        goToProductDetailsActivity(bundle)
    }

    override fun onFailure() {
        stopProgressBar()
    }

    override fun onProductNotFound(message: String) {
        val mngr = getSystemService(ACTIVITY_SERVICE) as ActivityManager?
        val taskList = mngr!!.getRunningTasks(10)
        if (taskList[0].numActivities == 1 && taskList[0].topActivity!!.className == this.localClassName
                && taskList.get(0).baseActivity?.className == ProductDetailsDeepLinkActivity::class.java.name) {
            restartApp()
        } else {
            finish()
            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
        }
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun startProgressBar() {
        if (!productDetailsprogressBar.isVisible)
            productDetailsprogressBar.visibility = View.VISIBLE
    }

    override fun stopProgressBar() {
        if (productDetailsprogressBar.isVisible)
            productDetailsprogressBar.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DEEP_LINK_REQUEST_CODE)
            finish()

    }
}