package za.co.woolworths.financial.services.android.cart.service.repository

import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.JsonParseException
import org.json.JSONException
import org.json.JSONObject
import za.co.woolworths.financial.services.android.cart.service.network.CartItemGroup
import za.co.woolworths.financial.services.android.cart.service.network.CartResponse
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.models.dto.ChangeQuantity
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.CouponClaimCode
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.ProductType
import za.co.woolworths.financial.services.android.util.StoreUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.io.IOException
import javax.inject.Inject

/**
 * Created by Kunal Uttarwar on 24/01/23.
 */
class CartRepository @Inject constructor() {

    private var cartItemList = ArrayList<CommerceItem>()
    private var isMixedBasket = false
    private var isFBHOnly = false

    suspend fun getShoppingCartV2(): Resource<CartResponse> {
        return try {
            val response = OneAppService().getShoppingCartV2()
            if (response.isSuccessful) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(convertResponseToCartResponseObject(it))
                        else ->
                            Resource.error(R.string.error_unknown, convertResponseToCartResponseObject(it))
                    }
                } ?: Resource.error(R.string.error_unknown, null)
            } else {
                var errorResponse: CartResponse? = null
                try {
                    errorResponse = Gson().fromJson(
                        response.errorBody()?.charStream(),
                        CartResponse::class.java
                    )
                } catch (jsonException: JsonParseException) {
                    FirebaseManager.logException(jsonException)
                }
                Resource.error(R.string.error_unknown, errorResponse)
            }
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_internet_connection, null)
        }
    }

    suspend fun getSavedAddress(): Resource<SavedAddressResponse> {
        return try {
            val response = OneAppService().getSavedAddress()
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

    suspend fun removeCartItem(commerceId: String): Resource<CartResponse> {
        return try {
            val response = OneAppService().removeSingleCartItem(commerceId)
            if (response.isSuccessful) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(convertResponseToCartResponseObject(it))
                        else ->
                            Resource.error(R.string.error_unknown, convertResponseToCartResponseObject(it))
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

    suspend fun removeAllCartItems(): Resource<CartResponse> {
        return try {
            val response = OneAppService().removeAllCartItems()
            if (response.isSuccessful) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(convertResponseToCartResponseObject(it))
                        else ->
                            Resource.error(R.string.error_unknown, convertResponseToCartResponseObject(it))
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

    suspend fun changeProductQuantityRequest(changeQuantity: ChangeQuantity?): Resource<CartResponse> {
        return try {
            val response = OneAppService().changeProductQuantityRequest(changeQuantity)
            if (response.isSuccessful) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(convertResponseToCartResponseObject(it))
                        else ->
                            Resource.error(R.string.error_unknown, convertResponseToCartResponseObject(it))
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

    suspend fun onRemovePromoCode(couponClaimCode: CouponClaimCode): Resource<CartResponse> {
        return try {
            val response = OneAppService().removePromoCode(couponClaimCode)
            if (response.isSuccessful) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(convertResponseToCartResponseObject(it))
                        else ->
                            Resource.error(R.string.error_unknown, convertResponseToCartResponseObject(it))
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

    suspend fun getInventorySkuForInventory(store_id: String, multipleSku: String, isUserBrowsing: Boolean): Resource<SkusInventoryForStoreResponse> {
        return try {
            val response = OneAppService().fetchInventorySkuForStore(store_id, multipleSku, isUserBrowsing)
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
        } catch (exception: IOException) {
            FirebaseManager.logException(exception)
            val response = SkusInventoryForStoreResponse()
            response.exception = exception
            Resource.error(R.string.error_internet_connection, response)
        }
    }

    fun getCartItemList(): ArrayList<CommerceItem> {
        return cartItemList
    }

    fun isMixedBasket(): Boolean {
        return isMixedBasket
    }

    fun isFBHOnly(): Boolean{
        return isFBHOnly
    }

    fun convertResponseToCartResponseObject(response: ShoppingCartResponse?): CartResponse? {
        if (response == null) return null
        val cartResponse: CartResponse?
        try {
            cartResponse =
                CartResponse()
            cartResponse.httpCode = response.httpCode
            val data = response.data[0]
            cartResponse.orderSummary = data.orderSummary
            cartResponse.voucherDetails = data.voucherDetails
            cartResponse.productCountMap = data.productCountMap // set delivery location
            cartResponse.liquorOrder = data.liquorOrder
            cartResponse.blackCardHolder = data.blackCardHolder
            cartResponse.noLiquorImageUrl = data.noLiquorImageUrl
            cartResponse.globalMessages = data.globalMessages
            cartResponse.jSessionId = data.jSessionId
            cartResponse.response = response.response
            val fulfillmentDetailsObj = cartResponse.orderSummary.fulfillmentDetails
            if (fulfillmentDetailsObj?.address?.placeId != null) {
                val shoppingDeliveryLocation = ShoppingDeliveryLocation(fulfillmentDetailsObj)
                Utils.savePreferredDeliveryLocation(shoppingDeliveryLocation)
            }
            val itemsObject = JSONObject(Gson().toJson(data.items))
            isMixedBasket =
                itemsObject.has(ProductType.FOOD_COMMERCE_ITEM.value) && itemsObject.length() > 1
            val keys = itemsObject.keys()
            val cartItemGroups = ArrayList<CartItemGroup>()
            while ((keys.hasNext())) {
                val cartItemGroup =
                    CartItemGroup()
                val key = keys.next()
                //GENERAL - "default",HOME - "homeCommerceItem",FOOD
                // - "foodCommerceItem",CLOTHING
                // - "clothingCommerceItem",PREMIUM BRANDS
                // - connectCommerceItem,
                // - "premiumBrandCommerceItem",
                // Anything else: OTHER
                when {
                    key.contains(ProductType.DEFAULT.value) ->
                        cartItemGroup.setType(ProductType.DEFAULT.shortHeader)

                    key.contains(ProductType.GIFT_COMMERCE_ITEM.value) ->
                        cartItemGroup.setType(ProductType.GIFT_COMMERCE_ITEM.shortHeader)

                    key.contains(ProductType.HOME_COMMERCE_ITEM.value) ->
                        cartItemGroup.setType(ProductType.HOME_COMMERCE_ITEM.shortHeader)

                    key.contains(ProductType.FOOD_COMMERCE_ITEM.value) ->
                        cartItemGroup.setType(ProductType.FOOD_COMMERCE_ITEM.shortHeader)

                    key.contains(ProductType.CLOTHING_COMMERCE_ITEM.value) ->
                        cartItemGroup.setType(ProductType.CLOTHING_COMMERCE_ITEM.shortHeader)

                    key.contains(ProductType.PREMIUM_BRAND_COMMERCE_ITEM.value) ->
                        cartItemGroup.setType(ProductType.PREMIUM_BRAND_COMMERCE_ITEM.shortHeader)

                    key.contains(ProductType.CONNECT_COMMERCE_ITEM.value) ->
                        cartItemGroup.setType(ProductType.CONNECT_COMMERCE_ITEM.shortHeader)

                    else -> cartItemGroup.setType(ProductType.OTHER_ITEMS.shortHeader)
                }
                val productsArray = itemsObject.getJSONArray(key)
                if (productsArray.length() > 0) {
                    val productList = ArrayList<CommerceItem>()
                    for (i in 0 until productsArray.length()) {
                        val commerceItemObject = productsArray.getJSONObject(i)
                        val commerceItem =
                            Gson().fromJson(commerceItemObject.toString(), CommerceItem::class.java)
                        val fulfillmentStoreId = Utils.retrieveStoreId(commerceItem.fulfillmentType)
                        commerceItem.fulfillmentStoreId =
                            fulfillmentStoreId!!.replace("\"".toRegex(), "")
                        productList.add(commerceItem)
                        isFBHOnly = if (!itemsObject.has(ProductType.FOOD_COMMERCE_ITEM.value)) {
                            commerceItem.fulfillmentType == StoreUtils.Companion.FulfillmentType.CLOTHING_ITEMS?.type || commerceItem.fulfillmentType == StoreUtils.Companion.FulfillmentType.CRG_ITEMS?.type
                        } else false
                    }
                    this.cartItemList = productList
                    cartItemGroup.setCommerceItems(productList)
                }
                cartItemGroups.add(cartItemGroup)
            }
            var giftCartItemGroup =
                CartItemGroup()
            giftCartItemGroup.type = GIFT_ITEM
            val generalCartItemGroup =
                CartItemGroup()
            generalCartItemGroup.type = GENERAL_ITEM
            val connectCartItemGroup =
                CartItemGroup()
            connectCartItemGroup.type = CONNECT_ITEM
            var generalIndex = -1
            var connectIndex = -1
            if (cartItemGroups.contains(connectCartItemGroup) && cartItemGroups.contains(
                    giftCartItemGroup
                ) && cartItemGroups.contains(
                    generalCartItemGroup
                )
            ) {
                for (cartGroupIndex in cartItemGroups.indices) {
                    val cartItemGroup = cartItemGroups[cartGroupIndex]
                    if (cartItemGroup.type.equals(CONNECT_ITEM, ignoreCase = true)) {
                        connectIndex = cartGroupIndex
                    }
                    if (cartItemGroup.type.equals(GIFT_ITEM, ignoreCase = true)) {
                        giftCartItemGroup = cartItemGroup
                        cartItemGroups.removeAt(cartGroupIndex)
                    }
                }
                cartItemGroups.add(connectIndex + 1, giftCartItemGroup)
            }
            else if (cartItemGroups.contains(giftCartItemGroup) && cartItemGroups.contains(
                    generalCartItemGroup
                )
            ) {
                for (cartGroupIndex in cartItemGroups.indices) {
                    val cartItemGroup = cartItemGroups[cartGroupIndex]
                    if (cartItemGroup.type.equals(GENERAL_ITEM, ignoreCase = true)) {
                        generalIndex = cartGroupIndex
                    }
                    if (cartItemGroup.type.equals(GIFT_ITEM, ignoreCase = true)) {
                        giftCartItemGroup = cartItemGroup
                        cartItemGroups.removeAt(cartGroupIndex)
                    }
                }
                cartItemGroups.add(generalIndex + 1, giftCartItemGroup)
            }
            cartResponse.cartItems = cartItemGroups
        } catch (e: JSONException) {
            FirebaseManager.logException(e)
            return null
        }
        return cartResponse
    }

    companion object {
        private const val GENERAL_ITEM = "GENERAL"
        private const val GIFT_ITEM = "GIFT"
        private const val CONNECT_ITEM = "WCONNECT"
    }
}