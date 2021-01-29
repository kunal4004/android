package za.co.woolworths.financial.services.android.models

data class CreditCardDelivery(var callCenterNumber: String, var deliveryTrackingUrl: String, var formattedCardDeliveryFee: String,  val cardTypes: MutableList<CreditCardDeliveryCardTypes>)