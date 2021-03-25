package za.co.woolworths.financial.services.android.ui.activities.product

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.awfs.coordination.R
import com.google.gson.JsonElement
import kotlinx.android.synthetic.main.product_details_activity.*
import za.co.woolworths.financial.services.android.contracts.IToastInterface
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.ProductDetailsExtension.Companion.retrieveProduct
import za.co.woolworths.financial.services.android.ui.activities.dashboard.ProductDetailsExtension.ProductDetailsStatusListner
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList.Companion.navigateToShoppingListOnToastClicked
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.buildShoppingListToast
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.DeepLinkingUtils
import za.co.woolworths.financial.services.android.util.DeepLinkingUtils.Companion.getProductSearchTypeAndSearchTerm
import za.co.woolworths.financial.services.android.util.Utils

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
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
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
            var bundle = Bundle()
            bundle.putString("parameters", "{\"url\": \"${appLinkData}\"}")
            bundle.putString("feature", AppConstant.DP_LINKING_PRODUCT_LISTING)

            val productSearchTypeAndSearchTerm = getProductSearchTypeAndSearchTerm(appLinkData.toString())
            if (!productSearchTypeAndSearchTerm.searchTerm.isEmpty() && !productSearchTypeAndSearchTerm.searchTerm.equals(DeepLinkingUtils.WHITE_LISTED_DOMAIN, ignoreCase = true)) {
                /*Map<String, String> arguments = new HashMap<>();
                        arguments.put(FirebaseManagerAnalyticsProperties.PropertyNames.ENTRY_POINT, FirebaseManagerAnalyticsProperties.EntryPoint.DEEP_LINK.getValue());
                        arguments.put(FirebaseManagerAnalyticsProperties.PropertyNames.DEEP_LINK_URL, linkData.toString());
                        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYCARTDELIVERY, arguments);*/
                //pushFragment(ProductListingFragment.Companion.newInstance(productSearchTypeAndSearchTerm.getSearchType(), "", productSearchTypeAndSearchTerm.getSearchTerm()));
                val productId = productSearchTypeAndSearchTerm.searchTerm.substring(2)
                retrieveProduct(productId, productId, this, this)
            }
        } else
            return
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
