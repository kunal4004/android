package za.co.woolworths.financial.services.android.ui.fragments.colorandsize

import android.text.TextUtils
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.ColourSizeVariants
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.CONST_NO_SIZE
import za.co.woolworths.financial.services.android.util.Utils

class ColorAndSizeViewModel(
    private val savedStateHandle: SavedStateHandle?
) : ViewModel() {

    companion object {
        private const val ARG_PRODUCT_ITEM = "productItem"
    }

    private val _uiColorState = MutableStateFlow<UiState>(UiState.Loading)
    val uiColorState: StateFlow<UiState> = _uiColorState.stateIn(
        scope = viewModelScope,
        initialValue = UiState.Loading,
        started = SharingStarted.WhileSubscribed(5000L)
    )

    private val _uiSizeState = MutableStateFlow<UiState>(UiState.Loading)
    val uiSizeState: StateFlow<UiState> = _uiSizeState.stateIn(
        scope = viewModelScope,
        initialValue = UiState.Loading,
        started = SharingStarted.WhileSubscribed(5000L)
    )
    private var otherSKUsByGroupKey: LinkedHashMap<String, ArrayList<OtherSkus>> = linkedMapOf()

    init {
        viewModelScope.launch {
            setProductItem()
            productItem?.apply {
                selectedSku = getDefaultSku(otherSkus, sku)
            }
            val (hasColor, hasSize) = getColorAndSizeAvailability()
            getColorsList().collect {
                _uiColorState.value = UiState.Success(
                    isAvailable = hasColor && it.isNotEmpty(),
                    data = it
                )
            }
            getSizeList(selectedSku?.colour ?: "").collect {
                _uiSizeState.value = UiState.Success(
                    isAvailable = (hasSize && it.isNotEmpty()),
                    sizeGuideId = productItem?.sizeGuideId,
                    data = it
                )
            }
        }
    }

    var productItem: ProductDetails? = null

    var selectedSku: OtherSkus? = null

    fun getColorAndSizeAvailability(): Pair<Boolean, Boolean> {
        return when (ColourSizeVariants.find(productItem?.colourSizeVariants ?: "")) {
            ColourSizeVariants.DEFAULT, ColourSizeVariants.NO_VARIANT -> {
                Pair(false, false)
            }

            ColourSizeVariants.COLOUR_VARIANT -> {
                Pair(true, false)
            }

            ColourSizeVariants.SIZE_VARIANT, ColourSizeVariants.COLOUR_SIZE_VARIANT -> {
                Pair(true, true)
            }

            ColourSizeVariants.NO_COLOUR_SIZE_VARIANT -> {
                Pair(false, true)
            }

            else -> {
                Pair(false, false)
            }
        }
    }

    fun getProductStoreId(): String {
        return Utils.retrieveStoreId(productItem?.fulfillmentType)
            ?: ""
    }

    fun getOtherSkuByGroupKey(): LinkedHashMap<String, ArrayList<OtherSkus>> {
        otherSKUsByGroupKey = groupOtherSKUsByColor(productItem?.otherSkus)
        return otherSKUsByGroupKey
    }

    private fun groupOtherSKUsByColor(otherSKUsList: ArrayList<OtherSkus>?): LinkedHashMap<String, ArrayList<OtherSkus>> {
        if (otherSKUsList != null) {
            val variant = ColourSizeVariants.find(productItem?.colourSizeVariants ?: "")
            for (otherSkuObj in otherSKUsList) {
                var groupKey =
                    if (TextUtils.isEmpty(otherSkuObj.colour) && !TextUtils.isEmpty(otherSkuObj.size)) {
                        otherSkuObj.size?.trim()
                    } else if (!TextUtils.isEmpty(otherSkuObj.colour) && !TextUtils.isEmpty(
                            otherSkuObj.size
                        )
                    ) {
                        otherSkuObj.colour?.trim()
                    } else {
                        otherSkuObj.colour?.trim()
                    }

                if (variant == ColourSizeVariants.NO_COLOUR_SIZE_VARIANT) {
                    otherSkuObj.apply { size = colour }
                    groupKey = "N/A"
                }

                if (!otherSKUsByGroupKey.containsKey(groupKey) && !groupKey.isNullOrEmpty()) {
                    this.otherSKUsByGroupKey[groupKey] = ArrayList()
                }
                if (!otherSKUsByGroupKey[groupKey]!!.any { it.sku == otherSkuObj.sku }) this.otherSKUsByGroupKey[groupKey]!!.add(
                    otherSkuObj
                )
            }
        }
        return otherSKUsByGroupKey
    }

    private fun getColorsList(): Flow<List<OtherSkus>> = flow {
        if (productItem == null) {
            setProductItem()
        }
        val userList = productItem?.otherSkus?.distinctBy { it.colour } ?: emptyList()
        emit(userList)
    }.flowOn(Dispatchers.IO)

    private fun getSizeList(colour: String?): Flow<List<OtherSkus>> = flow {
        if (productItem == null) {
            setProductItem()
        }
        val (hasColor, hasSize) = getColorAndSizeAvailability()
        // If NoColorSizeVariant is the variant only distinct by size
        val isNoColorSizeVariant = !hasColor && hasSize

        var list: List<OtherSkus> = emptyList()
        productItem?.otherSkus?.let { skuList ->
            list = if (isNoColorSizeVariant) {
                skuList.map { it.size = it.colour }
                skuList.distinctBy {
                    it.size
                }
            } else {
                skuList.filter {
                    it.colour.equals(colour, ignoreCase = false)
                            && !CONST_NO_SIZE.equals(it.size, ignoreCase = true)
                }.distinctBy {
                    it.size
                }
            }
        }
        emit(list)
    }.flowOn(Dispatchers.IO)

    private fun setProductItem() {
        val string: String? = savedStateHandle?.get(ARG_PRODUCT_ITEM)
        productItem = Gson().fromJson(string, ProductDetails::class.java)
    }

    private fun getDefaultSku(otherSku: List<OtherSkus>?, sku: String?): OtherSkus? {
        return otherSku?.first { it.sku == sku }
    }

    fun updateSizesOnColorSelection(selectedColorSku: OtherSkus?) {
        viewModelScope.launch {
            selectedSku = selectedColorSku
            selectedSku ?: return@launch

            getSizeList(selectedSku?.colour).collect { sizeList ->
                _uiSizeState.update { currentState ->
                    when (currentState) {
                        UiState.Loading -> currentState
                        is UiState.Success -> {
                            UiState.Success(
                                currentState.isAvailable,
                                defaultSelection = true,
                                sizeGuideId = currentState.sizeGuideId,
                                data = sizeList
                            )
                        }
                    }
                }
            }
        }
    }
}

sealed interface UiState {
    object Loading : UiState
    data class Success(
        val isAvailable: Boolean,
        val defaultSelection: Boolean = false,
        val sizeGuideId: String? = null,
        val data: List<OtherSkus>
    ) : UiState
}