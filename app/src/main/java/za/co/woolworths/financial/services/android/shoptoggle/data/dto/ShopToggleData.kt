package za.co.woolworths.financial.services.android.shoptoggle.data.dto

data class ShopToggleData(
    val id: Int,
    val title: String?,
    val subTitle: String? = "",
    val icon: Int,
    val deliveryType: String,
    val deliveryTime: String,
    val deliveryProduct: String,
    val deliveryCost: String,
    val learnMore: String,
    val deliveryButtonText: String,
    val isDashDelivery: Boolean
)
