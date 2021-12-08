package za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_individual_statements

import za.co.absa.openbankingapi.woolworths.integration.dto.ArchivedStatement
import za.co.woolworths.financial.services.android.ui.fragments.integration.remote.AbsaRemoteApi
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.NetworkState
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.contentLength
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.resultOf
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.toAes256Encrypt

class AbsaGetIndividualStatementImpl : IAbsaGetIndividualStatement {

    override fun requestBody(archivedStatement: ArchivedStatement?): String? {
      return archivedStatement?.let { IndividualStatementRequestProperty(it).urlEncodedFormData }
    }

    override suspend fun fetchAbsaIndividualStatement(cookie: String,archivedStatement: ArchivedStatement?): NetworkState<AbsaProxyResponseProperty> {
        val requestBody = requestBody(archivedStatement)
        requestBody?.contentLength()
        val withEncryptedBody = requestBody?.toAes256Encrypt()
        return resultOf(AbsaRemoteApi.service.queryAbsaServiceGetIndividualStatement(cookie, withEncryptedBody))
    }
}

