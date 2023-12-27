package za.co.woolworths.financial.services.android.ui.fragments.order_again

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.awfs.coordination.R
import com.google.gson.JsonElement
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.contracts.IToastInterface
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.order_again.ProductItem
import za.co.woolworths.financial.services.android.models.dto.order_again.toAddToListRequest
import za.co.woolworths.financial.services.android.shoppinglist.listener.MyShoppingListItemClickListener
import za.co.woolworths.financial.services.android.shoppinglist.model.EditOptionType
import za.co.woolworths.financial.services.android.shoppinglist.view.MoreOptionDialogFragment
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_CART
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_PRODUCT
import za.co.woolworths.financial.services.android.ui.views.ToastFactory
import za.co.woolworths.financial.services.android.ui.wfs.common.contentView
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.OrderAgainScreen
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.OrderAgainScreenEvents
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.SnackbarDetails
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.OrderAgainViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.CustomProgressBar
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.ScreenManager

@AndroidEntryPoint
class OrderAgainFragment : Fragment(), MyShoppingListItemClickListener, IToastInterface {

    private val viewModel: OrderAgainViewModel by viewModels()
    private var customProgressDialog: CustomProgressBar? = null

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
                    OrderAgainScreenEvents.DeliveryLocationClick -> deliverySelectionIntent()
                    OrderAgainScreenEvents.StartShoppingClicked -> onStartShoppingClicked()
                    OrderAgainScreenEvents.SnackbarViewClicked -> onAddToCartToastViewClick()
                    OrderAgainScreenEvents.CopyToListClicked -> onCopyToListClicked()
                    is OrderAgainScreenEvents.CopyItemToListClicked -> onCopyToListClicked(it.item)
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
            ToastFactory.buildItemsAddedToList(
                requireActivity(),
                it,
                snackbarDetails.listName.ifEmpty { requireContext().getString(R.string.multiple_lists) },
                false,
                snackbarDetails.count
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

    private fun onCopyToListClicked(item: ProductItem? = null) {
        val items = if(item != null) {
            ArrayList<AddToListRequest>(0).apply {
                add(item.toAddToListRequest())
            }
        } else {
            viewModel.getCopyToListItems()
        }

        val fragment = MoreOptionDialogFragment.newInstance(
            this@OrderAgainFragment,
            items.size,
            "",
            false,
            items
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

    private fun deliverySelectionIntent() {
        activity ?: return
        KotlinUtils.presentEditDeliveryGeoLocationActivity(
            requireActivity(),
            AppConstant.REQUEST_CODE_DELIVERY_LOCATION_CHANGE,
            KotlinUtils.getPreferredDeliveryType(),
            GeoUtils.getPlaceId(),
            isFromDashTab = false,
            isComingFromCheckout = false,
            isComingFromSlotSelection = false,
            isMixedBasket = false,
            isFBHOnly = false,
            savedAddressResponse = null,
            defaultAddress = null,
            whoISCollecting = null,
            liquorCompliance = null
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstant.REQUEST_CODE_DELIVERY_LOCATION_CHANGE) {
            viewModel.setDeliveryLocation()
            viewModel.refreshInventory()
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