package za.co.woolworths.financial.services.android.models.dto

data class GetPaymentMethod(
        val cardExpired: Boolean,
        var cardNumber: String,
        val expirationDate: String,
        val last4Digits: String,
        val token: String,
        val type: String,
        val vendor: String,
        var isCardChecked: Boolean = false)
