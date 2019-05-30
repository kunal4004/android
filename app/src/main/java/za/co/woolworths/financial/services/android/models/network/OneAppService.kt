package za.co.woolworths.financial.services.android.models.network

import com.awfs.coordination.BuildConfig
import retrofit2.Call
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.util.Utils

object OneAppService : RetrofitService() {
    fun getConfig(): Call<ConfigResponse> = mApiInterface.getConfig(
            WoolworthsApplication.getApiId(),
            BuildConfig.SHA1,
            getDeviceManufacturer(),
            getDeviceModel(),
            getNetworkCarrier(),
            getOS(),
            getOsVersion(),
            getSessionToken(),
            WoolworthsApplication.getAppVersionName())

    fun getRootCategory(): Call<RootCategories> = mApiInterface.getRootCategories(
            getOsVersion(),
            getApiId(),
            getOS(),
            getSha1Password(),
            getDeviceModel(),
            getNetworkCarrier(),
            getDeviceManufacturer(),
            "Android",
            getSessionToken())

    fun getShoppingLists(): Call<ShoppingListsResponse> = mApiInterface.getShoppingLists(
            getApiId(),
            getSha1Password(),
            getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(),
            getOS(), getOsVersion(),
            getSessionToken())

    fun getOrders(): Call<OrdersResponse> = mApiInterface.getOrders(
            getApiId(),
            getSha1Password(),
            getDeviceManufacturer(),
            getDeviceModel(),
            getNetworkCarrier(),
            getOS(),
            getOsVersion(),
            getSessionToken())


    fun getAccounts(): Call<AccountsResponse> = mApiInterface.getAccounts(
            getApiId(),
            getSha1Password(),
            getDeviceManufacturer(),
            getDeviceModel(),
            getNetworkCarrier(),
            getOS(),
            getOsVersion(),
            "",
            "",
            getSessionToken())

    fun getSubCategory(category_id: String): Call<SubCategories> {
        return mApiInterface.getSubCategory(
                getOsVersion(),
                getApiId(),
                getOS(),
                getSha1Password(),
                getDeviceModel(),
                getNetworkCarrier(),
                getDeviceManufacturer(),
                "Android",
                getSessionToken(),
                category_id)
    }


    fun getProducts(requestParams: ProductsRequestParams): Call<ProductView> {
        val loc = getMyLocation()!!
        return if (Utils.isLocationEnabled(appContext())) mApiInterface.getProducts(
                getOsVersion(),
                getDeviceModel(),
                getDeviceManufacturer(),
                getOS(),
                getNetworkCarrier(),
                getApiId(), "",
                "", getSha1Password(),
                loc.latitude,
                loc.longitude,
                getSessionToken(),
                requestParams.searchTerm,
                requestParams.searchType.value,
                requestParams.responseType.value,
                requestParams.pageOffset,
                Utils.PAGE_SIZE,
                requestParams.sortOption,
                requestParams.refinement)
        else mApiInterface.getProductsWithoutLocation(
                getOsVersion(),
                getDeviceModel(),
                getDeviceManufacturer(),
                getOS(),
                getNetworkCarrier(),
                getApiId(),
                "",
                "",
                getSha1Password(),
                getSessionToken(),
                requestParams.searchTerm,
                requestParams.searchType.value,
                requestParams.responseType.value,
                requestParams.pageOffset,
                Utils.PAGE_SIZE,
                requestParams.sortOption,
                requestParams.refinement)
    }


}