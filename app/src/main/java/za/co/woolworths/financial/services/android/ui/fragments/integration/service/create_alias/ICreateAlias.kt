package za.co.woolworths.financial.services.android.ui.fragments.integration.service.create_alias

import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.NetworkState

interface ICreateAlias {
    fun createAliasRespRequestProperty(): CreateAliasRequestProperty
    suspend fun fetchCreateAlias(): NetworkState<AbsaProxyResponseProperty>
    fun handleCreateAliasResult(createAliasResponseProperty: CreateAliasResponseProperty?): String?
}