package za.co.woolworths.financial.services.android.ui.activities.product

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.google.gson.JsonElement
import za.co.woolworths.financial.services.android.contracts.IToastInterface
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList.Companion.navigateToShoppingListOnToastClicked
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.buildShoppingListToast

/**
 * Created by Kunal Uttarwar on 25/3/21.
 */

class ProductDetailsActivity : AppCompatActivity(), IToastInterface {

    var productDetailsFragmentNew: ProductDetailsFragment? = null
    var flContentFrame: FrameLayout? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        flContentFrame = findViewById(R.id.content_frame)
        productDetailsFragmentNew = newInstance()
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
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)

    }

    override fun onToastButtonClicked(jsonElement: JsonElement?) {
        //val navigateTo = NavigateToShoppingList
        if (jsonElement != null) navigateToShoppingListOnToastClicked(this, jsonElement)
    }
}
