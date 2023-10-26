package za.co.woolworths.financial.services.android.geolocation.viewmodel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.Response
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.request.SaveAddressLocationRequest
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.network.repository.ConfirmAddressRepository
import za.co.woolworths.financial.services.android.geolocation.network.repository.ConfirmAddressRepositoryImp
import za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel.ValidateStoreResponse
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.CreateList
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.mapNetworkCallToViewStateFlow
import javax.inject.Inject

@HiltViewModel
class ConfirmAddressViewModel @Inject constructor(
    private val geoLocationApiHelper: GeoLocationApiHelper,
    private val confirmAddressRepositoryImp: ConfirmAddressRepositoryImp,
) :
    ViewModel(), ConfirmAddressRepository by confirmAddressRepositoryImp {

    suspend fun getSavedAddress() =
        geoLocationApiHelper.getSavedAddress()

    suspend fun getValidateLocation(placeId: String) =
        geoLocationApiHelper.getValidateLocation(placeId)

    suspend fun postConfirmAddress(confirmLocationRequest: ConfirmLocationRequest) =
        geoLocationApiHelper.postConfirmLocation(confirmLocationRequest)

    suspend fun postSaveAddress(saveAddressLocationRequest: SaveAddressLocationRequest) =
        geoLocationApiHelper.postSaveAddress(saveAddressLocationRequest)

    fun isConnectedToInternet(context: Context) =
        geoLocationApiHelper.isConnectedToInternet(context)

    suspend fun getShoppingList() = geoLocationApiHelper.getShoppingList()

    suspend fun createNewList(createList: CreateList) =
        geoLocationApiHelper.createNewList(createList)

    suspend fun addProductsToList(
        productId: String,
        addToListRequest: List<AddToListRequest>,
    ): Response<ShoppingListItemsResponse> =
        geoLocationApiHelper.addProductsToList(
            productId, addToListRequest
        )

    private val _validateStoreInventoryData = MutableSharedFlow<ViewState<ValidateStoreResponse>>(0)
    val validateStoreInventoryData: SharedFlow<ViewState<ValidateStoreResponse>> =
        _validateStoreInventoryData

    suspend fun queryValidateStoreInventory(placeId: String, storeId: String) =
        viewModelScope.launch {
            mapNetworkCallToViewStateFlow {
                callValidateStoreInventory(placeId, storeId)
            }.collectLatest {
                _validateStoreInventoryData.emit(it)
            }
        }
}