package za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_all_balances

import za.co.absa.openbankingapi.woolworths.integration.dto.AbsaBalanceEnquiryRequest
import za.co.absa.openbankingapi.woolworths.integration.dto.Header
import za.co.woolworths.financial.services.android.ui.fragments.integration.remote.AbsaRemoteApi
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.NetworkState
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.contentLength
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.resultOf
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.toAes256Encrypt

class AbsaBalanceEnquiryFacadeGetAllBalancesImpl  : IAbsaBalanceEnquiryFacadeGetAllBalance {

    override fun requestBody(eSessionId: String?, nonce: String?, timestampString : String?): String? {
        val header = Header()
        header.service = "BalanceEnquiryFacade"
        header.operation = "GetAllBalances"
        header.channel = "I"
        header.language = "en"
        header.organization = "WCOBMOBAPP"
        header.brand = "WCOBMOBAPP"
        header.esessionId = eSessionId
        header.nonce = nonce
        header.timestamp = timestampString
        return  AbsaBalanceEnquiryRequest(header).json
    }

    override suspend fun fetchAbsaBalanceEnquiryFacadeGetAllBalance(eSessionId: String?, nonce: String?, timestampString: String?): NetworkState<AbsaProxyResponseProperty> {
        val requestBody  = requestBody(eSessionId, nonce, timestampString)
        requestBody?.contentLength()
        val withEncryptedBody = requestBody?.toAes256Encrypt()
        return  resultOf(AbsaRemoteApi.service.queryAbsaServiceGetAllBalances(withEncryptedBody))
    }
}