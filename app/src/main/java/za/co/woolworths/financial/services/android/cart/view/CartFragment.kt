package za.co.woolworths.financial.services.android.cart.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Spannable
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.ScrollView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentCartBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import za.co.woolworths.financial.services.android.cart.service.network.CartItemGroup
import za.co.woolworths.financial.services.android.cart.service.network.CartResponse
import za.co.woolworths.financial.services.android.cart.viewmodel.CartUtils
import za.co.woolworths.financial.services.android.cart.viewmodel.CartUtils.Companion.filterCommerceItemFromCartResponse
import za.co.woolworths.financial.services.android.cart.viewmodel.CartUtils.Companion.getAppliedVouchersCount
import za.co.woolworths.financial.services.android.cart.viewmodel.CartUtils.Companion.getUpdatedCommerceItem
import za.co.woolworths.financial.services.android.cart.viewmodel.CartUtils.Companion.updateItemLimitsBanner
import za.co.woolworths.financial.services.android.cart.viewmodel.CartViewModel
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.utils.AvailableVoucherPromoResultCallback
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.geolocation.GeoUtils.Companion.getDelivertyType
import za.co.woolworths.financial.services.android.geolocation.GeoUtils.Companion.getPlaceId
import za.co.woolworths.financial.services.android.geolocation.GeoUtils.Companion.getSelectedPlaceId
import za.co.woolworths.financial.services.android.geolocation.viewmodel.AddToCartLiveData
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.AppConfigSingleton.nativeCheckout
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.item_limits.ProductCountMap
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.CouponClaimCode
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.VoucherDetails
import za.co.woolworths.financial.services.android.models.network.*
import za.co.woolworths.financial.services.android.models.service.event.CartState
import za.co.woolworths.financial.services.android.models.service.event.ProductState
import za.co.woolworths.financial.services.android.recommendations.data.response.request.CartProducts
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Event
import za.co.woolworths.financial.services.android.recommendations.presentation.RecommendationEventHandler
import za.co.woolworths.financial.services.android.recommendations.presentation.viewmodel.RecommendationViewModel
import za.co.woolworths.financial.services.android.ui.activities.CartCheckoutActivity
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.*
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.response.DyHomePageViewModel
import za.co.woolworths.financial.services.android.ui.activities.online_voucher_redemption.AvailableVouchersToRedeemInCart
import za.co.woolworths.financial.services.android.ui.fragments.cart.GiftWithPurchaseDialogDetailFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.Cart
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.PrepareChangeAttributeRequestEvent
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.Properties
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.ViewModel.DyChangeAttributeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.views.LockableNestedScrollViewV2
import za.co.woolworths.financial.services.android.ui.views.UnsellableItemsBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView.IWalkthroughActionListener
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ActionSheetDialogFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_EXPECTATION_FAILED_502
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK_201
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_SESSION_TIMEOUT_440
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getPreferredDeliveryType
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.presentEditDeliveryGeoLocationActivity
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.setDeliveryAddressView
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.showGeneralInfoDialog
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.updateCheckOutLink
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter.Companion.instance
import za.co.woolworths.financial.services.android.util.ToastUtils.ToastInterface
import za.co.woolworths.financial.services.android.util.UnsellableUtils.Companion.getUnsellableCommerceItem
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper.FirebaseEventAction.*
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper.FirebaseEventOption.ADD_PROMO
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper.FirebaseEventOption.VOUCHERS
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper.triggerFirebaseEventVouchersOrPromoCode
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.logException
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.setCrashlyticsString
import za.co.woolworths.financial.services.android.util.analytics.dto.AddToWishListFirebaseEventData
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.util.wenum.Delivery.Companion.getType
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Context
import za.co.woolworths.financial.services.android.util.Utils.*

