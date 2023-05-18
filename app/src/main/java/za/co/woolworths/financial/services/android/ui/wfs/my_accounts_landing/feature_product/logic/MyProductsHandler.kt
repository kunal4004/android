package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.logic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import za.co.woolworths.financial.services.android.models.dto.account.InsuranceProducts
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductKeys
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.CreditCardType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.ProductTransformer
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductCardsGroup
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.ProductDetails
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.UserAccountResponse
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_view_application_status.ViewApplicationStatusImpl
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.FontHyperTextParser
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface MyProductsHandler {

    fun listOfDefaultProductItems(productDetails: ProductDetails? = null): MutableMap<String, AccountProductCardsGroup?>

    fun transformProductDetailToProductGroup(key: AccountProductKeys?, productDetails: Any?): AccountProductCardsGroup?

    fun isProductViewGroupVisible(size: Int) : Boolean

    fun isC2User() : Boolean

    fun isNowWfsUser(userAccountResponse: UserAccountResponse?): Boolean

    fun transformSingleProductResult(productGroupCode: String?, productDetails: Any?): AccountProductCardsGroup?

    fun addViewApplicationStatusIfNeeded(validProductList: List<ProductDetails>?,
                                         mapOfMyProducts: MutableMap<String, AccountProductCardsGroup?>)
}

class MyProductsHandlerImpl @Inject constructor(private val status: ViewApplicationStatusImpl) :
    MyProductsHandler {

    override fun isC2User() = runBlocking {
        withContext(Dispatchers.IO) { SessionUtilities.getInstance().isC2User }
    }

    override fun isNowWfsUser(userAccountResponse: UserAccountResponse?): Boolean {
        userAccountResponse ?: return false
        val productGroupCodes = mutableListOf(
            AccountProductKeys.PersonalLoan.value,
            AccountProductKeys.StoreCard.value,
            AccountProductKeys.BlackCreditCard.value
        )
        val validAccounts = userAccountResponse.products
        val unavailableAccounts = productGroupCodes.filterNot { productGroupCode ->
            validAccounts?.any { productDetails ->
                productGroupCode.contains(productDetails.productGroupCode)
            } == true
        }
        return unavailableAccounts.isNotEmpty() && unavailableAccounts.size == 3

    }

    override fun listOfDefaultProductItems(productDetails: ProductDetails?): MutableMap<String, AccountProductCardsGroup?> = mutableMapOf(
        AccountProductKeys.StoreCard.value to transformProductDetailToProductGroup(AccountProductKeys.StoreCard,productDetails),
        AccountProductKeys.BlackCreditCard.value to  transformProductDetailToProductGroup(AccountProductKeys.BlackCreditCard,productDetails),
        AccountProductKeys.PersonalLoan.value to  transformProductDetailToProductGroup(AccountProductKeys.PersonalLoan,productDetails),
        AccountProductKeys.PetInsurance.value to  transformProductDetailToProductGroup(AccountProductKeys.PetInsurance,productDetails))

    override fun transformProductDetailToProductGroup(
        key: AccountProductKeys?,
        productDetails: Any?
    ): AccountProductCardsGroup? {

        var productDetail : ProductDetails? = null
        var insuranceProducts : InsuranceProducts? = null
        var transformer = ProductTransformer()

        if (productDetails is ProductDetails) {
            productDetail = productDetails
            transformer = getProductTransformer(productDetail)
        }

        if (productDetails is InsuranceProducts) {
            insuranceProducts = productDetails
        }

        val productGroupMap = mapOf(
            AccountProductKeys.StoreCard to AccountProductCardsGroup.StoreCard(productDetails = productDetail, transformer = transformer),
            AccountProductKeys.PersonalLoan to AccountProductCardsGroup.PersonalLoan(productDetails = productDetail, transformer = transformer),
            AccountProductKeys.BlackCreditCard to AccountProductCardsGroup.BlackCreditCard(productDetails = productDetail, transformer = transformer),
            AccountProductKeys.GoldCreditCard to AccountProductCardsGroup.GoldCreditCard(productDetails = productDetail,transformer =  transformer),
            AccountProductKeys.SilverCreditCard to AccountProductCardsGroup.SilverCreditCard(productDetails = productDetail, transformer = transformer),
            AccountProductKeys.PetInsurance to AccountProductCardsGroup.PetInsurance(insuranceProducts = insuranceProducts),
            AccountProductKeys.ViewApplicationStatus to AccountProductCardsGroup.ApplicationStatus()
        )

        return productGroupMap[key]
    }

    private fun getProductTransformer(productDetail: ProductDetails): ProductTransformer {
        val isAccountInArrears = productDetail.productOfferingGoodStanding == false
        val isChargedOff = Utils.ACCOUNT_CHARGED_OFF.equals(productDetail.productOfferingStatus, ignoreCase = true)
        val availableFund = currencyFormatter(productDetail.availableFunds)

        return ProductTransformer(
            isAccountInArrears = isAccountInArrears,
            isAccountChargedOff = isChargedOff,
            currentAmount = availableFund
        )
    }

    private fun currencyFormatter(availableFunds: Int?): String? {
       return FontHyperTextParser.getSpannable(
            CurrencyFormatter.formatAmountToRandAndCentWithSpace(availableFunds), 1
        ).toString().replace("R ", "R")
    }

    override fun transformSingleProductResult(productGroupCode: String?, productDetails: Any?): AccountProductCardsGroup? {
        if (productDetails is InsuranceProducts) {
            return transformProductDetailToProductGroup(key = AccountProductKeys.PetInsurance, productDetails = productDetails)
        }
        val productDetail = productDetails as? ProductDetails
        val code = productGroupCode ?: productDetail?.productGroupCode ?: ""
        return transformProductDetailToProductGroup(key = getProductKeyByCode(code, productDetail), productDetails = productDetail)
    }

    private fun getProductKeyByCode(code: String, productDetails: ProductDetails?): AccountProductKeys? {
        return when (AccountProductKeys.fromString(code)) {
            AccountProductKeys.StoreCard -> AccountProductKeys.StoreCard
            AccountProductKeys.PersonalLoan -> AccountProductKeys.PersonalLoan
            AccountProductKeys.BlackCreditCard -> getCreditCardByAccountNumberBin(productDetails)
            else -> null
        }
    }

    private fun getCreditCardByAccountNumberBin(productDetails: ProductDetails?): AccountProductKeys {
        return when (CreditCardType.fromString(productDetails?.accountNumberBin ?: "")) {
            CreditCardType.SILVER_CARD -> AccountProductKeys.SilverCreditCard
            CreditCardType.GOLD_CARD -> AccountProductKeys.GoldCreditCard
            else -> AccountProductKeys.BlackCreditCard
        }
    }

    override fun isProductViewGroupVisible(size: Int) = size > 0

    override fun addViewApplicationStatusIfNeeded(
        validProductList: List<ProductDetails>?,
        mapOfMyProducts: MutableMap<String, AccountProductCardsGroup?>) {
        if (status.isVisible(validProductList?.size)) {
            val viewApplicationStatusProduct = transformProductDetailToProductGroup(key = AccountProductKeys.ViewApplicationStatus, productDetails = null)
            mapOfMyProducts += AccountProductKeys.ViewApplicationStatus.value to viewApplicationStatusProduct
        }
    }

}