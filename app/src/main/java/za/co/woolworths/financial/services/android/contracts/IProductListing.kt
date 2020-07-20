package za.co.woolworths.financial.services.android.contracts

import android.location.Location
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.ProductList

interface IProductListing {
    fun openProductDetailView(productList: ProductList)
    fun queryInventoryForStore(fulfilmentTypeId: String, addItemToCart: AddItemToCart?, productList: ProductList)
    fun addFoodProductTypeToCart(addItemToCart: AddItemToCart?)
    fun queryStoreFinderProductByFusedLocation(location: Location?)
}