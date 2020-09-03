package za.co.woolworths.financial.services.android.models.dto

import java.io.Serializable

data class GetPaymentMethod(
        var cardExpired: Boolean,
        var cardNumber: String,
        val expirationDate: String,
        val last4Digits: String,
        val token: String,
        val type: String,
        val vendor: String,
        var isCardChecked: Boolean = false) : Serializable
