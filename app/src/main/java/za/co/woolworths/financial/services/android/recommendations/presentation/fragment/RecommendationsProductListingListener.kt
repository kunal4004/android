package za.co.woolworths.financial.services.android.recommendations.presentation.fragment

import android.location.Location
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Product

interface RecommendationsProductListingListener {
    fun openProductDetailView(productList: Product)
    fun queryInventoryForStore(fulfilmentTypeId: String, addItemToCart: AddItemToCart?, productList: Product)
    fun addFoodProductTypeToCart(addItemToCart: AddItemToCart?)
    fun queryStoreFinderProductByFusedLocation(location: Location?)
    fun openChangeFulfillmentScreen()
    /**
     * This function shows liquor dialog to select suburb with title "Show Liquor" and description fetched from mobile config [Configs.liquor.message].
     * It should only be shown if below conditions are met:
     *  - Selected suburb should be listed in mobile config object [Configs.liquor]
     *  - isLiquor flag should be true in a product item from PLP service response [/app/v4/searchSortAndFilterV2]
     *  - it should be shown only once so use flag [AppInstanceObject.isLiquorModalShown]
     *
     *  @see Configs.liquor
     *  @see AppInstanceObject.isLiquorModalShown
     *  @see ApiInterface.getProducts
     */
}