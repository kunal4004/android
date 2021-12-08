package za.co.woolworths.financial.services.android.ui.fragments.integration.service.cekd

import za.co.absa.openbankingapi.woolworths.integration.dto.Header

data class CekdRequestProperty(
    val deviceId: String,
    val applicationId: String = "WCOBMOBAPP",
    val contentEncryptionSeed: String,
    val header: Header = Header()
)

data class CekdResponseProperty(val header: Header?, val keyId: String?)
