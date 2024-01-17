package za.co.woolworths.financial.services.android.presentation.common.delivery_location

sealed class DeliveryLocationEvent {

    object ChangeDeliveryClick : DeliveryLocationEvent()
    object ChangeAddressClick : DeliveryLocationEvent()
}
