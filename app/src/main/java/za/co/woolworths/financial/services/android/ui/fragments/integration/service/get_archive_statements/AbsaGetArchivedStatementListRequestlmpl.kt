package za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_archive_statements

import za.co.absa.openbankingapi.woolworths.integration.dto.Header
import za.co.absa.openbankingapi.woolworths.integration.dto.StatementListRequest
import za.co.woolworths.financial.services.android.ui.fragments.integration.remote.AbsaRemoteApi
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.NetworkState
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.contentLength
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.resultOf
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.toAes256Encrypt

class AbsaGetArchivedStatementListRequestImpl : IAbsaGetArchivedStatementList {
    override fun requestBody(header: Header?, accountNumber: String?): String? {
        header?.service = "ArchivedStatementFacade"
        header?.operation = "GetArchivedStatementList"
        return  StatementListRequest(header, accountNumber).json
    }

    override suspend fun fetchAbsaArchivedStatement(header: Header?, accountNumber: String?): NetworkState<AbsaProxyResponseProperty> {
        val requestBody = requestBody(header, accountNumber)
        requestBody?.contentLength()
        val withEncryptedBody = requestBody?.toAes256Encrypt()
        return resultOf(AbsaRemoteApi.service.queryAbsaServiceGetArchivedStatement(withEncryptedBody))
    }
}