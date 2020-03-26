package za.co.woolworths.financial.services.android.models.dto

data class CreditCardActivation(val otpEnabledForCreditCardActivation: Boolean = false, val endpointAvailabilityTimes: CreditCardActivationAvailabilityTimes)