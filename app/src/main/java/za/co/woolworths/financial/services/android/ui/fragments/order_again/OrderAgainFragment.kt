package za.co.woolworths.financial.services.android.ui.fragments.order_again

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.awfs.coordination.R
import com.google.gson.JsonElement
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.contracts.IToastInterface
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.order_again.ProductItem
import za.co.woolworths.financial.services.android.models.dto.order_again.toAddToListRequest
import za.co.woolworths.financial.services.android.presentation.addtolist.AddToListFragment
import za.co.woolworths.financial.services.android.shoppinglist.component.MoreOptionsElement
import za.co.woolworths.financial.services.android.shoppinglist.listener.MyShoppingListItemClickListener
import za.co.woolworths.financial.services.android.shoppinglist.model.EditOptionType
import za.co.woolworths.financial.services.android.shoppinglist.view.MoreOptionDialogFragment
import za.co.woolworths.financial.services.android.shoptoggle.common.UnsellableAccess
import za.co.woolworths.financial.services.android.shoptoggle.presentation.ShopToggleActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_CART
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_PRODUCT
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.views.ToastFactory
import za.co.woolworths.financial.services.android.ui.wfs.common.contentView
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.OrderAgainScreen
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.OrderAgainScreenEvents
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.SnackbarDetails
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.OrderAgainViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.CustomProgressBar
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.UnsellableUtils
import za.co.woolworths.financial.services.android.util.wenum.Delivery

@AndroidEntryPoint
class OrderAgainFragment : Fragment(), MyShoppingListItemClickListener, IToastInterface {

    private val viewModel: OrderAgainViewModel by viewModels()
    private var customProgressDialog: CustomProgressBar? = null
    private val confirmAddressViewModel: ConfirmAddressViewModel by activityViewModels()
    private val activityLauncher = BetterActivityResult.registerActivityForResult(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addFragmentResultListeners()
    }

