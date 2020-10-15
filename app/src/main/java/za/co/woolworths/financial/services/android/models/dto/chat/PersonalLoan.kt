package za.co.woolworths.financial.services.android.models.dto.chat

data class PersonalLoan(var landing: Boolean = false, var paymentOptions: Boolean = false, var transactions: Boolean = false, val statements: Boolean = false)