package za.co.woolworths.financial.services.android.shoptoggle.domain.model


data class ToggleModel(
    val id: Int,
    val title: String?,
    val subTitle: String? = "",
    val icon: Int,
    val deliveryType: String,
    var deliveryTime: String,
    var deliveryProduct: String,
    val deliveryCost: String,
    var learnMore: String,
    var dataFailure: Boolean,
    val deliveryButtonText: String,
    val isDashDelivery: Boolean
)