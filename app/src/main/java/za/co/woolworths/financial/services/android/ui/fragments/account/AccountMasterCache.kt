package za.co.woolworths.financial.services.android.ui.fragments.account

import za.co.woolworths.financial.services.android.models.dto.AccountsResponse
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.Products

object AccountMasterCache {
    var accountsResponse: AccountsResponse? = null
    var accountDetailsForCreditCardProduct: AccountsResponse? = null
    var accountDetailsForStoreCardProduct: AccountsResponse? = null
    var accountDetailsForPersonalLoanProduct: AccountsResponse? = null

    fun setAccountsProduct(products: Products?, response: AccountsResponse?) {
        when (AccountsProductGroupCode.getEnum(products?.productGroupCode)) {
            AccountsProductGroupCode.STORE_CARD -> accountDetailsForStoreCardProduct = response
            AccountsProductGroupCode.CREDIT_CARD -> accountDetailsForCreditCardProduct = response
            AccountsProductGroupCode.PERSONAL_LOAN -> accountDetailsForPersonalLoanProduct = response
        }
    }

    fun resetAccountResponse() {
        accountsResponse = AccountsResponse()
    }

}