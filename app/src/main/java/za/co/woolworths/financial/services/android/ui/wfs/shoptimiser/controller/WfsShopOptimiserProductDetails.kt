package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller

import androidx.compose.ui.text.AnnotatedString
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductKeys
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.CreditCardType
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto.ProductOnDisplay
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper.ShopOptimiserConstant.ShopOptimiserConstant.PayFlexKey
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper.convertTextToAnnotationString
import javax.inject.Inject

interface WfsShopOptimiserProductDetailsBuilder {
    fun getShopOptimiserProductImage(accountBinNumber: String? = null, productGroupCode: String?): Int
    fun getInfoDescription(productOnDisplay: ProductOnDisplay?): AnnotatedString?
}

class WfsShopOptimiserProductDetailsBuilderImpl @Inject constructor() :
    WfsShopOptimiserProductDetailsBuilder {

    /**
     * Retrieves the shop optimiser product image resource based on account bin number and product group code.
     * @param accountBinNumber The account bin number associated with the product.
     * @param productGroupCode The product group code associated with the product.
     * @return The image resource ID representing the product image.
     */
    override fun getShopOptimiserProductImage(accountBinNumber: String?, productGroupCode: String?): Int {
        // Determine the image resource based on the product group code
        return when (productGroupCode) {
            // If the product group code matches StoreCard, use the Store Card image
            AccountProductKeys.StoreCard.value -> R.drawable.shoptimiser_store_card_details_icon
            // If the product group code matches PayFlexKey, use the PayFlex image
            PayFlexKey -> R.drawable.shoptimiser_payflex_details_icon
            else -> {
                // If the product group code doesn't match, check the account bin number
                when (accountBinNumber) {
                    // If the account bin number matches Silver Card, use the Silver Credit Card image
                    CreditCardType.SILVER_CARD.rawValue -> R.drawable.shoptimiser_silver_credit_card_details_icon
                    // If the account bin number matches Gold Card, use the Gold Credit Card image
                    CreditCardType.GOLD_CARD.rawValue -> R.drawable.shoptimiser_gold_credit_card_details_icon
                    else -> {
                        // If neither product group code nor account bin number matches, use a default image
                        R.drawable.shoptimiser_black_credit_card_detail_icon
                    }
                }
            }
        }
    }

    /**
     * Retrieves an AnnotatedString containing information description for a product detail on display.
     * @param productOnDisplay The product details to retrieve the information description for.
     * @return An AnnotatedString representing the information description with annotations (bold parts).
     */
    override fun getInfoDescription(productOnDisplay: ProductOnDisplay?): AnnotatedString {
        // Get the infoDescription from the productOnDisplay, if available
        val infoDescription = productOnDisplay?.wfsPaymentMethods?.infoDescription

        // Get the list of bold items from the productOnDisplay, if available
        val listOfBoldItems = productOnDisplay?.wfsPaymentMethods?.infoDescriptionBoldParts

        // Convert the text with annotations (bold parts) into an AnnotatedString
        return convertTextToAnnotationString(infoDescription, listOfBoldItems)
    }

}
