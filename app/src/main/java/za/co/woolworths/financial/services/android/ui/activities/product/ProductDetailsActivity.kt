package za.co.woolworths.financial.services.android.ui.activities.product

import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.awfs.coordination.R
import com.google.gson.JsonElement
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IToastInterface
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity
import za.co.woolworths.financial.services.android.ui.activities.StartupActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.ProductDetailsExtension.Companion.retrieveProduct
import za.co.woolworths.financial.services.android.ui.activities.dashboard.ProductDetailsExtension.ProductDetailsStatusListner
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList.Companion.navigateToShoppingListOnToastClicked
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.buildShoppingListToast
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView
import za.co.woolworths.financial.services.android.util.Utils
import java.util.*

/**
 * Created by Kunal Uttarwar on 25/3/21.
 */
class ProductDetailsActivity : AppCompatActivity(), IToastInterface, ProductDetailsStatusListner {

    var productDetailsFragmentNew: ProductDetailsFragment? = null
    private val walkThroughPromtView: WMaterialShowcaseView? = null
    var flContentFrame: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        Utils.updateStatusBarBackground(this)
        setContentView(R.layout.product_details_activity)
        flContentFrame = findViewById(R.id.content_frame)
        productDetailsFragmentNew = newInstance()

        var bundle: Any? = intent?.data
        if (bundle == null && intent?.extras != null) {
            bundle = intent!!.extras!!
            intent?.action = Intent.ACTION_VIEW
        }
        if (Intent.ACTION_VIEW == intent?.action && bundle != null && bundle.toString().contains("A-")) {
            handleAppLink(bundle)
        } else {
            goToProductDetailsFragment(bundle as Bundle?)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_REQUEST_CODE
                && resultCode == AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE) {
            buildShoppingListToast(this, flContentFrame!!, true, data, this)
            return
        }
        if (productDetailsFragmentNew != null) productDetailsFragmentNew!!.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (walkThroughPromtView != null && !walkThroughPromtView.isDismissed) {
            walkThroughPromtView.hide()
            return
        }

        val mngr = getSystemService(ACTIVITY_SERVICE) as ActivityManager?
        val taskList = mngr!!.getRunningTasks(10)
        if (taskList[0].numActivities == 1 && taskList[0].topActivity!!.className == this.localClassName) {
            val intent = Intent(this, StartupActivity::class.java)
            this.startActivity(intent)
            finishAffinity()
        } else {
            finish()
            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (productDetailsFragmentNew != null) productDetailsFragmentNew!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onToastButtonClicked(jsonElement: JsonElement?) {
        //val navigateTo = NavigateToShoppingList
        if (jsonElement != null) navigateToShoppingListOnToastClicked(this, jsonElement)
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

            retrieveProduct(productId, productId, this, this)
        } else {
            finish()
        }
    }

    private fun restartApp() {
        val intent = Intent(this, StartupActivity::class.java)
        this.startActivity(intent)
        finishAffinity()
    }

    fun goToProductDetailsFragment(bundle: Bundle?) {
        productDetailsFragmentNew?.arguments = bundle
        val fragmentManager: FragmentManager = getSupportFragmentManager()
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, productDetailsFragmentNew!!).commit()
    }

    override fun onSuccess(bundle: Bundle) {
        goToProductDetailsFragment(bundle)
    }

    override fun onFailure() {
    }
}
