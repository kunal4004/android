package za.co.woolworths.financial.services.android.repository.shop

import android.util.Log
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Resource
import java.io.IOException

class MainShopRepository: ShopRepository {

    override suspend fun fetchDashCategories(): Resource<RootCategories> {
        return try {
            val response = OneAppService.getDashCategory()
            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.success(it)
                } ?: Resource.error("An unknown error occured", null)
            } else {
                Resource.error("An unknown error occured", null)
            }
        } catch (e: IOException) {
            Log.e("EXCEPTION", "EXCEPTION:", e)
            Resource.error("Couldn't reach the server. Check your internet connection", null)
        }
    }
}