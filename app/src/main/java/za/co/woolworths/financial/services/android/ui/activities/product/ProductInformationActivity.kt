package za.co.woolworths.financial.services.android.ui.activities.product

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_product_information.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsInformationFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductIngredientsInformationFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductNutritionalInformationFragment
import za.co.woolworths.financial.services.android.util.Utils

class ProductInformationActivity : AppCompatActivity() {

    private var productDetails: ProductDetails? = null
    private var productInformationType: ProductInformationType? = null

    enum class ProductInformationType {
        DETAILS, INGREDIENTS, NUTRITIONAL_INFO
    }

    companion object {
        val PRODUCT_INFORMATION_TYPE = "PRODUCT_INFORMATION_TYPE"
        val PRODUCT_DETAILS = "PRODUCT_DETAILS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_information)
        Utils.updateStatusBarBackground(this)
        actionBar()

        intent?.extras?.apply {
            productDetails = Utils.jsonStringToObject(getString(PRODUCT_DETAILS), ProductDetails::class.java) as ProductDetails
            productInformationType = getSerializable(PRODUCT_INFORMATION_TYPE) as ProductInformationType?
        }

        configureUI()
    }

    private fun actionBar() {
        setSupportActionBar(tbMyCard)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    private fun configureUI() {
        productDetails?.apply {
            when (productInformationType) {
                ProductInformationType.DETAILS -> {
                    addFragment(
                            fragment = ProductDetailsInformationFragment.newInstance(longDescription, productId),
                            tag = ProductDetailsInformationFragment::class.java.simpleName,
                            containerViewId = R.id.fragmentContainer)
                }
                ProductInformationType.INGREDIENTS -> {
                    addFragment(
                            fragment = ProductIngredientsInformationFragment.newInstance(this.ingredients),
                            tag = ProductIngredientsInformationFragment::class.java.simpleName,
                            containerViewId = R.id.fragmentContainer)
                }
                ProductInformationType.NUTRITIONAL_INFO -> {
                    val arguments = HashMap<String, String>()
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.NUTRITIONAL_INFORMATION_PRODUCT_ID] = productId
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_PRODUCTDETAIL_NUTRITIONAL_INFORMATION, arguments)
                    addFragment(
                            fragment = ProductNutritionalInformationFragment.newInstance(Utils.toJson(this.nutritionalInformationDetails)),
                            tag = ProductNutritionalInformationFragment::class.java.simpleName,
                            containerViewId = R.id.fragmentContainer)
                }
            }
        }
    }


    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

}