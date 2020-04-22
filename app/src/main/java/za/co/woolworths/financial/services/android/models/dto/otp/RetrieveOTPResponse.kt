package za.co.woolworths.financial.services.android.models.dto.otp

import za.co.woolworths.financial.services.android.models.dto.Response

class RetrieveOTPResponse {
    var otpSentTo: String? = null
    var httpCode: Int = 0
    var response: Response? = null
}