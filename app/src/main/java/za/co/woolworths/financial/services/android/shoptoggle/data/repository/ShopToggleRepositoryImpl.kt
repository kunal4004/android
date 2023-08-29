package za.co.woolworths.financial.services.android.shoptoggle.data.repository

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.shoptoggle.data.dto.ShopToggleData
import za.co.woolworths.financial.services.android.shoptoggle.domain.repository.ShopToggleRepository
import javax.inject.Inject

class ShopToggleRepositoryImpl @Inject constructor() : ShopToggleRepository {
    override fun getShopToggleList(): List<ShopToggleData> {
        return listOf(
            ShopToggleData(
                1,
                title = R.string.error_unknown.toString(),
                subTitle = "shop FASHION, BEAUTY, HOME AND food",
                icon = R.drawable.ic_toggle_collection_bag,
                deliveryType = "Earliest Standard Delivery Dates:",
                deliveryCost = "Fashion, Beauty, Home: Weds, 21 March",
                deliveryTime = "Food: Tues, 21 March *Unlimited items",
                deliveryProduct = "Delivery Cost:",
                learnMore = "Determined at checkout. Learn more",
                deliveryButtonText = "SET TO STANDARD DELIVERY",
                isDashDelivery = true),
            ShopToggleData(
                2,
                title = "use standard delivery",
                subTitle = "shop FASHION, BEAUTY, HOME AND food",
                icon = R.drawable.ic_toggle_collection_bag,
                deliveryType = "Earliest Standard Delivery Dates:",
                deliveryCost = "Fashion, Beauty, Home: Weds, 21 March",
                deliveryTime = "Food: Tues, 21 March *Unlimited items",
                deliveryProduct = "Delivery Cost:",
                learnMore = "Determined at checkout. Learn more",
                deliveryButtonText = "SET TO STANDARD DELIVERY",
                isDashDelivery = true),
            ShopToggleData(
                3,
                title = "use standard delivery",
                subTitle = "shop FASHION, BEAUTY, HOME AND food",
                icon = R.drawable.ic_toggle_collection_bag,
                deliveryType = "Earliest Standard Delivery Dates:",
                deliveryCost = "Fashion, Beauty, Home: Weds, 21 March",
                deliveryTime = "Food: Tues, 21 March *Unlimited items",
                deliveryProduct = "Delivery Cost:",
                learnMore = "Determined at checkout. Learn more",
                deliveryButtonText = "SET TO STANDARD DELIVERY",
                isDashDelivery = true),

            )
    }

}