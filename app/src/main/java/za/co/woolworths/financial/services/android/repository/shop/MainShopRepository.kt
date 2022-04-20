package za.co.woolworths.financial.services.android.repository.shop

import android.location.Location
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.coroutines.delay
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.shop.DashCategories
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.KotlinUtils
import java.io.IOException

class MainShopRepository : ShopRepository {

    override suspend fun fetchDashCategories(): Resource<DashCategories> {
        return try {
            delay(5000L)
            val responseString = KotlinUtils.getJSONFileFromRAWResFolder(
                context = WoolworthsApplication.getAppContext(),
                R.raw.dash_navigation
            )
            val response = Gson().fromJson(responseString.toString(), DashCategories::class.java)
            return Resource.success(response)

            /*val response = OneAppService.getDashCategory()
            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.success(it)
                } ?: Resource.error("An unknown error occured", null)
            } else {
                Resource.error("An unknown error occured", null)
            }*/
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error("Couldn't reach the server. Check your internet connection", null)
        }
    }

    override suspend fun fetchOnDemandCategories(location: Location?): Resource<RootCategories> {
        return try {

            val response = OneAppService.getDashCategoryNavigation(location)
            if (response.isSuccessful) {
                response.body()?.let {

                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(it)
                        else ->
                            Resource.error("An unknown error occurred", it)
                    }
                } ?: Resource.error("An unknown error occurred", null)
            } else {
                Resource.error("An unknown error occurred", null)
            }
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error("Couldn't reach the server. Check your internet connection", null)
        }
    }
}