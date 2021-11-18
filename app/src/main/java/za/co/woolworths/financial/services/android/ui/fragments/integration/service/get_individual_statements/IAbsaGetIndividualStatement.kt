package za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_individual_statements

import za.co.absa.openbankingapi.woolworths.integration.dto.ArchivedStatement
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.NetworkState

interface IAbsaGetIndividualStatement {
    fun requestBody(archivedStatement: ArchivedStatement?):String?
    suspend fun fetchAbsaIndividualStatement(archivedStatement: ArchivedStatement?): NetworkState<AbsaProxyResponseProperty>
}