package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller

import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.util.StoreUtils
import javax.inject.Inject

interface IRetailBNPL {
    fun isStandAlonePayFlexEnabled(productDetails: ProductDetails?): Boolean
}

class RetailBNPLImpl @Inject constructor(private val manageBNPLConfig: IManageBnpLConfig) : IRetailBNPL,
    IManageBnpLConfig by manageBNPLConfig {

    /**
     * Determines whether Stand-Alone PayFlex is enabled for a given product based on its fulfillment type.
     * @param productDetails The product details to check.
     * @return `true` if Stand-Alone PayFlex is enabled for the product, `false` otherwise.
     */
    override fun isStandAlonePayFlexEnabled(productDetails: ProductDetails?): Boolean {
        // Check if PayFlex BNPL configuration is enabled
        val isPayFlexEnabled = isPayFlexBNPLConfigEnabled()

        // Determine if PayFlex is enabled based on the fulfillment type of the product
        return when (productDetails?.fulfillmentType) {
            // If the fulfillment type is Clothing Items or CRG Items, enable PayFlex
            StoreUtils.Companion.FulfillmentType.CLOTHING_ITEMS?.type,
            StoreUtils.Companion.FulfillmentType.CRG_ITEMS?.type -> isPayFlexEnabled

            // For all other fulfillment types, disable PayFlex
            else -> false
        }
    }


}