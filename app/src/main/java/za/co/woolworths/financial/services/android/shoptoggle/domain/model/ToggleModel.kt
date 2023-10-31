package za.co.woolworths.financial.services.android.shoptoggle.domain.model


data class ToggleModel(
    val id: Int,
    val title: String?,
    val subTitle: String? = "",
    val icon: Int,
    val deliveryType: String,
    val deliveryTypeLabel: String,
    var deliverySlotFood: String,
    var deliverySlotFbh: String,
    var foodQuantity: Int,
    val deliveryCostLabel: String,
    var deliveryCost: String,
    var dataFailure: Boolean,
    val deliveryButtonText: String,
    val isDashDelivery: Boolean
)