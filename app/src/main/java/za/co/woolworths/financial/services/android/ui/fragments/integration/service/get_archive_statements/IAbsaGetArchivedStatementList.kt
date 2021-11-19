package za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_archive_statements

import za.co.absa.openbankingapi.woolworths.integration.dto.Header
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.NetworkState

interface IAbsaGetArchivedStatementList{
    fun requestBody(header: Header?, accountNumber: String?): String?
    suspend fun fetchAbsaArchivedStatement(header: Header?,cookie:String, accountNumber: String?): NetworkState<AbsaProxyResponseProperty>
}