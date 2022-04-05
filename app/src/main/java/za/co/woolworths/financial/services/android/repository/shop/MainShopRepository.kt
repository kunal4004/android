package za.co.woolworths.financial.services.android.repository.shop

import android.util.Log
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.coroutines.delay
import retrofit2.Response
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.shop.DashCategories
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.KotlinUtils
import java.io.IOException

class MainShopRepository : ShopRepository {

    override suspend fun fetchDashCategories(): Resource<DashCategories> {
        return try {
            delay(5000L)
            val responseString = KotlinUtils.getJSONFileFromRAWResFolder(context = WoolworthsApplication.getAppContext(), R.raw.dash_navigation)
            val response =  Gson().fromJson(responseString.toString(), DashCategories::class.java)
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
            Log.e("EXCEPTION", "EXCEPTION:", e)
            FirebaseManager.logException(e)
            Resource.error("Couldn't reach the server. Check your internet connection", null)
        }
    }

    override suspend fun fetchOnDemandCategories(): Resource<RootCategories> {
        return try {
            delay(2000L)
            val responseString = "{\n" +
                    "\t\"onDemandCategories\": [{\n" +
                    "\t\t\t\"categoryName\": \"Meat, Poultry & Fish\",\n" +
                    "\t\t\t\"categoryId\": \"0\",\n" +
                    "\t\t\t\"hasChildren\": false,\n" +
                    "\t\t\t\"dimValId\": \"N-r0vkur\",\n" +
                    "\t\t\t\"megaMenuSortPriority\": 18,\n" +
                    "\t\t\t\"navigationType\": \"onDemand\",\n" +
                    "\t\t\t\"liquorStoreIds\": [],\n" +
                    "\t\t\t\"imgUrl\": \"https://tinyurl.com/y23a7p9j\"\n" +
                    "\t\t},\n" +
                    "\t\t{\n" +
                    "\t\t\t\"categoryName\": \"Wine & Bubbles\",\n" +
                    "\t\t\t\"categoryId\": \"33\",  \n" +
                    "\t\t\t\"hasChildren\": false,\n" +
                    "\t\t\t\"dimValId\": \"N-r0vkur\",\n" +
                    "\t\t\t\"megaMenuSortPriority\": 20,\n" +
                    "\t\t\t\"navigationType\": \"onDemand\",\n" +
                    "\t\t\t\"liquorStoreIds\": [],\n" +
                    "\t\t\t\"imgUrl\": \"https://tinyurl.com/yckw854h\"\n" +
                    "\t\t},\n" +
                    "\t\t{\n" +
                    "\t\t\t\"categoryName\": \"Food Cupboard\",\n" +
                    "\t\t\t\"categoryId\": \"33\",\n" +
                    "\t\t\t\"hasChildren\": false,\n" +
                    "\t\t\t\"dimValId\": \"N-r0vkur\",\n" +
                    "\t\t\t\"megaMenuSortPriority\": 20,\n" +
                    "\t\t\t\"navigationType\": \"onDemand\",\n" +
                    "\t\t\t\"liquorStoreIds\": [],\n" +
                    "\t\t\t\"imgUrl\": \"https://tinyurl.com/5dnwbjh8\"\n" +
                    "\t\t},\n" +
                    "\t\t{\n" +
                    "\t\t\t\"categoryName\": \"Bread Bakery & Desserts\",\n" +
                    "\t\t\t\"categoryId\": \"33\",\n" +
                    "\t\t\t\"hasChildren\": false,\n" +
                    "\t\t\t\"dimValId\": \"N-r0vkur\",\n" +
                    "\t\t\t\"megaMenuSortPriority\": 20,\n" +
                    "\t\t\t\"navigationType\": \"onDemand\",\n" +
                    "\t\t\t\"liquorStoreIds\": [],\n" +
                    "\t\t\t\"imgUrl\": \"https://tinyurl.com/3jtnwrcd\"\n" +
                    "\t\t},\n" +
                    "\t\t{\n" +
                    "\t\t\t\"categoryName\": \"Flowers & Plants\",\n" +
                    "\t\t\t\"categoryId\": \"25\",\n" +
                    "\t\t\t\"hasChildren\": false,\n" +
                    "\t\t\t\"dimValId\": \"N-r0vkur\",\n" +
                    "\t\t\t\"megaMenuSortPriority\": 19,\n" +
                    "\t\t\t\"navigationType\": \"onDemand\",\n" +
                    "\t\t\t\"liquorStoreIds\": [],\n" +
                    "\t\t\t\"imgUrl\": \"https://tinyurl.com/2p9xw7ku\"\n" +
                    "\t\t}\n" +
                    "\t],\n" +
                    "\t\"dash\": {\n" +
                    "\t\t\"imgUrl\": \"https://s3-eu-west-1.amazonaws.com/wfs-oneapp-images-qa/category-images/woolies-dash.png\",\n" +
                    "\t\t\"categoryName\": \"WOOLIES DASH\",\n" +
                    "\t\t\"categoryId\": \"WOOLIES DASH\",\n" +
                    "\t\t\"dashBreakoutLink\": \"https://play.google.com/store/apps/details?id=za.co.woolworths.dash\",\n" +
                    "\t\t\"bannerText\": \"Same Day Delivery. Download Our App.\"\n" +
                    "\t},\n" +
                    "\t\"response\": {\n" +
                    "\t\t\"code\": \"-1\",\n" +
                    "\t\t\"desc\": \"Success\",\n" +
                    "\t\t\"version\": 288994731\n" +
                    "\t},\n" +
                    "\t\"httpCode\": 200\n" +
                    "}"
            val result = Gson().fromJson(responseString, RootCategories::class.java)

            return Resource.success(result)

            /*val response = OneAppService.getDashCategory()
            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.success(it)
                } ?: Resource.error("An unknown error occured", null)
            } else {
                Resource.error("An unknown error occured", null)
            }*/
        } catch (e: IOException) {
            Log.e("EXCEPTION", "EXCEPTION:", e)
            FirebaseManager.logException(e)
            Resource.error("Couldn't reach the server. Check your internet connection", null)
        }
    }
}