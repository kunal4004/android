package za.co.woolworths.financial.services.android.models.dto.chat

data class CreditCard(var landing: Boolean = false, var paymentOptions: Boolean = false, var transactions: Boolean = false, var statements: Boolean = false)