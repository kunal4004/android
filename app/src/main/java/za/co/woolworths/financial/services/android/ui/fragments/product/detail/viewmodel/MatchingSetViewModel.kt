package za.co.woolworths.financial.services.android.ui.fragments.product.detail.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse
import za.co.woolworths.financial.services.android.models.dto.ProductRequest
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.mapNetworkCallToViewStateFlow
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.component.MatchingSetData
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.service.MatchingSetRepository
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

/**
 * Created by Kunal Uttarwar on 06/02/24.
 */

@HiltViewModel
class MatchingSetViewModel @Inject constructor(private val matchingSetRepository: MatchingSetRepository) :
    ViewModel() {

    val matchingSetData = mutableStateOf(MatchingSetData(arrayListOf()))

    private val _inventoryForMatchingItemDetails =
        MutableSharedFlow<ViewState<SkusInventoryForStoreResponse>>(0)
    val inventoryForMatchingItemDetails: SharedFlow<ViewState<SkusInventoryForStoreResponse>> =
        _inventoryForMatchingItemDetails

    private var productDetails: ProductDetailResponse? = null
    suspend fun callProductDetailAPI(productRequest: ProductRequest) {
        viewModelScope.launch {
            mapNetworkCallToViewStateFlow {
                matchingSetRepository.getMatchingItemDetail(productRequest)
            }.collectLatest { productDetailResponse ->
                with(productDetailResponse) {
                    renderSuccess {
                        productDetails = output
                        val storeIdForInventory =
                            Utils.retrieveStoreId(output.product.fulfillmentType) ?: ""
                        val multiSKUs =
                            output.product.otherSkus?.joinToString(separator = "-") { it.sku.toString() }
                                ?: ""
                        callProductDetailsInventoryAPi(storeIdForInventory, multiSKUs)
                    }
                }
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

    fun getProductDetails() = productDetails

}