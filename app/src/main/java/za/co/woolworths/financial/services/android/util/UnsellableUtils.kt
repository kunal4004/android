package za.co.woolworths.financial.services.android.util

import android.view.View
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.cart.service.network.CartItemGroup
import za.co.woolworths.financial.services.android.cart.view.CartFragment
import za.co.woolworths.financial.services.android.common.ClickOnDialogButton
import za.co.woolworths.financial.services.android.common.CommonErrorBottomSheetDialogImpl
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationParams
import za.co.woolworths.financial.services.android.geolocation.viewmodel.AddToCartLiveData
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmLocationResponseLiveData
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.CreateList
import za.co.woolworths.financial.services.android.models.dto.Price
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK_201
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.analytics.dto.AddToWishListFirebaseEventData
import za.co.woolworths.financial.services.android.util.analytics.dto.toAnalyticItem
import za.co.woolworths.financial.services.android.util.wenum.Delivery

/**
 * Created by Kunal Uttarwar on 20/06/23.
 */
class UnsellableUtils {
    companion object {

        private var errorBottomSheetDialog = CommonErrorBottomSheetDialogImpl()
        private var commerceItemList: ArrayList<UnSellableCommerceItem>? = null
        private var customProgressDialog: CustomProgressBar? = null
        private var customBottomSheetDialogFragment: CustomBottomSheetDialogFragment? = null
        internal const val ADD_TO_LIST_SUCCESS_RESULT_CODE = "64924"
        fun callConfirmPlace(
            fragment: Fragment,
            confirmLocationParams: ConfirmLocationParams?,
            progressBar: ProgressBar,
            confirmAddressViewModel: ConfirmAddressViewModel,
            deliveryType: Delivery,
            isCheckBoxSelected: Boolean= false,
        ) {
            commerceItemList = confirmLocationParams?.commerceItemList
            // Call Confirm location API.
            if (fragment.view != null) {
                fragment.viewLifecycleOwner.lifecycleScope.launch {
                    progressBar?.visibility = View.VISIBLE
                    try {
                        val confirmLocationRequest = confirmLocationParams?.confirmLocationRequest
                            ?: KotlinUtils.getConfirmLocationRequest(deliveryType)
                        if (confirmLocationRequest.address.placeId.isNullOrEmpty()) {
                            progressBar?.visibility = View.GONE
                            return@launch
                        }
                        val confirmLocationResponse =
                            confirmAddressViewModel.postConfirmAddress(confirmLocationRequest)
                        progressBar?.visibility = View.GONE
                        if (confirmLocationResponse != null) {
                            when (confirmLocationResponse.httpCode) {
                                AppConstant.HTTP_OK, HTTP_OK_201 -> {
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
                                    if (isCheckBoxSelected) {
                                        // If unsellable items are removed from popup with addToList checkBox selected then call getList and createList/AddToList API.
                                        callGetListAPI(
                                            progressBar,
                                            fragment,
                                            confirmAddressViewModel,
                                        )
                                    } else {
                                        // This will update the previous fragment data like location details.
                                        ConfirmLocationResponseLiveData.value = true
                                        //This is not a unsellable flow or we don't have unsellable items so this will give callBack to AddToCart function or Checkout Summary Flow.
                                        AddToCartLiveData.value = true
                                    }
                                }

                                else -> {
                                    showConfirmLocationErrorDialog(
                                        fragment,
                                        confirmLocationParams,
                                        progressBar,
                                        confirmAddressViewModel,
                                        deliveryType
                                    )
                                }
                            }
                        }
                    } catch (coroutineException: CancellationException) {
                        FirebaseManager.logException(coroutineException)
                        progressBar?.visibility = View.GONE
                    } catch (e: Exception) {
                        FirebaseManager.logException(e)
                        progressBar?.visibility = View.GONE
                        showConfirmLocationErrorDialog(
                            fragment,
                            confirmLocationParams,
                            progressBar,
                            confirmAddressViewModel,
                            deliveryType
                        )
                    }
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
                            AppConstant.HTTP_OK, HTTP_OK_201 -> {
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
                                    callAddToListAPI(
                                        fragment,
                                        progressBar,
                                        confirmAddressViewModel,
                                        shoppingList.listId
                                    )

                                } else {
                                    // Name *Auto Shopping List* don't exist so call create List API to create this name.
                                    callCreateListAPI(
                                        fragment,
                                        progressBar,
                                        confirmAddressViewModel
                                    )
                                }
                            }

                            else -> {
                                // If API fails then we don't want to block user for the main functionality that was addToCart So call addToCart function after error dialog dismiss.
                                showListErrorDialog(fragment, progressBar, confirmAddressViewModel)
                            }
                        }
                    }
                } catch (e: Exception) {
                    FirebaseManager.logException(e)
                    progressBar?.visibility = View.GONE
                    // If API fails then we don't want to block user for the main functionality that was addToCart So call addToCart function after error dialog dismiss.
                    showListErrorDialog(fragment, progressBar, confirmAddressViewModel)
                } catch (e: JsonSyntaxException) {
                    FirebaseManager.logException(e)
                    progressBar?.visibility = View.GONE
                    showListErrorDialog(fragment, progressBar, confirmAddressViewModel)
                }
            }
        }

        private fun callCreateListAPI(
            fragment: Fragment,
            progressBar: ProgressBar,
            confirmAddressViewModel: ConfirmAddressViewModel,
        ) {
            if (this.commerceItemList != null) {
                val createList =
                    CreateList(Constant.AUTO_SHOPPING_LIST_NAME, getAddToListRequestParams(null))
                showLoadingProgress(fragment)
                fragment.viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val createListResponse =
                            confirmAddressViewModel.createNewList(createList)
                        val createNewListResponse = createListResponse?.body()
                        if (createNewListResponse != null) {
                            when (createNewListResponse.httpCode) {
                                AppConstant.HTTP_OK, HTTP_OK_201 -> {
                                    createNewListResponse.lists.listIterator().forEach {
                                        if (it.listName.equals(
                                                Constant.AUTO_SHOPPING_LIST_NAME,
                                                true
                                            )
                                        ) {
                                            // Once new list name is created then call AddToList
                                            callAddToListAPI(
                                                fragment,
                                                progressBar,
                                                confirmAddressViewModel,
                                                it.listId
                                            )
                                            return@forEach
                                        }
                                    }
                                }

                                else -> {
                                    hideLoadingProgress()
                                    showListErrorDialog(
                                        fragment,
                                        progressBar,
                                        confirmAddressViewModel
                                    )
                                }
                            }
                        } else {
                            hideLoadingProgress()
                        }
                    } catch (e: Exception) {
                        FirebaseManager.logException(e)
                        hideLoadingProgress()
                        showListErrorDialog(fragment, progressBar, confirmAddressViewModel)
                    }
                }
            }
        }

        private fun callAddToListAPI(
            fragment: Fragment,
            progressBar: ProgressBar,
            confirmAddressViewModel: ConfirmAddressViewModel,
            requestedListId: String,
        ) {
            callAddToWishlistFirebaseEvent()
            showLoadingProgress(fragment)
            fragment.viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val listItems = getAddToListRequestParams(requestedListId)
                    listItems.map { item ->
                        if (item.giftListId.isNullOrEmpty()) {
                            item.giftListId = requestedListId
                        }
                    }
                    val addProductToListResponse =
                        confirmAddressViewModel.addProductsToList(
                            requestedListId,
                            listItems.toList()
                        )
                    hideLoadingProgress()
                    val addToListResponse = addProductToListResponse?.body()
                    if (addToListResponse != null) {
                        when (addToListResponse.httpCode) {
                            AppConstant.HTTP_OK, HTTP_OK_201 -> {
                                showAddToListSuccessPopup(fragment)
                            }

                            else -> {
                                showListErrorDialog(
                                    fragment,
                                    progressBar,
                                    confirmAddressViewModel
                                )
                            }
                        }
                    } else {
                        ConfirmLocationResponseLiveData.value = true
                    }
                } catch (e: Exception) {
                    FirebaseManager.logException(e)
                    hideLoadingProgress()
                    showListErrorDialog(fragment, progressBar, confirmAddressViewModel)
                }
            }
        }

        private fun getAddToListRequestParams(requestedListId: String?): MutableList<AddToListRequest> {
            val items: MutableList<AddToListRequest> = mutableListOf()
            if (this.commerceItemList != null) {
                for (listItem in this.commerceItemList!!) {
                    val addToListRequest = AddToListRequest()
                    addToListRequest.apply {
                        skuID = listItem.catalogRefId
                        giftListId = listItem.catalogRefId
                        catalogRefId = listItem.catalogRefId
                        quantity = listItem.quantity.toString()
                        listId = requestedListId ?: listItem.catalogRefId
                    }

                    items.add(addToListRequest)
                }
            }
            return items
        }

        private fun showLoadingProgress(fragment: Fragment) {
            if (customProgressDialog != null && customProgressDialog!!.isVisible)
                return
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
                fragment.parentFragmentManager,
                CustomBottomSheetDialogFragment::class.java.simpleName
            )
        }

        private fun showListErrorDialog(
            fragment: Fragment,
            progressBar: ProgressBar,
            confirmAddressViewModel: ConfirmAddressViewModel,
        ) {
            errorBottomSheetDialog.showCommonErrorBottomDialog(
                object : ClickOnDialogButton {
                    override fun onClick() {
                        callGetListAPI(
                            progressBar,
                            fragment,
                            confirmAddressViewModel
                        )
                    }

                    override fun onDismiss() {
                        // If API fails then we don't want to block user for the main functionality that was addToCart So call addToCart function from here.
                        fragment.setFragmentResult(ADD_TO_LIST_SUCCESS_RESULT_CODE, bundleOf())
                    }
                },
                fragment.requireContext(),
                fragment.getString(R.string.generic_error_something_wrong_newline),
                fragment.getString(R.string.add_to_list_error_description),
                fragment.getString(R.string.retry),
                true
            )
        }

        private fun showConfirmLocationErrorDialog(
            fragment: Fragment,
            confirmLocationParams: ConfirmLocationParams?,
            progressBar: ProgressBar,
            confirmAddressViewModel: ConfirmAddressViewModel,
            deliveryType: Delivery,
        ) {
            errorBottomSheetDialog.showCommonErrorBottomDialog(
                object : ClickOnDialogButton {
                    override fun onClick() {
                        callConfirmPlace(
                            fragment,
                            confirmLocationParams,
                            progressBar,
                            confirmAddressViewModel,
                            deliveryType
                        )
                    }

                    override fun onDismiss() {
                        // Do Nothing.
                    }
                },
                fragment.requireContext(),
                fragment.getString(R.string.generic_error_something_wrong_newline),
                fragment.getString(R.string.empty),
                fragment.getString(R.string.retry),
                true
            )
        }

        private fun callAddToWishlistFirebaseEvent() {
            val analyticProducts = commerceItemList?.map { it.toAnalyticItem() }
            val addToWishListFirebaseEventData = AddToWishListFirebaseEventData(
                shoppingListName = Constant.AUTO_SHOPPING_LIST_NAME,
                products = analyticProducts
            )
            FirebaseAnalyticsEventHelper.addToWishlistEvent(addToWishListFirebaseEventData)
        }

        fun removeItemsFromCart(
            progressBar: ProgressBar,
            commerceList: ArrayList<UnSellableCommerceItem>?,
            isCheckBoxSelected: Boolean,
            currentFragment: CartFragment,
            confirmAddressViewModel: ConfirmAddressViewModel,
        ) {
            progressBar.visibility = View.VISIBLE
            commerceItemList = commerceList
            commerceList?.forEach {
                removeItem(
                    it.commerceId,
                    progressBar,
                    isCheckBoxSelected,
                    currentFragment,
                    confirmAddressViewModel
                )
            }
        }

        private fun removeItem(
            commerceId: String, progressBar: ProgressBar, isCheckBoxSelected: Boolean,
            currentFragment: CartFragment,
            confirmAddressViewModel: ConfirmAddressViewModel,
        ) {
            OneAppService().removeCartItem(commerceId)
                .enqueue(CompletionHandler(object : IResponseListener<ShoppingCartResponse> {
                    override fun onSuccess(shoppingCartResponse: ShoppingCartResponse?) {
                        onItemRemoved(
                            commerceId,
                            progressBar,
                            isCheckBoxSelected,
                            currentFragment,
                            confirmAddressViewModel
                        )
                    }

                    override fun onFailure(error: Throwable?) {
                        onItemRemovedFailed(
                            commerceId,
                            progressBar,
                            isCheckBoxSelected,
                            currentFragment,
                            confirmAddressViewModel
                        )
                    }
                }, ShoppingCartResponse::class.java))
        }

        private fun onItemRemoved(
            commerceId: String, progressBar: ProgressBar, isCheckBoxSelected: Boolean,
            currentFragment: CartFragment,
            confirmAddressViewModel: ConfirmAddressViewModel,
        ) {
            commerceItemList?.find { it.commerceId == commerceId }?.isItemRemoved = true

            if (commerceItemList?.filter { commerceItem -> !commerceItem.isItemRemoved }
                    .isNullOrEmpty()) {
                progressBar.visibility = View.GONE
                if (isCheckBoxSelected) {
                    callGetListAPI(
                        progressBar,
                        currentFragment,
                        confirmAddressViewModel,
                    )
                } else {
                    //This is not a unsellable flow or we don't have unsellable items so this will give callBack to Checkout Summary Flow.
                    AddToCartLiveData.value = true
                }
            }
        }

        private fun onItemRemovedFailed(
            commerceId: String, progressBar: ProgressBar, isCheckBoxSelected: Boolean,
            currentFragment: CartFragment,
            confirmAddressViewModel: ConfirmAddressViewModel,
        ) {
            commerceItemList?.find { it.commerceId == commerceId }?.isItemRemoved =
                false

            if (commerceItemList?.filter { commerceItem -> !commerceItem.isItemRemoved }
                    .isNullOrEmpty()) {
                progressBar.visibility = View.GONE
                if (isCheckBoxSelected) {
                    callGetListAPI(
                        progressBar,
                        currentFragment,
                        confirmAddressViewModel,
                    )
                } else {
                    //This is not a unsellable flow or we don't have unsellable items so this will give callBack to Checkout Summary Flow.
                    AddToCartLiveData.value = true
                }
            }
        }

        fun getUnsellableCommerceItem(
            cartItemsGroup: ArrayList<CartItemGroup>?,
            commerceItemList: ArrayList<CommerceItem>,
        ): ArrayList<UnSellableCommerceItem> {
            var unsellableCommerceItemList = ArrayList<UnSellableCommerceItem>()
            commerceItemList.forEachIndexed { i, item ->
                val price = Price(
                    item.priceInfo.amount,
                    0.0,
                    item.priceInfo.rawTotalPrice,
                    item.priceInfo.salePrice,
                    item.priceInfo.listPrice
                )
                val unSellableCommerceItem = UnSellableCommerceItem(
                    item.commerceItemInfo.quantity,
                    item.commerceItemInfo.productId,
                    "",
                    item.commerceItemInfo.internalImageURL,
                    item.commerceItemInfo.catalogRefId,
                    item.commerceItemClassType,
                    item.color,
                    "",
                    item.size,
                    "",
                    price,
                    item.commerceItemInfo.externalImageRefV2,
                    item.commerceItemInfo.productDisplayName,
                    item.fulfillmentType,
                    cartItemsGroup?.get(i)?.type,
                    item.commerceItemInfo.commerceId,
                    item.isItemRemoved
                )
                unsellableCommerceItemList.add(unSellableCommerceItem)
            }
            return unsellableCommerceItemList
        }
    }
}