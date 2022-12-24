package za.co.woolworths.financial.services.android.ui.fragments.product.shop

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentCartBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
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
import za.co.woolworths.financial.services.android.models.network.OneAppService.getChangeQuantity
import za.co.woolworths.financial.services.android.models.network.OneAppService.getInventorySkuForStore
import za.co.woolworths.financial.services.android.models.network.OneAppService.getSavedAddresses
import za.co.woolworths.financial.services.android.models.network.OneAppService.getShoppingCart
import za.co.woolworths.financial.services.android.models.network.OneAppService.removeAllCartItems
import za.co.woolworths.financial.services.android.models.network.OneAppService.removeCartItem
import za.co.woolworths.financial.services.android.models.network.OneAppService.removePromoCode
import za.co.woolworths.financial.services.android.models.service.event.CartState
import za.co.woolworths.financial.services.android.models.service.event.ProductState
import za.co.woolworths.financial.services.android.ui.activities.*
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.activities.online_voucher_redemption.AvailableVouchersToRedeemInCart
import za.co.woolworths.financial.services.android.ui.adapters.CartProductAdapter
import za.co.woolworths.financial.services.android.ui.fragments.cart.GiftWithPurchaseDialogDetailFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.RemoveProductsFromCartDialogFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.RemoveProductsFromCartDialogFragment.IRemoveProductsFromCartDialog
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.buildAddToCartSuccessToast
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.showItemsLimitToastOnAddToCart
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView.IWalkthroughActionListener
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ActionSheetDialogFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.CartUtils.Companion.filterCommerceItemFromCartResponse
import za.co.woolworths.financial.services.android.util.CartUtils.Companion.getAppliedVouchersCount
import za.co.woolworths.financial.services.android.util.CartUtils.Companion.updateItemLimitsBanner
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
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class CartFragment : BaseFragmentBinding<FragmentCartBinding>(FragmentCartBinding::inflate),
    CartProductAdapter.OnItemClick,
    View.OnClickListener, NetworkChangeListener, ToastInterface, IWalkthroughActionListener,
    IRemoveProductsFromCartDialog {

    private val TAG = this.javaClass.simpleName
    private var mNumberOfListSelected = 0
    private var changeQuantityWasClicked = false
    private var errorMessageWasPopUp = false
    private var onRemoveItemFailed = false
    private var mRemoveAllItemFailed = false
    private var isMixedBasket = false
    private var isFBHOnly = false
    private var mRemoveAllItemFromCartTapped = false
    private var isAllInventoryAPICallSucceed = false
    private var isMaterialPopUpClosed = true

    private var mChangeQuantityList: MutableList<ChangeQuantity?>? = null
    private var mSkuInventories: HashMap<String, List<SkuInventory>>? = null
    private var mapStoreIdWithCommerceItems: Map<String, Collection<CommerceItem>>? = null
    var cartItems: ArrayList<CartItemGroup>? = null
        private set
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        initViews()
        hideEditCart()
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
                            val cartState = stateObject
                            if (!TextUtils.isEmpty(cartState.state)) {
                                //setDeliveryLocation(cartState.getState());
                            } else if (cartState.indexState == CartState.CHANGE_QUANTITY) {
                                mChangeQuantity!!.quantity = cartState.quantity
                                queryServiceChangeQuantity()
                            }
                        } else if (stateObject is ProductState) {
                            val productState = stateObject
                            when (productState.state) {
                                ProductState.CANCEL_DIALOG_TAPPED ->
                                    cartProductAdapter?.onPopUpCancel(ProductState.CANCEL_DIALOG_TAPPED)

                                ProductState.CLOSE_PDP_FROM_ADD_TO_LIST -> {
                                    mToastUtils?.apply {
                                        activity = requireActivity()
                                        currentState = TAG_ADDED_TO_LIST_TOAST
                                        val shoppingList = getString(R.string.shopping_list)
                                        mNumberOfListSelected = productState.count
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
    }

    private fun initializeLoggedInUserCartUI() {
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            return
        }

        //One time biometricsWalkthrough
        if (isVisible) {
            ScreenManager.presentBiometricWalkthrough(activity)
        }
        loadShoppingCart(false)
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
            pbRemoveAllItem.visibility = View.GONE
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
        changeQuantityAPI(mChangeQuantityList?.get(0))
        mChangeQuantityList?.removeAt(0)
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

    fun onRemoveItem(visibility: Boolean) {
        binding.apply {
            pbRemoveAllItem.visibility =
                if (visibility) View.VISIBLE else View.GONE
            btnClearCart.visibility = if (visibility) View.GONE else View.VISIBLE
            btnEditCart.isEnabled = !visibility
        }
    }

    fun onRemoveSuccess() {
        binding.apply {
            pbRemoveAllItem.visibility = View.GONE
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
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.MYCARTREMOVEALL,
                    requireActivity()
                )
                removeAllCartItem(null)
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
                    loadShoppingCart(false)
                }
            }
            R.id.btnCheckOut -> {

                if (binding.btnCheckOut.isEnabled == true && orderSummary != null) {
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
                        callSavedAddress()
                    }
                }
            }
            else -> {}
        }
    }

    private fun toggleCartMode() {
        val isEditMode = toggleEditMode()
        binding.btnEditCart.setText(if (isEditMode) R.string.done else R.string.edit)
        binding.btnClearCart.visibility = if (isEditMode) View.VISIBLE else View.GONE
        setDeliveryLocationEnabled(!isEditMode)
        if (!isEditMode)
            setMinimumCartErrorMessage()
    }

    private fun dismissProgress() {
        binding.pbRemoveAllItem.visibility = View.GONE
    }

    private fun callSavedAddress() {
        binding.cartProgressBar.visibility = View.VISIBLE
        val savedAddressCall = getSavedAddresses()
        savedAddressCall.enqueue(
            CompletionHandler(
                (object : IResponseListener<SavedAddressResponse> {
                    override fun onSuccess(response: SavedAddressResponse?) {
                        when (response!!.httpCode) {
                            AppConstant.HTTP_OK -> {
                                binding.cartProgressBar.visibility = View.GONE
                                navigateToCheckout(response)
                            }
                            else -> {
                                binding.cartProgressBar.visibility = View.GONE
                                if (response.response != null) {
                                    showErrorDialog(
                                        ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON,
                                        response.response!!.message
                                    )
                                }
                            }
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        showErrorDialog(
                            ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON,
                            error!!.message
                        )
                    }
                }), SavedAddressResponse::class.java
            )
        )
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
            if ((liquorCompliance != null) && liquorCompliance!!.isLiquorOrder && (AppConfigSingleton.liquor!!.noLiquorImgUrl != null) && !AppConfigSingleton.liquor!!.noLiquorImgUrl.isEmpty()) {
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
                putExtra(CheckoutAddressManagementBaseFragment.CART_ITEM_LIST, cartItemList)
                liquorCompliance.let {
                    if ((it != null) && it.isLiquorOrder && (AppConfigSingleton.liquor!!.noLiquorImgUrl != null) && !AppConfigSingleton.liquor!!.noLiquorImgUrl.isEmpty()) {
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
                isMixedBasket = this.isMixedBasket,
                isFBHOnly = this.isFBHOnly,
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
        removeItemAPI(commerceItem)
    }

    override fun onItemDeleteClick(commerceId: CommerceItem) {
        enableItemDelete(true)
        removeItemAPI(commerceId)
    }

    override fun onChangeQuantity(commerceId: CommerceItem) {
        mCommerceItem = commerceId
        mChangeQuantity?.commerceId = commerceId.commerceItemInfo.getCommerceId()
        if (WoolworthsApplication.getInstance() != null) {
            val wGlobalState = WoolworthsApplication.getInstance().wGlobalState
            wGlobalState?.navigateFromQuantity(1)
        }
        val editQuantityIntent = Intent(activity, ConfirmColorSizeActivity::class.java).also {
            it.putExtra(
                ConfirmColorSizeActivity.SELECT_PAGE,
                ConfirmColorSizeActivity.QUANTITY
            )
            it.putExtra("CART_QUANTITY_In_STOCK", commerceId.quantityInStock)
        }
        val activity: Activity = requireActivity()
        activity.startActivity(editQuantityIntent)
        activity.overridePendingTransition(0, 0)
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
                    productCountMap = cartResponse.productCountMap
                    liquorCompliance = LiquorCompliance(
                        cartResponse.liquorOrder,
                        if (cartResponse.noLiquorImageUrl != null) cartResponse.noLiquorImageUrl else ""
                    )
                    cartProductAdapter = CartProductAdapter(
                        cartItems,
                        this@CartFragment,
                        orderSummary,
                        requireActivity(),
                        voucherDetails,
                        liquorCompliance
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

    fun updateCart(cartResponse: CartResponse?, commerceItemToRemove: CommerceItem?) {
        orderSummary = cartResponse?.orderSummary
        voucherDetails = cartResponse?.voucherDetails
        productCountMap = cartResponse?.productCountMap
        liquorCompliance =
            (if (cartResponse?.noLiquorImageUrl != null) cartResponse?.noLiquorImageUrl else "")?.let {
                LiquorCompliance(
                    cartResponse?.liquorOrder ?: false,
                    it
                )
            }
        setItemLimitsBanner()
        if (cartResponse?.cartItems?.size ?: 0 > 0 && cartProductAdapter != null) {
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
                voucherDetails,
                liquorCompliance
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
        if (cartResponse?.cartItems?.size ?: 0 > 0 && cartProductAdapter != null) {
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
                productCountMap = cartResponse.productCountMap
                liquorCompliance = LiquorCompliance(
                    cartResponse.liquorOrder,
                    if (cartResponse.noLiquorImageUrl != null) cartResponse.noLiquorImageUrl else ""
                )
                cartProductAdapter!!.notifyAdapter(
                    cartItems,
                    orderSummary,
                    voucherDetails,
                    liquorCompliance
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
                    productCountMap = cartResponse?.productCountMap
                    liquorCompliance = LiquorCompliance(
                        cartResponse?.liquorOrder ?: false,
                        cartResponse?.noLiquorImageUrl ?: ""
                    )
                    cartProductAdapter!!.notifyAdapter(
                        currentCartItemGroup,
                        orderSummary,
                        voucherDetails,
                        liquorCompliance
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
            enableEditCart()
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

    private fun loadShoppingCart(onItemRemove: Boolean): Call<ShoppingCartResponse> {
        setDeliveryLocationEnabled(false)
        binding.apply {
            rlCheckOut.isEnabled = !onItemRemove
            rlCheckOut.visibility = if (onItemRemove) View.VISIBLE else View.GONE
            cartProgressBar.visibility = View.VISIBLE
        }
        cartProductAdapter?.clear()
        hideEditCart()
        val shoppingCartResponseCall = getShoppingCart()
        shoppingCartResponseCall.enqueue(
            CompletionHandler(
                (object : IResponseListener<ShoppingCartResponse> {
                    override fun onSuccess(response: ShoppingCartResponse?) {
                        try {
                            binding.cartProgressBar.visibility = View.GONE
                            setDeliveryLocationEnabled(true)

                            when (response?.httpCode) {
                                200 -> {
                                    onRemoveItemFailed = false
                                    binding.rlCheckOut.visibility = View.VISIBLE
                                    binding.rlCheckOut.isEnabled = true
                                    val cartResponse =
                                        convertResponseToCartResponseObject(response)
                                    updateCheckOutLink(response.data[0].jSessionId)
                                    bindCartData(cartResponse)
                                    if (onItemRemove) {
                                        cartProductAdapter?.setEditMode(true)
                                    }
                                    setDeliveryLocationEnabled(true)
                                    if (response.data[0].orderSummary.fulfillmentDetails?.address?.placeId != null) {
                                        Utils.savePreferredDeliveryLocation(
                                            ShoppingDeliveryLocation(
                                                response.data[0].orderSummary.fulfillmentDetails
                                            )
                                        )
                                    }
                                    setItemLimitsBanner()
                                    instance.queryCartSummaryCount()
                                }
                                440 -> {
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
                                    setDeliveryLocationEnabled(true)
                                    response?.response?.let {
                                        Utils.displayValidationMessage(
                                            requireActivity(),
                                            CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                            it.desc,
                                            true
                                        )
                                    }
                                }
                            }
                        } catch (ex: Exception) {
                            logException(ex)
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                if (!onItemRemove) {
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
                }), ShoppingCartResponse::class.java
            )
        )
        return shoppingCartResponseCall
    }

    private fun changeQuantityAPI(changeQuantity: ChangeQuantity?): Call<ShoppingCartResponse> {
        cartProductAdapter?.onChangeQuantityLoad()
        fadeCheckoutButton(true)
        val shoppingCartResponseCall = getChangeQuantity(
            changeQuantity
        )
        shoppingCartResponseCall.enqueue(
            CompletionHandler(
                (object : IResponseListener<ShoppingCartResponse> {
                    override fun onSuccess(response: ShoppingCartResponse?) {
                        try {
                            if (response?.httpCode == 200) {
                                val cartResponse =
                                    convertResponseToCartResponseObject(response)
                                changeQuantity(cartResponse, changeQuantity)
                            } else {
                                onChangeQuantityComplete()
                            }
                        } catch (ex: Exception) {
                            logException(ex)
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        requireActivity().runOnUiThread {
                            mErrorHandlerView?.showToast()
                            changeQuantityWasClicked = true
                            cartProductAdapter?.onChangeQuantityError()
                        }
                    }
                }), ShoppingCartResponse::class.java
            )
        )
        return shoppingCartResponseCall
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

    private fun removeCartItem(commerceItem: CommerceItem): Call<ShoppingCartResponse> {
        mCommerceItem = commerceItem
        val shoppingCartResponseCall = removeCartItem(commerceItem.commerceItemInfo.getCommerceId())
        shoppingCartResponseCall.enqueue(
            CompletionHandler(
                (object : IResponseListener<ShoppingCartResponse> {
                    override fun onSuccess(response: ShoppingCartResponse?) {
                        try {
                            if (response?.httpCode == 200) {
                                val cartResponse =
                                    convertResponseToCartResponseObject(response)
                                updateCart(cartResponse, commerceItem)
                                if (cartResponse?.cartItems != null) {
                                    if (cartResponse.cartItems.isEmpty()) onRemoveSuccess()
                                } else {
                                    onRemoveSuccess()
                                }
                            } else {
                                resetItemDelete(true)
                            }
                            enableItemDelete(false)
                            setMinimumCartErrorMessage()
                        } catch (ex: Exception) {
                            logException(ex)
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        requireActivity().runOnUiThread {
                            if (cartProductAdapter != null) {
                                onRemoveItemLoadFail(commerceItem, true)
                                onRemoveItemFailed = true
                                enableItemDelete(false)
                            }
                            mErrorHandlerView?.showToast()
                        }
                    }
                }), ShoppingCartResponse::class.java
            )
        )
        return shoppingCartResponseCall
    }

    private fun removeAllCartItem(commerceItem: CommerceItem?): Call<ShoppingCartResponse> {
        mRemoveAllItemFromCartTapped = true
        onRemoveItem(true)
        val shoppingCartResponseCall = removeAllCartItems()
        shoppingCartResponseCall.enqueue(
            CompletionHandler(
                (object : IResponseListener<ShoppingCartResponse> {
                    override fun onSuccess(response: ShoppingCartResponse?) {
                        try {
                            if (response?.httpCode == 200) {
                                val cartResponse =
                                    convertResponseToCartResponseObject(response)
                                mRemoveAllItemFromCartTapped = false
                                updateCart(cartResponse, commerceItem)
                                updateCartSummary(0)
                                onRemoveSuccess()
                            } else {
                                onRemoveItem(false)
                            }

                            setDeliveryLocationEnabled(true)
                        } catch (ex: Exception) {
                            ex.message?.let { Log.e(TAG, it) }
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        requireActivity().runOnUiThread {
                            mRemoveAllItemFailed = true
                            onRemoveItem(false)
                            mErrorHandlerView?.hideErrorHandler()
                            mErrorHandlerView?.showToast()
                        }
                    }
                }), ShoppingCartResponse::class.java
            )
        )
        return shoppingCartResponseCall
    }

    private fun onRemoveItemLoadFail(commerceItem: CommerceItem, state: Boolean) {
        mCommerceItem = commerceItem
        resetItemDelete(true)
    }

    fun convertResponseToCartResponseObject(response: ShoppingCartResponse?): CartResponse? {
        if (response == null) return null
        val cartResponse: CartResponse?

        try {
            displayUpSellMessage(response.data[0])
            cartResponse = CartResponse()
            cartResponse.httpCode = response.httpCode
            val data = response.data[0]
            cartResponse.orderSummary = data.orderSummary
            cartResponse.voucherDetails = data.voucherDetails
            cartResponse.productCountMap = data.productCountMap // set delivery location
            cartResponse.liquorOrder = data.liquorOrder
            cartResponse.noLiquorImageUrl = data.noLiquorImageUrl
            val fulfillmentDetailsObj = cartResponse.orderSummary.fulfillmentDetails
            if (fulfillmentDetailsObj?.address?.placeId != null) {
                val shoppingDeliveryLocation = ShoppingDeliveryLocation(fulfillmentDetailsObj)
                Utils.savePreferredDeliveryLocation(shoppingDeliveryLocation)
                setDeliveryLocation(shoppingDeliveryLocation)
            } else {
                // If user logs out and login with new registration who don't have location.
                setDeliveryLocation(ShoppingDeliveryLocation(fulfillmentDetailsObj))
            }
            val itemsObject = JSONObject(Gson().toJson(data.items))
            isMixedBasket = itemsObject.has(ProductType.FOOD_COMMERCE_ITEM.value) && itemsObject.length() > 1
            val keys = itemsObject.keys()
            val cartItemGroups = ArrayList<CartItemGroup>()
            while ((keys.hasNext())) {
                val cartItemGroup = CartItemGroup()
                val key = keys.next()
                //GENERAL - "default",HOME - "homeCommerceItem",FOOD
                // - "foodCommerceItem",CLOTHING
                // - "clothingCommerceItem",PREMIUM BRANDS
                // - "premiumBrandCommerceItem",
                // Anything else: OTHER
                when {
                    key.contains(ProductType.DEFAULT.value) ->
                        cartItemGroup.setType(ProductType.DEFAULT.shortHeader)
                    key.contains(ProductType.GIFT_COMMERCE_ITEM.value) ->
                        cartItemGroup.setType(ProductType.GIFT_COMMERCE_ITEM.shortHeader)
                    key.contains(ProductType.HOME_COMMERCE_ITEM.value) ->
                        cartItemGroup.setType(ProductType.HOME_COMMERCE_ITEM.shortHeader)
                    key.contains(ProductType.FOOD_COMMERCE_ITEM.value) ->
                        cartItemGroup.setType(ProductType.FOOD_COMMERCE_ITEM.shortHeader)
                    key.contains(ProductType.CLOTHING_COMMERCE_ITEM.value) ->
                        cartItemGroup.setType(ProductType.CLOTHING_COMMERCE_ITEM.shortHeader)
                    key.contains(ProductType.PREMIUM_BRAND_COMMERCE_ITEM.value) ->
                        cartItemGroup.setType(ProductType.PREMIUM_BRAND_COMMERCE_ITEM.shortHeader)
                    else -> cartItemGroup.setType(ProductType.OTHER_ITEMS.shortHeader)
                }
                val productsArray = itemsObject.getJSONArray(key)
                if (productsArray.length() > 0) {
                    val productList = ArrayList<CommerceItem>()
                    for (i in 0 until productsArray.length()) {
                        val commerceItemObject = productsArray.getJSONObject(i)
                        val commerceItem =
                            Gson().fromJson(commerceItemObject.toString(), CommerceItem::class.java)
                        val fulfillmentStoreId = Utils.retrieveStoreId(commerceItem.fulfillmentType)
                        commerceItem.fulfillmentStoreId =
                            fulfillmentStoreId!!.replace("\"".toRegex(), "")
                        productList.add(commerceItem)
                        isFBHOnly = if(!itemsObject.has(ProductType.FOOD_COMMERCE_ITEM.value)) {
                            commerceItem.fulfillmentType == StoreUtils.Companion.FulfillmentType.CLOTHING_ITEMS?.type
                        } else false
                    }
                    this.cartItemList = productList
                    cartItemGroup.setCommerceItems(productList)
                }
                cartItemGroups.add(cartItemGroup)
            }
            var giftCartItemGroup = CartItemGroup()
            giftCartItemGroup.type = GIFT_ITEM
            val generalCartItemGroup = CartItemGroup()
            generalCartItemGroup.type = GENERAL_ITEM
            var generalIndex = -1
            if (cartItemGroups.contains(giftCartItemGroup) && cartItemGroups.contains(
                    generalCartItemGroup
                )
            ) {
                for (cartGroupIndex in cartItemGroups.indices) {
                    val cartItemGroup = cartItemGroups[cartGroupIndex]
                    if (cartItemGroup.type.equals(GENERAL_ITEM, ignoreCase = true)) {
                        generalIndex = cartGroupIndex
                    }
                    if (cartItemGroup.type.equals(GIFT_ITEM, ignoreCase = true)) {
                        giftCartItemGroup = cartItemGroup
                        cartItemGroups.removeAt(cartGroupIndex)
                    }
                }
                cartItemGroups.add(generalIndex + 1, giftCartItemGroup)
            }
            cartResponse.cartItems = cartItemGroups
        } catch (e: JSONException) {
            logException(e)
            return null
        }
        return cartResponse
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
                        loadShoppingCart(false)
                        loadShoppingCartAndSetDeliveryLocation()
                    }
                } else {
                    // Checkout was cancelled
                    loadShoppingCart(false)
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
                    loadShoppingCart(false)
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
                    updateCart(convertResponseToCartResponseObject(shoppingCartResponse), null)
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
            callSavedAddress()
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
        setupToolbar()
        initializeBottomTab()
        initializeLoggedInUserCartUI()
        loadShoppingCart(false)
    }

    override fun onConnectionChanged() {
        if (onRemoveItemFailed) {
            mErrorHandlerView?.hideErrorHandler()
            loadShoppingCart(true)
            return
        }
        if (mRemoveAllItemFailed) {
            removeAllCartItem(null)
            mRemoveAllItemFailed = false
            return
        }
        if (changeQuantityWasClicked) {
            cartProductAdapter?.onChangeQuantityLoad(mCommerceItem)
            queryServiceChangeQuantity()
            changeQuantityWasClicked = false
        }
    }

    private fun removeItemAPI(mCommerceItem: CommerceItem) {
        removeCartItem(mCommerceItem)
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
                initInventoryRequest(fulfilmentStoreId, groupBySkuIds)
            }
        }
    }

    private fun initInventoryRequest(
        storeId: String?,
        multiSku: String?,
    ): Call<SkusInventoryForStoreResponse> {
        val skuInventoryForStoreResponseCall = getInventorySkuForStore(
            (storeId)!!, (multiSku)!!, false
        )
        skuInventoryForStoreResponseCall.enqueue(
            CompletionHandler(
                (object : IResponseListener<SkusInventoryForStoreResponse> {
                    override fun onSuccess(response: SkusInventoryForStoreResponse?) {
                        if (response?.httpCode == 200) {
                            mSkuInventories!![response.storeId] =
                                response.skuInventory
                            if (mSkuInventories!!.size == mapStoreIdWithCommerceItems!!.size) {
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

                    override fun onFailure(error: Throwable?) {
                        disableQuantitySelector(error)
                    }
                }), SkusInventoryForStoreResponse::class.java
            )
        )
        return skuInventoryForStoreResponseCall
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
            enableEditCart()
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
        if (binding.btnCheckOut.isEnabled == false && isAllInventoryAPICallSucceed && !isAnyItemNeedsQuantityUpdate) {
            fadeCheckoutButton(false)
            if (isAdded) showAvailableVouchersToast(voucherDetails?.activeVouchersCount ?: 0)
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
                navigateToAvailableVouchersPage()
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

    private fun enableEditCart() {
        Utils.fadeInFadeOutAnimation(binding.btnEditCart, false)
        binding.btnEditCart.isEnabled = true
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
        if (isAdded) showAvailableVouchersToast(voucherDetails?.activeVouchersCount ?: 0)
    }

    private fun displayUpSellMessage(data: Data?) {
        if (mRemoveAllItemFromCartTapped) return
        data?.globalMessages?.let {
            if (it.qualifierMessages.isNullOrEmpty()) {
                binding.upSellMessageTextView?.visibility = View.GONE
                return
            }
            val qualifierMessage = it.qualifierMessages[0]
            binding.upSellMessageTextView.text = qualifierMessage
            binding.upSellMessageTextView.visibility =
                if (TextUtils.isEmpty(qualifierMessage)) View.GONE else View.VISIBLE
        }
    }

    override fun onOutOfStockProductsRemoved() {
        loadShoppingCart(false)
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
            cartText = requireContext().getString(R.string.available)
            pixel = (binding.btnCheckOut.height ?: 0 * 2.5).toInt()
            view = binding.btnCheckOut
            message =
                availableVouchersCount.toString() + requireContext().getString(
                    if (availableVouchersCount > 1) R.string.available_vouchers_toast_message
                    else R.string.available_voucher_toast_message
                )
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

    private fun navigateToAvailableVouchersPage() {
        val intent = Intent(context, AvailableVouchersToRedeemInCart::class.java)
        intent.putExtra(
            "VoucherDetails", Utils.toJson(voucherDetails)
        )
        startActivityForResult(
            intent, REDEEM_VOUCHERS_REQUEST_CODE
        )
    }

    override fun updateOrderTotal() {
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
        val activity: FragmentActivity = requireActivity()
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.Cart_promo_remove, activity)
        showProgressBar()
        removePromoCode(CouponClaimCode(promoCode)).enqueue(
            CompletionHandler(
                (object : IResponseListener<ShoppingCartResponse> {
                    override fun onSuccess(response: ShoppingCartResponse?) {
                        hideProgressBar()
                        when (response?.httpCode) {
                            200 -> {
                                updateCart(convertResponseToCartResponseObject(response), null)
                                if (voucherDetails?.promoCodes == null || voucherDetails?.promoCodes?.size == 0)
                                    showVouchersOrPromoCodeAppliedToast(
                                        getString(R.string.promo_code_removed_toast_message)
                                    )
                            }
                            502 -> response.response?.let {
                                Utils.displayValidationMessage(
                                    activity,
                                    CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                    response.response.desc,
                                    true
                                )
                            }
                            440 -> {
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

                    override fun onFailure(error: Throwable?) {
                        hideProgressBar()
                        Utils.displayValidationMessage(
                            activity,
                            CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                            getString(R.string.general_error_desc),
                            true
                        )
                    }
                }), ShoppingCartResponse::class.java
            )
        )
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

    fun enableItemDelete(enable: Boolean) {
        fadeCheckoutButton(!enable)
        enableEditCart(enable)
        setDeliveryLocationEnabled(enable)
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
        private const val GENERAL_ITEM = "GENERAL"
        private const val GIFT_ITEM = "GIFT"
    }
}