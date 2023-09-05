package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.app_config.WfsPaymentMethods
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductKeys
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.CreditCardType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.ProductDetails
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.UserAccountResponse
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto.FBHProduct
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto.PdpProductVariant
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto.ProductOnDisplay
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper.ShopOptimiserConstant.ShopOptimiserConstant.PayFlexKey
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper.ShopOptimiserConstant.ShopOptimiserConstant.installmentAmountTag
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.SessionUtilities
import javax.inject.Inject

interface WfsShopOptimiserProduct : IManageBnpLConfig, IManageShopOptimiserSQLite {
    fun isShopOptimiserEnabled(pdpProductVariant: PdpProductVariant?):Boolean
    fun displayFBHProductAndLoadWFSProduct(pdpProductVariant: PdpProductVariant?): ProductOnDisplay?
    fun displayWfsProducts(pdpProductVariant: PdpProductVariant?, userAccountResponse: UserAccountResponse?): MutableMap<String, ProductOnDisplay>
    fun getWFSProductsForUser(userAccountResponse: UserAccountResponse?): MutableMap<String, ProductDetails?>
    fun payFlexCalculation(fbhProductPrice: Double?): String
    fun constructPayFlex(wfsPaymentMethods: MutableList<WfsPaymentMethods>?, fbhProductPrice: Double): ProductOnDisplay?
}

