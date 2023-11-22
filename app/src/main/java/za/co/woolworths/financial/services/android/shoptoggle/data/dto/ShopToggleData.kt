package za.co.woolworths.financial.services.android.shoptoggle.data.dto

data class ShopToggleData(
    val id: Int,
    val title: String?,
    val subTitle: String? = "",
    val icon: Int,
    val deliveryTypeLabel: String,
    val deliveryType: String,
    val deliverySlotFood: String,
    val deliverySlotFbh: String,
    val deliveryCost: String,
    val learnMore: String,
    val deliveryButtonText: String,
    val isDashDelivery: Boolean,
    val quantity: Int,
    val deliveryButtonTextContinue: String,
)
