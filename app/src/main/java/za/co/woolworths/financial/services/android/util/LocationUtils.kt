package za.co.woolworths.financial.services.android.util

import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationParams
import za.co.woolworths.financial.services.android.geolocation.viewmodel.AddToCartLiveData
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmLocationResponseLiveData
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.CreateList
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

/**
 * Created by Kunal Uttarwar on 20/06/23.
 */
class LocationUtils {
    companion object {

        private var commerceItemList: ArrayList<UnSellableCommerceItem>? = null
        private var customProgressDialog: CustomProgressBar? = null
        private var customBottomSheetDialogFragment: CustomBottomSheetDialogFragment? = null
        internal const val ADD_TO_LIST_SUCCESS_RESULT_CODE = "64924"
        fun callConfirmPlace(
            fragment: Fragment,
            confirmLocationParams: ConfirmLocationParams?,
            progressBar: ProgressBar,
            confirmAddressViewModel: ConfirmAddressViewModel,
        ) {
            commerceItemList = confirmLocationParams?.commerceItemList
            // Call Confirm location API.
            fragment.viewLifecycleOwner.lifecycleScope.launch {
                progressBar?.visibility = View.VISIBLE
                try {
                    val confirmLocationRequest = confirmLocationParams?.confirmLocationRequest
                        ?: KotlinUtils.getConfirmLocationRequest(KotlinUtils.browsingDeliveryType)
                    val confirmLocationResponse =
                        confirmAddressViewModel.postConfirmAddress(confirmLocationRequest)
                    progressBar?.visibility = View.GONE
                    if (confirmLocationResponse != null) {
                        when (confirmLocationResponse.httpCode) {
                            AppConstant.HTTP_OK -> {
                                if (SessionUtilities.getInstance().isUserAuthenticated) {
                                    Utils.savePreferredDeliveryLocation(
                                        ShoppingDeliveryLocation(
                                            confirmLocationResponse.orderSummary?.fulfillmentDetails
                                        )
                                    )
                                    if (KotlinUtils.getAnonymousUserLocationDetails() != null)
                                        KotlinUtils.clearAnonymousUserLocationDetails()
                                } else {
                                    KotlinUtils.saveAnonymousUserLocationDetails(
                                        ShoppingDeliveryLocation(
                                            confirmLocationResponse.orderSummary?.fulfillmentDetails
                                        )
                                    )
                                }
                                val savedPlaceId = KotlinUtils.getDeliveryType()?.address?.placeId
                                KotlinUtils.apply {
                                    this.placeId = confirmLocationRequest.address.placeId
                                    isLocationPlaceIdSame =
                                        confirmLocationRequest.address.placeId?.equals(
                                            savedPlaceId
                                        )
                                }
                                // This will update the previous fragment data like location details.
                                ConfirmLocationResponseLiveData.value = true
                                if (confirmLocationParams?.commerceItemList != null) {
                                    // If unsellable items are removed from popup with addToList checkBox selected then call getList and createList/AddToList API.
                                    callGetListAPI(progressBar, fragment, confirmAddressViewModel)
                                } else {
                                    //This is not a unsellable flow or we don't have unsellable items so this will give callBack to AddToCart function or Checkout Summary Flow.
                                    AddToCartLiveData.value = true
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    FirebaseManager.logException(e)
                    progressBar?.visibility = View.GONE
                }
            }
        }

        private fun callGetListAPI(
            progressBar: ProgressBar,
            fragment: Fragment,
            confirmAddressViewModel: ConfirmAddressViewModel,
        ) {
            progressBar?.visibility = View.VISIBLE
            fragment.viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val getShoppingList =
                        confirmAddressViewModel.getShoppingList()
                    progressBar?.visibility = View.GONE
                    val shoppingListResponse = getShoppingList?.body()
                    if (shoppingListResponse != null) {
                        when (shoppingListResponse.httpCode) {
                            AppConstant.HTTP_OK -> {
                                var shoppingList: ShoppingList? = null
                                for (myList in shoppingListResponse.lists) {
                                    if (myList.listName.equals(
                                            Constant.AUTO_SHOPPING_LIST_NAME,
                                            true
                                        )
                                    ) {
                                        shoppingList = myList
                                        break
                                    }
                                }
                                if (shoppingList != null) {
                                    // This means Auto Shopping List name already exist and we need to call add to list API
                                    callAddToListAPI(fragment, confirmAddressViewModel)

                                } else {
                                    // Name *Auto Shopping List* don't exist so call create List API to create this name.
                                    callCreateListAPI(
                                        fragment,
                                        confirmAddressViewModel
                                    )
                                }
                            }

                            else -> {
                                // If API fails then we don't want to block user for the main functionality that was addToCart So call addToCart function from here.
                                // addItemToCart()
                            }
                        }
                    }
                } catch (e: Exception) {
                    FirebaseManager.logException(e)
                    progressBar?.visibility = View.GONE
                } catch (e: JsonSyntaxException) {
                    FirebaseManager.logException(e)
                    progressBar?.visibility = View.GONE
                }
            }
        }

        private fun callCreateListAPI(
            fragment: Fragment,
            confirmAddressViewModel: ConfirmAddressViewModel,
        ) {
            if (commerceItemList != null) {
                var items: List<AddToListRequest> = mutableListOf()
                for (listItem in commerceItemList!!) {
                    var addToListRequest = AddToListRequest()
                    addToListRequest.apply {
                        skuID = listItem.productId
                        giftListId = ""
                        catalogRefId = ""
                        quantity = listItem.quantity.toString()
                        listId = ""

                    }

                    items.plus(addToListRequest)
                }
                val createList = CreateList(Constant.AUTO_SHOPPING_LIST_NAME, items)
                showLoadingProgress(fragment)
                fragment.viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val createListResponse =
                            confirmAddressViewModel.createNewList(createList)
                        hideLoadingProgress()
                        val createNewListResponse = createListResponse?.body()
                        if (createNewListResponse != null) {
                            when (createNewListResponse.httpCode) {
                                AppConstant.HTTP_OK -> {
                                    showAddToListSuccessPopup(fragment)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        FirebaseManager.logException(e)
                        hideLoadingProgress()
                    }
                }
            }
        }

        private fun callAddToListAPI(
            fragment: Fragment,
            confirmAddressViewModel: ConfirmAddressViewModel,
        ) {
            if (commerceItemList != null) {
                showLoadingProgress(fragment)
                fragment.viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val addProductToListResponse =
                            confirmAddressViewModel.addProductsToList("", listOf())
                        hideLoadingProgress()
                        val addToListResponse = addProductToListResponse?.body()
                        if (addToListResponse != null) {
                            when (addToListResponse.httpCode) {
                                AppConstant.HTTP_OK -> {
                                    showAddToListSuccessPopup(fragment)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        FirebaseManager.logException(e)
                        hideLoadingProgress()
                    }
                }
            }
        }

        private fun showLoadingProgress(fragment: Fragment) {
            customProgressDialog = CustomProgressBar.newInstance(
                fragment.getString(R.string.add_to_list_progress_bar_title),
                fragment.getString(R.string.processing_your_request_desc)
            )
            customProgressDialog?.show(
                fragment.requireActivity().supportFragmentManager,
                CustomProgressBar::class.java.simpleName
            )
        }

        private fun hideLoadingProgress() {
            customProgressDialog?.dismiss()
        }

        private fun showAddToListSuccessPopup(fragment: Fragment) {
            customBottomSheetDialogFragment =
                CustomBottomSheetDialogFragment.newInstance(
                    fragment.getString(R.string.add_to_list_bottom_sheet_success_title),
                    fragment.getString(R.string.add_to_list_bottom_sheet_success_sub_title),
                    fragment.getString(R.string.got_it),
                    null,
                    ADD_TO_LIST_SUCCESS_RESULT_CODE
                )
            customBottomSheetDialogFragment?.show(
                fragment.requireActivity().supportFragmentManager,
                CustomBottomSheetDialogFragment::class.java.simpleName
            )
        }
    }
}