class ShopOptimiserShopOptimiserProductImpl @Inject constructor(
    private val manageBnpLConfig: IManageBnpLConfig,
    private val manageTimestamp: IManageShopOptimiserSQLite,
    private val calculation : IManageShopOptimiserCalculation
) : WfsShopOptimiserProduct,
    IManageBnpLConfig by manageBnpLConfig,
    IManageShopOptimiserSQLite by manageTimestamp,
    IManageShopOptimiserCalculation by calculation {

    /**
     * Checks if the user is browsing an FBH product.
     * @param fbhProduct The FBH product to check.
     * @return True if the user is browsing an FBH product, otherwise false.
     */
    private fun isUserBrowsingFBHProduct(fbhProduct: FBHProduct?): Boolean {
        return FBHProduct.isUserBrowsingFBHProduct(productType = fbhProduct?.productType)
    }

    /**
     * Checks if the user is logged in.
     * @return True if the user is logged in, otherwise false.
     */
    private fun isLoggedIn(): Boolean = SessionUtilities.getInstance().isUserAuthenticated

    /**
     * Checks if Shop Optimizer is enabled for the given PDP (Product Detail Page) product variant.
     * @param pdpProductVariant The PdpProductVariant containing product details.
     * @return True if Shop Optimizer is enabled, otherwise false.
     */
    override fun isShopOptimiserEnabled(pdpProductVariant: PdpProductVariant?): Boolean {
        return isLoggedIn()
                && isUserBrowsingFBHProduct(pdpProductVariant?.fbhProduct)
                && isPaymentMethodsNotEmpty()
                && SessionUtilities.getInstance().isC2User
    }


    /**
     * Checks if the paymentMethods array is not empty.
     * @return True if the paymentMethods array is not empty, otherwise false.
     */
    private fun isPaymentMethodsNotEmpty(): Boolean {
        return manageBnpLConfig.getWfsPaymentMethodsWithPayflex()?.isNotEmpty() == true
    }

    /**
     * Displays FBH product and loads WFS (Wealth and Financial Services) product if enabled.
     * @param pdpProductVariant The PdpProductVariant containing product details.
     * @return A ProductOnDisplay representing the FBH product or null if not enabled.
     */
    override fun displayFBHProductAndLoadWFSProduct(pdpProductVariant: PdpProductVariant?): ProductOnDisplay? {
        // Check if Shop Optimizer is enabled for the given product variant
        if (isShopOptimiserEnabled(pdpProductVariant = pdpProductVariant)) {
            val wfsPaymentMethods = manageBnpLConfig.getWfsPaymentMethodsWithPayflex()
            val fbhProductPrice = pdpProductVariant?.price?.toDouble() ?: 0.0

            // Construct and return the PayFlex product representation
            return constructPayFlex(wfsPaymentMethods, fbhProductPrice)
        }
        return null
    }

    /**
     * Constructs a PayFlex product representation based on provided data.
     * @param wfsPaymentMethods List of WfsPaymentMethods containing payment method details.
     * @param fbhProductPrice The price of the FBH product.
     * @return A ProductOnDisplay representing the PayFlex product, or null if not found or not applicable.
     */
    override fun constructPayFlex(
        wfsPaymentMethods: MutableList<WfsPaymentMethods>?,
        fbhProductPrice: Double
    ): ProductOnDisplay? {
        wfsPaymentMethods?.firstOrNull { it.productGroupCode == null }?.let { resultPayFlex ->
            val drawableId = getProductImage(productGroupCode = PayFlexKey)

            val installmentAmountFormatted = payFlexCalculation(fbhProductPrice = fbhProductPrice)

            val paymentMethod = resultPayFlex.copy(
                description = resultPayFlex.description?.replace(
                    installmentAmountTag,
                    installmentAmountFormatted
                ),
                infoFooterTitle = resultPayFlex.infoFooterTitle?.replace(
                    installmentAmountTag,
                    installmentAmountFormatted
                )
            )

            return ProductOnDisplay(
                isSufficientFundsAvailable = true,
                installmentAmount = installmentAmountFormatted,
                drawableId = drawableId,
                wfsPaymentMethods = paymentMethod
            )
        }
        return null
    }

    /**
     * Displays WFS products based on the provided product variant and user account response.
     * @param pdpProductVariant The PdpProductVariant containing product details.
     * @param userAccountResponse The UserAccountResponse containing user account and product details.
     * @return Mutable map of WFS product group codes and their associated product representations.
     */
    override fun displayWfsProducts(
        pdpProductVariant: PdpProductVariant?,
        userAccountResponse: UserAccountResponse?
    ): MutableMap<String, ProductOnDisplay> {
        // Get WFS products associated with the user
        val wfsProducts = getWFSProductsForUser(userAccountResponse)

        // Check conditions for displaying WFS products
        return if (!isUserBrowsingFBHProduct(pdpProductVariant?.fbhProduct) ||
            !isLoggedIn() ||
            !isPaymentMethodsNotEmpty() ||
            wfsProducts.isEmpty()
        ) {
            // Return an empty map if conditions are not met
            mutableMapOf()
        } else {
            // Return WFS products with sufficient funds available
            isSufficientFundsAvailableForFbhProduct(pdpProductVariant, wfsProducts)
        }
    }


    /**
     * Determines the WFS products associated with the user.
     * @param userAccountResponse The UserAccountResponse containing user account and product details.
     * @return Mutable map of WFS product group codes and their associated product details.
     */
    override fun getWFSProductsForUser(userAccountResponse: UserAccountResponse?): MutableMap<String, ProductDetails?> {
        // Define the product group codes to check
        val keysToCheck = listOf(
            AccountProductKeys.StoreCard.value,
            AccountProductKeys.BlackCreditCard.value
        )

        // Get the valid product list and product detail list from the user account response
        val validProductList = userAccountResponse?.products.orEmpty()
        val productDetailList = userAccountResponse?.accountList.orEmpty()

        // Create a mutable map to store WFS products and their details
        val productsMap = mutableMapOf<String, ProductDetails?>()

        // Populate the productsMap with product group codes and their associated details
        for (productGroupCode in validProductList.map { it.productGroupCode }) {
            val productDetails = productDetailList.firstOrNull { it.productGroupCode == productGroupCode }
            productsMap[productGroupCode] = productDetails
        }

        // Check if any of the specified keys are present and have good standing, otherwise clear the map
        for (productKey in keysToCheck) {
            if (productsMap.containsKey(productKey) && productsMap[productKey]?.productOfferingGoodStanding == false) {
                productsMap.clear()
                break
            }
        }

        // Remove the "PersonalLoan" product key if present
        productsMap.remove(AccountProductKeys.PersonalLoan.value)

        return productsMap
    }

    /**
     * Checks if sufficient funds are available for FBH (Fulfilled by Home) products and constructs product representations.
     * @param pdpProductVariant The PdpProductVariant containing product details.
     * @param wfsProducts Mutable map of WFS product details.
     * @return Mutable map of product representations.
     */
    private fun isSufficientFundsAvailableForFbhProduct(
        pdpProductVariant: PdpProductVariant?,
        wfsProducts: MutableMap<String, ProductDetails?>
    ): MutableMap<String, ProductOnDisplay> {
        val fbhProductPrice = pdpProductVariant?.price?.toDouble() ?: 0.0

        val scProductGroupCode = AccountProductKeys.StoreCard.value
        val ccProductGroupCode = AccountProductKeys.BlackCreditCard.value

        val storeCardProduct = wfsProducts[scProductGroupCode]
        val creditCardProduct = wfsProducts[ccProductGroupCode]
        val availableFundsForSC = storeCardProduct?.availableFunds?.toDouble()?.div(100)
        val availableFundsForCC = creditCardProduct?.availableFunds?.toDouble()?.div(100)
        val accountBinNumberForCC = creditCardProduct?.accountNumberBin
        val wfsPaymentMethods = manageBnpLConfig.getWfsPaymentMethodsWithPayflex()

        val productOnDisplayList = mutableMapOf<String, ProductOnDisplay>()

        wfsProducts.forEach { (productGroupCode, _) ->
            when (productGroupCode.uppercase()) {
                scProductGroupCode -> {
                    constructStoreCard(
                        wfsPaymentMethods,
                        scProductGroupCode,
                        availableFundsForSC,
                        fbhProductPrice,
                        productOnDisplayList
                    )
                }
                ccProductGroupCode -> {
                    val constructCCProduct =
                        constructCreditCard(wfsPaymentMethods, accountBinNumberForCC, availableFundsForCC, fbhProductPrice)
                    constructCCProduct?.let { productOnDisplayList[ccProductGroupCode] = it }
                }
            }
        }

        return productOnDisplayList
    }


    /**
     * Constructs a store card representation based on provided data.
     * @param wfsPaymentMethods List of WfsPaymentMethods containing payment method details.
     * @param scProductGroupCode The product group code for the store card.
     * @param availableFundsForSC Available funds for the store card.
     * @param fbhProductPrice The price of the FBH  product.
     * @param productOnDisplayList Mutable map representing products on display.
     */
    private fun constructStoreCard(
        wfsPaymentMethods: MutableList<WfsPaymentMethods>?,
        scProductGroupCode: String,
        availableFundsForSC: Double?,
        fbhProductPrice: Double,
        productOnDisplayList: MutableMap<String, ProductOnDisplay>
    ) {
        wfsPaymentMethods?.firstOrNull { it.productGroupCode.equals(scProductGroupCode, ignoreCase = true) }?.let { wfsStoreCardProductConfig ->
            availableFundsForSC?.let { availableFunds ->
                val drawableId =
                    getProductImage(productGroupCode = AccountProductKeys.StoreCard.value)

                val isSufficientFundsAvailable = availableFunds > fbhProductPrice
                val currencyFormatter = convertEarnCashBackPrice(wfsStoreCardProductConfig, fbhProductPrice)
                productOnDisplayList += scProductGroupCode to
                        ProductOnDisplay(
                            availableFunds = CurrencyFormatter.formatAmountToRandAndCentNoSpace(
                                availableFunds
                            ),
                            earnCashBack = currencyFormatter,
                            drawableId = drawableId,
                            isSufficientFundsAvailable = isSufficientFundsAvailable,
                            wfsPaymentMethods = wfsStoreCardProductConfig
                        )
            }
        }
    }

    /**
     * Constructs a credit card representation based on provided data.
     * @param wfsPaymentMethods List of WfsPaymentMethods containing payment method details.
     * @param accountBinNumberForCC The account bin number for the credit card.
     * @param availableFundsForCC Available funds for the credit card.
     * @param fbhProductPrice The price of the FBH  product.
     * @return A ProductOnDisplay representing the credit card, or null if not found or funds are insufficient.
     */
    private fun constructCreditCard(
        wfsPaymentMethods: MutableList<WfsPaymentMethods>?,
        accountBinNumberForCC: String?,
        availableFundsForCC: Double?,
        fbhProductPrice: Double
    ): ProductOnDisplay? {
        wfsPaymentMethods?.firstOrNull { it.accountNumberBin.equals(accountBinNumberForCC, ignoreCase = true) }?.let { wfsCreditCardProductConfig ->
            availableFundsForCC?.let { availableFunds ->
                val drawableId = getProductImage(accountBinNumber = accountBinNumberForCC, productGroupCode = AccountProductKeys.BlackCreditCard.value)

                val isSufficientFundsAvailableForFbhProductCC = availableFunds > fbhProductPrice
                val currencyFormat = convertEarnCashBackPrice(wfsCreditCardProductConfig, fbhProductPrice)

                return ProductOnDisplay(
                    availableFunds = CurrencyFormatter.formatAmountToRandAndCentNoSpace(
                        availableFunds
                    ),
                    earnCashBack = currencyFormat,
                    drawableId = drawableId,
                    isSufficientFundsAvailable = isSufficientFundsAvailableForFbhProductCC,
                    wfsPaymentMethods = wfsCreditCardProductConfig
                )
            }
        }
        return null
    }

    /**
     * Converts the earned cashback price based on the cashback percentage and product price.
     *
     * @param wfsStoreCardProductConfig The WfsPaymentMethods configuration for the store card.
     * @param fbhProductPrice The price of the FBH  product.
     * @return A formatted string representing the earned cashback amount, or null if no cashback is applicable.
     */
    private fun convertEarnCashBackPrice(wfsStoreCardProductConfig: WfsPaymentMethods, fbhProductPrice: Double): String? {
        val cashbackPercentage = wfsStoreCardProductConfig.cashbackPercentage

        // Check if a cashback percentage is defined
        if (cashbackPercentage != null) {
            // Calculate the earned cashback amount
            val earnCashBack = (cashbackPercentage / 100.0) * fbhProductPrice

            // Format the cashback amount as a string
            return CurrencyFormatter.formatAmountToRandAndCentNoSpace(earnCashBack)
        }

        // If no cashback percentage is defined, return null
        return null
    }

    /**
     * Gets the product image resource ID based on the account bin number or product group code.
     * @param accountBinNumber The account bin number for credit cards (optional).
     * @param productGroupCode The product group code for other products.
     * @return The resource ID of the product image.
     */
    private fun getProductImage(accountBinNumber: String? = null, productGroupCode: String?): Int {
        return when (productGroupCode) {
            AccountProductKeys.StoreCard.value -> R.drawable.shoptimiser_store_card_icon
            PayFlexKey -> R.drawable.shoptimiser_payflex_icon
            else -> {
                // For credit cards, determine the image based on the account bin number
                when (accountBinNumber) {
                    CreditCardType.SILVER_CARD.rawValue -> R.drawable.shoptimiser_silver_credit_card_icon
                    CreditCardType.GOLD_CARD.rawValue -> R.drawable.shoptimiser_gold_credit_card_icon
                    else -> R.drawable.shoptimiser_black_credit_card_icon
                }
            }
        }
    }
}