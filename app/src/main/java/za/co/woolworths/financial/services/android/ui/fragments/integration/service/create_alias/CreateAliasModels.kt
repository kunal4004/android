package za.co.woolworths.financial.services.android.ui.fragments.integration.service.create_alias

import za.co.absa.openbankingapi.woolworths.integration.dto.Header

data class CreateAliasRequestProperty(
    val deviceId: String,
    val symmetricKey: String?,
    val symmetricKeyIV: String?,
    val header: Header? = Header()
)

data class CreateAliasResponseProperty(val header: Header? = null, var aliasId: String? = null)