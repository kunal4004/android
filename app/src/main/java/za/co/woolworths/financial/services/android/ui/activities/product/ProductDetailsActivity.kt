package za.co.woolworths.financial.services.android.ui.activities.product

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.awfs.coordination.R
import com.google.gson.JsonElement
import za.co.woolworths.financial.services.android.contracts.IToastInterface
import za.co.woolworths.financial.services.android.startup.view.StartupActivity
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList.Companion.navigateToShoppingListOnToastClicked
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.buildShoppingListToast
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView
import za.co.woolworths.financial.services.android.util.Utils

/**
 * Created by Kunal Uttarwar on 25/3/21.
 */
class ProductDetailsActivity : AppCompatActivity(), IToastInterface {

    var productDetailsFragmentNew: ProductDetailsFragment? = null
    private val walkThroughPromtView: WMaterialShowcaseView? = null
    var flContentFrame: FrameLayout? = null

    companion object {
        const val DEEP_LINK_REQUEST_CODE = 123
        const val TAG = "ProductDetailsFragment"
        const val SHARE_LINK_REQUEST_CODE = 321
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        Utils.updateStatusBarBackground(this)
        setContentView(R.layout.product_details_activity)
        flContentFrame = findViewById(R.id.content_frame)
        productDetailsFragmentNew = newInstance()
        if (intent.extras != null)
            goToProductDetailsFragment(intent.extras)
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

    private fun restartApp() {
        val intent = Intent(this, StartupActivity::class.java)
        this.startActivity(intent)
        finishAffinity()
    }

    fun goToProductDetailsFragment(bundle: Bundle?) {
        productDetailsFragmentNew?.arguments = bundle
        val fragmentManager: FragmentManager = getSupportFragmentManager()
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, productDetailsFragmentNew!!, TAG).commit()
    }
}
