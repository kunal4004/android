package za.co.woolworths.financial.services.android.cart.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentCartBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import za.co.woolworths.financial.services.android.cart.service.network.CartItemGroup
import za.co.woolworths.financial.services.android.cart.service.network.CartResponse
import za.co.woolworths.financial.services.android.cart.viewmodel.CartUtils.Companion.filterCommerceItemFromCartResponse
import za.co.woolworths.financial.services.android.cart.viewmodel.CartUtils.Companion.getAppliedVouchersCount
import za.co.woolworths.financial.services.android.cart.viewmodel.CartUtils.Companion.updateItemLimitsBanner
import za.co.woolworths.financial.services.android.cart.viewmodel.CartViewModel
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.geolocation.GeoUtils.Companion.getDelivertyType
import za.co.woolworths.financial.services.android.geolocation.GeoUtils.Companion.getPlaceId
import za.co.woolworths.financial.services.android.geolocation.GeoUtils.Companion.getSelectedPlaceId
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.AppConfigSingleton.nativeCheckout
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.item_limits.ProductCountMap
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.CouponClaimCode
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.VoucherDetails
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService.removeCartItem
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.models.service.event.CartState
import za.co.woolworths.financial.services.android.models.service.event.ProductState
import za.co.woolworths.financial.services.android.ui.activities.CartCheckoutActivity
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.activities.online_voucher_redemption.AvailableVouchersToRedeemInCart
import za.co.woolworths.financial.services.android.ui.fragments.cart.GiftWithPurchaseDialogDetailFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.RemoveProductsFromCartDialogFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.RemoveProductsFromCartDialogFragment.IRemoveProductsFromCartDialog
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.buildAddToCartSuccessToast
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.showItemsLimitToastOnAddToCart
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView.IWalkthroughActionListener
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ActionSheetDialogFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_EXPECTATION_FAILED_502
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK_201
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_SESSION_TIMEOUT_440
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getPreferredDeliveryType
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getPreferredPlaceId
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.isDeliveryOptionClickAndCollect
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.isDeliveryOptionDash
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.presentEditDeliveryGeoLocationActivity
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.setDeliveryAddressView
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.showGeneralInfoDialog
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.updateCheckOutLink
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter.Companion.instance
import za.co.woolworths.financial.services.android.util.ToastUtils.ToastInterface
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.logException
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.setCrashlyticsString
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.util.wenum.Delivery.Companion.getType
import za.co.woolworths.financial.services.android.recommendations.data.response.request.CartProducts
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Event
import za.co.woolworths.financial.services.android.recommendations.presentation.RecommendationEventHandler
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.ui.wfs.common.getIpAddress
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@AndroidEntryPoint
class CartFragment : BaseFragmentBinding<FragmentCartBinding>(FragmentCartBinding::inflate),
    CartProductAdapter.OnItemClick,
    View.OnClickListener, NetworkChangeListener, ToastInterface, IWalkthroughActionListener,
    IRemoveProductsFromCartDialog, RecommendationEventHandler {

    private val viewModel: CartViewModel by viewModels(
        ownerProducer = { this }
    )

    private val TAG = this.javaClass.simpleName
    private var mNumberOfListSelected = 0
    private var changeQuantityWasClicked = false
    private var errorMessageWasPopUp = false
    private var onRemoveItemFailed = false
    private var mRemoveAllItemFailed = false
    private var mRemoveAllItemFromCartTapped = false
    private var isAllInventoryAPICallSucceed = false
    private var isMaterialPopUpClosed = true

    private var mChangeQuantityList: MutableList<ChangeQuantity?>? = null
    private var mSkuInventories: HashMap<String, List<SkuInventory>>? = null
    private var mapStoreIdWithCommerceItems: Map<String, Collection<CommerceItem>>? = null
    var cartItems: ArrayList<CartItemGroup>? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    private var cartProductAdapter: CartProductAdapter? = null
    private var orderSummary: OrderSummary? = null
    private var mConnectionBroadcast: BroadcastReceiver? = null
    private var mToastUtils: ToastUtils? = null
    private val mDisposables: CompositeDisposable = CompositeDisposable()
    private var mChangeQuantity: ChangeQuantity? = null
    private var mCommerceItem: CommerceItem? = null
    private var voucherDetails: VoucherDetails? = null
    var productCountMap: ProductCountMap? = null
    private var liquorCompliance: LiquorCompliance? = null
    private var cartItemList = ArrayList<CommerceItem>()
    private var isBlackCardHolder : Boolean = false
    private var isOnItemRemoved = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        initViews()
        hideEditCart()
        addFragmentListener()
        addObserverEvents()
        mChangeQuantityList = ArrayList(0)
        mChangeQuantity = ChangeQuantity()
        mConnectionBroadcast = Utils.connectionBroadCast(
            requireActivity(), this
        )
        mToastUtils = ToastUtils(this)
        mDisposables.add(
            WoolworthsApplication.getInstance()
                .bus()
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { stateObject ->
                    if (stateObject != null) {
                        if (stateObject is CartState) {
                            if (!TextUtils.isEmpty(stateObject.state)) {
                                //setDeliveryLocation(cartState.getState());
                            } else if (stateObject.indexState == CartState.CHANGE_QUANTITY) {
                                mChangeQuantity!!.quantity = stateObject.quantity
                                queryServiceChangeQuantity()
                            }
                        } else if (stateObject is ProductState) {
                            when (stateObject.state) {
                                ProductState.CANCEL_DIALOG_TAPPED ->
                                    cartProductAdapter?.onPopUpCancel(ProductState.CANCEL_DIALOG_TAPPED)

                                ProductState.CLOSE_PDP_FROM_ADD_TO_LIST -> {
                                    mToastUtils?.apply {
                                        activity = requireActivity()
                                        currentState = TAG_ADDED_TO_LIST_TOAST
                                        val shoppingList = getString(R.string.shopping_list)
                                        mNumberOfListSelected = stateObject.count
                                        // shopping list vs shopping lists
                                        cartText =
                                            if ((mNumberOfListSelected > 1)) shoppingList + "s"
                                            else shoppingList
                                        pixel = binding.btnCheckOut.height ?: (0 * 2)
                                        this.view = binding.btnCheckOut
                                        message = requireContext().getString(R.string.added_to)
                                        viewState = true
                                        build()
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
        )
        initializeLoggedInUserCartUI()
        setPriceInformationVisibility(false)
    }

    private fun initializeLoggedInUserCartUI() {
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            return
        }

        //One time biometricsWalkthrough
        if (isVisible) {
            ScreenManager.presentBiometricWalkthrough(activity)
        }
        loadShoppingCart()
    }

    private fun initViews() {

        binding.noConnectionHandler.let {
            it.btnRetry.setOnClickListener(this)
            mErrorHandlerView = ErrorHandlerView(activity, it.noConnectionLayout)
            mErrorHandlerView?.setMargin(it.noConnectionLayout, 0, 0, 0, 0)
            binding.btnCheckOut.setOnClickListener(this)
            binding.deliveryLocationConstLayout.setOnClickListener(this)
            binding.emptyStateTemplate.apply {
                btnDashSetAddress.text = getString(R.string.start_shopping)
                btnDashSetAddress.setOnClickListener(this@CartFragment)
            }
        }
    }

    private fun initializeBottomTab() {
        (requireActivity() as? BottomNavigationActivity)?.apply {
            showBottomNavigationMenu()
            hideToolbar()
            setToolbarTitle("")
        }
    }

    private fun setupToolbar() {
        Utils.updateStatusBarBackground(requireActivity())
        binding.apply {
            btnEditCart.setText(R.string.edit)
            btnClearCart.visibility = View.GONE
            btnEditCart.setOnClickListener(this@CartFragment)
            btnClearCart.setOnClickListener(this@CartFragment)
        }
        (requireActivity() as? BottomNavigationActivity?)?.hideToolbar()
    }

    /****
     * mChangeQuantityList save all ChangeQuantityRequest after quantity selection
     * Top ChangeQuantity item in list is selected
     * Extract commerceId of the selected ChangeQuantity object
     * Perform changeQuantity call
     * Remove top changeQuantity object from list
     */
    private fun queryServiceChangeQuantity() {
        mChangeQuantityList?.add(mChangeQuantity)
        viewModel.changeProductQuantityRequest(mChangeQuantityList?.getOrNull(0))
    }

    private fun setEmptyCartUIUserName() {
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            return
        }
        val firstName = SessionUtilities.getInstance().jwt.name[0]
        binding.emptyStateTemplate.root.visibility = View.VISIBLE
        binding.emptyStateTemplate.txtDashTitle.text =
            getString(R.string.hi) + firstName + "," + System.getProperty("line.separator") + getString(
                R.string.empty_cart_text
            )
    }

    private fun onRemoveItem(visibility: Boolean) {
        binding.apply {
            cartProgressBar.visibility =
                if (visibility) View.VISIBLE else View.GONE
            btnClearCart.visibility = if (visibility) View.GONE else View.VISIBLE
            btnEditCart.isEnabled = !visibility
        }
    }

    fun onRemoveSuccess() {
        binding.apply {
            cartProgressBar.visibility = View.GONE
            btnClearCart.visibility = View.GONE
        }
    }

    private fun resetToolBarIcons() {
        hideEditCart()
        binding.btnClearCart.visibility = View.GONE
    }

    private fun showEditCart() {
        binding.btnEditCart.apply {
            alpha = 1.0f
            visibility = View.VISIBLE
            isEnabled = true
        }
    }

    private fun hideEditCart() {
        binding.btnEditCart.apply {
            alpha = 0.0f
            visibility = View.GONE
            isEnabled = false
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnEditCart -> {
                toggleCartMode()
                // prevent remove all item progressbar visible
                dismissProgress()
            }
            R.id.btnClearCart -> {
                if (binding.btnClearCart.text.equals(getString(R.string.remove_all))) {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.MYCARTREMOVEALL,
                        requireActivity()
                    )

                    showDeleteConfirmationDialog(ON_CONFIRM_REMOVE_ALL)
                } else {
                    cartItems?.let { cartItems ->
                        for (cartItemGroup: CartItemGroup in cartItems) {
                            val commerceItemList = cartItemGroup.commerceItems
                            for (cm: CommerceItem in commerceItemList) {
                                if (cm.isDeletePressed) {
                                    cm.commerceItemDeletedId(cm)
                                    onItemDeleteClick(cm)
                                }
                            }
                        }
                    }
                }
            }

            R.id.deliveryLocationConstLayout -> locationSelectionClicked()
            R.id.btn_dash_set_address -> {
                (requireActivity() as? BottomNavigator)?.navigateToTabIndex(
                    BottomNavigationActivity.INDEX_PRODUCT,
                    null
                )
            }
            R.id.btnRetry -> {
                if (NetworkManager.getInstance().isConnectedToNetwork(requireActivity())) {
                    errorMessageWasPopUp = false
                    binding.rvCartList.visibility = View.VISIBLE
                    loadShoppingCart()
                }
            }
            R.id.btnCheckOut -> {

                if (binding.btnCheckOut.isEnabled && orderSummary != null) {
                    val deliveryType =
                        getType(Utils.getPreferredDeliveryLocation().fulfillmentDetails.deliveryType)

                    if ((deliveryType === Delivery.CNC) && (productCountMap != null)
                        && (productCountMap?.quantityLimit != null)
                        && !productCountMap?.quantityLimit?.allowsCheckout!!
                    ) {
                        Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.CART_CLCK_CLLCT_CNFRM_LMT,
                            requireActivity()
                        )
                        showMaxItemView()
                        return
                    }

                    if ((deliveryType === Delivery.DASH) && (productCountMap != null)
                        && (productCountMap!!.quantityLimit != null)
                        && !productCountMap!!.quantityLimit!!.allowsCheckout!!
                    ) {
                        showMaxItemView()
                        return
                    }

                    if (deliveryType == Delivery.DASH
                        && WoolworthsApplication.getValidatePlaceDetails()?.deliverable == true
                        && (WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.firstAvailableFoodDeliveryTime.isNullOrEmpty()
                                || WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.deliveryTimeSlots?.isNullOrEmpty() == true)) {
                        showNoTimeSlotsView()
                        return
                    }

                    // Go to Web checkout journey if...
                    if (nativeCheckout?.isNativeCheckoutEnabled == false) {
                        val openCheckOutActivity = Intent(context, CartCheckoutActivity::class.java)
                        requireActivity().startActivityForResult(
                            openCheckOutActivity,
                            CheckOutFragment.REQUEST_CART_REFRESH_ON_DESTROY
                        )
                        requireActivity().overridePendingTransition(0, 0)
                    } else {
                        if (binding.cartProgressBar.visibility == View.VISIBLE) {
                            return
                        }
                        // Get list of saved address and navigate to proper Checkout page.
                        viewModel.getSavedAddress()
                    }
                }
            }
            else -> {}
        }
    }

    private fun toggleCartMode() {
        val isEditMode = toggleEditMode()
        binding.btnEditCart.setText(if (isEditMode) R.string.cancel else R.string.edit)
        binding.btnClearCart.visibility = if (isEditMode) View.VISIBLE else View.GONE
        setPriceInformationVisibility(!isEditMode)
        setDeliveryLocationEnabled(!isEditMode)
        if (!isEditMode)
            setMinimumCartErrorMessage()
    }

    private fun dismissProgress() {
        binding.cartProgressBar.visibility = View.GONE
    }

    private fun showErrorDialog(errorType: Int, errorMessage: String?) {
        val intent = Intent(requireActivity(), ErrorHandlerActivity::class.java)
        intent.putExtra(ErrorHandlerActivity.ERROR_TYPE, errorType)
        intent.putExtra(ErrorHandlerActivity.ERROR_MESSAGE, errorMessage)
        requireActivity().startActivityForResult(intent, ErrorHandlerActivity.RESULT_RETRY)
    }

    private fun navigateToCheckout(response: SavedAddressResponse?) {
        val activity: Activity = requireActivity()
        if (((getPreferredDeliveryType() == Delivery.STANDARD)
                    && !TextUtils.isEmpty(response?.defaultAddressNickname))
        ) {
            //   - CNAV : Checkout  activity
            val beginCheckoutParams = Bundle()
            beginCheckoutParams.putString(
                FirebaseAnalytics.Param.CURRENCY,
                FirebaseManagerAnalyticsProperties.PropertyValues.CURRENCY_VALUE
            )

            val beginCheckoutItem = Bundle()
            beginCheckoutItem.putString(
                FirebaseAnalytics.Param.QUANTITY,
                FirebaseManagerAnalyticsProperties.PropertyValues.INDEX_VALUE
            )
            beginCheckoutItem.putString(
                FirebaseAnalytics.Param.ITEM_BRAND,
                FirebaseManagerAnalyticsProperties.PropertyValues.AFFILIATION_VALUE
            )

            beginCheckoutParams.putParcelableArray(
                FirebaseAnalytics.Param.ITEMS,
                arrayOf(beginCheckoutItem)
            )
            AnalyticsManager.logEvent(
                FirebaseManagerAnalyticsProperties.CART_BEGIN_CHECKOUT,
                beginCheckoutParams
            )

            val checkoutActivityIntent = Intent(activity, CheckoutActivity::class.java)
            checkoutActivityIntent.apply {
                putExtra(CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY, response)
                putExtra(CheckoutAddressConfirmationFragment.IS_EDIT_ADDRESS_SCREEN, true)
                putExtra(CheckoutAddressManagementBaseFragment.GEO_SLOT_SELECTION, true)
            }
            if ((liquorCompliance != null) && liquorCompliance!!.isLiquorOrder && (AppConfigSingleton.liquor!!.noLiquorImgUrl != null) && AppConfigSingleton.liquor!!.noLiquorImgUrl.isNotEmpty()) {
                checkoutActivityIntent.putExtra(
                    Constant.LIQUOR_ORDER,
                    liquorCompliance!!.isLiquorOrder
                )
                checkoutActivityIntent.putExtra(
                    Constant.NO_LIQUOR_IMAGE_URL,
                    AppConfigSingleton.liquor!!.noLiquorImgUrl
                )
            }
            activity.startActivityForResult(
                checkoutActivityIntent,
                REQUEST_PAYMENT_STATUS
            )
            activity.overridePendingTransition(
                R.anim.slide_from_right,
                R.anim.slide_out_to_left
            )
        } else if (getPreferredDeliveryType() == Delivery.DASH &&
            !TextUtils.isEmpty(response?.defaultAddressNickname)
        ) {
            val checkoutActivityIntent = Intent(activity, CheckoutActivity::class.java)
            checkoutActivityIntent.apply {
                putExtra(CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY, response)
                putExtra(CheckoutAddressConfirmationFragment.IS_EDIT_ADDRESS_SCREEN, true)
                putExtra(CheckoutAddressManagementBaseFragment.DASH_SLOT_SELECTION, true)
                putExtra(CheckoutAddressManagementBaseFragment.CART_ITEM_LIST,
                    viewModel.getCartItemList())
                liquorCompliance.let {
                    if ((it != null) && it.isLiquorOrder && (AppConfigSingleton.liquor!!.noLiquorImgUrl != null) && AppConfigSingleton.liquor!!.noLiquorImgUrl.isNotEmpty()) {
                        putExtra(Constant.LIQUOR_ORDER, it.isLiquorOrder)
                        putExtra(
                            Constant.NO_LIQUOR_IMAGE_URL,
                            AppConfigSingleton.liquor!!.noLiquorImgUrl
                        )
                    }
                }
                activity.startActivityForResult(
                    checkoutActivityIntent,
                    REQUEST_PAYMENT_STATUS
                )
            }
            activity.overridePendingTransition(
                R.anim.slide_from_right,
                R.anim.slide_out_to_left
            )
        } else {
//            - GNAV
//            CNC or No Address or no default address*/
            val placeId: String? = when {
                getDelivertyType() === Delivery.CNC -> {
                    getPlaceId()
                }
                else -> {
                    response?.let { getSelectedPlaceId(it) } ?: ""
                }
            }
            presentEditDeliveryGeoLocationActivity(
                requireActivity(),
                REQUEST_PAYMENT_STATUS,
                getDelivertyType(),
                placeId,
                isComingFromCheckout = true,
                isMixedBasket = viewModel.isMixedBasket(),
                isFBHOnly = viewModel.isFBHOnly(),
                isComingFromSlotSelection = false,
                savedAddressResponse = response,
                defaultAddress = null,
                whoISCollecting = "",
                liquorCompliance = liquorCompliance
            )
        }
    }

    override fun onItemDeleteClickInEditMode(commerceItem: CommerceItem) {
        // TODO: Make API call to remove item + show loading before removing from list
        mCommerceItem = commerceItem
        showDeleteConfirmationDialog(ON_CONFIRM_REMOVE_WITH_DELETE_ICON_PRESSED)
    }

    override fun onItemDeleteClick(commerceItem: CommerceItem) {
        mCommerceItem = commerceItem
        showDeleteConfirmationDialog(ON_CONFIRM_REMOVE_WITH_DELETE_PRESSED)
    }

    override fun onCheckBoxChange(isChecked: Boolean, commerceItem: CommerceItem) {
        var listSelectionCounter = 0
        var cartItemCount = 0
        cartItems?.let { cartItems ->
            for (cartItemGroup: CartItemGroup in cartItems) {
                val commerceItemList = cartItemGroup.commerceItems
                for (cm: CommerceItem in commerceItemList) {
                    if (cm.commerceItemInfo.commerceId.equals(commerceItem.commerceItemInfo.commerceId)) {
                        cm.isDeletePressed = isChecked
                    }
                    if (cm.isDeletePressed) {
                        listSelectionCounter++
                    }
                }
                cartItemCount += cartItemGroup.commerceItems.size
            }
        }
        binding.btnClearCart.text =
            if (listSelectionCounter == 0 || (listSelectionCounter > 0 && listSelectionCounter == cartItemCount)) {
                getString(R.string.remove_all)
            } else if (listSelectionCounter in 1 until cartItemCount) {
                getString(R.string.remove_selected)
            } else {
                getString(R.string.remove_all)
            }
    }

    override fun onCartRefresh() {
        //refresh the pricing view
        if(cartProductAdapter?.cartItems?.isNullOrEmpty() == true){
            setPriceInformationVisibility(false)
        } else {
            updatePriceInformation()
        }
    }

    override fun onChangeQuantity(commerceId: CommerceItem, quantity: Int) {
        mCommerceItem = commerceId
        mChangeQuantity?.commerceId = commerceId.commerceItemInfo.getCommerceId()
        mChangeQuantity?.quantity = quantity
        if (WoolworthsApplication.getInstance() != null) {
            Utils.sendBus(CartState(CartState.CHANGE_QUANTITY, quantity))
        }
    }

    override fun totalItemInBasket(total: Int) {}

    override fun onOpenProductDetail(commerceItem: CommerceItem) {
        val activity = requireActivity()
        if (activity !is BottomNavigationActivity) {
            return
        }
        val productDetails = ProductDetails()
        val commerceItemInfo = commerceItem.commerceItemInfo
        productDetails.externalImageRefV2 = commerceItemInfo.externalImageRefV2
        productDetails.productName = commerceItemInfo.productDisplayName
        productDetails.fromPrice = commerceItem.priceInfo.getAmount().toFloat()
        productDetails.productId = commerceItemInfo.productId
        productDetails.sku = commerceItemInfo.catalogRefId
        val strProductList = Gson().toJson(productDetails)

        // Move to shop tab first.
        (activity as? BottomNavigationActivity)?.bottomNavigationById?.currentItem =
            BottomNavigationActivity.INDEX_PRODUCT
        ScreenManager.openProductDetailFragment(activity, "", strProductList)
    }

    override fun onGiftItemClicked(commerceItem: CommerceItem) {
        val activity: FragmentActivity = requireActivity()
        val giftWithPurchaseDialogDetailFragment = GiftWithPurchaseDialogDetailFragment()
        giftWithPurchaseDialogDetailFragment.show(
            (activity as AppCompatActivity).supportFragmentManager,
            GiftWithPurchaseDialogDetailFragment::class.java.simpleName
        )
    }

    private fun toggleEditMode(): Boolean {
        val isEditMode = cartProductAdapter?.toggleEditMode() ?: false
        if (isAllInventoryAPICallSucceed) Utils.fadeInFadeOutAnimation(
            binding.btnCheckOut,
            isEditMode
        )
        resetItemDelete(isEditMode)
        return isEditMode
    }

    private fun setPriceInformationVisibility(visibility: Boolean){
        binding.includedPrice.orderSummeryLayout.visibility = if(visibility) View.VISIBLE else View.GONE
    }

    private fun setPriceValue(textView: WTextView, value: Double) {
        textView.setText(CurrencyFormatter.formatAmountToRandAndCentWithSpace(value))
    }

    private fun setDiscountPriceValue(textView: WTextView, value: Double) {
        textView.setText("- " + CurrencyFormatter.formatAmountToRandAndCentWithSpace(value))
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun resetItemDelete(isEditMode: Boolean) {
        if (isEditMode) {
            cartItems?.let { cartItems ->
                for (cartItemGroup: CartItemGroup in cartItems) {
                    val commerceItemList = cartItemGroup.commerceItems
                    for (cm: CommerceItem in commerceItemList) {
                        cm.setDeleteIconWasPressed(false)
                        cm.isDeletePressed = false
                    }
                }
            }
        }
        cartProductAdapter?.notifyDataSetChanged()
        onCartRefresh()
    }

    private fun locationSelectionClicked() {
        presentEditDeliveryGeoLocationActivity(
            requireActivity(),
            REQUEST_SUBURB_CHANGE,
            getPreferredDeliveryType(),
            getPreferredPlaceId(),
            isComingFromCheckout = false,
            isComingFromSlotSelection = false,
            savedAddressResponse = null,
            defaultAddress = null,
            whoISCollecting = "",
            liquorCompliance = liquorCompliance
        )
    }

    fun bindCartData(cartResponse: CartResponse?) {
        binding.apply {
            parentLayout.visibility = View.VISIBLE
            mSkuInventories = HashMap()
            when {
                cartResponse != null && (cartResponse.cartItems?.size ?: 0) > 0 -> {
                    emptyStateTemplate.root.visibility = View.GONE
                    rvCartList.visibility = View.VISIBLE
                    rlCheckOut.visibility = View.VISIBLE
                    showEditCart()
                    cartItems = cartResponse.cartItems
                    orderSummary = cartResponse.orderSummary
                    voucherDetails = cartResponse.voucherDetails
                    isBlackCardHolder = cartResponse.blackCardHolder
                    productCountMap = cartResponse.productCountMap
                    liquorCompliance = LiquorCompliance(
                        cartResponse.liquorOrder,
                        if (cartResponse.noLiquorImageUrl != null) cartResponse.noLiquorImageUrl else ""
                    )
                    cartProductAdapter = CartProductAdapter(
                        cartItems,
                        this@CartFragment,
                        orderSummary,
                        requireActivity()
                    )
                    queryServiceInventoryCall(cartResponse.cartItems)
                    val mLayoutManager = LinearLayoutManager(activity)
                    mLayoutManager.orientation = LinearLayoutManager.VERTICAL
                    rvCartList.layoutManager = mLayoutManager
                    rvCartList.adapter = cartProductAdapter
                    updateOrderTotal()
                    isMaterialPopUpClosed = false
                    showRedeemVoucherFeatureWalkthrough()
                }
                else -> {
                    productCountMap = null
                    updateCartSummary(0)
                    rvCartList.visibility = View.GONE
                    rlCheckOut.visibility = View.GONE
                    onRemoveSuccess()
                    setEmptyCartUIUserName()
                    setDeliveryLocationEnabled(true)
                    resetToolBarIcons()
                    isMaterialPopUpClosed = true
                    showEditDeliveryLocationFeatureWalkthrough()
                }
            }
            setItemLimitsBanner()
        }
    }

    private fun updatePriceInformation() {
        val priceHolder = binding.includedPrice
        if (orderSummary != null) {
            setPriceInformationVisibility(true)
            orderSummary?.basketTotal?.let {
                setPriceValue(priceHolder.txtYourCartPrice, it)
            }
            priceHolder.orderTotal.text = CurrencyFormatter.formatAmountToRandAndCentWithSpace(
                orderSummary?.total
            )
            val discountDetails = orderSummary?.discountDetails
            if (discountDetails != null) {

                if (discountDetails.companyDiscount > 0) {
                    setDiscountPriceValue(
                        priceHolder.txtCompanyDiscount,
                        discountDetails.companyDiscount
                    )
                    priceHolder.rlCompanyDiscount.visibility = View.VISIBLE
                } else {
                    priceHolder.rlCompanyDiscount.visibility = View.GONE
                }
                if (discountDetails.totalOrderDiscount > 0) {
                    setDiscountPriceValue(
                        priceHolder.txtTotalDiscount,
                        discountDetails.totalOrderDiscount
                    )
                    priceHolder.rlTotalDiscount.visibility = View.VISIBLE
                } else {
                    priceHolder.rlTotalDiscount.visibility = View.GONE
                }
                if (discountDetails.otherDiscount > 0) {
                    setDiscountPriceValue(
                        priceHolder.txtDiscount,
                        discountDetails.otherDiscount
                    )
                    priceHolder.rlDiscount.visibility = View.VISIBLE
                } else {
                    priceHolder.rlDiscount.visibility = View.GONE
                }
                if (discountDetails.voucherDiscount > 0) {
                    setDiscountPriceValue(
                        priceHolder.txtWrewardsDiscount,
                        discountDetails.voucherDiscount
                    )
                    priceHolder.rlWrewardsDiscount.visibility = View.VISIBLE
                } else {
                    priceHolder.rlWrewardsDiscount.visibility = View.GONE
                }
                if (discountDetails.promoCodeDiscount > 0) {
                    setDiscountPriceValue(
                        priceHolder.txtPromoCodeDiscount,
                        discountDetails.promoCodeDiscount
                    )
                    priceHolder.rlPromoCodeDiscount.visibility = View.VISIBLE
                } else {
                    priceHolder.rlPromoCodeDiscount.visibility = View.GONE
                }
            }
        } else {
            setPriceInformationVisibility(false)
        }
        priceHolder.vouchersMain.rlAvailableWRewardsVouchers.setOnClickListener {
            onViewVouchers()
            triggerFirebaseEventForCart(appliedVouchersCount)
        }
        priceHolder.vouchersMain.rlAvailableCashVouchers?.setOnClickListener {
            onViewCashBackVouchers()
            triggerFirebaseEventForCart(appliedVouchersCount)
        }

        if (voucherDetails == null) {
            return
        }
        val activeCashVouchersCount = voucherDetails?.let {
            it.activeCashVouchersCount
        }
        if (activeCashVouchersCount != null && activeCashVouchersCount > 0) {
            val availableVouchersLabel =
                resources?.getQuantityString(
                    R.plurals.available_cash_vouchers_message,
                    activeCashVouchersCount,
                    activeCashVouchersCount
                )
            priceHolder.vouchersMain.availableCashVouchersCount.text = availableVouchersLabel
            priceHolder.vouchersMain.viewCashVouchers.isEnabled = true
            priceHolder.vouchersMain.rlAvailableCashVouchers.isClickable = true
        } else {
            priceHolder.vouchersMain.availableCashVouchersCount.text =
                getString(R.string.zero_cash_vouchers_available)
            priceHolder.vouchersMain.viewCashVouchers.isEnabled = false
            priceHolder.vouchersMain.rlAvailableCashVouchers.isClickable = false
        }

        val activeVouchersCount = voucherDetails?.let {
            it.activeVouchersCount
        }
        if (activeVouchersCount != null && activeVouchersCount > 0) {
            if (appliedVouchersCount > 0) {
                val availableVouchersLabel =
                    resources?.getQuantityString(
                        R.plurals._rewards_vouchers_message_applied,
                        appliedVouchersCount,
                        appliedVouchersCount
                    )
                priceHolder.vouchersMain.availableVouchersCount.text = availableVouchersLabel
                priceHolder.vouchersMain.viewVouchers.text = getString(R.string.edit)
                priceHolder.vouchersMain.viewVouchers.isEnabled = true
                priceHolder.vouchersMain.rlAvailableWRewardsVouchers.isClickable = true
            } else {
                val availableVouchersLabel =
                    resources?.getQuantityString(
                        R.plurals.available_rewards_vouchers_message,
                        activeVouchersCount,
                        activeVouchersCount
                    )
                priceHolder.vouchersMain.availableVouchersCount.text = availableVouchersLabel
                priceHolder.vouchersMain.viewVouchers.text = getString(R.string.view)
                priceHolder.vouchersMain.viewVouchers.isEnabled = true
                priceHolder.vouchersMain.rlAvailableWRewardsVouchers.isClickable = true
            }
        } else {
            priceHolder.vouchersMain.availableVouchersCount.text =
                getString(R.string.zero_wrewards_vouchers_available)
            priceHolder.vouchersMain.viewVouchers.text = getString(R.string.view)
            priceHolder.vouchersMain.viewVouchers.isEnabled = false
            priceHolder.vouchersMain.rlAvailableWRewardsVouchers.isClickable = false

        }
        priceHolder.vouchersMain.promoCodeAction.text =
            getString(if (voucherDetails?.promoCodes != null && voucherDetails!!.promoCodes.size > 0) R.string.remove else R.string.enter)
        if (voucherDetails!!.promoCodes != null && voucherDetails!!.promoCodes.size > 0) {
            val appliedPromoCodeText =
                getString(R.string.promo_code_applied) + voucherDetails!!.promoCodes[0].promoCode
            priceHolder.vouchersMain.promoCodeLabel.text = appliedPromoCodeText
        } else {
            priceHolder.vouchersMain.promoCodeLabel.text =
                getString(R.string.do_you_have_a_promo_code)
        }
        priceHolder.vouchersMain.rlPromoCode.setOnClickListener {
            if (voucherDetails!!.promoCodes != null && voucherDetails!!.promoCodes.size > 0) onRemovePromoCode(
                voucherDetails!!.promoCodes[0].promoCode
            ) else onEnterPromoCode()
        }
        priceHolder.promoDiscountInfo.setOnClickListener { onPromoDiscountInfo() }
        if (liquorCompliance != null && liquorCompliance!!.isLiquorOrder) {
            priceHolder.liquorComplianceMain.liquorBannerRootConstraintLayout.visibility = View.VISIBLE
            if (!AppConfigSingleton.liquor?.noLiquorImgUrl.isNullOrEmpty()) ImageManager.setPicture(
                priceHolder.liquorComplianceMain.imgLiquorBanner,
                AppConfigSingleton.liquor?.noLiquorImgUrl
            )
        } else {
            priceHolder.liquorComplianceMain.liquorBannerRootConstraintLayout.visibility = View.GONE
        }
        if (getPreferredDeliveryType() == Delivery.CNC) {
            priceHolder.deliveryFeeLabel.text = getString(R.string.collection_fee)
        }
    }

    private fun triggerFirebaseEventForCart(appliedVouchersCount: Int) {
        activity?.let { context ->
            Utils.triggerFireBaseEvents(
                if (appliedVouchersCount > 0) FirebaseManagerAnalyticsProperties.Cart_ovr_edit else FirebaseManagerAnalyticsProperties.Cart_ovr_view,
                context
            )
        }
    }

    private val appliedVouchersCount: Int
        get() = if (voucherDetails == null) {
            -1
        } else getAppliedVouchersCount(voucherDetails!!.vouchers)

    fun updateCart(cartResponse: CartResponse?, commerceItemToRemove: CommerceItem?) {
        orderSummary = cartResponse?.orderSummary
        voucherDetails = cartResponse?.voucherDetails
       isBlackCardHolder = cartResponse?.blackCardHolder ?: false
        productCountMap = cartResponse?.productCountMap
        liquorCompliance =
            (if (cartResponse?.noLiquorImageUrl != null) cartResponse?.noLiquorImageUrl else "")?.let {
                LiquorCompliance(
                    cartResponse?.liquorOrder ?: false,
                    it
                )
            }
        setItemLimitsBanner()
        if ((cartResponse?.cartItems?.size ?: 0) > 0 && cartProductAdapter != null) {
            val emptyCartItemGroups = ArrayList<CartItemGroup>(0)
            cartItems?.forEach { cartItemGroup: CartItemGroup ->
                if (commerceItemToRemove != null) {
                    for (commerceItem: CommerceItem in cartItemGroup.commerceItems) {
                        if (commerceItem.commerceItemInfo.commerceId.equals(
                                commerceItemToRemove.commerceItemInfo.commerceId,
                                ignoreCase = true
                            )
                        ) {
                            cartItemGroup.commerceItems.remove(commerceItem)
                            break
                        }
                    }
                }
                for (commerceItem: CommerceItem in cartItemGroup.commerceItems) {
                    val updatedCommerceItem = cartResponse?.let {
                        filterCommerceItemFromCartResponse(
                            it, commerceItem.commerceItemInfo.commerceId
                        )
                    }
                    if (updatedCommerceItem != null) {
                        commerceItem.priceInfo = updatedCommerceItem.priceInfo
                    }
                }
                if (cartItemGroup.type.equals("GIFT", ignoreCase = true)) {
                    var isGiftsThere = false
                    cartResponse?.cartItems?.forEach { UpdatedCartItemGroup: CartItemGroup ->
                        if (UpdatedCartItemGroup.type.equals("GIFT", ignoreCase = true)) {
                            cartItemGroup.commerceItems = UpdatedCartItemGroup.commerceItems
                            isGiftsThere = true
                        }
                    }
                    if (!isGiftsThere) cartItemGroup.commerceItems.clear()
                }
                /***
                 * Remove header when commerceItems is empty
                 */
                /***
                 * Remove header when commerceItems is empty
                 */
                if (cartItemGroup.commerceItems.size == 0) {
                    emptyCartItemGroups.add(cartItemGroup) // Gather all the empty groups after deleting item.
                }
            }

            //remove all the empty groups
            for (cartItemGroup: CartItemGroup in emptyCartItemGroups) {
                cartItems?.remove(cartItemGroup)
            }
            cartProductAdapter?.notifyAdapter(
                cartItems,
                orderSummary,
            )
        } else {
            cartProductAdapter?.clear()
            resetToolBarIcons()
            binding.apply {
                rlCheckOut.visibility = View.GONE
                rvCartList.visibility = View.GONE
                emptyStateTemplate.root.visibility = View.VISIBLE
            }
            setDeliveryLocationEnabled(true)
        }

        productCountMap?.totalProductCount?.let { count ->
            updateCartSummary(if (count > 0) count else 0)
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (!mDisposables.isDisposed) {
            mDisposables.dispose()
        }
        (requireActivity() as? BottomNavigationActivity)?.walkThroughPromtView?.removeFromWindow()
    }

    fun changeQuantity(cartResponse: CartResponse?, changeQuantity: ChangeQuantity?) {
        if ((cartResponse?.cartItems?.size ?: 0) > 0 && cartProductAdapter != null) {
            val updatedCommerceItem =
                cartResponse?.cartItems?.let { cartItems ->
                    changeQuantity?.commerceId?.let { commerceId ->
                        getUpdatedCommerceItem(
                            cartItems,
                            commerceId
                        )
                    }
                }
            //update list instead of using the new list to handle inventory data
            cartResponse?.cartItems?.forEach { cartItemGroupUpdated: CartItemGroup ->
                var isGroup = false
                cartItems?.let { cartItems ->
                    for (cartItemGroup in cartItems) {
                        if (cartItemGroupUpdated.type.equals(
                                cartItemGroup.type,
                                ignoreCase = true
                            )
                        ) {
                            isGroup = true
                            break
                        }
                    }
                }
                if (!isGroup) cartItems?.add(cartItemGroupUpdated)
            }
            if (updatedCommerceItem != null) {
                val emptyCartItemGroups = ArrayList<CartItemGroup>()
                cartItems?.forEach { cartItemGroup: CartItemGroup ->
                    for (commerceItem: CommerceItem in cartItemGroup.commerceItems) {
                        if (commerceItem.commerceItemInfo.commerceId.equals(
                                updatedCommerceItem.commerceItemInfo.commerceId,
                                ignoreCase = true
                            )
                        ) {
                            commerceItem.commerceItemInfo = updatedCommerceItem.commerceItemInfo
                            commerceItem.priceInfo = updatedCommerceItem.priceInfo
                            commerceItem.quantityUploading = false
                        }
                    }
                    if (cartItemGroup.type.equals("GIFT", ignoreCase = true)) {
                        var isGiftsThere = false
                        for (UpdatedCartItemGroup: CartItemGroup in cartResponse.cartItems) {
                            if (UpdatedCartItemGroup.type.equals("GIFT", ignoreCase = true)) {
                                cartItemGroup.commerceItems = UpdatedCartItemGroup.commerceItems
                                isGiftsThere = true
                            }
                        }
                        if (!isGiftsThere) cartItemGroup.commerceItems.clear()
                    }
                    /***
                     * Remove header when commerceItems is empty
                     */
                    /***
                     * Remove header when commerceItems is empty
                     */
                    if (cartItemGroup.commerceItems.size == 0) {
                        emptyCartItemGroups.add(cartItemGroup) // Gather all the empty groups after deleting item.
                    }
                }

                //remove all the empty groups
                for (cartItemGroup: CartItemGroup in emptyCartItemGroups) {
                    cartItems?.remove(cartItemGroup)
                }

                orderSummary = cartResponse.orderSummary
                voucherDetails = cartResponse.voucherDetails
                isBlackCardHolder = cartResponse.blackCardHolder
                productCountMap = cartResponse.productCountMap
                liquorCompliance = LiquorCompliance(
                    cartResponse.liquorOrder,
                    if (cartResponse.noLiquorImageUrl != null) cartResponse.noLiquorImageUrl else ""
                )
                cartProductAdapter!!.notifyAdapter(
                    cartItems,
                    orderSummary,
                )
            } else {
                val currentCartItemGroup = cartProductAdapter?.cartItems
                currentCartItemGroup?.forEach { cartItemGroup: CartItemGroup ->
                    for (currentItem: CommerceItem in cartItemGroup.commerceItems) {
                        if (currentItem.commerceItemInfo.commerceId.equals(
                                changeQuantity?.commerceId,
                                ignoreCase = true
                            )
                        ) {
                            cartItemGroup.commerceItems.remove(currentItem)
                            if (cartItemGroup.commerceItems.size == 0) {
                                currentCartItemGroup.remove(cartItemGroup)
                            }
                            break
                        }
                    }
                }
                var shouldEnableCheckOutAndEditButton = true
                currentCartItemGroup?.forEach { items: CartItemGroup ->
                    for (commerceItem: CommerceItem in items.commerceItems) {
                        if (commerceItem.quantityUploading) {
                            shouldEnableCheckOutAndEditButton = false
                            break
                        }
                    }
                }
                if (shouldEnableCheckOutAndEditButton) {
                    orderSummary = cartResponse?.orderSummary
                    voucherDetails = cartResponse?.voucherDetails
                    isBlackCardHolder = cartResponse?.blackCardHolder ?: false
                    productCountMap = cartResponse?.productCountMap
                    liquorCompliance = LiquorCompliance(
                        cartResponse?.liquorOrder ?: false,
                        cartResponse?.noLiquorImageUrl ?: ""
                    )
                    cartProductAdapter!!.notifyAdapter(
                        currentCartItemGroup,
                        orderSummary
                    )
                    fadeCheckoutButton(false)
                }
            }
            productCountMap?.totalProductCount?.let { count ->
                if (count > 0) {
                    instance.setCartCount((count))
                }
            }
        } else {
            cartProductAdapter?.clear()
            resetToolBarIcons()

            binding.apply {
                rlCheckOut.visibility = View.GONE
                rvCartList.visibility = View.GONE
                emptyStateTemplate.root.visibility = View.VISIBLE
            }
        }
        onChangeQuantityComplete()
        setMinimumCartErrorMessage()
        setItemLimitsBanner()
    }

    private fun setMinimumCartErrorMessage() {
        if (orderSummary?.hasMinimumBasketAmount == false) {
            orderSummary?.minimumBasketAmount?.let { minBasketAmount ->
                binding.txtMinSpendErrorMsg.apply {
                    visibility = View.VISIBLE
                    text =
                        String.format(
                            getString(
                                R.string.minspend_error_msg_cart,
                                CurrencyFormatter.formatAmountToRandNoDecimal(minBasketAmount)
                            )
                        )
                }
            }
            binding.btnCheckOut.isEnabled = false
            fadeCheckoutButton(true)
            enableEditCart(false)
        } else {
            binding.txtMinSpendErrorMsg.visibility = View.GONE
            if (binding.btnEditCart.text?.equals(R.string.edit) == false)
                binding.btnCheckOut.isEnabled = true
        }
    }

    private fun getUpdatedCommerceItem(
        cartItems: ArrayList<CartItemGroup>,
        commerceId: String,
    ): CommerceItem? {
        for (cartItemGroup: CartItemGroup in cartItems) {
            for (commerceItem: CommerceItem in cartItemGroup.commerceItems) {
                if (commerceItem.commerceItemInfo.commerceId.equals(
                        commerceId,
                        ignoreCase = true
                    )
                ) return commerceItem
            }
        }
        return null
    }

    private fun updateCartSummary(cartCount: Int) {
        instance.setCartCount(cartCount)
        if (cartCount == 0) {
            setEmptyCartUIUserName()
        }
    }

    private fun onChangeQuantityComplete() {
        var quantityUploaded = false
        cartItems?.forEach { cartItemGroup: CartItemGroup ->
            for (commerceItem: CommerceItem in cartItemGroup.commerceItems) {
                if (commerceItem.quantityUploading) quantityUploaded = true
            }
        }
        if (isAllInventoryAPICallSucceed && !quantityUploaded) {
            mChangeQuantityList = ArrayList()
            fadeCheckoutButton(false)
        }
        cartProductAdapter?.onChangeQuantityComplete()
    }

    private fun loadShoppingCart() {
        viewModel.getShoppingCartV2()
    }

    private fun onCartV2Response(
        shoppingCartResponse: CartResponse?,
    ) {
        if (!isAdded || !isVisible) return
        when (shoppingCartResponse?.httpCode) {
            HTTP_OK, HTTP_OK_201 -> {
                binding.cartProgressBar.visibility = View.GONE
                setDeliveryLocationEnabled(true)
                onRemoveItemFailed = false
                binding.rlCheckOut.visibility = View.VISIBLE
                binding.rlCheckOut.isEnabled = true
                updateUIForCartResponse(shoppingCartResponse)
                updateCheckOutLink(shoppingCartResponse.jSessionId)
                bindCartData(shoppingCartResponse)
                if (isOnItemRemoved) {
                    cartProductAdapter?.setEditMode(true)
                    isOnItemRemoved = false
                }
                setDeliveryLocationEnabled(true)
                if (shoppingCartResponse.orderSummary.fulfillmentDetails?.address?.placeId != null) {
                    Utils.savePreferredDeliveryLocation(
                        ShoppingDeliveryLocation(
                            shoppingCartResponse.orderSummary.fulfillmentDetails
                        )
                    )
                }
                setItemLimitsBanner()
                instance.queryCartSummaryCount()
                showRecommendedProducts()
            }
            HTTP_SESSION_TIMEOUT_440 -> {
                //TODO:: improve error handling
                SessionUtilities.getInstance()
                    .setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                SessionExpiredUtilities.getInstance().showSessionExpireDialog(
                    requireActivity() as AppCompatActivity?,
                    this@CartFragment
                )
                onChangeQuantityComplete()
            }
            else -> {
                when (shoppingCartResponse?.response) {
                    null -> {
                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                if (!isOnItemRemoved) {
                                    setDeliveryLocationEnabled(true)
                                    binding.apply {
                                        rvCartList.visibility = View.GONE
                                        rlCheckOut.visibility = View.GONE
                                        cartProgressBar.visibility = View.GONE
                                    }
                                    mErrorHandlerView?.showErrorHandler()
                                }
                            }
                        }
                    }
                    else -> {
                        setDeliveryLocationEnabled(true)
                        shoppingCartResponse?.response?.let {
                            Utils.displayValidationMessage(
                                requireActivity(),
                                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                it.desc,
                                true
                            )
                        }
                    }
                }

            }
        }
    }

    private fun showRecommendedProducts() {
        val bundle = Bundle()
        val cartLinesValue: MutableList<CartProducts> = arrayListOf()

        cartItems?.forEach { item ->
            cartLinesValue.addAll(item.commerceItems.map {
                CartProducts(it.commerceItemInfo.productId, it.commerceItemInfo.quantity, it.priceInfo.amount, it.commerceItemInfo.catalogRefId, Constants.CURRENCY_VALUE)
            })
        }

        bundle.putParcelable(
            BundleKeysConstants.RECOMMENDATIONS_EVENT_DATA, Event(eventType = "monetate:context:PageView", url = "/cart", pageType = "cart", null, null, null)
        )
        bundle.putParcelable(
            BundleKeysConstants.RECOMMENDATIONS_EVENT_DATA_TYPE, Event(eventType = "monetate:context:Cart", null, null, null, null, cartLinesValue
            )
        )
        bundle.putParcelable(
            BundleKeysConstants.RECOMMENDATIONS_USER_AGENT, Event(
                eventType = BundleKeysConstants.RECOMMENDATIONS_USER_AGENT,
                userAgent = System.getProperty("http.agent") ?: ""
            )
        )
        bundle.putParcelable(
            BundleKeysConstants.RECOMMENDATIONS_IP_ADDRESS,
            Event(eventType = BundleKeysConstants.RECOMMENDATIONS_IP_ADDRESS,
                ipAddress = getIpAddress(requireActivity())
            )
        )

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.navHostRecommendation) as NavHostFragment
        val navController = navHostFragment?.navController
        val navGraph = navController?.navInflater?.inflate(R.navigation.nav_recommendation_graph)

        navGraph?.startDestination = R.id.recommendationFragment
        navGraph?.let {
            navController?.graph = it
        }
        navGraph?.let {
            navController?.setGraph(
                it, bundleOf("bundle" to bundle)
            )
        }
    }

    private fun removeItem(commerceItem: CommerceItem) {
        removeCartItem(commerceItem.commerceItemInfo.commerceId).enqueue(
            CompletionHandler(
                (object : IResponseListener<ShoppingCartResponse> {
                    override fun onSuccess(response: ShoppingCartResponse?) {}
                    override fun onFailure(error: Throwable?) {}
                }), ShoppingCartResponse::class.java
            )
        )
    }

    private fun onRemoveItemLoadFail(commerceItem: CommerceItem) {
        mCommerceItem = commerceItem
        resetItemDelete(true)
    }

    private fun updateUIForCartResponse(response: CartResponse?) {
        if (response == null) return
        displayUpSellMessage(response.globalMessages)
        val fulfillmentDetailsObj = response?.orderSummary?.fulfillmentDetails
        if (fulfillmentDetailsObj?.address?.placeId != null) {
            val shoppingDeliveryLocation = ShoppingDeliveryLocation(fulfillmentDetailsObj)
            setDeliveryLocation(shoppingDeliveryLocation)
        } else {
            // If user logs out and login with new registration who don't have location.
            setDeliveryLocation(ShoppingDeliveryLocation(fulfillmentDetailsObj))
        }
    }

    override fun onResume() {
        super.onResume()
        val activity: Activity = requireActivity()
        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.CART_LIST)
        activity.registerReceiver(
            mConnectionBroadcast,
            IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        )
        val lastDeliveryLocation = Utils.getPreferredDeliveryLocation()
        lastDeliveryLocation?.let { setDeliveryLocation(it) }
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.VIEW_CART, requireActivity())
    }

    override fun onPause() {
        super.onPause()
        mConnectionBroadcast?.let {
            requireActivity().unregisterReceiver(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == CustomPopUpWindow.CART_DEFAULT_ERROR_TAPPED || resultCode == ActionSheetDialogFragment.DIALOG_REQUEST_CODE) {
            val activity: Activity = requireActivity()
            activity.setResult(CustomPopUpWindow.CART_DEFAULT_ERROR_TAPPED)
            activity.finish()
            activity.overridePendingTransition(R.anim.slide_down_anim, R.anim.stay)
            return
        }
        if (requestCode == SSOActivity.SSOActivityResult.LAUNCH.rawValue()) {
            if (SessionUtilities.getInstance().isUserAuthenticated) {
                if (resultCode == Activity.RESULT_OK) {
                    // Checkout completed successfully
                    val lastDeliveryLocation = Utils.getPreferredDeliveryLocation()
                    if (lastDeliveryLocation != null) {
                        binding.apply {
                            // Show loading state
                            rlCheckOut.visibility = View.GONE
                            cartProgressBar.visibility = View.VISIBLE
                        }

                        cartProductAdapter?.clear()

                        hideEditCart()
                        //TODO: need to refactor
                        /* Call<SetDeliveryLocationSuburbResponse> setDeliveryLocationSuburb = OneAppService.INSTANCE.setSuburb(lastDeliveryLocation.storePickup ? lastDeliveryLocation.store.getId() : lastDeliveryLocation.suburb.id);
                        setDeliveryLocationSuburb.enqueue(new CompletionHandler<>(new IResponseListener<SetDeliveryLocationSuburbResponse>() {
                            @Override
                            public void onSuccess(SetDeliveryLocationSuburbResponse setDeliveryLocationSuburbResponse) {
                                if (setDeliveryLocationSuburbResponse.httpCode == 200) {
                                    Utils.savePreferredDeliveryLocation(lastDeliveryLocation);
                                    setDeliveryLocation(lastDeliveryLocation);
                                    //Utils.sendBus(new CartState(lastDeliveryLocation.suburb.name + ", " + lastDeliveryLocation.province.name));
                                }
                                loadShoppingCart(false);
                                loadShoppingCartAndSetDeliveryLocation();
                            }

                            @Override
                            public void onFailure(Throwable error) {
                                Activity activity = getActivity();
                                if (activity == null || error.getMessage() == null) return;

                                activity.runOnUiThread(() -> {
                                            loadShoppingCart(false);
                                            loadShoppingCartAndSetDeliveryLocation();
                                        }
                                );

                            }
                        }, SetDeliveryLocationSuburbResponse.class));*/
                    } else {
                        // Fallback if there is no cached location
                        loadShoppingCart()
                        loadShoppingCartAndSetDeliveryLocation()
                    }
                } else {
                    // Checkout was cancelled
                    loadShoppingCart()
                    loadShoppingCartAndSetDeliveryLocation()
                }
            } else {
                requireActivity().onBackPressed()
            }
        } else if (requestCode == CART_BACK_PRESSED_CODE) {
            reloadFragment()
            return
        } else if (requestCode == PDP_LOCATION_CHANGED_BACK_PRESSED_CODE || requestCode == ScreenManager.SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE) {
            reloadFragment()
        }
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                BottomNavigationActivity.PDP_REQUEST_CODE -> {
                    val activity: FragmentActivity = activity ?: return
                    loadShoppingCart()
                    loadShoppingCartAndSetDeliveryLocation()
                    val productCountMap = Utils.jsonStringToObject(
                        data?.getStringExtra("ProductCountMap"), ProductCountMap::class.java
                    ) as ProductCountMap
                    val itemsCount = data?.getIntExtra("ItemsCount", 0)
                    if ((isDeliveryOptionClickAndCollect() || isDeliveryOptionDash())
                        && productCountMap.quantityLimit?.foodLayoutColour != null
                    ) {
                        showItemsLimitToastOnAddToCart(
                            binding.rlCheckOut,
                            productCountMap,
                            activity,
                            count = itemsCount ?: 0,
                            viewButtonVisible = false
                        )
                    } else {
                        buildAddToCartSuccessToast(binding.rlCheckOut, false, activity, null)
                    }
                }
                REDEEM_VOUCHERS_REQUEST_CODE, APPLY_PROMO_CODE_REQUEST_CODE -> {
                    val shoppingCartResponse = Utils.strToJson(
                        data?.getStringExtra("ShoppingCartResponse"),
                        ShoppingCartResponse::class.java
                    ) as ShoppingCartResponse
                    val cartResponse = viewModel.getConvertedCartResponse(shoppingCartResponse)
                    updateUIForCartResponse(cartResponse)
                    updateCart(cartResponse, null)
                    if (requestCode == REDEEM_VOUCHERS_REQUEST_CODE) showVouchersOrPromoCodeAppliedToast(
                        getString(
                            if (voucherDetails?.vouchers?.let {
                                    getAppliedVouchersCount(
                                        it
                                    )
                                } ?: 0 > 0
                            ) R.string.vouchers_applied_toast_message else R.string.vouchers_removed_toast_message
                        )
                    )
                    if (requestCode == APPLY_PROMO_CODE_REQUEST_CODE) showVouchersOrPromoCodeAppliedToast(
                        getString(R.string.promo_code_applied_toast_message)
                    )
                }
                else -> {}
            }
        }
        if (requestCode == REQUEST_PAYMENT_STATUS) {
            when (resultCode) {
                CheckOutFragment.REQUEST_CHECKOUT_ON_DESTROY -> reloadFragment()
                CheckOutFragment.RESULT_RELOAD_CART -> reloadFragment()
                Activity.RESULT_OK -> requireActivity().onBackPressed()
                Activity.RESULT_CANCELED -> reloadFragment()
            }
        }
        if (requestCode == REQUEST_SUBURB_CHANGE) {
            initializeLoggedInUserCartUI()
            loadShoppingCartAndSetDeliveryLocation()
        }
        if (requestCode == ScreenManager.CART_LAUNCH_VALUE && resultCode == SSOActivity.SSOActivityResult.STATE_MISMATCH.rawValue()) {
            // login screen opens on cart and user closes it without login then move tab to last opened tab.
            (requireActivity() as? BottomNavigationActivity)?.let { activity ->
                val previousTabIndex = activity.previousTabIndex
                activity.bottomNavigationById.currentItem = previousTabIndex
            }
        }

        // Retry callback when saved address api fails
        if (resultCode == ErrorHandlerActivity.RESULT_RETRY) {
            viewModel.getSavedAddress()
        }
    }

    /* private fun checkLocationChangeAndReload() {
        //TODO: need to refactor
         ShoppingDeliveryLocation deliveryLocation = Utils.getPreferredDeliveryLocation();
        String currentSuburbId = null;
        String currentStoreId = null;
        int currentCartCount = QueryBadgeCounter.getInstance().getCartCount();
        if (deliveryLocation != null) {
            if (deliveryLocation.suburb != null)
                currentSuburbId = deliveryLocation.suburb.id;
            if (deliveryLocation.store != null)
                currentStoreId = deliveryLocation.store.getId();
        }
        if (currentStoreId == null && currentSuburbId == null) {
            //Fresh install with no location selection.
        } else if (currentSuburbId == null && !(currentStoreId.equals(localStoreId))) {
            localStoreId = currentStoreId;
            localSuburbId = null;
            reloadFragment();
            return;

        } else if (currentStoreId == null && !(localSuburbId.equals(currentSuburbId))) {
            localSuburbId = currentSuburbId;
            localStoreId = null;
            reloadFragment();
            return;
        } else if (productCountMap != null && productCountMap.getTotalProductCount() != currentCartCount) {
            reloadFragment();
        }
    }*/

    private fun loadShoppingCartAndSetDeliveryLocation() {
        val lastDeliveryLocation = Utils.getPreferredDeliveryLocation()
        lastDeliveryLocation?.let { setDeliveryLocation(it) }
    }

    fun reloadFragment() {
        //Reload screen
        setPriceInformationVisibility(false)
        setupToolbar()
        initializeBottomTab()
        initializeLoggedInUserCartUI()
        loadShoppingCart()
    }

    override fun onConnectionChanged() {
        if (onRemoveItemFailed) {
            mErrorHandlerView?.hideErrorHandler()
            isOnItemRemoved = true
            loadShoppingCart()
            return
        }
        if (mRemoveAllItemFailed) {
            viewModel.removeAllCartItem()
            mRemoveAllItemFailed = false
            return
        }
        if (changeQuantityWasClicked) {
            mCommerceItem?.let { cartProductAdapter?.onChangeQuantityLoad(it) }
            queryServiceChangeQuantity()
            changeQuantityWasClicked = false
        }
    }

    private fun removeItemAPI(commerceItem: CommerceItem) {
        mCommerceItem = commerceItem
        viewModel.removeCartItem(commerceItem.commerceItemInfo.getCommerceId())
    }

    private fun queryServiceInventoryCall(items: ArrayList<CartItemGroup>) {
        val multiMapCommerceItem = MultiMap.create<String, CommerceItem>()
        fadeCheckoutButton(true)
        for (cartItemGroup: CartItemGroup in items) {
            for (commerceItem: CommerceItem in cartItemGroup.getCommerceItems()) {
                multiMapCommerceItem.put(commerceItem.fulfillmentStoreId, commerceItem)
            }
        }
        mapStoreIdWithCommerceItems = multiMapCommerceItem.entries
        for (commerceItemCollectionMap: Map.Entry<String, Collection<CommerceItem>>
        in (mapStoreIdWithCommerceItems as MutableMap<String, MutableCollection<CommerceItem>>?)?.entries!!) {
            val commerceItemCollectionValue = commerceItemCollectionMap.value
            val fulfilmentStoreId = commerceItemCollectionMap.key.replace("[^0-9]".toRegex(), "")
            val skuIds: MutableList<String?> = ArrayList()
            for (commerceItem: CommerceItem in commerceItemCollectionValue) {
                val commerceItemInfo = commerceItem.commerceItemInfo
                if (!commerceItemInfo.isGWP) skuIds.add(commerceItemInfo.catalogRefId)
            }
            val groupBySkuIds = TextUtils.join("-", skuIds)
            /***
             * Handles products with  no fulfilmentStoreId
             * quantity = -2 is required to prevent change quantity api call
             * triggered when commerceItemInfo.quantity > quantityInStock
             */
            if (TextUtils.isEmpty(fulfilmentStoreId)) {
                val cartItems = cartProductAdapter?.cartItems
                cartItems?.forEach { cartItemGroup: CartItemGroup ->
                    for (commerceItem: CommerceItem in cartItemGroup.commerceItems) {
                        if (commerceItem.fulfillmentStoreId.isEmpty()) {
                            commerceItem.quantityInStock = 0
                            commerceItem.commerceItemInfo.quantity = -2
                            commerceItem.isStockChecked = true
                            removeItem(commerceItem)
                        }
                    }
                }
                this.cartItems = cartItems
            } else {
                viewModel.getInventorySkuForInventory(fulfilmentStoreId, groupBySkuIds, false)
            }
        }
    }

    private fun disableQuantitySelector(error: Throwable?) {
        if (!isAdded) return
        requireActivity().runOnUiThread {
            when (error) {
                is SocketTimeoutException -> {
                    val cartItems: ArrayList<CartItemGroup> =
                        cartProductAdapter?.cartItems ?: ArrayList(0)
                    for (cartItemGroup: CartItemGroup in cartItems) {
                        for (commerceItem: CommerceItem in cartItemGroup.commerceItems) {
                            if (!commerceItem.isStockChecked) {
                                commerceItem.quantityInStock = -1
                                commerceItem.isStockChecked = true
                            }
                        }
                    }
                    cartProductAdapter?.updateStockAvailability(cartItems)
                }
                is ConnectException, is UnknownHostException -> {
                    val cartItems: ArrayList<CartItemGroup> =
                        cartProductAdapter?.cartItems ?: ArrayList(0)
                    for (cartItemGroup: CartItemGroup in cartItems) {
                        for (commerceItem: CommerceItem in cartItemGroup.commerceItems) {
                            if (!commerceItem.isStockChecked) {
                                commerceItem.quantityInStock = -1
                            }
                        }
                    }
                    cartProductAdapter?.updateStockAvailability(cartItems)
                }
            }
            enableEditCart(false)
            binding.btnCheckOut.isEnabled = false
            binding.rlCheckOut.isEnabled = false
        }
    }

    private fun updateCartListWithAvailableStock(mSkuInventories: HashMap<String, List<SkuInventory>>?) {
        isAllInventoryAPICallSucceed = true
        cartItems?.forEach { cartItemGroup: CartItemGroup ->
            if (cartItemGroup.type.equals(GIFT_ITEM, ignoreCase = true)) {
                for (commerceItem: CommerceItem in cartItemGroup.commerceItems) {
                    commerceItem.commerceItemInfo.quantity = 1
                    commerceItem.quantityInStock = 2
                    commerceItem.isStockChecked = true
                }
            }
            for (commerceItem: CommerceItem in cartItemGroup.commerceItems) {
                val fulfilmentStoreId = commerceItem.fulfillmentStoreId
                val skuId = commerceItem.commerceItemInfo.getCatalogRefId()
                if (mSkuInventories!!.containsKey(fulfilmentStoreId)) {
                    val skuInventories = mSkuInventories[fulfilmentStoreId]
                    if (skuInventories != null) {
                        for (skuInventory: SkuInventory in skuInventories) {
                            if ((skuInventory.sku == skuId)) {
                                commerceItem.quantityInStock = skuInventory.quantity
                                commerceItem.isStockChecked = true
                            }
                        }
                    }
                }
            }
        }
        updateItemQuantityToMatchStock()
        cartProductAdapter?.updateStockAvailability(cartItems)
        setMinimumCartErrorMessage()
    }

    // If CommerceItem quantity in cart is more then inStock Update quantity to match stock
    private fun updateItemQuantityToMatchStock() {
        var isAnyItemNeedsQuantityUpdate = false
        val itemsTobeRemovedFromCart = ArrayList<CommerceItem>()
        cartItems?.forEach { cartItemGroup: CartItemGroup ->
            for (commerceItem: CommerceItem in cartItemGroup.commerceItems) {
                if (commerceItem.quantityInStock == 0) {
                    itemsTobeRemovedFromCart.add(commerceItem)
                } else if (commerceItem.commerceItemInfo.getQuantity() > commerceItem.quantityInStock) {
                    isAnyItemNeedsQuantityUpdate = true
                    mCommerceItem = commerceItem
                    mChangeQuantity!!.commerceId = commerceItem.commerceItemInfo.getCommerceId()
                    mChangeQuantity!!.quantity = commerceItem.quantityInStock
                    mCommerceItem!!.quantityUploading = true
                    queryServiceChangeQuantity()
                }
            }
        }
        if (!binding.btnCheckOut.isEnabled && isAllInventoryAPICallSucceed && !isAnyItemNeedsQuantityUpdate) {
            fadeCheckoutButton(false)
            if (isAdded) showAvailableVouchersToast(voucherDetails?.activeTotalVouchersCount ?: 0)
        }
        if (itemsTobeRemovedFromCart.size > 0) {
            if (activity != null && isAdded) {
                val fromCartDialogFragment = newInstance(itemsTobeRemovedFromCart)
                fromCartDialogFragment.show(this.childFragmentManager, this.javaClass.simpleName)
            }
        }
    }

    /***
     * @method fadeCheckoutButton() is called before inventory api get executed to
     * disable the checkout button
     * It is called again after the last inventory call if
     * @params mShouldDisplayCheckout is true only to avoid blinking animation on
     * checkout button
     */
    private fun fadeCheckoutButton(value: Boolean) {
        enableEditCart(value)
        Utils.fadeInFadeOutAnimation(binding.btnCheckOut, value)
    }

    private fun setDeliveryLocationEnabled(isEditMode: Boolean) {
        Utils.deliveryLocationEnabled(
            requireActivity(),
            isEditMode,
            binding.deliveryLocationConstLayout
        )
    }

    override fun onToastButtonClicked(currentState: String) {
        when (currentState) {
            TAG_ADDED_TO_LIST_TOAST -> {
                val activity: FragmentActivity = requireActivity()
                val intent = Intent().also { intent ->
                    intent.putExtra("count", mNumberOfListSelected)
                    if (mNumberOfListSelected == 1) {
                        WoolworthsApplication.getInstance()?.wGlobalState?.shoppingListRequest?.let { shoppingListRequest ->
                            for (shoppingList: ShoppingList in shoppingListRequest) {
                                if (shoppingList.shoppingListRowWasSelected) {
                                    intent.putExtra("listId", shoppingList.listId)
                                    intent.putExtra("listName", shoppingList.listName)
                                }
                            }
                        }
                    }
                }
                activity.setResult(MOVE_TO_LIST_ON_TOAST_VIEW_CLICKED, intent)
                activity.finish()
                activity.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
            }
            TAG_AVAILABLE_VOUCHERS_TOAST -> {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.Cart_ovr_popup_view,
                    requireActivity()
                )
                when {
                    voucherDetails?.activeVouchersCount?.let { it > 0 } == true -> navigateToAvailableVouchersPage()
                    voucherDetails?.activeCashVouchersCount?.let { it > 0 } == true -> navigateToCashBackVouchers()
                }
            }
            else -> {}
        }
    }

    private fun setDeliveryLocation(shoppingDeliveryLocation: ShoppingDeliveryLocation?) {
        //TODO: Redesign data mapping
        shoppingDeliveryLocation?.let {
            requireActivity().apply {
                setDeliveryAddressView(
                    this,
                    shoppingDeliveryLocation.fulfillmentDetails,
                    binding.tvDeliveryTitle,
                    binding.tvDeliverySubtitle,
                    binding.imgCartDelivery
                )
            }
        }
    }

    private fun enableEditCart(enable: Boolean) {
        Utils.fadeInFadeOutAnimation(binding.btnEditCart, enable)
        binding.btnEditCart.isEnabled = !enable
    }

    private fun enableRemoveAllButton(enable: Boolean) {
        binding.btnClearCart.isEnabled = enable
        binding.btnClearCart.isClickable = enable
    }

    private fun showEditDeliveryLocationFeatureWalkthrough() {
        if ((!AppInstanceObject.get().featureWalkThrough.showTutorials
                    || AppInstanceObject.get().featureWalkThrough.deliveryLocation
                    || !isAdded || !isVisible)
        ) return
        val activity = requireActivity() as? BottomNavigationActivity
        setCrashlyticsString(
            getString(R.string.crashlytics_materialshowcase_key),
            this.javaClass.simpleName
        )
        activity?.walkThroughPromtView =
            WMaterialShowcaseView.Builder(activity, WMaterialShowcaseView.Feature.DELIVERY_LOCATION)
                .setTarget(binding.imgCartDelivery)
                .setTitle(R.string.your_delivery_location)
                .setDescription(R.string.walkthrough_delivery_location_desc)
                .setActionText(R.string.tips_edit_delivery_location)
                .setImage(R.drawable.tips_tricks_ic_stores)
                .setAction(this)
                .setShapePadding(24)
                .setArrowPosition(WMaterialShowcaseView.Arrow.TOP_LEFT)
                .setMaskColour(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.semi_transparent_black
                    )
                ).build()
        activity?.walkThroughPromtView?.show(activity)
    }

    override fun onWalkthroughActionButtonClick(feature: WMaterialShowcaseView.Feature) {
        if (feature == WMaterialShowcaseView.Feature.DELIVERY_LOCATION) onClick((binding.deliveryLocationConstLayout)!!)
    }

    override fun onPromptDismiss(feature: WMaterialShowcaseView.Feature) {
        isMaterialPopUpClosed = true
        if (isAdded) showAvailableVouchersToast(voucherDetails?.activeTotalVouchersCount ?: 0)
    }

    private fun displayUpSellMessage(globalMessages: GlobalMessages) {
        if (mRemoveAllItemFromCartTapped) return
        if (globalMessages.qualifierMessages.isNullOrEmpty()) {
            binding.upSellMessageTextView?.visibility = View.GONE
            return
        }
        val qualifierMessage = globalMessages.qualifierMessages[0]
        binding.upSellMessageTextView.text = qualifierMessage
        binding.upSellMessageTextView.visibility =
            if (TextUtils.isEmpty(qualifierMessage)) View.GONE else View.VISIBLE
    }

    override fun onOutOfStockProductsRemoved() {
        loadShoppingCart()
    }

    private fun showMaxItemView() {
        showGeneralInfoDialog(
            requireActivity().supportFragmentManager,
            getString(R.string.unable_process_checkout_desc),
            getString(R.string.unable_process_checkout_title),
            getString(R.string.got_it),
            R.drawable.payment_overdue_icon
        )
    }

    private fun showNoTimeSlotsView() {
        showGeneralInfoDialog(
            requireActivity().supportFragmentManager,
            getString(R.string.timeslot_desc),
            getString(R.string.timeslot_title),
            getString(R.string.got_it),
            R.drawable.icon_dash_delivery_scooter
        )
    }

    private fun showRedeemVoucherFeatureWalkthrough() {
        val activity = requireActivity() as? BottomNavigationActivity
        if (activity == null || !isAdded || !isVisible) {
            return
        }
        if (!AppInstanceObject.get().featureWalkThrough.showTutorials || AppInstanceObject.get().featureWalkThrough.cartRedeemVoucher) {
            isMaterialPopUpClosed = true
            return
        }
        activity.walkThroughPromtView = WMaterialShowcaseView.Builder(
            activity,
            WMaterialShowcaseView.Feature.CART_REDEEM_VOUCHERS
        )
            .setTarget(View(activity))
            .setTitle(R.string.redeem_voucher_walkthrough_title)
            .setDescription(R.string.redeem_voucher_walkthrough_desc)
            .setActionText(R.string.got_it)
            .setImage(R.drawable.tips_tricks_ic_redeem_voucher)
            .setAction(this)
            .setShouldRender(false)
            .setArrowPosition(WMaterialShowcaseView.Arrow.NONE)
            .setMaskColour(ContextCompat.getColor(requireContext(), R.color.semi_transparent_black))
            .build()
        activity.walkThroughPromtView.show(activity)
    }

    private fun showAvailableVouchersToast(availableVouchersCount: Int) {
        if (availableVouchersCount < 1 || !isMaterialPopUpClosed) return
        mToastUtils?.apply {
            activity = requireActivity()
            currentState = TAG_AVAILABLE_VOUCHERS_TOAST
            cartText = availableVouchersCount.toString()
            pixel = (binding.btnCheckOut.height * 3.5).toInt()
            view = binding.btnCheckOut
            message =
                requireContext().resources.getQuantityString(R.plurals.vouchers_available,
                    availableVouchersCount,
                    availableVouchersCount)
            setAllCapsUpperCase(true)
            viewState = true
            build()
        }
    }

    fun showVouchersOrPromoCodeAppliedToast(message: String?) {
        if (isAdded) {
            mToastUtils?.apply {
                activity = requireActivity()
                currentState = TAG
                pixel = (binding.btnCheckOut.height * 2.5).toInt()
                view = binding.btnCheckOut
                this.message = message
                viewState = false
                buildCustomToast()
            }
        }
    }

    override fun onViewVouchers() {
        navigateToAvailableVouchersPage()
    }

    override fun onViewCashBackVouchers() {
        navigateToCashBackVouchers()
    }

    private fun navigateToCashBackVouchers() {
        val intent = Intent(context, AvailableVouchersToRedeemInCart::class.java)
        intent.putExtra(
            VOUCHER_DETAILS, Utils.toJson(voucherDetails)
        )
        intent.putExtra(
            CASH_BACK_VOUCHERS, true)
        intent.putExtra(
            BLACK_CARD_HOLDER,  isBlackCardHolder)

        startActivityForResult(
            intent, REDEEM_VOUCHERS_REQUEST_CODE
        )
    }

    private fun navigateToAvailableVouchersPage() {
        val intent = Intent(context, AvailableVouchersToRedeemInCart::class.java)
        intent.putExtra(
            VOUCHER_DETAILS, Utils.toJson(voucherDetails)
        )
        intent.putExtra(
            CASH_BACK_VOUCHERS, false)
        intent.putExtra(
            BLACK_CARD_HOLDER,  isBlackCardHolder)
        startActivityForResult(
            intent, REDEEM_VOUCHERS_REQUEST_CODE
        )
    }

    override fun updateOrderTotal() {
        updatePriceInformation()
//        orderSummary?.total?.let { orderTotal?.text = formatAmountToRandAndCentWithSpace(it) }
    }

    override fun onEnterPromoCode() {
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.Cart_promo_enter,
            requireActivity()
        )
        navigateToApplyPromoCodePage()
    }

    override fun onRemovePromoCode(promoCode: String) {
        viewModel.onRemovePromoCode(CouponClaimCode(promoCode))
    }

    private fun navigateToApplyPromoCodePage() {
        val intent = Intent(context, AvailableVouchersToRedeemInCart::class.java)
        startActivityForResult(
            intent, APPLY_PROMO_CODE_REQUEST_CODE
        )
    }

    private fun hideProgressBar() {
        binding.cartProgressBar.visibility = View.GONE
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun showProgressBar() {
        binding.cartProgressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    override fun onPromoDiscountInfo() {
        showGeneralInfoDialog(
            requireActivity().supportFragmentManager,
            getString(R.string.promo_discount_dialog_desc),
            getString(R.string.promo_discount_dialog_title),
            getString(R.string.got_it),
            0
        )
    }

    private fun setItemLimitsBanner() {
        if (isAdded) {
            binding.cartItemLimitsBanner.apply {
                updateItemLimitsBanner(
                    productCountMap,
                    itemLimitsBanner,
                    itemLimitsMessage,
                    itemLimitsCounter,
                    showBanner = (getPreferredDeliveryType() === Delivery.CNC || getPreferredDeliveryType() === Delivery.DASH)
                )
            }
        }
    }

    private fun showDeleteConfirmationDialog(resultCode: String) {
        val customBottomSheetDialogFragment =
            CustomBottomSheetDialogFragment.newInstance(
                getString(R.string.are_you_sure),
                getString(R.string.delete_confirmation_text),
                getString(R.string.remove),
                getString(R.string.cancel),
                resultCode)
        customBottomSheetDialogFragment.show(requireFragmentManager(),
            CustomBottomSheetDialogFragment::class.java.simpleName)
    }

    private fun addObserverEvents() {
        viewModel.getCarV2.observe(viewLifecycleOwner) {
            val response = it.peekContent().data
            when (it.peekContent().status) {
                Status.LOADING -> {
                    mErrorHandlerView?.hideErrorHandler()
                    setDeliveryLocationEnabled(false)
                    binding.apply {
                        rlCheckOut.isEnabled = !isOnItemRemoved
                        rlCheckOut.visibility = if (isOnItemRemoved) View.VISIBLE else View.GONE
                        cartProgressBar.visibility = View.VISIBLE
                    }
                    cartProductAdapter?.clear()
                    hideEditCart()
                }
                Status.SUCCESS -> {
                    onCartV2Response(response)
                }
                Status.ERROR -> {
                    onCartV2Response(response)
                }
            }
        }

        viewModel.getSavedAddress.observe(viewLifecycleOwner) {
            val response = it.peekContent().data
            when (it.peekContent().status) {
                Status.LOADING -> {
                    binding.cartProgressBar.visibility = View.VISIBLE
                }
                Status.SUCCESS -> {
                    when (response?.httpCode) {
                        HTTP_OK -> {
                            binding.cartProgressBar.visibility = View.GONE
                            navigateToCheckout(response)
                        }
                        else -> {
                            binding.cartProgressBar.visibility = View.GONE
                            if (response?.response != null) {
                                showErrorDialog(
                                    ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON,
                                    response.response?.message
                                )
                            }
                        }
                    }
                }
                Status.ERROR -> {
                    showErrorDialog(
                        ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON,
                        response?.response?.message
                    )
                }
            }
        }

        viewModel.removeCartItem.observe(viewLifecycleOwner) {
            val response = it.peekContent().data
            when (it.peekContent().status) {
                Status.LOADING -> {
                    showProgressBar()
                }
                Status.SUCCESS -> {
                    try {
                        if (response?.httpCode == HTTP_OK) {
                            updateUIForCartResponse(response)
                            updateCart(response, mCommerceItem)
                            if (response?.cartItems.isNullOrEmpty()) {
                                onRemoveSuccess()
                            }
                        } else {
                            resetItemDelete(true)
                        }
                        hideProgressBar()
                        fadeCheckoutButton(false)
                        setDeliveryLocationEnabled(true)
                        enableRemoveAllButton(true)
                        setMinimumCartErrorMessage()
                    } catch (ex: Exception) {
                        logException(ex)
                    }
                }
                Status.ERROR -> {
                    requireActivity().runOnUiThread {
                        if (cartProductAdapter != null) {
                            mCommerceItem?.let { it1 -> onRemoveItemLoadFail(it1) }
                            onRemoveItemFailed = true
                            enableItemDelete(false)
                            hideProgressBar()
                        }
                        mErrorHandlerView?.showToast()
                    }
                }
            }
        }

        viewModel.removeAllCartItem.observe(viewLifecycleOwner) {
            val response = it.peekContent().data
            when (it.peekContent().status) {
                Status.LOADING -> {
                    mRemoveAllItemFromCartTapped = true
                    showProgressBar()
                    onRemoveItem(true)
                }
                Status.SUCCESS -> {
                    try {
                        if (response?.httpCode == HTTP_OK) {
                            updateUIForCartResponse(response)
                            mRemoveAllItemFromCartTapped = false
                            updateCart(response, null)
                            updateCartSummary(0)
                            onRemoveSuccess()
                        } else {
                            onRemoveItem(false)
                        }
                        hideProgressBar()
                        setDeliveryLocationEnabled(true)
                    } catch (ex: Exception) {
                        ex.message?.let { Log.e(TAG, it) }
                    }
                }
                Status.ERROR -> {
                    requireActivity().runOnUiThread {
                        mRemoveAllItemFailed = true
                        onRemoveItem(false)
                        mErrorHandlerView?.hideErrorHandler()
                        mErrorHandlerView?.showToast()
                        hideProgressBar()
                    }
                }
            }
        }

        viewModel.getInventorySkuForInventory.observe(viewLifecycleOwner) {
            val response = it.peekContent().data
            when (it.peekContent().status) {
                Status.LOADING -> {
                    showProgressBar()
                }
                Status.SUCCESS -> {
                    hideProgressBar()
                    if (response?.httpCode == HTTP_OK || response?.httpCode == HTTP_OK_201) {
                        mSkuInventories?.set(response.storeId, response.skuInventory)
                        if (mSkuInventories?.size == mapStoreIdWithCommerceItems?.size) {
                            updateCartListWithAvailableStock(mSkuInventories)
                        }
                    } else {
                        isAllInventoryAPICallSucceed = false
                        if (!errorMessageWasPopUp) {
                            Utils.displayValidationMessage(
                                requireActivity(),
                                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                response?.response?.desc ?: ""
                            )
                            errorMessageWasPopUp = true
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgressBar()
                    disableQuantitySelector(response?.exception)
                }
            }
        }

        viewModel.changeProductQuantity.observe(viewLifecycleOwner) {
            val response = it.peekContent().data
            when (it.peekContent().status) {
                Status.LOADING -> {
                    cartProductAdapter?.onChangeQuantityLoad()
                    fadeCheckoutButton(true)
                }
                Status.SUCCESS -> {
                    if (response?.httpCode == HTTP_OK) {
                        updateUIForCartResponse(response)
                        changeQuantity(response, mChangeQuantityList?.getOrNull(0))
                    } else {
                        onChangeQuantityComplete()
                    }
                    mChangeQuantityList?.removeFirstOrNull()
                }
                Status.ERROR -> {
                    requireActivity().runOnUiThread {
                        mErrorHandlerView?.showToast()
                        changeQuantityWasClicked = true
                        cartProductAdapter?.onChangeQuantityError()
                    }
                }
            }
        }

        viewModel.onRemovePromoCode.observe(viewLifecycleOwner) {
            val response = it.peekContent().data
            when (it.peekContent().status) {
                Status.LOADING -> {
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.Cart_promo_remove,
                        requireActivity())
                    showProgressBar()
                }
                Status.SUCCESS -> {
                    hideProgressBar()
                    when (response?.httpCode) {
                        HTTP_OK -> {
                            updateUIForCartResponse(response)
                            updateCart(response, null)
                            if (voucherDetails?.promoCodes == null || voucherDetails?.promoCodes?.size == 0)
                                showVouchersOrPromoCodeAppliedToast(
                                    getString(R.string.promo_code_removed_toast_message)
                                )
                        }
                        HTTP_EXPECTATION_FAILED_502 -> response.response?.let {
                            Utils.displayValidationMessage(
                                activity,
                                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                response.response.desc,
                                true
                            )
                        }
                        HTTP_SESSION_TIMEOUT_440 -> {
                            SessionUtilities.getInstance()
                                .setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                            SessionExpiredUtilities.getInstance().showSessionExpireDialog(
                                activity as AppCompatActivity?,
                                this@CartFragment
                            )
                        }
                        else -> response?.response?.let {
                            Utils.displayValidationMessage(
                                activity,
                                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                getString(R.string.general_error_desc),
                                true
                            )
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgressBar()
                    Utils.displayValidationMessage(
                        activity,
                        CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                        getString(R.string.general_error_desc),
                        true
                    )
                }
            }
        }
    }

    private fun addFragmentListener() {
        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT) { _, _ ->
            fadeCheckoutButton(false)
            setDeliveryLocationEnabled(true)
            setMinimumCartErrorMessage()
            resetItemDelete(true)
        }
        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_DISMISS_RESULT) { _, _ ->
            fadeCheckoutButton(false)
            setDeliveryLocationEnabled(true)
            setMinimumCartErrorMessage()
            resetItemDelete(true)
        }
        setFragmentResultListener(ON_CONFIRM_REMOVE_WITH_DELETE_PRESSED) { _, _ ->
            enableItemDelete(false)
            mCommerceItem?.let { removeItemAPI(it) }
        }
        setFragmentResultListener(ON_CONFIRM_REMOVE_WITH_DELETE_ICON_PRESSED) { _, _ ->
            enableItemDelete(false)
            enableRemoveAllButton(false)
            mCommerceItem?.let { removeItemAPI(it) }
        }
        setFragmentResultListener(ON_CONFIRM_REMOVE_ALL) { _, _ ->
            enableItemDelete(false)
            viewModel.removeAllCartItem()
        }
    }

    fun enableItemDelete(enable: Boolean) {
        fadeCheckoutButton(!enable)
        setDeliveryLocationEnabled(enable)
    }

    override fun onItemAddedToCart() {
        if(isAdded){
            loadShoppingCart()
            binding.nestedScrollView.fullScroll(ScrollView.FOCUS_UP)
        }
    }

    companion object {
        const val CART_BACK_PRESSED_CODE = 9
        const val PDP_LOCATION_CHANGED_BACK_PRESSED_CODE = 18
        const val REQUEST_SUBURB_CHANGE = 143
        const val MOVE_TO_LIST_ON_TOAST_VIEW_CLICKED = 1020
        const val REDEEM_VOUCHERS_REQUEST_CODE = 1979
        const val APPLY_PROMO_CODE_REQUEST_CODE = 1989
        const val REQUEST_PAYMENT_STATUS = 4775

        private const val TAG_ADDED_TO_LIST_TOAST = "ADDED_TO_LIST"
        private const val TAG_AVAILABLE_VOUCHERS_TOAST = "AVAILABLE_VOUCHERS"
        private const val GIFT_ITEM = "GIFT"

        // constants for deletion confirmation.
        private const val ON_CONFIRM_REMOVE_WITH_DELETE_PRESSED = "remove_with_delete_pressed"
        private const val ON_CONFIRM_REMOVE_WITH_DELETE_ICON_PRESSED =
            "remove_with_delete_icon_pressed"
        private const val ON_CONFIRM_REMOVE_ALL = "on_confirm_remove_all"
        const val VOUCHER_DETAILS = "VoucherDetails"
        const val CASH_BACK_VOUCHERS = "cash_back_vouchers"
        const val BLACK_CARD_HOLDER = "black_card"
    }
}
