package za.co.woolworths.financial.services.android.geolocation.viewmodel


import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Response
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.request.SaveAddressLocationRequest
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.CreateList
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import javax.inject.Inject

@HiltViewModel
class ConfirmAddressViewModel @Inject constructor(private val geoLocationApiHelper: GeoLocationApiHelper) :
    ViewModel() {

    suspend fun getSavedAddress() =
        geoLocationApiHelper.getSavedAddress()

    suspend fun getValidateLocation(placeId: String) =
        geoLocationApiHelper.getValidateLocation(placeId)

    suspend fun postConfirmAddress(confirmLocationRequest: ConfirmLocationRequest) =
        geoLocationApiHelper.postConfirmLocation(confirmLocationRequest)

    suspend fun postSaveAddress(saveAddressLocationRequest: SaveAddressLocationRequest) =
        geoLocationApiHelper.postSaveAddress(saveAddressLocationRequest)

    fun isConnectedToInternet(context: Context) = geoLocationApiHelper.isConnectedToInternet(context)

    suspend fun getShoppingList() = geoLocationApiHelper.getShoppingList()

    suspend fun createNewList(createList: CreateList) =
        geoLocationApiHelper.createNewList(createList)

    suspend fun addProductsToList(productId: String, addToListRequest: List<AddToListRequest>): Response<ShoppingListItemsResponse> =
        geoLocationApiHelper.addProductsToList(
            productId, addToListRequest
        )
}