@AndroidEntryPoint
class CartFragment : BaseFragmentBinding<FragmentCartBinding>(FragmentCartBinding::inflate),
    CartProductAdapter.OnItemClick,
    View.OnClickListener, NetworkChangeListener, ToastInterface, IWalkthroughActionListener,
    RecommendationEventHandler {

    private val viewModel: CartViewModel by viewModels(
        ownerProducer = { this }
    )
    private val recommendationViewModel: RecommendationViewModel by viewModels()
    private val confirmAddressViewModel: ConfirmAddressViewModel by activityViewModels()

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
    private var cartItems: ArrayList<CartItemGroup>? = null
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
    private var isBlackCardHolder: Boolean = false
    private var isOnItemRemoved = false
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var isFromBottomNavigation: Boolean = false

    private var dyServerId: String? = null
    private var dySessionId: String? = null
    private var config: NetworkConfig? = null
    private var dyHomePageViewModel: DyHomePageViewModel? = null
    private lateinit var dyChangeAttributeViewModel: DyChangeAttributeViewModel

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
                                        pixel = binding.btnCheckOut.height
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
        addScrollListeners()
        config = NetworkConfig(AppContextProviderImpl())
        if (Utils.getSessionDaoDyServerId(SessionDao.KEY.DY_SERVER_ID) != null)
            dyServerId = Utils.getSessionDaoDyServerId(SessionDao.KEY.DY_SERVER_ID)
        if (Utils.getSessionDaoDySessionId(SessionDao.KEY.DY_SESSION_ID) != null)
            dySessionId = Utils.getSessionDaoDySessionId(SessionDao.KEY.DY_SESSION_ID)
        dyCategoryChooseVariationViewModel()
        dyReportEventViewModel()
    }

    private fun dyReportEventViewModel() {
        dyChangeAttributeViewModel = ViewModelProvider(this).get(DyChangeAttributeViewModel::class.java)
    }

    private fun dyCategoryChooseVariationViewModel() {
        dyHomePageViewModel = ViewModelProvider(this).get(DyHomePageViewModel::class.java)
    }

    private fun initializeLoggedInUserCartUI() {
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            return
        }

        //One time biometrics Walkthrough
        if (isVisible) {
            ScreenManager.presentBiometricWalkthrough(activity)
            loadShoppingCart()
        }
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

    private fun onRemoveSuccess() {
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

            R.id.deliveryLocationConstLayout -> CartUtils.onLocationSelectionClicked(
                requireActivity(),
                liquorCompliance
            )

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

                    if ((deliveryType == Delivery.DASH
                                && WoolworthsApplication.getValidatePlaceDetails()?.deliverable == true) && (WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.firstAvailableFoodDeliveryTime.isNullOrEmpty() || WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.deliveryTimeSlots.isNullOrEmpty())
                    ) {
                        showNoTimeSlotsView()
                        return
                    }

                    // Go to Web checkout journey if...
                    if (nativeCheckout?.isNativeCheckoutEnabled == false) {
                        launchCheckoutActivity(Intent(context, CartCheckoutActivity::class.java))
                    } else {
                        if (binding.cartProgressBar.visibility == View.VISIBLE) {
                            return
                        }
                        // Get list of saved address and navigate to proper Checkout page.
                        viewModel.getSavedAddress()
                    }
                }
                AppConfigSingleton.dynamicYieldConfig?.apply {
                    if (isDynamicYieldEnabled == true)
                        prepareDynamicYieldCheckoutRequest()
                }
            }

            else -> {}
        }
    }

    private fun toggleCartMode() {
        val isEditMode = toggleEditMode()
        binding.btnEditCart.setText(if (isEditMode) R.string.cancel else R.string.edit)
        binding.btnClearCart.visibility = if (isEditMode) View.VISIBLE else View.GONE
        setPriceInformationVisibility(!isEditMode, isEditMode)
        setDeliveryLocationEnabled(!isEditMode)
        if (!isEditMode)
            setMinimumCartErrorMessage()
    }

    private fun dismissProgress() {
        binding.cartProgressBar.visibility = View.GONE
    }

    private fun showCommonErrorDialog(errorMessage: String?) {
        val intent = Intent(requireActivity(), ErrorHandlerActivity::class.java)
        intent.putExtra(
            ErrorHandlerActivity.ERROR_TYPE,
            ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON
        )
        intent.putExtra(ErrorHandlerActivity.ERROR_MESSAGE, errorMessage)
        requireActivity().startActivityForResult(intent, ErrorHandlerActivity.RESULT_RETRY)
    }

    private fun navigateToCheckout(response: SavedAddressResponse?) {
        val activity: Activity = requireActivity()
        FirebaseAnalyticsEventHelper.cartBeginEventAnalytics(orderSummary, viewModel)
        val checkoutActivityIntent = Intent(activity, CheckoutActivity::class.java)
        checkoutActivityIntent.apply {
            putExtra(CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY, response)
            putExtra(CheckoutAddressConfirmationFragment.IS_EDIT_ADDRESS_SCREEN, true)
            putExtra(
                CheckoutAddressManagementBaseFragment.CART_ITEM_LIST,
                viewModel.getCartItemList()
            )
            liquorCompliance.let {
                if ((it != null) && it!!.isLiquorOrder && !AppConfigSingleton.liquor!!.noLiquorImgUrl.isNullOrEmpty()) {
                    putExtra(Constant.LIQUOR_ORDER, it!!.isLiquorOrder)
                    putExtra(
                        Constant.NO_LIQUOR_IMAGE_URL,
                        AppConfigSingleton.liquor!!.noLiquorImgUrl
                    )
                }
            }
        }

        if (((getPreferredDeliveryType() == Delivery.STANDARD)
                    && !TextUtils.isEmpty(response?.defaultAddressNickname))
        ) {
            checkoutActivityIntent.putExtra(
                CheckoutAddressManagementBaseFragment.GEO_SLOT_SELECTION,
                true
            )
            launchCheckoutActivity(checkoutActivityIntent)

        } else if (getPreferredDeliveryType() == Delivery.DASH &&
            !TextUtils.isEmpty(response?.defaultAddressNickname)
        ) {
            checkoutActivityIntent.putExtra(
                CheckoutAddressManagementBaseFragment.DASH_SLOT_SELECTION,
                true
            )
            launchCheckoutActivity(checkoutActivityIntent)

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
                liquorCompliance = liquorCompliance,
                cartItemList = viewModel.getCartItemList()
            )
        }
    }

    private fun launchCheckoutActivity(intent: Intent) {
        activityLauncher.launch(intent, onActivityResult = { result ->
            when (result.resultCode) {
                CheckOutFragment.REQUEST_CHECKOUT_ON_DESTROY,
                CheckOutFragment.RESULT_RELOAD_CART,
                Activity.RESULT_CANCELED,
                CheckOutFragment.REQUEST_CART_REFRESH_ON_DESTROY,
                -> reloadFragment()

                Activity.RESULT_OK -> requireActivity().onBackPressed()
            }
        })
        activity?.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_out_to_left)
    }

    private fun launchPromoCodeAndVouchersActivity(intent: Intent) {
        activityLauncher.launch(intent, onActivityResult = { result ->
            AvailableVoucherPromoResultCallback().voucherPromoCallback(result).apply {
                if (this != null) {
                    val cartResponse = this?.let { viewModel.getConvertedCartResponse(it) }
                    updateUIForCartResponse(cartResponse)
                    updateCart(cartResponse, null)
                    when (result.resultCode) {
                        REDEEM_VOUCHERS_REQUEST_CODE -> {
                            showVouchersOrPromoCodeAppliedToast(
                                getString(
                                    if ((voucherDetails?.vouchers?.let {
                                            getAppliedVouchersCount(
                                                it
                                            )
                                        } ?: 0) > 0
                                    ) R.string.vouchers_applied_toast_message else R.string.vouchers_removed_toast_message
                                )
                            )
                        }

                        APPLY_PROMO_CODE_REQUEST_CODE -> {
                            showVouchersOrPromoCodeAppliedToast(
                                getString(R.string.promo_code_applied_toast_message)
                            )
                        }
                    }
                }
            }
        })
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
        if (cartProductAdapter?.cartItems?.isNullOrEmpty() == true) {
            setPriceInformationVisibility(false)
            setRecommendationDividerVisibility(visibility = false)
        } else {
            updatePriceInformation()
        }
    }

    override fun openAddToListPopup(
        addToListRequests: ArrayList<AddToListRequest>,
        addToWishListEventData: AddToWishListFirebaseEventData?,
    ) {
        KotlinUtils.openAddToListPopup(
            requireActivity(),
            requireActivity().supportFragmentManager,
            addToListRequests,
            eventData = addToWishListEventData
        )
    }

    override fun onChangeQuantity(commerceId: CommerceItem, quantity: Int) {
        mCommerceItem = commerceId
        mChangeQuantity?.commerceId = commerceId.commerceItemInfo.getCommerceId()
        mChangeQuantity?.quantity = quantity
        if (WoolworthsApplication.getInstance() != null) {
            Utils.sendBus(CartState(CartState.CHANGE_QUANTITY, quantity))
        }
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

    private fun setPriceInformationVisibility(
        visibility: Boolean,
        isEditModeChanged: Boolean = false,
    ) {
        binding.includedPrice.orderSummeryLayout.visibility =
            if (visibility) View.VISIBLE else View.GONE
        if (!visibility && !isEditModeChanged) {
            setLiquorBannerVisibility(false)
        }
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

    private fun bindCartData(cartResponse: CartResponse?) {
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
        binding.includedPrice.apply {
            //Added the BNPL flag checking logic.
            AppConfigSingleton.bnplConfig?.apply {
                if (isBnplRequiredInThisVersion && isBnplEnabled) {
                    if (viewModel.isFBHOnly()) {
                        vouchersMain.rlpayflexInfo.visibility = View.GONE
                    } else {
                        vouchersMain.rlpayflexInfo.visibility = View.VISIBLE
                    }
                } else {
                    vouchersMain.rlpayflexInfo.visibility = View.GONE
                }
            }

            if (orderSummary != null) {
                setPriceInformationVisibility(true)
                orderSummary?.basketTotal?.let {
                    setPriceValue(txtYourCartPrice, it)
                }
                orderTotal.text = CurrencyFormatter.formatAmountToRandAndCentWithSpace(
                    orderSummary?.total
                )
                val discountDetails = orderSummary?.discountDetails
                if (discountDetails != null) {

                    if (discountDetails.companyDiscount > 0) {
                        setDiscountPriceValue(
                            txtCompanyDiscount,
                            discountDetails.companyDiscount
                        )
                        rlCompanyDiscount.visibility = View.VISIBLE
                    } else {
                        rlCompanyDiscount.visibility = View.GONE
                    }
                    if (discountDetails.totalOrderDiscount > 0) {
                        setDiscountPriceValue(
                            txtTotalDiscount,
                            discountDetails.totalOrderDiscount
                        )
                        rlTotalDiscount.visibility = View.VISIBLE
                    } else {
                        rlTotalDiscount.visibility = View.GONE
                    }
                    if (discountDetails.otherDiscount > 0) {
                        setDiscountPriceValue(
                            txtDiscount,
                            discountDetails.otherDiscount
                        )
                        rlDiscount.visibility = View.VISIBLE
                    } else {
                        rlDiscount.visibility = View.GONE
                    }
                    if (discountDetails.voucherDiscount > 0) {
                        setDiscountPriceValue(
                            txtWrewardsDiscount,
                            discountDetails.voucherDiscount
                        )
                        rlWrewardsDiscount.visibility = View.VISIBLE
                    } else {
                        rlWrewardsDiscount.visibility = View.GONE
                    }
                    if (discountDetails.promoCodeDiscount > 0) {
                        setDiscountPriceValue(
                            txtPromoCodeDiscount,
                            discountDetails.promoCodeDiscount
                        )
                        rlPromoCodeDiscount.visibility = View.VISIBLE
                    } else {
                        rlPromoCodeDiscount.visibility = View.GONE
                    }
                }
            } else {
                setPriceInformationVisibility(false)
            }
            vouchersMain.rlAvailableWRewardsVouchers.setOnClickListener {
                onViewVouchers()
                triggerFirebaseEventForCart(appliedVouchersCount)
                triggerFirebaseEventVouchersOrPromoCode(
                    VIEW_WREWARDS_VOUCHERS.value,
                    VOUCHERS.value, requireActivity()
                )
            }
            vouchersMain.rlAvailableCashVouchers?.setOnClickListener {
                onViewCashBackVouchers()
                triggerFirebaseEventForCart(appliedVouchersCount)
                triggerFirebaseEventVouchersOrPromoCode(
                    VIEW_VOUCHER.value,
                    VOUCHERS.value, requireActivity()
                )
            }

            if (voucherDetails == null) {
                return
            }
            val activeCashVouchersCount = voucherDetails?.activeCashVouchersCount
            if (activeCashVouchersCount != null && activeCashVouchersCount > 0) {
                val availableVouchersLabel =
                    resources?.getQuantityString(
                        R.plurals.available_cash_vouchers_message,
                        activeCashVouchersCount,
                        activeCashVouchersCount
                    )
                vouchersMain.availableCashVouchersCount.text = availableVouchersLabel
                vouchersMain.viewCashVouchers.isEnabled = true
                vouchersMain.rlAvailableCashVouchers.isClickable = true
            } else {
                vouchersMain.availableCashVouchersCount.text =
                    getString(R.string.zero_cash_vouchers_available)
                vouchersMain.viewCashVouchers.isEnabled = false
                vouchersMain.rlAvailableCashVouchers.isClickable = false
            }

            val activeVouchersCount = voucherDetails?.activeVouchersCount
            if (activeVouchersCount != null && activeVouchersCount > 0) {
                if (appliedVouchersCount > 0) {
                    val availableVouchersLabel =
                        resources?.getQuantityString(
                            R.plurals._rewards_vouchers_message_applied,
                            appliedVouchersCount,
                            appliedVouchersCount
                        )
                    vouchersMain.availableVouchersCount.text = availableVouchersLabel
                    vouchersMain.viewVouchers.text = getString(R.string.edit)
                    vouchersMain.viewVouchers.isEnabled = true
                    vouchersMain.rlAvailableWRewardsVouchers.isClickable = true
                } else {
                    val availableVouchersLabel =
                        resources?.getQuantityString(
                            R.plurals.available_rewards_vouchers_message,
                            activeVouchersCount,
                            activeVouchersCount
                        )
                    vouchersMain.availableVouchersCount.text = availableVouchersLabel
                    vouchersMain.viewVouchers.text = getString(R.string.view)
                    vouchersMain.viewVouchers.isEnabled = true
                    vouchersMain.rlAvailableWRewardsVouchers.isClickable = true
                }
            } else {
                vouchersMain.availableVouchersCount.text =
                    getString(R.string.zero_wrewards_vouchers_available)
                vouchersMain.viewVouchers.text = getString(R.string.view)
                vouchersMain.viewVouchers.isEnabled = false
                vouchersMain.rlAvailableWRewardsVouchers.isClickable = false

            }
            vouchersMain.promoCodeAction.text =
                getString(if (voucherDetails?.promoCodes != null && voucherDetails!!.promoCodes.size > 0) R.string.remove else R.string.enter)
            if (voucherDetails!!.promoCodes != null && voucherDetails!!.promoCodes.size > 0) {
                val appliedPromoCodeText =
                    getString(R.string.promo_code_applied) + voucherDetails!!.promoCodes[0].promoCode
                vouchersMain.promoCodeLabel.text = appliedPromoCodeText
            } else {
                vouchersMain.promoCodeLabel.text =
                    getString(R.string.do_you_have_a_promo_code)
            }
            vouchersMain.rlPromoCode.setOnClickListener {
                if (voucherDetails!!.promoCodes != null && voucherDetails!!.promoCodes.size > 0) onRemovePromoCode(
                    voucherDetails!!.promoCodes[0].promoCode
                ) else onEnterPromoCode()
            }
            promoDiscountInfo.setOnClickListener { onPromoDiscountInfo() }
            updateLiquorBanner()
            if (getPreferredDeliveryType() == Delivery.CNC) {
                deliveryFeeLabel.text = getString(R.string.collection_fee)
            }
        }
    }

    private fun updateLiquorBanner() {
        if (liquorCompliance != null && liquorCompliance!!.isLiquorOrder) {
            setLiquorBannerVisibility(true)
            if (!AppConfigSingleton.liquor?.noLiquorImgUrl.isNullOrEmpty()) ImageManager.setPicture(
                binding.liquorComplianceMain.imgLiquorBanner,
                AppConfigSingleton.liquor?.noLiquorImgUrl
            )
        } else {
            setLiquorBannerVisibility(false)
        }
    }

    private fun setLiquorBannerVisibility(visibility: Boolean) {
        binding.liquorComplianceMain.liquorBannerRootConstraintLayout.visibility =
            if (visibility) View.VISIBLE else View.GONE
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

    private fun updateCart(cartResponse: CartResponse?, commerceItemToRemove: CommerceItem?) {
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
        updateLiquorBanner()
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

    private fun changeQuantity(cartResponse: CartResponse?, changeQuantity: ChangeQuantity?) {
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
                    text = buildSpannedString {
                        val amount = CurrencyFormatter.formatAmountToRandNoDecimal(minBasketAmount)
                        val error = String.format(
                            getString(R.string.minspend_error_msg_cart, amount)
                        )
                        append(error)
                        val start = error.indexOf(amount) - 1
                        val typeface = ResourcesCompat.getFont(context, R.font.opensans_semi_bold)
                        setSpan(
                            CustomTypefaceSpan("opensans", typeface),
                            start,
                            start.plus(amount.length).plus(1),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
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
                if (isFromBottomNavigation) {
                    orderSummary?.total?.let {
                        viewCartEvent(
                            viewModel.getCartItemList(),
                            it
                        )
                    }
                    isFromBottomNavigation = false
                }
                showRecommendedProducts()
                AppConfigSingleton.dynamicYieldConfig?.apply {
                    if (isDynamicYieldEnabled == true) {
                        prepareDynamicYieldCartViewRequestEvent()
                        prepareSyncCartRequestEvent()
                    }
                }
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

    private fun prepareDynamicYieldCartViewRequestEvent() {
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(Utils.IPAddress, config?.getDeviceModel())
        val productList: ArrayList<String>? = ArrayList()
        for (otherProductId in viewModel.getCartItemList()) {
            if (otherProductId.commerceItemInfo.commerceId != null) {
                var productID = otherProductId.commerceItemInfo.productId
                productList?.add(productID!!)
            }
        }
        val page = Page(productList, DY_LOCATION, DY_CART_TYPE, null)
        val context = Context(device, page, DY_CHANNEL)
        val options = Options(true)
        val homePageRequestEvent = HomePageRequestEvent(user, session, context, options)
        dyHomePageViewModel?.createDyRequest(homePageRequestEvent)
    }

    private fun viewCartEvent(commerceItems: List<CommerceItem>, value: Double) {
        FirebaseAnalyticsEventHelper.viewCartAnalyticsEvent(commerceItems, value)
    }

    private fun showRecommendedProducts() {
        setRecommendationDividerVisibility(visibility = false)
        val bundle = Bundle()
        val cartLinesValue: MutableList<CartProducts> = arrayListOf()

        cartItems?.forEach { item ->
            cartLinesValue.addAll(item.commerceItems.map {
                CartProducts(
                    it.commerceItemInfo.productId,
                    it.commerceItemInfo.quantity,
                    it.priceInfo.amount.toString(),
                    it.commerceItemInfo.productId,
                    Constants.CURRENCY_VALUE
                )
            })
        }

        bundle.putParcelable(
            BundleKeysConstants.RECOMMENDATIONS_EVENT_DATA,
            Event(
                eventType = "monetate:context:PageView",
                url = "/cart",
                pageType = "cart",
                null,
                null,
                null
            )
        )
        bundle.putParcelable(
            BundleKeysConstants.RECOMMENDATIONS_EVENT_DATA_TYPE, Event(
                eventType = "monetate:context:Cart", null, null, null, null, cartLinesValue
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
                it, bundleOf(BUNDLE to bundle)
            )
        }
    }

    private fun removeItem(commerceItem: CommerceItem) {
        OneAppService().removeCartItem(commerceItem.commerceItemInfo.commerceId).enqueue(
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
        setDeliveryLocation(ShoppingDeliveryLocation(fulfillmentDetailsObj))
    }

    override fun onResume() {
        super.onResume()
        val activity: Activity = requireActivity()
        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.CART_LIST)
        activity.registerReceiver(
            mConnectionBroadcast,
            IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        )
        loadShoppingCartAndSetDeliveryLocation()
        requestInAppReview(FirebaseManagerAnalyticsProperties.VIEW_CART, activity)
    }

    override fun onPause() {
        super.onPause()
        mConnectionBroadcast?.let {
            requireActivity().unregisterReceiver(it)
        }
    }

    private val activityLauncher = BetterActivityResult.registerActivityForResult(this)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SSOActivity.SSOActivityResult.LAUNCH.rawValue() -> {
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
            }

            CART_BACK_PRESSED_CODE, PDP_LOCATION_CHANGED_BACK_PRESSED_CODE, ScreenManager.SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE -> {
                reloadFragment()
                return
            }

            REQUEST_SUBURB_CHANGE -> {
                initializeLoggedInUserCartUI()
                loadShoppingCartAndSetDeliveryLocation()
                return
            }

            ScreenManager.CART_LAUNCH_VALUE -> {
                if (resultCode == SSOActivity.SSOActivityResult.STATE_MISMATCH.rawValue()) {
                    // login screen opens on cart and user closes it without login then move tab to last opened tab.
                    (requireActivity() as? BottomNavigationActivity)?.let { activity ->
                        val previousTabIndex = activity.previousTabIndex
                        activity.bottomNavigationById.currentItem = previousTabIndex
                    }
                    return
                }
            }
        }

        if (resultCode == CustomPopUpWindow.CART_DEFAULT_ERROR_TAPPED || resultCode == ActionSheetDialogFragment.DIALOG_REQUEST_CODE) {
            val activity: Activity = requireActivity()
            activity.setResult(CustomPopUpWindow.CART_DEFAULT_ERROR_TAPPED)
            activity.finish()
            activity.overridePendingTransition(R.anim.slide_down_anim, R.anim.stay)
            return
        }

        // Retry callback when saved address api fails
        if (resultCode == ErrorHandlerActivity.RESULT_RETRY) {
            viewModel.getSavedAddress()
        }
    }

    private fun loadShoppingCartAndSetDeliveryLocation() {
        val lastDeliveryLocation = Utils.getPreferredDeliveryLocation()
        lastDeliveryLocation?.let { setDeliveryLocation(it) }
    }

    fun reloadFragment(isFromBottomNavigation: Boolean = false) {
        //Reload screen
        this@CartFragment.isFromBottomNavigation = isFromBottomNavigation
        setPriceInformationVisibility(false)
        setupToolbar()
        initializeBottomTab()
        initializeLoggedInUserCartUI()
        if (!isVisible) {
            loadShoppingCart()
        }
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
        postAnalyticsRemoveFromCart(listOf(commerceItem))
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
                getPreferredDeliveryType()?.let {
                    navigateToUnsellableItemsFragment(
                        getUnsellableCommerceItem(cartItems, itemsTobeRemovedFromCart),
                        it
                    )
                }
            }
        }
    }

    private fun navigateToUnsellableItemsFragment(
        unSellableCommerceItems: ArrayList<UnSellableCommerceItem>,
        deliveryType: Delivery,
    ) {
        val unsellableItemsBottomSheetDialog =
            confirmAddressViewModel?.let { it1 ->
                UnsellableItemsBottomSheetDialog.newInstance(
                    unSellableCommerceItems, deliveryType, binding.cartProgressBar,
                    it1, this
                )
            }
        unsellableItemsBottomSheetDialog?.show(
            requireFragmentManager(),
            UnsellableItemsBottomSheetDialog::class.java.simpleName
        )
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
                requireContext().resources.getQuantityString(
                    R.plurals.vouchers_available,
                    availableVouchersCount,
                    availableVouchersCount
                )
            setAllCapsUpperCase(true)
            viewState = true
            build()
        }
    }

    private fun showVouchersOrPromoCodeAppliedToast(message: String?) {
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
            CASH_BACK_VOUCHERS, true
        )
        intent.putExtra(
            BLACK_CARD_HOLDER, isBlackCardHolder
        )
        intent.putExtra(INTENT_REQUEST_CODE, REDEEM_VOUCHERS_REQUEST_CODE)

        launchPromoCodeAndVouchersActivity(intent)
    }

    private fun navigateToAvailableVouchersPage() {
        val intent = Intent(context, AvailableVouchersToRedeemInCart::class.java)
        intent.putExtra(
            VOUCHER_DETAILS, Utils.toJson(voucherDetails)
        )
        intent.putExtra(
            CASH_BACK_VOUCHERS, false
        )
        intent.putExtra(
            BLACK_CARD_HOLDER, isBlackCardHolder
        )
        intent.putExtra(INTENT_REQUEST_CODE, REDEEM_VOUCHERS_REQUEST_CODE)

        launchPromoCodeAndVouchersActivity(intent)
    }

    override fun updateOrderTotal() {
        updatePriceInformation()
//        orderSummary?.total?.let { orderTotal?.text = formatAmountToRandAndCentWithSpace(it) }
    }

    override fun onEnterPromoCode() {
        triggerFirebaseEventVouchersOrPromoCode(
            ADD_PROMO_CODE.value,
            ADD_PROMO.value, requireActivity()
        )
        navigateToApplyPromoCodePage()
    }


    override fun onRemovePromoCode(promoCode: String) {
        viewModel.onRemovePromoCode(CouponClaimCode(promoCode))
    }

    private fun navigateToApplyPromoCodePage() {
        val intent = Intent(context, AvailableVouchersToRedeemInCart::class.java)
        intent.putExtra(INTENT_REQUEST_CODE, APPLY_PROMO_CODE_REQUEST_CODE)

        launchPromoCodeAndVouchersActivity(intent)
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
                    showBanner = (KotlinUtils.isDeliveryOptionClickAndCollect() ||
                            (KotlinUtils.isDeliveryOptionDash() && productCountMap?.totalProductCount?: 0 > CartUtils.THRESHOLD_FOR_DASH_CART_LIMIT_BANNER))
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
                resultCode
            )
        customBottomSheetDialogFragment.show(
            requireFragmentManager(),
            CustomBottomSheetDialogFragment::class.java.simpleName
        )
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
                                showCommonErrorDialog(response.response?.message)
                            }
                        }
                    }
                }

                Status.ERROR -> {
                    showCommonErrorDialog(response?.response?.message)
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
                    AppConfigSingleton.dynamicYieldConfig?.apply {
                        if (isDynamicYieldEnabled == true) {
                            prepareDyRemoveFromCartRequestEvent(mCommerceItem)
                        }
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

        viewModel.removeAllCartItem.observe(viewLifecycleOwner) { removeItemResponse ->
            val response = removeItemResponse.peekContent().data
            when (removeItemResponse.peekContent().status) {
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
                    prepareSyncCartRequestEvent()
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
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.Cart_promo_remove,
                        requireActivity()
                    )
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
        AddToCartLiveData.observe(viewLifecycleOwner) {
            if (it) {
                AddToCartLiveData.value = false
                loadShoppingCart()
            }
        }
    }

    private fun prepareDynamicYieldCheckoutRequest() {
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(Utils.IPAddress, config?.getDeviceModel())
        val productList: ArrayList<String>? = ArrayList()
        val page = Page(productList, DY_CHECKOUT, DY_CART_CHECKOUT_TYPE, null)
        val context = Context(device, page, DY_CHANNEL)
        val options = Options(true)
        val homePageRequestEvent = HomePageRequestEvent(user, session, context, options)
        dyHomePageViewModel?.createDyRequest(homePageRequestEvent)
    }

    private fun prepareDyRemoveFromCartRequestEvent(mCommerceItem: CommerceItem?) {
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(IPAddress, config?.getDeviceModel())
        val context = Context(device, null, DY_CHANNEL)
        val cartLinesValue: MutableList<Cart> = arrayListOf()
        cartItems?.let { cartItems ->
            for (cartItemGroup: CartItemGroup in cartItems) {
                val commerceItemList = cartItemGroup.commerceItems
                for (cm: CommerceItem in commerceItemList) {
                    val cart = Cart(cm.commerceItemInfo.productId, cm.commerceItemInfo.quantity,cm.priceInfo.amount.toString())
                    cartLinesValue.add(cart)
                }
            }
        }
        val properties = Properties(null,null,REMOVE_FROM_CART_V1,null,mCommerceItem?.priceInfo?.amount.toString(),Constants.CURRENCY_VALUE,mCommerceItem?.commerceItemInfo?.quantity,mCommerceItem?.commerceItemInfo?.productId,null,null,null,mCommerceItem?.commerceItemInfo?.size,null,null,null,null,null,cartLinesValue)
        val eventsDyChangeAttribute = Event(null,null,null,null,null,null,null,null,null,null,null,null,REMOVE_FROM_CART,properties)
        val events = ArrayList<Event>()
        events.add(eventsDyChangeAttribute);
        val prepareDyAddToCartRequestEvent = PrepareChangeAttributeRequestEvent(
            context,
            events,
            session,
            user
        )
        dyChangeAttributeViewModel.createDyChangeAttributeRequest(prepareDyAddToCartRequestEvent)
    }

    private fun  prepareSyncCartRequestEvent() {
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(IPAddress, config?.getDeviceModel())
        val context = Context(device, null, DY_CHANNEL)
        val cartLinesValue: MutableList<Cart> = arrayListOf()
        cartItems?.let { cartItems ->
          for (cartItemGroup: CartItemGroup in cartItems) {
              val commerceItemList = cartItemGroup.commerceItems
              for (cm: CommerceItem in commerceItemList) {
                  val cart = Cart(cm.commerceItemInfo.productId, cm.commerceItemInfo.quantity,cm.priceInfo.amount.toString())
                  cartLinesValue.add(cart)
              }
          }
        }
        val properties = Properties(null,null,SYNC_CART_V1,null,null,Constants.CURRENCY_VALUE,null,null,null,null,null,null,null,null,null,null,null,cartLinesValue)
        val eventsDyChangeAttribute = Event(null,null,null,null,null,null,null,null,null,null,null,null,SYNC_CART,properties)
        val events = ArrayList<Event>()
        events.add(eventsDyChangeAttribute);
        val prepareDySyncCartRequestEvent = PrepareChangeAttributeRequestEvent(
            context,
            events,
            session,
            user
        )
        dyChangeAttributeViewModel.createDyChangeAttributeRequest(prepareDySyncCartRequestEvent)
    }

    private fun addFragmentListener() {
        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT) { _, _ ->
            fadeCheckoutButton(false)
            setDeliveryLocationEnabled(true)
            setMinimumCartErrorMessage()
            resetItemDelete(true)
        }
        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_DISMISS_RESULT) { _, bundle ->
            val resultCode =
                bundle.getString(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT)
            if (resultCode == UnsellableUtils.ADD_TO_LIST_SUCCESS_RESULT_CODE) {
                // Proceed with reload cart as unsellable items are removed.
                loadShoppingCart()
            } else {
                fadeCheckoutButton(false)
                setDeliveryLocationEnabled(true)
                setMinimumCartErrorMessage()
                resetItemDelete(true)
            }
        }
        setFragmentResultListener(UnsellableUtils.ADD_TO_LIST_SUCCESS_RESULT_CODE) { _, _ ->
            // Proceed with reload cart as unsellable items are removed.
            loadShoppingCart()
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
            val list = arrayListOf<CommerceItem>()
            cartItems?.forEach {
                list.addAll(it.commerceItems)
            }
            postAnalyticsRemoveFromCart(list)
            enableItemDelete(false)
            viewModel.removeAllCartItem()
        }

        KotlinUtils.setAddToListFragmentResultListener(
            activity = requireActivity(),
            lifecycleOwner = viewLifecycleOwner,
            toastContainerView = binding.rlCheckOut,
            onToastClick = {}
        )
    }

    private fun postAnalyticsRemoveFromCart(commerceItems: List<CommerceItem>) {
        FirebaseAnalyticsEventHelper.removeFromCart(commerceItems)
    }

    private fun enableItemDelete(enable: Boolean) {
        fadeCheckoutButton(!enable)
        setDeliveryLocationEnabled(enable)
    }

    override fun onItemAddedToCart() {
        if (isAdded) {
            loadShoppingCart()
            binding.nestedScrollView.fullScroll(ScrollView.FOCUS_UP)
        }
    }

    override fun onRecommendationsLoadedSuccessfully() {
        if (isAdded) {
            setRecommendationDividerVisibility(visibility = !cartProductAdapter?.cartItems.isNullOrEmpty())
        }
    }

    private fun setRecommendationDividerVisibility(visibility: Boolean) {
        binding.viewRecommendationDivider.visibility = if (visibility) View.VISIBLE else View.GONE
    }

    private fun addScrollListeners() {
        binding.nestedScrollView.apply {
            setOnTouchListener(onTouchListener)
            setOnScrollStoppedListener(onScrollStoppedListener)
        }
    }

    private val onTouchListener = OnTouchListener { _, event ->
        if (event?.action == MotionEvent.ACTION_UP) {
            binding.nestedScrollView.startScrollerTask()
        }
        false
    }

    private val onScrollStoppedListener =
        object : LockableNestedScrollViewV2.OnScrollStoppedListener {
            override fun onScrollStopped() {
                if (!isAdded) {
                    return
                }
                val visible =
                    binding.nestedScrollView.isViewVisible(binding.layoutRecommendationContainer.root)
                if (visible) {
                    recommendationViewModel.parentPageScrolledToRecommendation()
                }
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
        const val INTENT_REQUEST_CODE = "intent_request_code"
        const val SHOPPING_CART_RESPONSE = "ShoppingCartResponse"

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
