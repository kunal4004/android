package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.ProductList

interface IProductListing {
    fun openProductDetailView(productList: ProductList)
    fun queryInventoryForStore(storeId: String, addItemToCart: AddItemToCart?)
    fun addFoodProductTypeToCart(addItemToCart: AddItemToCart?)
}