package za.co.woolworths.financial.services.android.ui.fragments.product.detail.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import za.co.woolworths.financial.services.android.models.dto.AuxiliaryImage
import za.co.woolworths.financial.services.android.models.dto.ColourSKUsPrices
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.component.MatchingSetData
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.component.MatchingSetDetails
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.service.MatchingSetRepository
import javax.inject.Inject

/**
 * Created by Kunal Uttarwar on 06/02/24.
 */

@HiltViewModel
class MatchingSetViewModel @Inject constructor(private val matchingSetRepository: MatchingSetRepository) :
    ViewModel() {

    val matchingSetData = mutableStateOf(MatchingSetData(arrayListOf()))
    private var _seeMoreClicked = MutableStateFlow(false)
    val seeMoreClicked = _seeMoreClicked.asStateFlow()

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
                    if (mainAuxImgList.size > 1) {
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
                                            colorSku.value.priceMin.toString(),
                                            relatedProducts.productName
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
                                colorSku.value.priceMin.toString(),
                                relatedProducts.productName
                            )
                            matchingSetDetailsList.add(matchingSetDetails)
                        }
                    }
                }
            }
            setMatchingSetDetails(matchingSetDetailsList)
        }
    }

    private fun getAuxiliaryImageList(auxiliaryImages: JsonElement): Map<String, AuxiliaryImage> {
        return Gson().fromJson(
            auxiliaryImages,
            object : TypeToken<Map<String, AuxiliaryImage>>() {}.type
        )
    }

    private fun getColorSKUPrices(colorSku: JsonElement): Map<String, ColourSKUsPrices> {
        return Gson().fromJson(
            colorSku,
            object : TypeToken<Map<String, ColourSKUsPrices>>() {}.type
        )
    }

    private fun setMatchingSetDetails(matchingSetDetailsList: ArrayList<MatchingSetDetails>) {
        matchingSetData.value =
            matchingSetData.value.copy(matchingSetDetails = matchingSetDetailsList)
    }

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

    fun updateSeeMoreValue(value: Boolean){
        _seeMoreClicked.value = value
    }
}