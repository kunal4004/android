package za.co.woolworths.financial.services.android.models.dto.linkdevice

import za.co.woolworths.financial.services.android.models.dto.Response

data class LinkedDeviceResponse (val deviceIdentityToken: String?, val deviceIdentityId: Long?, val response: Response,val httpCode: String?)