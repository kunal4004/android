package za.co.woolworths.financial.services.android.ui.fragments.product.detail.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import za.co.woolworths.financial.services.android.models.dto.AuxiliaryImage
import za.co.woolworths.financial.services.android.models.dto.ColourSKUsPrices
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse
import za.co.woolworths.financial.services.android.models.dto.ProductRequest
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.mapNetworkCallToViewStateFlow
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.component.MatchingSetData
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.component.MatchingSetDetails
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.service.MatchingSetRepository
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import javax.inject.Inject

/**
 * Created by Kunal Uttarwar on 06/02/24.
 */

@HiltViewModel
class MatchingSetViewModel @Inject constructor(private val matchingSetRepository: MatchingSetRepository) :
    ViewModel() {
    private val noIfProductsToShow = 2
    private val noIfColorForMainProduct = 1
    val matchingSetData = mutableStateOf(MatchingSetData(arrayListOf(), noIfProductsToShow))
    private var _seeMoreClicked = MutableStateFlow(false)
    val seeMoreClicked = _seeMoreClicked.asStateFlow()

    private val _productDetailsForMatchingItem =
        MutableSharedFlow<ViewState<ProductDetailResponse>>(0)
    val productDetailsForMatchingItem: SharedFlow<ViewState<ProductDetailResponse>> =
        _productDetailsForMatchingItem

    private val _inventoryForMatchingItemDetails =
        MutableSharedFlow<ViewState<SkusInventoryForStoreResponse>>(0)
    val inventoryForMatchingItemDetails: SharedFlow<ViewState<SkusInventoryForStoreResponse>> =
        _inventoryForMatchingItemDetails

    private val _addToCartResponseForMatchingItemDetails =
        MutableSharedFlow<ViewState<AddItemToCartResponse>>(0)
    val addToCartResponseForMatchingItemDetails: SharedFlow<ViewState<AddItemToCartResponse>> =
        _addToCartResponseForMatchingItemDetails

    private var productDetails: ProductDetailResponse? = null

    /**
     * Sets the matching set data used for showing related products.
     * @param productDetails product details of the main product. we get it from product details API.
     * @param selectedGroupKey Selected Color. Default color will be 1st color in the list.
     */
    fun setMatchingSetData(
        productDetails: ProductDetails,
        selectedGroupKey: String?
    ) {
        if (productDetails.relatedProducts.isNullOrEmpty()) {
            matchingSetData.value = matchingSetData.value.copy()
        } else {
            updateSeeMoreValue(false) // Make it default value.
            val matchingSetDetailsList = arrayListOf<MatchingSetDetails>()
            val mainAuxImgList = getAuxiliaryImageList(productDetails.auxiliaryImages)
            for (relatedProducts in productDetails.relatedProducts) {
                val relatedProductAuxImage = relatedProducts.auxiliaryImages?.let {
                    getAuxiliaryImageList(it)
                }
                relatedProductAuxImage?.forEach AuxImg@{
                    if (mainAuxImgList.size > noIfColorForMainProduct) {
                        // If primary product has multiple colors.
                        getImageCodeForAuxiliaryImages(selectedGroupKey).forEach ImgCode@{ selectedColorCode ->
                            if (it.key.contains(selectedColorCode, true)) {
                                val colorSkusList =
                                    relatedProducts.colourSKUsPrices?.let { it1 ->
                                        getColorSKUPrices(it1)
                                    }
                                colorSkusList?.entries?.forEach ColorSku@{ colorSku ->
                                    if (colorSku.key == it.value.styleId) {
                                        val matchingSetDetails = MatchingSetDetails(
                                            it.value.externalImageRefV2,
                                            it.value.styleId,
                                            selectedGroupKey ?: "",
                                            CurrencyFormatter.formatAmountToRandAndCentWithSpace(
                                                colorSku.value.priceMin
                                            ),
                                            relatedProducts.productName,
                                            relatedProducts.productId
                                        )
                                        matchingSetDetailsList.add(matchingSetDetails)
                                    }
                                }
                                return@ImgCode
                            }
                        }
                    } else {
                        // If main product has single color then show all matching sets.
                        val colorSkusList = relatedProducts.colourSKUsPrices?.let { it1 ->
                            getColorSKUPrices(it1)
                        }
                        colorSkusList?.entries?.forEach { colorSku ->
                            val matchingSetDetails = MatchingSetDetails(
                                it.value.externalImageRefV2,
                                it.value.styleId,
                                selectedGroupKey ?: "",
                                CurrencyFormatter.formatAmountToRandAndCentWithSpace(colorSku.value.priceMin.toString()),
                                relatedProducts.productName,
                                relatedProducts.productId
                            )
                            matchingSetDetailsList.add(matchingSetDetails)
                        }
                    }
                }
            }
            setMatchingSetDetails(matchingSetDetailsList)
        }
    }

    /**
     * get the Auxiliary Image list from the Json Element.
     * @param auxiliaryImages Json Element of Aux Images.
     */
    private fun getAuxiliaryImageList(auxiliaryImages: JsonElement): Map<String, AuxiliaryImage> {
        return Gson().fromJson(
            auxiliaryImages,
            object : TypeToken<Map<String, AuxiliaryImage>>() {}.type
        )
    }

    /**
     * Get the list of colors from the Json Element.
     * @param colorSku Json Element of colors.
     */
    private fun getColorSKUPrices(colorSku: JsonElement): Map<String, ColourSKUsPrices> {
        return Gson().fromJson(
            colorSku,
            object : TypeToken<Map<String, ColourSKUsPrices>>() {}.type
        )
    }

    /**
     * Sets the matching set data used for showing related products.
     * @param matchingSetDetailsList List of matching set Products.
     */
    private fun setMatchingSetDetails(matchingSetDetailsList: ArrayList<MatchingSetDetails>) {
        matchingSetData.value =
            matchingSetData.value.copy(matchingSetDetails = matchingSetDetailsList)
    }

    /**
     * Get the color code from selected Color
     * @param groupKey selected Color
     */
    private fun getImageCodeForAuxiliaryImages(groupKey: String?): ArrayList<String> {
        var imageCode = ""
        val imageCodesList = arrayListOf<String>()
        groupKey?.split("\\s".toRegex())?.let {
            when (it.size) {
                1 -> imageCodesList.add(it[0])
                else -> {
                    it.forEachIndexed { i, s ->
                        imageCode = if (i == 0) s[0].toString() else imageCode.plus(s)
                    }
                    imageCodesList.add(imageCode)
                    imageCodesList.add(it.joinToString(""))
                }
            }
        }
        return imageCodesList
    }

    /**
     * Get the Bool value for see more button.
     * @param value True means See More button is clicked. False means See Less Button is clicked.
     */

    fun updateSeeMoreValue(value: Boolean) {
        _seeMoreClicked.value = value
    }

    suspend fun callProductDetailAPI(productRequest: ProductRequest) {
        viewModelScope.launch {
            mapNetworkCallToViewStateFlow {
                matchingSetRepository.getMatchingItemDetail(productRequest)
            }.collectLatest {
                _productDetailsForMatchingItem.emit(it)
            }
        }
    }

    fun callProductDetailsInventoryAPi(storeId: String, multipleSku: String) {
        viewModelScope.launch {
            mapNetworkCallToViewStateFlow {
                matchingSetRepository.getInventoryForMatchingItems(storeId, multipleSku)
            }.collectLatest {
                _inventoryForMatchingItemDetails.emit(it)
            }
        }
    }


    fun callAddToCartForMatchingSets(addToCart: MutableList<AddItemToCart>) {
        viewModelScope.launch {
            mapNetworkCallToViewStateFlow {
                matchingSetRepository.addToCartForMatchingItems(addToCart)
            }.collectLatest {
                _addToCartResponseForMatchingItemDetails.emit(it)
            }
        }
    }

    fun setProductDetails(productDetailResponse: ProductDetailResponse) {
        productDetails = productDetailResponse
    }
    fun getProductDetails() = productDetails
}