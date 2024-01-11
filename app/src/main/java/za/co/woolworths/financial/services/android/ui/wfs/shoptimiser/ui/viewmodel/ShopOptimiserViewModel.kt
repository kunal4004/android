package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductKeys
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.UserAccountResponse
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.network.repository.MyAccountsLandingRemoteService
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller.IRetailBNPL
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller.WfsShopOptimiserProduct
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller.WfsShopOptimiserProductDetailsBuilder
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto.FBHProduct
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto.PdpProductVariant
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto.ProductOnDisplay
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto.ShopOptimiserVisibleUiType
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper.ShopOptimiserConstant.ShopOptimiserConstant.PayFlexKey
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper.sortMapByCustomKeyOrder
import javax.inject.Inject

enum class AccordionDividerVisibility { NONE, VISIBLE, HIDDEN }
@HiltViewModel
class ShopOptimiserViewModel @Inject constructor(
    private val accountLandingNetwork: MyAccountsLandingRemoteService,
    private val wfsShopOptimiserProduct: WfsShopOptimiserProduct,
    private val wfsShopOptimiserProductDetailsBuilder: WfsShopOptimiserProductDetailsBuilder,
    private val retailBNPL : IRetailBNPL,
) : ViewModel(), MyAccountsLandingRemoteService by accountLandingNetwork,
    WfsShopOptimiserProduct by wfsShopOptimiserProduct,
    WfsShopOptimiserProductDetailsBuilder by wfsShopOptimiserProductDetailsBuilder,
    IRetailBNPL by retailBNPL {

    val userAccountsFlow = MutableSharedFlow<NetworkStatusUI<UserAccountResponse>>(replay = 0)

    var isPayFlexViewVisible : Boolean = false
    // Other properties...
    private var pdpProductVariant by mutableStateOf(PdpProductVariant())
    var shoptimiserProductsList = mutableStateMapOf<String, ProductOnDisplay>()
    var isExpanded by mutableStateOf(false)
    var selectedOnDisplayProduct : ProductOnDisplay? = null
    var shopOptimiserVisibleUiType by mutableStateOf(ShopOptimiserVisibleUiType.GONE)
    var accordionDividerVisibility by mutableStateOf(AccordionDividerVisibility.NONE)

    /**
     * Sets the visibility of the Shop Optimiser UI to the Accordion view.
     */
    fun setAccordionUIVisible() {
            shopOptimiserVisibleUiType = ShopOptimiserVisibleUiType.ACCORDION
    }

    /**
     * Sets the visibility of the Shop Optimiser UI to the Stand-Alone view.
     */
    fun setStandaloneUIVisible() {
        shopOptimiserVisibleUiType = ShopOptimiserVisibleUiType.STANDALONE
    }

    /**
     * Clears the visibility of the Shop Optimiser UI.
     */
    fun clearUIVisibility() {
        shopOptimiserVisibleUiType = ShopOptimiserVisibleUiType.GONE
    }

    /**
     * Adds a price to the current Product Detail Page (PDP) variant.
     * @param price The price to be added to the PDP variant.
     */
    fun addPriceToPDPVariant(price: String?) {
        pdpProductVariant = pdpProductVariant.copy(price = price)
    }

    /**
     * Adds a product type to the current Product Detail Page (PDP) variant and sets the view to a non-expanded state.
     * @param productType The product type to be added to the PDP variant.
     */
    fun addProductTypeToPDPVariant(productType: String?) {
        isExpanded = false
        pdpProductVariant = pdpProductVariant.copy(fbhProduct = FBHProduct.getFBHProduct(productType))
    }

    /**
     * Adds product details to the current Product Detail Page (PDP) variant.
     * @param productDetails The product details to be added to the PDP variant.
     */
    fun addProductDetails(productDetails: ProductDetails?) {
        pdpProductVariant = pdpProductVariant.copy(productDetail = productDetails)
    }

    /**
     * Retrieves WFS (Wireless Financial Services) products for the user, updating the user accounts flow state.
     */
    fun getWFSProductsForUser() {
        viewModelScope.launch {
            queryUserAccountService(isRefreshing = !isAccountResponseCachedWithin3Hours(), _state = userAccountsFlow)
        }
    }

    /**
     * Creates initial Shop Optimiser products based on the current product variant and loads WFS product data.
     * This function updates the Shoptimiser product list with loading placeholders and the PayFlex product.
     */
    fun createShopOptimiserProduct() {
        // Load WFS product data and assign it to the initialProduct
        val initialProduct = displayFBHProductAndLoadWFSProduct(pdpProductVariant = pdpProductVariant)

        // If initialProduct is not null, proceed to update the Shoptimiser product list
        initialProduct?.let {
            shoptimiserProductsList.apply {
                // Clear the existing product list
                clear()

                // Add loading placeholders for Store Card and Black Credit Card
                this[AccountProductKeys.StoreCard.value] = it.copy(isLoading = true)
                this[AccountProductKeys.BlackCreditCard.value] = it.copy(isLoading = true)

                // Add the PayFlex product with appropriate flags
                this[PayFlexKey] = it.copy(isLastProduct = true, isLoading = false)
            }
        }
    }

    /**
     * Sets the user account response and updates the Shoptimiser product list accordingly.
     * Also triggers a callback to determine if the Stand-Alone View is visible.
     * @param userAccountResponse The user account response containing product details.
     * @param isStandAloneViewVisible A callback function to determine if the Stand-Alone View is visible.
     */
    fun setUserAccountResponse(userAccountResponse: UserAccountResponse, isStandAloneViewVisible: (Boolean) -> Unit) {
        // Save the Shop Optimiser timestamp in the SQLite model
        saveShopOptimiserTimestampInSQLiteModel()

        // Display the WFS products based on the user account response
        val productsToDisplay = displayWfsProducts(pdpProductVariant = pdpProductVariant, userAccountResponse = userAccountResponse)

        with(shoptimiserProductsList) {
            // Remove WFS products not found in the response
            listOf(AccountProductKeys.BlackCreditCard.value, AccountProductKeys.StoreCard.value).forEach { key ->
                if (!productsToDisplay.containsKey(key)) {
                    remove(key)
                }
            }

            // Define the order in which products should be displayed
            val order = mutableListOf<String>()
            userAccountResponse.products?.forEach { order.add(it.productGroupCode) }
            order.add(PayFlexKey)

            // Update the Shoptimiser product list with the products to display
            productsToDisplay.forEach { (key, product) -> this[key] = product }

            // Sort the Shoptimiser product list by custom key order
            shoptimiserProductsList = sortMapByCustomKeyOrder(this, order)

            // Set a flag for the last product in the list
            entries.lastOrNull()?.apply { this@with[key] = value.copy(isLastProduct = true) }
        }

        /**
         * If productsOnDisplay.size <= 1, it means only PayFlex is available in shoptimiserProductsList,
         * therefore, we display the PayFlex WebView. Note that this check includes the account in arrears scenario.
         */
        isStandAloneViewVisible(productsToDisplay.isEmpty())
    }

    /**
     * Checks if the Shop Optimiser feature is enabled.
     * @return `true` if Shop Optimiser is enabled, `false` otherwise.
     */
    fun isShopOptimiserEnabled() = isShopOptimiserEnabled(pdpProductVariant = pdpProductVariant)

    /**
     * Removes specific products from the Shoptimiser product list when an account has an error.
     */
    fun removeLoaderWhenAccountHasError() {
        shoptimiserProductsList.remove(AccountProductKeys.StoreCard.value)
        shoptimiserProductsList.remove(AccountProductKeys.BlackCreditCard.value)
    }

    /**
     * Checks if the product detail page was reopened.
     * @return `true` if the default PDP is displayed and the account response is cached within 3 hours, `false` otherwise.
     */
    fun wasProductDetailPageReOpened() : Boolean {
        return retrieveShopOptimiserSQLiteModel().isDefaultPdpDisplayed && isAccountResponseCachedWithin3Hours()
    }

    /**
     * Generates an AnnotatedString for PayFlex information.
     * @return An AnnotatedString representing PayFlex information or `null` if not applicable.
     */
    fun generatePayFlexAnnotatedString(): AnnotatedString? {
        val price = payFlexCalculation(fbhProductPrice = pdpProductVariant.price?.toDouble())
        return formatPayFlexDescriptionWithPrice(price = price)
    }

    /**
     * Checks if the Stand-Alone PayFlex view is enabled for the current product.
     * @return `true` if Stand-Alone PayFlex is enabled, `false` otherwise.
     */
    fun isStandAlonePayFlexViewEnabled() = isLoggedIn() && isStandAlonePayFlexEnabled(pdpProductVariant.productDetail)

    /**
     * Retrieves the PayFlex payment details for display.
     * @return A ProductOnDisplay object representing the PayFlex payment details.
     */
    fun standAlonePayFlexPaymentOnDisplay(): ProductOnDisplay? {
        val wfsPaymentMethods = getWfsPaymentMethodsWithPayflex()
        return constructPayFlex(wfsPaymentMethods, fbhProductPrice = pdpProductVariant.price?.toDouble() ?: 0.0)
    }

    fun getInfoDisabledFinancialProductTitleAndDescription(): Pair<String?, AnnotatedString?> {
        val infoDisabledFinancialProduct = getInfoDisabledFinancialProduct()
        val infoTitle = infoDisabledFinancialProduct?.infoTitle
        val infoDescription = getInfoDescription(infoDescription = infoDisabledFinancialProduct?.infoDescription,infoDescriptionBoldParts  =  infoDisabledFinancialProduct?.infoDescriptionBoldParts)
        return infoTitle to infoDescription
    }

    fun insufficientFundsFooterLabel(): String? {
        return  getInfoDisabledFinancialProduct()?.footerLabel
    }
}
