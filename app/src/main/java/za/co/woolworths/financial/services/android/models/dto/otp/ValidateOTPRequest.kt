package za.co.woolworths.financial.services.android.models.dto.otp

data class ValidateOTPRequest(val otpMethod: String, val otp: String)