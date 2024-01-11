package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems

import com.awfs.coordination.R
import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.ApiInterface
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.shoppinglist.model.RemoveItemApiRequest
import za.co.woolworths.financial.services.android.shoppinglist.service.network.CopyItemToListRequest
import za.co.woolworths.financial.services.android.shoppinglist.service.network.CopyListResponse
import za.co.woolworths.financial.services.android.shoppinglist.service.network.MoveItemApiRequest
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.io.IOException
import javax.inject.Inject

class MainShoppingListDetailRepository @Inject constructor(private val apiInterface: ApiInterface) :
    ShoppingListDetailRepository, CoreDataSource(), ApiInterface by apiInterface  {

    override suspend fun getShoppingListItems(listId: String): Resource<ShoppingListItemsResponse> {
        return try {
            val response = OneAppService().getShoppingListItems(listId)
            if (response.isSuccessful) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(it)
                        else ->
                            Resource.error(R.string.error_unknown, it)
                    }
                } ?: Resource.error(R.string.error_unknown, null)
            } else {
                Resource.error(R.string.error_unknown, null)
            }
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_internet_connection, null)
        }
    }

    override suspend fun getInventorySkuForStore(
        storeId: String,
        multiSku: String,
        isUserBrowsing: Boolean
    ): Resource<SkusInventoryForStoreResponse> {
        return try {
            val response =
                OneAppService().getInventorySkusForStore(storeId, multiSku, isUserBrowsing)
            if (response.isSuccessful) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(it)

                        else ->
                            Resource.error(R.string.error_unknown, it)
                    }
                } ?: Resource.error(R.string.error_unknown, null)
            } else {
                Resource.error(R.string.error_unknown, null)
            }
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_internet_connection, null)
        }
    }

    override suspend fun removeMultipleItemsFromList(
        listId: String,
        removeItemApiRequest: RemoveItemApiRequest
    ): Resource<ShoppingListItemsResponse>  {
        return try {
            val response =
                OneAppService().removeItemFromShoppingItemList(listId, removeItemApiRequest)
            if (response.isSuccessful) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(it)
                        else ->
                            Resource.error(R.string.error_unknown, it)
                    }
                } ?: Resource.error(R.string.error_unknown, null)
            } else {
                Resource.error(R.string.error_unknown, null)
            }
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_internet_connection, null)
        }
    }

    override suspend fun copyMultipleItemsFromList(
        copyItemApiRequest: CopyItemToListRequest
    ): Resource<CopyListResponse>  {
        return try {
            val response =
                OneAppService().copyItemFromList(copyItemApiRequest = copyItemApiRequest)
            if (response.isSuccessful) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(it)
                        else ->
                            Resource.error(R.string.error_unknown, it)
                    }
                } ?: Resource.error(R.string.error_unknown, null)
            } else {
                Resource.error(R.string.error_unknown, null)
            }
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_internet_connection, null)
        }
    }
    override suspend fun moveMultipleItemsFromList(moveItemApiRequest: MoveItemApiRequest): Flow<CoreDataSource.IOTaskResult<CopyListResponse>> =
        executeSafeNetworkApiCall {
            moveItemFromList(
                getDeviceIdentityToken(),
                moveItemApiRequest
            )
     }
}