package za.co.woolworths.financial.services.android.models.dto.cart

class DeliveryDetails {
    var shippingAddress: ShippingAddress? =null
    var shippingAmount: Int = 0
    var deliveryInfos: Array<DeliveryInfo>? =null
}