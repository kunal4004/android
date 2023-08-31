package za.co.woolworths.financial.services.android.shoptoggle.data.repository

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.common.ResourcesProvider
import za.co.woolworths.financial.services.android.shoptoggle.data.dto.ShopToggleData
import za.co.woolworths.financial.services.android.shoptoggle.domain.repository.ShopToggleRepository
import javax.inject.Inject

class ShopToggleRepositoryImpl @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
) : ShopToggleRepository {
    // TODO: Remaining string value need to set from backend response
    override fun getShopToggleList(): List<ShopToggleData> {
        return listOf(
            ShopToggleData(
                id = 1,
                title = resourcesProvider.getString(R.string.use_standard_delivery),
                subTitle = resourcesProvider.getString(R.string.standard_shop_fashion),
                icon = R.drawable.ic_toggle_delivery_truck,
                deliveryType = resourcesProvider.getString(R.string.earliest_standard_delivery_dates),
                deliveryCost = resourcesProvider.getString(R.string.delivery_cost),
                deliveryTime = "Food: Tues, 21 March *Unlimited items",
                deliveryProduct = "Fashion, Beauty, Home: Weds, 21 March",
                learnMore = resourcesProvider.getString(R.string.determined_at_checkout),
                deliveryButtonText = resourcesProvider.getString(R.string.set_to_standard_delivery),
                isDashDelivery = false),
            ShopToggleData(
                id = 2,
                title = resourcesProvider.getString(R.string.use_dash_delivery),
                subTitle = resourcesProvider.getString(R.string.get_food_today),
                icon = R.drawable.ic_toggle_dash_scooter,
                deliveryType = resourcesProvider.getString(R.string.next_dash_delivery_timeslot),
                deliveryCost = resourcesProvider.getString(R.string.delivery_cost),
                deliveryTime = "Today, 10am - 11am",
                deliveryProduct = "Food only *Limited shop of 30 food items",
                learnMore = resourcesProvider.getString(R.string.determined_at_checkout),
                deliveryButtonText = resourcesProvider.getString(R.string.set_to_dash_delivery),
                isDashDelivery = true),
            ShopToggleData(
                id = 3,
                title = resourcesProvider.getString(R.string.use_click_collect),
                subTitle = resourcesProvider.getString(R.string.collect_fashion_food),
                icon = R.drawable.ic_toggle_collection_bag,
                deliveryType = resourcesProvider.getString(R.string.earliest_click_and_collect),
                deliveryCost = resourcesProvider.getString(R.string.delivery_cost),
                deliveryTime = "Food: Tues, 21 March *Unlimited items",
                deliveryProduct = "Food only *Limited shop of 30 food items",
                learnMore = "R 35.00 Learn more",
                deliveryButtonText = resourcesProvider.getString(R.string.set_to_click_and_collect),
                isDashDelivery = false)

            )
    }

}