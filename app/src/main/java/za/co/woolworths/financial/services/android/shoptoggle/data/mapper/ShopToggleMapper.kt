package za.co.woolworths.financial.services.android.shoptoggle.data.mapper

import za.co.woolworths.financial.services.android.shoptoggle.data.dto.ShopToggleData
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel


fun ShopToggleData.toDomain(): ToggleModel {
    return ToggleModel(
        id = id,
        title = title,
        subTitle = subTitle,
        icon = icon,
        deliveryType = deliveryType,
        deliverySlotFood = deliverySlotFood,
        deliverySlotFbh = deliverySlotFbh,
        deliveryCostLabel = deliveryCost,
        deliveryCost = learnMore,
        deliveryButtonText = deliveryButtonText,
        isDashDelivery = isDashDelivery,
        dataFailure = false,
        foodQuantity = quantity,
        deliveryTypeLabel = deliveryTypeLabel,
        deliveryButtonTextContinue = deliveryButtonTextContinue
    )
}