    private fun addFragmentResultListeners() {
        //After unsellable add to list is called. On Got it clicked result back.
        setFragmentResultListener(UnsellableUtils.ADD_TO_LIST_SUCCESS_RESULT_CODE) { _, _ ->
            viewModel.setDeliveryLocation()
            viewModel.refreshInventory()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)) {
        OneAppTheme {
            OrderAgainScreen(viewModel, onBackPressed = {
                hideBottomNavigation(false)
                requireActivity().onBackPressed()
            }) {
                when (it) {
                    OrderAgainScreenEvents.ChangeDeliveryClick -> launchShopToggleScreen()
                    OrderAgainScreenEvents.ChangeAddressClick -> launchStoreOrLocationSelection()
                    OrderAgainScreenEvents.StartShoppingClicked -> onStartShoppingClicked()
                    OrderAgainScreenEvents.SnackbarViewClicked -> onAddToCartToastViewClick()
                    OrderAgainScreenEvents.CopyToListClicked -> onCopyToListClicked()
                    is OrderAgainScreenEvents.CopyItemToListClicked -> onCopyItemsToListClicked(it.item)
                    is OrderAgainScreenEvents.ShowSnackBar -> showAddToCartSnackbar(it.snackbarDetails)
                    is OrderAgainScreenEvents.CopyToListSuccess -> onCopyListSuccess(it.snackbarDetails)
                    is OrderAgainScreenEvents.ShowProgressView -> showLoadingProgress(
                        this@OrderAgainFragment,
                        requireContext().resources.getQuantityString(
                            it.titleRes,
                            it.count,
                            it.count
                        )
                    )

                    is OrderAgainScreenEvents.ShowAddToCartError -> onAddToCartError(
                        it.code,
                        it.errorMessage
                    )

                    is OrderAgainScreenEvents.HideBottomBar -> hideBottomNavigation(it.hidden)
                    else -> {}
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.sendOrderAgainEvent()
    }

    private fun onCopyListSuccess(snackbarDetails: SnackbarDetails) {
        hideLoadingProgress()
        view?.let {
            val title = requireContext().resources?.getQuantityString(
                R.plurals.copy_item_msg,
                snackbarDetails.count, snackbarDetails.count, snackbarDetails.listName.ifEmpty { requireContext().getString(R.string.multiple_lists) }
            ) ?: ""
            ToastFactory.buildItemsAddedToList(
                requireActivity(),
                it,
                snackbarDetails.listName.ifEmpty { requireContext().getString(R.string.multiple_lists) },
                false,
                snackbarDetails.count,
                title
            ) {
                if (snackbarDetails.listId.isEmpty()) {
                    ScreenManager.presentMyListScreen(activity)
                } else {
                    ScreenManager.presentShoppingListDetailActivity(
                        activity,
                        snackbarDetails.listId,
                        snackbarDetails.listName,
                        false
                    )
                }
            }
        }
    }

    private fun showAddToCartSnackbar(snackbarDetails: SnackbarDetails) {
        view?.let {
            snackbarDetails.productCountMap?.let { productCountMap ->
                ToastFactory.showItemsLimitToastOnAddToCart(
                    it,
                    productCountMap,
                    requireActivity(),
                    snackbarDetails.count,
                    true
                )
            }
        }
    }

    private fun showLoadingProgress(fragment: Fragment, message: String) {
        if (customProgressDialog != null && customProgressDialog!!.isVisible)
            return
        customProgressDialog = CustomProgressBar.newInstance(
            message,
            getString(R.string.processing_your_request_desc)
        )
        customProgressDialog?.show(
            fragment.requireActivity().supportFragmentManager,
            CustomProgressBar::class.java.simpleName
        )
    }

    private fun hideLoadingProgress() {
        customProgressDialog?.dismiss()
    }

    private fun onCopyToListClicked() {
        viewModel.collapseItems()
        val items = viewModel.getCopyToListItems()
        val fragment =
            AddToListFragment.newInstance(
                shoppingListItemClickListener = this@OrderAgainFragment,
                listId = "",
                copyItemToList = true,
                moveItemToList = false,
                items
            )
        fragment.show(parentFragmentManager, AddToListFragment::class.simpleName)
    }

    private fun onCopyItemsToListClicked(item: ProductItem? = null) {
        viewModel.collapseItems()
        val items = if (item != null) {
            ArrayList<AddToListRequest>(0).apply {
                add(item.toAddToListRequest())
            }
        } else {
            viewModel.getCopyToListItems()
        }

        val options = ArrayList<MoreOptionsElement>(0).apply {
            add(
                MoreOptionsElement(
                    R.drawable.ic_copy,
                    requireContext().getString(R.string.copy_to_list)
                )
            )
        }
        val fragment = MoreOptionDialogFragment.newInstance(
            this@OrderAgainFragment,
            items.size,
            "",
            false,
            items,
            options
        )
        fragment.show(parentFragmentManager, MoreOptionDialogFragment::class.simpleName)
    }

    private fun onAddToCartError(code: Int, errorMessage: String) {
        if (code.toString() == AppConstant.RESPONSE_ERROR_CODE_1235) {
            KotlinUtils.showGeneralInfoDialog(
                fragmentManager = requireActivity().supportFragmentManager,
                description = "",
                title = errorMessage,
                actionText = requireContext().getString(R.string.got_it),
                infoIcon = R.drawable.icon_dash_delivery_scooter
            )
        }
    }

    private fun onAddToCartToastViewClick() {
        (requireActivity() as? BottomNavigationActivity)?.apply {
            navigateToTabIndex(INDEX_CART, null)
        }
    }

    private fun onStartShoppingClicked() {
        (requireActivity() as? BottomNavigationActivity)?.apply {
            navigateToTabIndex(INDEX_PRODUCT, null)
        }
    }

    private fun hideBottomNavigation(hide: Boolean) {
        (requireActivity() as? BottomNavigationActivity)?.apply {
            if (hide) {
                hideBottomNavigationMenu()
            } else {
                showBottomNavigationMenu()
            }
        }
    }

    private fun launchShopToggleScreen() {
        viewModel.collapseItems()
        activityLauncher.launch(
            input = Intent(requireActivity(), ShopToggleActivity::class.java),
            onActivityResult = { result ->
                when (result.resultCode) {
                    Activity.RESULT_OK -> {
                        onLocationChange(result.data)
                    }
                }
            })
    }

    private fun onLocationChange(data: Intent?) {

        val toggleFulfilmentResultWithUnsellable =
            UnsellableAccess.getToggleFulfilmentResultWithUnSellable(data)
        if (toggleFulfilmentResultWithUnsellable != null) {
            UnsellableAccess.navigateToUnsellableItemsFragment(
                ArrayList(toggleFulfilmentResultWithUnsellable.unsellableItemsList),
                toggleFulfilmentResultWithUnsellable.deliveryType,
                confirmAddressViewModel,
                ProgressBar(requireContext()),
                this,
                parentFragmentManager
            )
        } else {
            viewModel.setDeliveryLocation()
            viewModel.refreshInventory()
        }
    }

    private fun launchStoreOrLocationSelection() {
        viewModel.collapseItems()
        val delivery = Delivery.getType(KotlinUtils.getDeliveryType()?.deliveryType)
        if (delivery == Delivery.CNC) {
            launchStoreSelection()
        } else {
            launchGeoLocationFlow()
        }
    }

    private fun launchGeoLocationFlow() {
        KotlinUtils.presentEditDeliveryGeoLocationActivity(
            activity,
            BundleKeysConstants.UPDATE_LOCATION_REQUEST,
            Delivery.getType(KotlinUtils.getDeliveryType()?.deliveryType),
            KotlinUtils.getDeliveryType()?.address?.placeId ?: "",
            isLocationUpdateRequest = true,
            newDelivery = Delivery.getType(KotlinUtils.getDeliveryType()?.deliveryType)
                ?: KotlinUtils.browsingDeliveryType
        )
    }

    private fun launchStoreSelection() {
        KotlinUtils.presentEditDeliveryGeoLocationActivity(
            activity,
            BundleKeysConstants.UPDATE_STORE_REQUEST,
            Delivery.getType(KotlinUtils.getDeliveryType()?.deliveryType),
            KotlinUtils.getDeliveryType()?.address?.placeId ?: "",
            isFromNewToggleFulfilmentScreen = true,
            newDelivery = Delivery.CNC,
            needStoreSelection = true,
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            BundleKeysConstants.UPDATE_STORE_REQUEST -> {
                onLocationChange(data)
            }
        }
    }


    override fun itemEditOptionsClick(
        editOptionType: EditOptionType
    ) {
        when (editOptionType) {
            is EditOptionType.RemoveItemFromList -> {
                //TODO
            }

            is EditOptionType.CopyItemFromList -> {
                viewModel.copyItemsToList(editOptionType.list, editOptionType.itemsToBeAdded)
            }

            is EditOptionType.MoveItemFromList -> {
                //TODO
            }

            else -> {}
        }
    }

    override fun onToastButtonClicked(jsonElement: JsonElement?) {

    }
}