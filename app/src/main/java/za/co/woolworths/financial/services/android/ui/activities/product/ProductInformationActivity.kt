package za.co.woolworths.financial.services.android.ui.activities.product

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ActivityProductInformationBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.ACTION_ALLERGEN_INFORMATION
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.ACTION_DIETARY_INFORMATION
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.ACTION_INGREDIENTS_INFORMATION
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.ACTION_NUTRITIONAL_INFORMATION
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.ACTION_PRODUCTDETAILS_INFORMATION
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.ACTION_SIZE_GUIDE
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.*
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.size_guide.ProductSizeGuideFragment
import za.co.woolworths.financial.services.android.util.Utils

class ProductInformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductInformationBinding
    private var productDetails: ProductDetails? = null
    private var productInformationType: ProductInformationType? = null

    enum class ProductInformationType {
        DETAILS, INGREDIENTS, NUTRITIONAL_INFO, DIETARY_INFO, ALLERGEN_INFO, SIZE_GUIDE
    }

    companion object {
        const val PRODUCT_INFORMATION_TYPE = "PRODUCT_INFORMATION_TYPE"
        const val PRODUCT_DETAILS = "PRODUCT_DETAILS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.updateStatusBarBackground(this)
        actionBar()

        intent?.extras?.apply {
            productDetails = Utils.jsonStringToObject(getString(PRODUCT_DETAILS), ProductDetails::class.java) as ProductDetails
            productInformationType = getSerializable(PRODUCT_INFORMATION_TYPE) as ProductInformationType?
        }

        configureUI()
    }

    private fun actionBar() {
        setSupportActionBar(binding.tbMyCard)
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
                    val arguments = HashMap<String, String>()
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.PRODUCT_DETAILS_INFORMATION_PRODUCT_ID] = productId
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE] = ACTION_PRODUCTDETAILS_INFORMATION
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_PRODUCTDETAILS_INFORMATION, arguments, this@ProductInformationActivity)
                    addFragment(
                            fragment = ProductDetailsInformationFragment.newInstance(longDescription, productId,productType),
                            tag = ProductDetailsInformationFragment::class.java.simpleName,
                            containerViewId = R.id.fragmentContainer)
                }
                ProductInformationType.INGREDIENTS -> {
                    val arguments = HashMap<String, String>()
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.INGREDIENTS_INFORMATION_PRODUCT_ID] = productId
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE] = ACTION_INGREDIENTS_INFORMATION
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_PRODUCTDETAIL_INGREDIENTS_INFORMATION, arguments, this@ProductInformationActivity)
                    addFragment(
                            fragment = ProductIngredientsInformationFragment.newInstance(this.ingredients),
                            tag = ProductIngredientsInformationFragment::class.java.simpleName,
                            containerViewId = R.id.fragmentContainer)
                }
                ProductInformationType.NUTRITIONAL_INFO -> {
                    val arguments = HashMap<String, String>()
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.NUTRITIONAL_INFORMATION_PRODUCT_ID] = productId
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE] = ACTION_NUTRITIONAL_INFORMATION
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_PRODUCTDETAIL_NUTRITIONAL_INFORMATION, arguments, this@ProductInformationActivity)
                    addFragment(
                            fragment = ProductNutritionalInformationFragment.newInstance(Utils.toJson(this.nutritionalInformationDetails)),
                            tag = ProductNutritionalInformationFragment::class.java.simpleName,
                            containerViewId = R.id.fragmentContainer)
                }
                ProductInformationType.ALLERGEN_INFO -> {
                    val arguments = HashMap<String, String>()
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ALLERGEN_INFORMATION_PRODUCT_ID] = productId
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE] = ACTION_ALLERGEN_INFORMATION
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_PRODUCTDETAIL_ALLERGEN_INFORMATION, arguments, this@ProductInformationActivity)
                    addFragment(
                            fragment = ProductAllergensInformationFragment.newInstance(this.allergens.get(0)),
                            tag = ProductAllergensInformationFragment::class.java.simpleName,
                            containerViewId = R.id.fragmentContainer)
                }
                ProductInformationType.DIETARY_INFO -> {
                    val arguments = HashMap<String, String>()
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.DIETARY_INFORMATION_PRODUCT_ID] = productId
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE] = ACTION_DIETARY_INFORMATION
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_PRODUCTDETAIL_DIETARY_INFORMATION, arguments, this@ProductInformationActivity)
                    addFragment(
                            fragment = ProductDietaryInformationFragment.newInstance(this.dietary.get(0)),
                            tag = ProductDietaryInformationFragment::class.java.simpleName,
                            containerViewId = R.id.fragmentContainer)
                }
                ProductInformationType.SIZE_GUIDE->{
                    supportActionBar?.apply { setDisplayHomeAsUpEnabled(false) }
                    val arguments = HashMap<String, String>()
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.PRODUCT_ID] = productId
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE] = ACTION_SIZE_GUIDE
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_PRODUCTDETAIL_SIZE_GUIDE, arguments, this@ProductInformationActivity)
                    addFragment(
                            fragment = ProductSizeGuideFragment.newInstance(productDetails?.sizeGuideId),
                            tag = ProductSizeGuideFragment::class.java.simpleName,
                            containerViewId = R.id.fragmentContainer)
                }
                else -> {
                    // Nothing
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