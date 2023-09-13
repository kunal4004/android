package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.pdp

import android.view.View
import androidx.compose.runtime.LaunchedEffect
import androidx.constraintlayout.widget.ConstraintLayout
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ProductDetailsFragmentBinding
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto.ShopOptimiserVisibleUiType
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.component.accordion.ShopOptimiserAccordionWidget
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.viewmodel.ShopOptimiserViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import javax.inject.Inject

interface ShoptimiserProductDetailPage {
    fun initWfsEmbeddedFinance()
    fun addProductDetails(productDetails: ProductDetails)
    fun addProductDetailsToPdpVariant(productDetails: ProductDetails?)
    fun  addPrice(otherSkus : OtherSkus?)
}

class ShoptimiserProductDetailPageImpl @Inject constructor(
    private val binding: ProductDetailsFragmentBinding,
    private var shoptimiserViewModel: ShopOptimiserViewModel
) : ShoptimiserProductDetailPage {

    /**
     * Initialize the Shop Optimizer embedded finance UI.
     * This function sets up the Shop Optimizer accordion widget and observes its visibility type.
     */
    override fun initWfsEmbeddedFinance() {
        binding.wfsShoptimiserComposable.setContent {
            OneAppTheme {
                // Initialize the Shop Optimizer Accordion Widget
                shoptimiserViewModel.ShopOptimiserAccordionWidget()

                // Observe the visibility type and adjust the layout accordingly
                LaunchedEffect(shoptimiserViewModel.shopOptimiserVisibleType) {
                    shoptimiserViewModel.shopOptimiserVisibleType.collect { type ->
                        when (type) {
                            ShopOptimiserVisibleUiType.ACCORDION,
                            ShopOptimiserVisibleUiType.STANDALONE -> {
                                // Move and adjust the layout for Shop Optimizer
                                binding.moveShopOptimiserLayout()
                                shoptimiserViewModel.saveDefaultPdpDisplayedInSQLiteModel(true)
                                binding.wfsShoptimiserComposable.visibility = View.VISIBLE
                            }

                            ShopOptimiserVisibleUiType.GONE -> {
                                // Hide Shop Optimizer and reset the layout
                                binding.wfsShoptimiserComposable.visibility = View.GONE
                                binding.resetShopOptimiserLayout()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Add product details to the Shop Optimizer ViewModel.
     * @param productDetails The product details to add.
     */
    override fun addProductDetails(productDetails: ProductDetails) {
        shoptimiserViewModel.addProductDetails(productDetails)
    }

    /**
     * Add price information to the Shop Optimizer ViewModel for the Product Details Page (PDP) variant.
     * @param otherSkus The price and SKU information to add.
     */
    override fun addPrice(otherSkus: OtherSkus?) {
        shoptimiserViewModel.addPriceToPDPVariant(price = otherSkus?.price)
    }

    /**
     * Add product details, price, and product type information to the Shop Optimizer ViewModel
     * for the Product Details Page (PDP) variant.
     * @param productDetails The product details to add.
     */
    override fun addProductDetailsToPdpVariant(productDetails: ProductDetails?) {
        productDetails?.let {
            shoptimiserViewModel.apply {
                addPriceToPDPVariant(price = it.price)
                addProductTypeToPDPVariant(productType = it.productType)
                addProductDetails(it)
            }
        }
    }

    /**
     * Move and adjust the layout properties for Shop Optimizer in the Product Details Fragment.
     * This function updates the top-to-bottom constraints and visibility for specific views.
     */
    private fun ProductDetailsFragmentBinding.moveShopOptimiserLayout() {
        // Adjust the top-to-bottom constraint of the freeGiftWithPurchaseLayout
        (freeGiftWithPurchaseLayout.root.layoutParams as ConstraintLayout.LayoutParams).let { layoutParams ->
            layoutParams.topToBottom = R.id.wfsShoptimiserComposable
            freeGiftWithPurchaseLayout.root.layoutParams = layoutParams
        }

        // Adjust the top-to-bottom constraint of the sizeColorSelectorLayout
        (sizeColorSelectorLayout.root.layoutParams as ConstraintLayout.LayoutParams).let { layoutParams ->
            layoutParams.topToBottom = R.id.freeGiftWithPurchaseLayout
            sizeColorSelectorLayout.root.layoutParams = layoutParams
        }

        // Hide the divider1 in sizeColorSelectorLayout
        sizeColorSelectorLayout.divider1.visibility = View.GONE
    }

    /**
     * Reset the layout properties for Shop Optimizer in the Product Details Fragment.
     * This function adjusts the visibility and layout constraints for specific views.
     */
    private fun ProductDetailsFragmentBinding.resetShopOptimiserLayout() {
        // Make the divider1 in sizeColorSelectorLayout visible
        sizeColorSelectorLayout.divider1.visibility = View.VISIBLE

        // Adjust the topToBottom constraint of the freeGiftWithPurchaseLayout
        (freeGiftWithPurchaseLayout.root.layoutParams as ConstraintLayout.LayoutParams).let { layoutParams ->
            layoutParams.topToBottom = R.id.onlinePromotionalTextView3
            freeGiftWithPurchaseLayout.root.layoutParams = layoutParams
        }

        // Adjust the topToBottom constraint of the sizeColorSelectorLayout
        (sizeColorSelectorLayout.root.layoutParams as ConstraintLayout.LayoutParams).let { layoutParams ->
            layoutParams.topToBottom = R.id.wfsShoptimiserComposable
            sizeColorSelectorLayout.root.layoutParams = layoutParams
        }
    }

}