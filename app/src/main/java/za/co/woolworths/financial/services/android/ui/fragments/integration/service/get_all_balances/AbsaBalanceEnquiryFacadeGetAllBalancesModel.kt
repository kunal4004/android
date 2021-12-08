package za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_all_balances

import za.co.absa.openbankingapi.woolworths.integration.dto.AccountListItem
import za.co.absa.openbankingapi.woolworths.integration.dto.Header
import java.util.ArrayList

data class AbsaBalanceEnquiryRequestProperty(private val header: Header) {
    init {
        header.service = "BalanceEnquiryFacade"
        header.operation = "GetAllBalances"
    }
}

data class AbsaBalanceEnquiryResponseProperty (val header: Header? = null , val accountList: ArrayList<AccountListItem>? = null)