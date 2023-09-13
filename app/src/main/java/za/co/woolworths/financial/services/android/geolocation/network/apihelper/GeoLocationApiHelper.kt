package za.co.woolworths.financial.services.android.geolocation.network.apihelper

import android.content.Context
import retrofit2.await
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.request.SaveAddressLocationRequest
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.CreateList
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl
import za.co.woolworths.financial.services.android.models.network.RetrofitApiProviderImpl
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.util.NetworkManager
import javax.inject.Inject


class GeoLocationApiHelper @Inject constructor() : RetrofitConfig(AppContextProviderImpl(), RetrofitApiProviderImpl()) {

    suspend fun getSavedAddress() =
        mApiInterface.getSavedAddresses("", "", getSessionToken(), getDeviceIdentityToken()).await()

    suspend fun getValidateLocation(placeId: String) =
        mApiInterface.geoValidateLocation(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            "").await()

    suspend fun postConfirmLocation(confirmLocationRequest: ConfirmLocationRequest) =
        mApiInterface.confirmLocation("",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            confirmLocationRequest
        ).await()

    suspend fun postSaveAddress(saveAddressLocationRequest: SaveAddressLocationRequest) =
        mApiInterface.saveLocation("",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            saveAddressLocationRequest
        ).await()

    fun initConfirmLocation(confirmLocationRequest: ConfirmLocationRequest) =
        mApiInterface.confirmLocation("",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            confirmLocationRequest
        )

    fun validateLocation(placeId: String) =
        mApiInterface.validateLocation("",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            placeId)

    fun isConnectedToInternet(context: Context) =
        NetworkManager.getInstance().isConnectedToNetwork(context)

    suspend fun getShoppingList() =
        mApiInterface.getShoppingList(getSessionToken(), getDeviceIdentityToken())

    suspend fun createNewList(createList: CreateList) =
        mApiInterface.createNewList(getSessionToken(), getDeviceIdentityToken(), createList)

    suspend fun addProductsToList(
        productId: String,
        addToListRequest: List<AddToListRequest>,
    ): retrofit2.Response<ShoppingListItemsResponse> =
        mApiInterface.addProductsToList(
            getSessionToken(),
            getDeviceIdentityToken(),
            productId,
            addToListRequest
        )
}