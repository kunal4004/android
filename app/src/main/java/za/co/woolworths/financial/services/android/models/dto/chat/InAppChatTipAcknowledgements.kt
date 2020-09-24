package za.co.woolworths.financial.services.android.models.dto.chat

data class InAppChatTipAcknowledgements(var accountsLanding: Boolean = false, var storeCard: StoreCard, var creditCard: CreditCard, var personalLoan: PersonalLoan,var isWhatsAppOnBoardingScreenVisible: Boolean = false)
