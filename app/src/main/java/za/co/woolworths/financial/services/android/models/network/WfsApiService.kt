package za.co.woolworths.financial.services.android.models.network

import retrofit2.Call
import retrofit2.http.*
import za.co.woolworths.financial.services.android.models.dto.*

interface WfsApiService {

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600")
    @GET("mobileconfigs")
    fun getConfig(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("appVersion") appVersion: String
    ): Call<ConfigResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("categories")
    fun getRootCategories(
            @Header("osVersion") osVersion: String,
            @Header("apiId") apiId: String,
            @Header("os") os: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("apiKey") userAgent: String,
            @Header("sessionToken") sessionToken: String
    ): Call<RootCategories>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("list")
    fun getShoppingLists(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String): Call<ShoppingListsResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("order")
    fun getOrders(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String): Call<OrdersResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:28800")
    @GET("user/accounts")
    fun getAccounts(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String): Call<AccountsResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("categories/{cat}")
    fun getSubCategory(
            @Header("osVersion") osVersion: String,
            @Header("apiId") apiId: String,
            @Header("os") os: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("apiKey") apiKey: String,
            @Header("sessionToken") sessionToken: String,
            @Path("cat") category: String): Call<SubCategories>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("searchSortAndFilter")
    fun getProducts(
            @Header("osVersion") osVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("os") os: String,
            @Header("network") network: String,
            @Header("apiId") apiId: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sha1Password") sha1Password: String,
            @Header("longitude") longitude: Double,
            @Header("latitude") latitude: Double,
            @Header("sessionToken") sessionToken: String,
            @Query("searchTerm") searchTerm: String,
            @Query("searchType") searchType: String,
            @Query("responseType") responseType: String,
            @Query("pageOffset") pageOffset: Int,
            @Query("pageSize") pageSize: Int,
            @Query("sortOption") sortOption: String,
            @Query("refinement") refinement: String): Call<ProductView>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("/searchSortAndFilter")
    fun getProductsWithoutLocation(
            @Header("osVersion") osVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("os") os: String,
            @Header("network") network: String,
            @Header("apiId") apiId: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sha1Password") sha1Password: String,
            @Header("sessionToken") sessionToken: String,
            @Query("searchTerm") searchTerm: String,
            @Query("searchType") searchType: String,
            @Query("responseType") responseType: String,
            @Query("pageOffset") pageOffset: Int,
            @Query("pageSize") pageSize: Int,
            @Query("sortOption") sortOption: String,
            @Query("refinement") refinement: String): Call<ProductView>
}