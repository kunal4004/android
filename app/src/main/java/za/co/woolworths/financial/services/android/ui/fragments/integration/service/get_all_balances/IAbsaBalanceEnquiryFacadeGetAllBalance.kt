package za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_all_balances

import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.NetworkState

interface IAbsaBalanceEnquiryFacadeGetAllBalance {
    fun requestBody(eSessionId: String?, nonce: String?, timestampString : String?): String?
    suspend fun fetchAbsaBalanceEnquiryFacadeGetAllBalance(eSessionId: String?, nonce: String?, timestampString : String?): NetworkState<AbsaProxyResponseProperty>
}