package za.co.woolworths.financial.services.android.ui.fragments.product.detail.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.models.dto.AuxiliaryImage
import za.co.woolworths.financial.services.android.models.dto.ColourSKUsPrices
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.models.dto.RelatedProducts
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.component.MatchingSetData
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.component.MatchingSetDetails
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.service.MatchingSetRepository
import java.util.ArrayList
import javax.inject.Inject

/**
 * Created by Kunal Uttarwar on 06/02/24.
 */

@HiltViewModel
class MatchingSetViewModel @Inject constructor(private val matchingSetRepository: MatchingSetRepository) :
    ViewModel() {

    val matchingSetData = mutableStateOf(MatchingSetData(arrayListOf(), emptyList()))

    fun setMatchingSetData(
        productDetails: ProductDetails,
        selectedGroupKey: String?,
        selectedColor: String?
    ) {
        if (productDetails.relatedProducts.isNullOrEmpty()) {
            matchingSetData.value = matchingSetData.value.copy()
        } else {
            setRelatedProducts(productDetails.relatedProducts) // set Related product object as it is.
            if (getAuxiliaryImageList(productDetails.auxiliaryImages).size > 1) {
                // If primary product has multiple colors.
                for (relatedProducts in productDetails.relatedProducts) {
                    val matchingSetDetailsList = arrayListOf<MatchingSetDetails>()
                    val relatedProductAuxImage = relatedProducts.auxiliaryImages?.let {
                        getAuxiliaryImageList(it)
                    }
                    relatedProductAuxImage?.forEach {
                        if (selectedGroupKey?.let { selectedColor ->
                                it.key.contains(
                                    selectedColor,
                                    true
                                )
                            } == true) {

                            val colorSkusList = relatedProducts.colourSKUsPrices?.let { it1 ->
                                getColorSKUPrices(it1)
                            }
                            colorSkusList?.entries?.forEach { colorSku ->
                                if (colorSku.key.equals(it.value.styleId)) {
                                    val matchingSetDetails = MatchingSetDetails(
                                        it.value.externalImageRefV2,
                                        it.value.styleId,
                                        selectedColor ?: "",
                                        colorSku.value.priceMin.toString()
                                    )
                                    matchingSetDetailsList.add(matchingSetDetails)
                                }
                            }
                        }
                        setMatchingSetDetails(matchingSetDetailsList)
                    }
                }

            } else {
                // If main product has single color then show all matching sets.
                val matchingSetDetailsList = arrayListOf<MatchingSetDetails>()
                for (relatedProducts in productDetails.relatedProducts) {
                    val relatedProductAuxImage = relatedProducts.auxiliaryImages?.let {
                        getAuxiliaryImageList(it)
                    }
                    relatedProductAuxImage?.forEach {
                        val colorSkusList = relatedProducts.colourSKUsPrices?.let { it1 ->
                            getColorSKUPrices(it1)
                        }
                        colorSkusList?.entries?.forEach { colorSku ->
                            val matchingSetDetails = MatchingSetDetails(
                                it.value.externalImageRefV2,
                                it.value.styleId,
                                selectedColor ?: "",
                                colorSku.value.priceMin.toString()
                            )
                            matchingSetDetailsList.add(matchingSetDetails)
                        }
                    }
                }
                setMatchingSetDetails(matchingSetDetailsList)
            }
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

    private fun setRelatedProducts(relatedProducts: ArrayList<RelatedProducts>) {
        matchingSetData.value = matchingSetData.value.copy(relatedProducts = relatedProducts)
    }

    private fun setMatchingSetDetails(matchingSetDetailsList: ArrayList<MatchingSetDetails>) {
        matchingSetData.value =
            matchingSetData.value.copy(matchingSetDetails = matchingSetDetailsList)
    }

    /*private fun getImageUrlList(
        productDetails: ProductDetails,
        selectedGroupKey: String?
    ): ArrayList<String> {
        val auxiliaryImagesForGroupKey = ArrayList<String>()
        val allAuxImages = Gson().fromJson<Map<String, AuxiliaryImage>>(
            productDetails?.auxiliaryImages,
            object : TypeToken<Map<String, AuxiliaryImage>>() {}.type
        )
        val relatedProductAuxImages = Gson().fromJson<Map<String, AuxiliaryImage>>(
            productDetails?.relatedProducts?.get(0)?.auxiliaryImages,
            object : TypeToken<Map<String, AuxiliaryImage>>() {}.type
        )

        getImageCodeForAuxiliaryImages(selectedGroupKey).forEach { imageCode ->
            if (allAuxImages.size == 1) {
                // If main product has single color then add all matching sets to show.
                relatedProductAuxImages.entries.forEach { entry ->
                    auxiliaryImagesForGroupKey.add(entry.value.externalImageRefV2)
                }
            } else {
                relatedProductAuxImages.entries.forEach { entry ->
                    if (entry.key.contains(imageCode, true)) {
                        auxiliaryImagesForGroupKey.add(entry.value.externalImageRefV2)
                    }
                }
            }
        }
        return auxiliaryImagesForGroupKey
    }*/

    /*private fun getImageCodeForAuxiliaryImages(groupKey: String?): ArrayList<String> {
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
    }*/

}