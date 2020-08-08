package za.co.woolworths.financial.services.android.models.dto

data class GetPaymentMethod(
        val cardExpired: Boolean,
        val cardNumber: String,
        val expirationDate: String,
        val last4Digits: String,
        val token: String,
        val type: String,
        val vendor: String)