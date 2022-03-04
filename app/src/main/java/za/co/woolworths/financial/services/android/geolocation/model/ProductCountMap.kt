package za.co.woolworths.financial.services.android.geolocation.model

data class ProductCountMap(
    val foodProductCartItemIdMap: FoodProductCartItemIdMap,
    val productItemCountMap: ProductItemCountMap,
    val quantityLimit: QuantityLimit,
    val totalProductCount: Int
)