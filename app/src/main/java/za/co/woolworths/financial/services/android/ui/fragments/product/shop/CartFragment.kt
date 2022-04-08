package za.co.woolworths.financial.services.android.ui.fragments.product.shop

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_cart.*
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
import za.co.woolworths.financial.services.android.ui.views.WButton
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView.IWalkthroughActionListener
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ActionSheetDialogFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.CartUtils.Companion.filterCommerceItemFromCartResponse
import za.co.woolworths.financial.services.android.util.CartUtils.Companion.getAppliedVouchersCount
import za.co.woolworths.financial.services.android.util.CartUtils.Companion.updateItemLimitsBanner
import za.co.woolworths.financial.services.android.util.CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace
import za.co.woolworths.financial.services.android.util.FirebaseManager.Companion.logException
import za.co.woolworths.financial.services.android.util.FirebaseManager.Companion.setCrashlyticsString
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getPreferredDeliveryType
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.isDeliveryOptionClickAndCollect
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.presentEditDeliveryGeoLocationActivity
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.setDeliveryAddressView
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.showGeneralInfoDialog
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.updateCheckOutLink
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter.Companion.instance
import za.co.woolworths.financial.services.android.util.ToastUtils.ToastInterface
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.util.wenum.Delivery.Companion.getType
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*

class CartFragment : Fragment(), CartProductAdapter.OnItemClick, View.OnClickListener,
    NetworkChangeListener, ToastInterface, IWalkthroughActionListener,
    IRemoveProductsFromCartDialog {
    private val TAG = this.javaClass.simpleName
    private val GIFT_ITEM = "GIFT"
    private val GENERAL_ITEM = "GENERAL"
    private val TAG_AVAILABLE_VOUCHERS_TOAST = "AVAILABLE_VOUCHERS"
    private val TAG_ADDED_TO_LIST_TOAST = "ADDED_TO_LIST"
    private var mNumberOfListSelected = 0
    private var localCartCount = 0
    private var changeQuantityWasClicked = false
    private var errorMessageWasPopUp = false
    private val toastButtonWasClicked = false
    private var onRemoveItemFailed = false
    private var mRemoveAllItemFailed = false
    private var mRemoveAllItemFromCartTapped = false
    private var isAllInventoryAPICallSucceed = false
    private var mChangeQuantityList: MutableList<ChangeQuantity?>? = null
    private var mSkuInventories: HashMap<String, List<SkuInventory>>? = null
    private var mapStoreIdWithCommerceItems: Map<String, Collection<CommerceItem>>? = null
    var cartItems: ArrayList<CartItemGroup>? = null
        private set
    var itemLimitsBanner: ConstraintLayout? = null
    private var parentLayout: RelativeLayout? = null
    var itemLimitsMessage: TextView? = null
    var itemLimitsCounter: TextView? = null
    var upSellMessageTextView: TextView? = null
    var orderTotal: TextView? = null
    private var deliverLocationIcon: ImageView? = null
    private var nestedScrollView: NestedScrollView? = null
    private var rvCartList: RecyclerView? = null
    private var pBar: ProgressBar? = null
    private var pbRemoveAllItem: ProgressBar? = null
    private var tvDeliveryLocation: WTextView? = null
    private var btnClearCart: WTextView? = null
    private var rlCheckOut: LinearLayout? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    private var cartProductAdapter: CartProductAdapter? = null
    private var orderSummary: OrderSummary? = null
    private var mConnectionBroadcast: BroadcastReceiver? = null
    private var mToastUtils: ToastUtils? = null
    private val mDisposables: CompositeDisposable? = CompositeDisposable()
    private var mChangeQuantity: ChangeQuantity? = null
    private var mCommerceItem: CommerceItem? = null
    private var voucherDetails: VoucherDetails? = null
    var productCountMap: ProductCountMap? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        initViews()
        hideEditCart()
        localCartCount = instance.getCartItemCount()
        mChangeQuantityList = ArrayList(0)
        mChangeQuantity = ChangeQuantity()
        mConnectionBroadcast = Utils.connectionBroadCast(
            activity, this
        )
        mToastUtils = ToastUtils(this)
        mDisposables!!.add(
            WoolworthsApplication.getInstance()
                .bus()
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { `object` ->
                    if (`object` != null) {
                        if (`object` is CartState) {
                            val cartState = `object`
                            if (!TextUtils.isEmpty(cartState.state)) {
                                //setDeliveryLocation(cartState.getState());
                            } else if (cartState.indexState == CartState.CHANGE_QUANTITY) {
                                mChangeQuantity!!.quantity = cartState.quantity
                                queryServiceChangeQuantity()
                            }
                        } else if (`object` is ProductState) {
                            val productState = `object`
                            when (productState.state) {
                                ProductState.CANCEL_DIALOG_TAPPED -> if (cartProductAdapter != null) cartProductAdapter!!.onPopUpCancel(
                                    ProductState.CANCEL_DIALOG_TAPPED
                                )
                                ProductState.CLOSE_PDP_FROM_ADD_TO_LIST -> if (activity != null) {
                                    mToastUtils!!.activity = activity
                                    mToastUtils!!.currentState = TAG_ADDED_TO_LIST_TOAST
                                    val shoppingList = getString(R.string.shopping_list)
                                    mNumberOfListSelected = productState.count
                                    // shopping list vs shopping lists
                                    mToastUtils!!.cartText =
                                        if ((mNumberOfListSelected > 1)) shoppingList + "s" else shoppingList
                                    mToastUtils!!.pixel = btnCheckOut!!.height * 2
                                    mToastUtils!!.view = btnCheckOut
                                    mToastUtils!!.setMessage(R.string.added_to)
                                    mToastUtils!!.viewState = true
                                    mToastUtils!!.build()
                                }
                                else -> {}
                            }
                        }
                    }
                })
        )
        initializeLoggedInUserCartUI()
    }

    private fun initializeLoggedInUserCartUI() {
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            return
        }

        //Things to do after login is successful
        setEmptyCartUIUserName()
        //One time biometricsWalkthrough
        if (isVisible) {
            ScreenManager.presentBiometricWalkthrough(activity)
        }
        loadShoppingCart(false)
    }

    private fun initViews() {
        if (view == null) {
            return
        }
        val view = view
        val mBtnRetry: WButton = view!!.findViewById(R.id.btnRetry)
        mBtnRetry.setOnClickListener(this)
        rvCartList = view.findViewById(R.id.cartList)
        rlCheckOut = view.findViewById(R.id.rlCheckOut)
        parentLayout = view.findViewById(R.id.parentLayout)
        pBar = view.findViewById(R.id.loadingBar)
        upSellMessageTextView = view.findViewById(R.id.upSellMessageTextView)
        tvDeliveryLocation = view.findViewById(R.id.tvDeliveryLocation)
        deliverLocationIcon = view.findViewById(R.id.deliverLocationIcon)
        orderTotal = view.findViewById(R.id.orderTotal)
        nestedScrollView = view.findViewById(R.id.nestedScrollView)
        itemLimitsBanner = view.findViewById(R.id.itemLimitsBanner)
        itemLimitsMessage = view.findViewById(R.id.itemLimitsMessage)
        itemLimitsCounter = view.findViewById(R.id.itemLimitsCounter)
        val rlNoConnectionLayout = view.findViewById<RelativeLayout>(R.id.no_connection_layout)
        mErrorHandlerView = ErrorHandlerView(activity, rlNoConnectionLayout)
        mErrorHandlerView!!.setMargin(rlNoConnectionLayout, 0, 0, 0, 0)
        btnCheckOut?.setOnClickListener(this)
        orderTotalLayout.setOnClickListener(this)
        deliveryLocationConstLayout.setOnClickListener(this)

        //Empty cart UI
        view.findViewById<View>(R.id.empty_state_template).visibility = View.VISIBLE
        view.findViewById<View>(R.id.txt_dash_sub_title).visibility = View.GONE
        val imgView = view.findViewById<ImageView>(R.id.img_view)
        imgView.setImageResource(R.drawable.empty_cart_icon)
        val txtEmptyStateTitle = view.findViewById<TextView>(R.id.txt_dash_title)
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            val firstName = SessionUtilities.getInstance().jwt.name[0]
            txtEmptyStateTitle.text =
                getString(R.string.hi) + firstName + "," + System.getProperty("line.separator") + getString(
                    R.string.empty_cart_text
                )
        }
        val btnGoToProduct = view.findViewById<Button>(R.id.btn_dash_set_address)
        btnGoToProduct.text = getString(R.string.start_shopping)
        btnGoToProduct.setOnClickListener(this)
    }

    private fun initializeBottomTab() {
        if (activity is BottomNavigationActivity) {
            (activity as BottomNavigationActivity?)!!.showBottomNavigationMenu()
            (activity as BottomNavigationActivity?)!!.hideToolbar()
            (activity as BottomNavigationActivity?)!!.setToolbarTitle("")
        }
    }

    private fun setupToolbar() {
        Utils.updateStatusBarBackground(activity)
        btnEditCart?.setText(R.string.edit)
        btnClearCart?.visibility = View.GONE
        pbRemoveAllItem?.visibility = View.GONE
        btnEditCart?.setOnClickListener(this)
        btnClearCart?.setOnClickListener(this)
        if (activity is BottomNavigationActivity) {
            (activity as BottomNavigationActivity?)!!.hideToolbar()
        }
    }

    /****
     * mChangeQuantityList save all ChangeQuantityRequest after quantity selection
     * Top ChangeQuantity item in list is selected
     * Extract commerceId of the selected ChangeQuantity object
     * Perform changeQuantity call
     * Remove top changeQuantity object from list
     */
    private fun queryServiceChangeQuantity() {
        mChangeQuantityList!!.add(mChangeQuantity)
        changeQuantityAPI(mChangeQuantityList!![0])
        mChangeQuantityList!!.removeAt(0)
    }

    private fun setEmptyCartUIUserName() {
        if (!SessionUtilities.getInstance().isUserAuthenticated || view == null) {
            return
        }
        val view = view
        val firstName = SessionUtilities.getInstance().jwt.name[0]
        view!!.findViewById<View>(R.id.empty_state_template).visibility =
            View.VISIBLE
        val txtEmptyStateTitle = view.findViewById<TextView>(R.id.txt_dash_title)
        txtEmptyStateTitle.text =
            getString(R.string.hi) + firstName + "," + System.getProperty("line.separator") + getString(
                R.string.empty_cart_text
            )
    }

    fun onRemoveItem(visibility: Boolean) {
        pbRemoveAllItem!!.visibility =
            if (visibility) View.VISIBLE else View.GONE
        btnClearCart!!.visibility = if (visibility) View.GONE else View.VISIBLE
        btnEditCart!!.isEnabled = !visibility
    }

    fun onRemoveSuccess() {
        pbRemoveAllItem!!.visibility = View.GONE
        btnClearCart!!.visibility = View.GONE
    }

    fun resetToolBarIcons() {
        hideEditCart()
        btnClearCart!!.visibility = View.GONE
    }

    fun showEditCart() {
        btnEditCart!!.alpha = 1.0f
        btnEditCart!!.visibility = View.VISIBLE
        btnEditCart!!.isEnabled = true
    }

    fun hideEditCart() {
        btnEditCart!!.alpha = 0.0f
        btnEditCart!!.visibility = View.GONE
        btnEditCart!!.isEnabled = false
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
                    activity
                )
                removeAllCartItem(null)
            }
            R.id.deliveryLocationConstLayout -> locationSelectionClicked()
            R.id.btn_dash_set_address -> {
                val activity: Activity? = activity
                if (activity is BottomNavigator) {
                    val navigator = activity as BottomNavigator
                    navigator.navigateToTabIndex(BottomNavigationActivity.INDEX_PRODUCT, null)
                }
            }
            R.id.btnRetry -> if (NetworkManager.getInstance().isConnectedToNetwork(
                    activity
                )
            ) {
                errorMessageWasPopUp = false
                rvCartList!!.visibility = View.VISIBLE
                loadShoppingCart(false)
            }
            R.id.btnCheckOut -> {
                val checkOutActivity: Activity? = activity
                if ((checkOutActivity != null) && btnCheckOut!!.isEnabled && (orderSummary != null)) {
                    val deliveryType =
                        getType(Utils.getPreferredDeliveryLocation().fulfillmentDetails.deliveryType)
                    if ((deliveryType === Delivery.CNC) && (productCountMap != null) && (productCountMap!!.quantityLimit != null) && !productCountMap!!.quantityLimit!!.allowsCheckout!!) {
                        Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.CART_CLCK_CLLCT_CNFRM_LMT,
                            checkOutActivity
                        )
                        showMaxItemView()
                        return
                    }
                    // Go to Web checkout journey if...
                    if ((nativeCheckout != null
                                && !nativeCheckout!!.isNativeCheckoutEnabled)
                    ) {
                        val openCheckOutActivity = Intent(context, CartCheckoutActivity::class.java)
                        requireActivity().startActivityForResult(
                            openCheckOutActivity,
                            CheckOutFragment.REQUEST_CART_REFRESH_ON_DESTROY
                        )
                        checkOutActivity.overridePendingTransition(0, 0)
                    } else {
                        if (pBar!!.visibility == View.VISIBLE) {
                            return
                        }
                        // Get list of saved address and navigate to proper Checkout page.
                        callSavedAddress()
                    }
                }
            }
            R.id.orderTotalLayout -> nestedScrollView!!.post({ nestedScrollView!!.fullScroll(View.FOCUS_DOWN) })
            else -> {}
        }
    }

    fun toggleCartMode() {
        val isEditMode = toggleEditMode()
        btnEditCart!!.setText(if (isEditMode) R.string.done else R.string.edit)
        btnClearCart!!.visibility = if (isEditMode) View.VISIBLE else View.GONE
        deliveryLocationEnabled(!isEditMode)
    }

    private fun dismissProgress() {
        pbRemoveAllItem!!.visibility = View.GONE
    }

    private fun callSavedAddress() {
        pBar!!.visibility = View.VISIBLE
        val savedAddressCall = getSavedAddresses()
        savedAddressCall.enqueue(
            CompletionHandler(
                (object : IResponseListener<SavedAddressResponse> {
                    override fun onSuccess(response: SavedAddressResponse?) {
                        when (response!!.httpCode) {
                            AppConstant.HTTP_OK -> {
                                pBar!!.visibility = View.GONE
                                navigateToCheckout(response)
                            }
                            else -> {
                                pBar!!.visibility = View.GONE
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
        if (activity != null) {
            val activity: Activity? = activity
            val intent = Intent(activity, ErrorHandlerActivity::class.java)
            intent.putExtra(ErrorHandlerActivity.ERROR_TYPE, errorType)
            intent.putExtra(ErrorHandlerActivity.ERROR_MESSAGE, errorMessage)
            activity!!.startActivityForResult(intent, ErrorHandlerActivity.RESULT_RETRY)
        }
    }

    private fun navigateToCheckout(response: SavedAddressResponse?) {
        val activity: Activity? = activity
        if (((getPreferredDeliveryType() == Delivery.STANDARD) && !TextUtils.isEmpty(response!!.defaultAddressNickname))) {
            //   - CNAV : Checkout  activity
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.CART_BEGIN_CHECKOUT,
                getActivity()
            )
            val checkoutActivityIntent = Intent(getActivity(), CheckoutActivity::class.java)
            checkoutActivityIntent.putExtra(
                CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY,
                response
            )
            checkoutActivityIntent.putExtra(
                CheckoutAddressManagementBaseFragment.GEO_SLOT_SELECTION,
                true
            )
            activity!!.startActivityForResult(
                checkoutActivityIntent,
                REQUEST_PAYMENT_STATUS
            )
            activity.overridePendingTransition(
                R.anim.slide_from_right,
                R.anim.slide_out_to_left
            )
        } else {
//            - GNAV
//            CNC or No Address or no default address*/
            var placeId: String? = ""
            if (getDelivertyType() === Delivery.CNC) {
                placeId = getPlaceId()
            } else {
                placeId = getSelectedPlaceId((response)!!)
            }
            presentEditDeliveryGeoLocationActivity(
                requireActivity(),
                REQUEST_PAYMENT_STATUS,
                getDelivertyType(),
                placeId,
                true,
                false,
                response,
                null,
                ""
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
        mChangeQuantity!!.commerceId = commerceId.commerceItemInfo.getCommerceId()
        if (WoolworthsApplication.getInstance() != null) {
            val wGlobalState = WoolworthsApplication.getInstance().wGlobalState
            wGlobalState?.navigateFromQuantity(1)
        }
        val activity: Activity? = activity
        if (activity != null) {
            val editQuantityIntent = Intent(activity, ConfirmColorSizeActivity::class.java)
            editQuantityIntent.putExtra(
                ConfirmColorSizeActivity.SELECT_PAGE,
                ConfirmColorSizeActivity.QUANTITY
            )
            editQuantityIntent.putExtra("CART_QUANTITY_In_STOCK", commerceId.quantityInStock)
            activity.startActivity(editQuantityIntent)
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun totalItemInBasket(total: Int) {}
    override fun onOpenProductDetail(commerceItem: CommerceItem) {
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
        val gson = Gson()
        val strProductList = gson.toJson(productDetails)
        if (activity is BottomNavigationActivity) {
            // Move to shop tab first.
            (activity as BottomNavigationActivity?)!!.bottomNavigationById.currentItem =
                BottomNavigationActivity.INDEX_PRODUCT
            ScreenManager.openProductDetailFragment(activity, "", strProductList)
        }
    }

    override fun onGiftItemClicked(commerceItem: CommerceItem) {
        val activity: FragmentActivity = activity ?: return
        val giftWithPurchaseDialogDetailFragment = GiftWithPurchaseDialogDetailFragment()
        giftWithPurchaseDialogDetailFragment.show(
            (activity as AppCompatActivity).supportFragmentManager,
            GiftWithPurchaseDialogDetailFragment::class.java.simpleName
        )
    }

    fun toggleEditMode(): Boolean {
        val isEditMode = cartProductAdapter!!.toggleEditMode()
        if (isAllInventoryAPICallSucceed) Utils.fadeInFadeOutAnimation(btnCheckOut, isEditMode)
        resetItemDelete(isEditMode)
        return isEditMode
    }

    private fun resetItemDelete(isEditMode: Boolean) {
        if (isEditMode) {
            for (cartItemGroup: CartItemGroup in cartItems!!) {
                val commerceItemList = cartItemGroup.commerceItems
                for (cm: CommerceItem in commerceItemList) {
                    cm.setDeleteIconWasPressed(false)
                    cm.isDeletePressed = false
                }
            }
        }
        if (cartProductAdapter != null) cartProductAdapter!!.notifyDataSetChanged()
    }

    private fun locationSelectionClicked() {
        val activity: Activity? = activity
        if (activity != null) {
            var placeId: String? = ""
            if (Utils.getPreferredDeliveryLocation() != null) {
                if (Utils.getPreferredDeliveryLocation().fulfillmentDetails.address != null) {
                    placeId =
                        Utils.getPreferredDeliveryLocation().fulfillmentDetails.address!!.placeId
                }
            }
            presentEditDeliveryGeoLocationActivity(
                activity, REQUEST_SUBURB_CHANGE,
                getPreferredDeliveryType(),
                placeId,
                false,
                false,
                null,
                null,
                ""
            )
        }
    }

    fun bindCartData(cartResponse: CartResponse?) {
        parentLayout!!.visibility = View.VISIBLE
        mSkuInventories = HashMap()
        if (cartResponse!!.cartItems.size > 0) {
            empty_state_template?.visibility = View.GONE
            rvCartList!!.visibility = View.VISIBLE
            rlCheckOut!!.visibility = View.VISIBLE
            showEditCart()
            cartItems = cartResponse.cartItems
            orderSummary = cartResponse.orderSummary
            voucherDetails = cartResponse.voucherDetails
            productCountMap = cartResponse.productCountMap
            cartProductAdapter =
                CartProductAdapter(cartItems, this, orderSummary, activity, voucherDetails)
            queryServiceInventoryCall(cartResponse.cartItems)
            val mLayoutManager = LinearLayoutManager(activity)
            mLayoutManager.orientation = LinearLayoutManager.VERTICAL
            rvCartList!!.layoutManager = mLayoutManager
            rvCartList!!.adapter = cartProductAdapter
            updateOrderTotal()
            isMaterialPopUpClosed = false
            showRedeemVoucherFeatureWalkthrough()
        } else {
            updateCartSummary(0)
            rvCartList!!.visibility = View.GONE
            rlCheckOut!!.visibility = View.GONE
            onRemoveSuccess()
            empty_state_template?.visibility = View.VISIBLE
            Utils.deliveryLocationEnabled(activity, true, deliveryLocationConstLayout)
            resetToolBarIcons()
            isMaterialPopUpClosed = true
            showEditDeliveryLocationFeatureWalkthrough()
        }
        setItemLimitsBanner()
    }

    fun updateCart(cartResponse: CartResponse?, commerceItemToRemove: CommerceItem?) {
        orderSummary = cartResponse!!.orderSummary
        voucherDetails = cartResponse.voucherDetails
        productCountMap = cartResponse.productCountMap
        setItemLimitsBanner()
        if (cartResponse.cartItems.size > 0 && cartProductAdapter != null) {
            val emptyCartItemGroups = ArrayList<CartItemGroup>(0)
            for (cartItemGroup: CartItemGroup in cartItems!!) {
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
                    val updatedCommerceItem = filterCommerceItemFromCartResponse(
                        (cartResponse), commerceItem.commerceItemInfo.commerceId
                    )
                    if (updatedCommerceItem != null) {
                        commerceItem.priceInfo = updatedCommerceItem.priceInfo
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
                if (cartItemGroup.commerceItems.size == 0) {
                    emptyCartItemGroups.add(cartItemGroup) // Gather all the empty groups after deleting item.
                }
            }
            //remove all the empty groups
            for (cartItemGroup: CartItemGroup in emptyCartItemGroups) {
                cartItems!!.remove(cartItemGroup)
            }
            cartProductAdapter!!.notifyAdapter(cartItems, orderSummary, voucherDetails)
        } else {
            cartProductAdapter!!.clear()
            resetToolBarIcons()
            rlCheckOut!!.visibility = View.GONE
            rvCartList!!.visibility = View.GONE
            empty_state_template?.visibility = View.VISIBLE
            Utils.deliveryLocationEnabled(activity, true, deliveryLocationConstLayout)
        }
        if (productCountMap != null) {
            updateCartSummary((if (productCountMap!!.totalProductCount!! > 0) productCountMap!!.totalProductCount else 0)!!)
        }
    }

    private val bottomNavigationActivity: BottomNavigationActivity?
        private get() {
            val activity: Activity? = activity
            return if (!(activity is BottomNavigationActivity)) null else activity
        }

    override fun onDetach() {
        super.onDetach()
        if ((mDisposables != null
                    && !mDisposables.isDisposed)
        ) {
            mDisposables.dispose()
        }
        if (bottomNavigationActivity != null && bottomNavigationActivity!!.walkThroughPromtView != null) {
            bottomNavigationActivity!!.walkThroughPromtView.removeFromWindow()
        }
    }

    fun changeQuantity(cartResponse: CartResponse?, changeQuantity: ChangeQuantity?) {
        if (cartResponse!!.cartItems.size > 0 && cartProductAdapter != null) {
            val updatedCommerceItem =
                getUpdatedCommerceItem(cartResponse.cartItems, changeQuantity!!.commerceId)
            //update list instead of using the new list to handle inventory data
            for (cartItemGroupUpdated: CartItemGroup in cartResponse.cartItems) {
                var isGroup = false
                for (cartItemGroup: CartItemGroup in cartItems!!) {
                    if (cartItemGroupUpdated.type.equals(cartItemGroup.type, ignoreCase = true)) {
                        isGroup = true
                        break
                    }
                }
                if (!isGroup) cartItems!!.add(cartItemGroupUpdated)
            }
            if (updatedCommerceItem != null) {
                val emptyCartItemGroups = ArrayList<CartItemGroup>()
                for (cartItemGroup: CartItemGroup in cartItems!!) {
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
                    if (cartItemGroup.commerceItems.size == 0) {
                        emptyCartItemGroups.add(cartItemGroup) // Gather all the empty groups after deleting item.
                    }
                }

                //remove all the empty groups
                for (cartItemGroup: CartItemGroup in emptyCartItemGroups) {
                    cartItems!!.remove(cartItemGroup)
                }
                orderSummary = cartResponse.orderSummary
                voucherDetails = cartResponse.voucherDetails
                productCountMap = cartResponse.productCountMap
                cartProductAdapter!!.notifyAdapter(cartItems, orderSummary, voucherDetails)
            } else {
                val currentCartItemGroup = cartProductAdapter!!.cartItems
                for (cartItemGroup: CartItemGroup in currentCartItemGroup) {
                    for (currentItem: CommerceItem in cartItemGroup.commerceItems) {
                        if (currentItem.commerceItemInfo.commerceId.equals(
                                changeQuantity.commerceId,
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
                for (items: CartItemGroup in currentCartItemGroup) {
                    for (commerceItem: CommerceItem in items.commerceItems) {
                        if (commerceItem.quantityUploading) {
                            shouldEnableCheckOutAndEditButton = false
                            break
                        }
                    }
                }
                if (shouldEnableCheckOutAndEditButton) {
                    orderSummary = cartResponse.orderSummary
                    voucherDetails = cartResponse.voucherDetails
                    productCountMap = cartResponse.productCountMap
                    cartProductAdapter!!.notifyAdapter(
                        currentCartItemGroup,
                        orderSummary,
                        voucherDetails
                    )
                    fadeCheckoutButton(false)
                }
            }
            if (productCountMap != null && productCountMap!!.totalProductCount!! > 0) {
                instance.setCartCount((productCountMap!!.totalProductCount)!!)
            }
        } else {
            cartProductAdapter!!.clear()
            resetToolBarIcons()
            rlCheckOut!!.visibility = View.GONE
            rvCartList!!.visibility = View.GONE
            empty_state_template?.visibility = View.VISIBLE
        }
        onChangeQuantityComplete()
        setItemLimitsBanner()
    }

    private fun getUpdatedCommerceItem(
        cartItems: ArrayList<CartItemGroup>,
        commerceId: String
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
    }

    private fun onChangeQuantityComplete() {
        var quantityUploaded = false
        for (cartItemGroup: CartItemGroup in cartItems!!) {
            for (commerceItem: CommerceItem in cartItemGroup.commerceItems) {
                if (commerceItem.quantityUploading) quantityUploaded = true
            }
        }
        if (isAllInventoryAPICallSucceed && !quantityUploaded) {
            mChangeQuantityList = ArrayList()
            fadeCheckoutButton(false)
        }
        if (cartProductAdapter != null) cartProductAdapter!!.onChangeQuantityComplete()
    }

    private fun onChangeQuantityLoad() {
        cartProductAdapter!!.onChangeQuantityLoad()
    }

    private fun loadShoppingCart(onItemRemove: Boolean): Call<ShoppingCartResponse> {
        Utils.deliveryLocationEnabled(activity, false, deliveryLocationConstLayout)
        rlCheckOut!!.isEnabled = !onItemRemove
        rlCheckOut!!.visibility = if (onItemRemove) View.VISIBLE else View.GONE
        pBar!!.visibility = View.VISIBLE
        if (cartProductAdapter != null) {
            cartProductAdapter!!.clear()
        }
        hideEditCart()
        val shoppingCartResponseCall = getShoppingCart()
        shoppingCartResponseCall.enqueue(
            CompletionHandler(
                (object : IResponseListener<ShoppingCartResponse> {
                    override fun onSuccess(shoppingCartResponse: ShoppingCartResponse?) {
                        try {
                            pBar!!.visibility = View.GONE
                            when (shoppingCartResponse!!.httpCode) {
                                200 -> {
                                    onRemoveItemFailed = false
                                    rlCheckOut!!.visibility = View.VISIBLE
                                    rlCheckOut!!.isEnabled = true
                                    val cartResponse =
                                        convertResponseToCartResponseObject(shoppingCartResponse)
                                    updateCheckOutLink(shoppingCartResponse.data[0].jSessionId)
                                    bindCartData(cartResponse)
                                    if (onItemRemove) {
                                        cartProductAdapter!!.setEditMode(true)
                                    }
                                    Utils.deliveryLocationEnabled(
                                        activity, true, deliveryLocationConstLayout
                                    )
                                    if (shoppingCartResponse.data[0].orderSummary.fulfillmentDetails != null) {
                                        Utils.savePreferredDeliveryLocation(
                                            ShoppingDeliveryLocation(
                                                shoppingCartResponse.data[0].orderSummary.fulfillmentDetails
                                            )
                                        )
                                    }
                                    setItemLimitsBanner()
                                }
                                440 -> {
                                    //TODO:: improve error handling
                                    SessionUtilities.getInstance()
                                        .setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                                    SessionExpiredUtilities.getInstance().showSessionExpireDialog(
                                        activity as AppCompatActivity?,
                                        this@CartFragment
                                    )
                                    onChangeQuantityComplete()
                                }
                                else -> {
                                    Utils.deliveryLocationEnabled(
                                        activity, true, deliveryLocationConstLayout
                                    )
                                    if (shoppingCartResponse.response != null) Utils.displayValidationMessage(
                                        activity,
                                        CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                        shoppingCartResponse.response.desc,
                                        true
                                    )
                                }
                            }
                            Utils.deliveryLocationEnabled(
                                activity, true, deliveryLocationConstLayout
                            )
                        } catch (ex: Exception) {
                            logException(ex)
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        val activity: Activity? = activity
                        if (activity != null && isAdded) {
                            activity.runOnUiThread(Runnable {
                                if (!onItemRemove) {
                                    Utils.deliveryLocationEnabled(
                                        requireActivity(),
                                        true,
                                        deliveryLocationConstLayout
                                    )
                                    rvCartList!!.visibility = View.GONE
                                    rlCheckOut!!.visibility = View.GONE
                                    if (pBar != null) pBar!!.visibility = View.GONE
                                    mErrorHandlerView!!.showErrorHandler()
                                }
                            })
                        }
                    }
                }), ShoppingCartResponse::class.java
            )
        )
        return shoppingCartResponseCall
    }

    private fun changeQuantityAPI(changeQuantity: ChangeQuantity?): Call<ShoppingCartResponse> {
        cartProductAdapter!!.onChangeQuantityLoad()
        fadeCheckoutButton(true)
        val shoppingCartResponseCall = getChangeQuantity(
            (changeQuantity)!!
        )
        shoppingCartResponseCall.enqueue(
            CompletionHandler<ShoppingCartResponse>(
                (object : IResponseListener<ShoppingCartResponse> {
                    override fun onSuccess(shoppingCartResponse: ShoppingCartResponse?) {
                        try {
                            if (shoppingCartResponse!!.httpCode == 200) {
                                val cartResponse =
                                    convertResponseToCartResponseObject(shoppingCartResponse)
                                changeQuantity(cartResponse, changeQuantity)
                            } else {
                                onChangeQuantityComplete()
                            }
                        } catch (ex: Exception) {
                            logException(ex)
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        val activity: Activity? = activity
                        activity?.runOnUiThread(object : Runnable {
                            override fun run() {
                                mErrorHandlerView!!.showToast()
                                changeQuantityWasClicked = true
                                if (cartProductAdapter != null) cartProductAdapter!!.onChangeQuantityError()
                            }
                        })
                    }
                }), ShoppingCartResponse::class.java
            )
        )
        return shoppingCartResponseCall
    }

    fun removeItem(commerceItem: CommerceItem) {
        removeCartItem(commerceItem.commerceItemInfo.commerceId).enqueue(
            CompletionHandler<ShoppingCartResponse>(
                (object : IResponseListener<ShoppingCartResponse> {
                    override fun onSuccess(response: ShoppingCartResponse?) {}
                    override fun onFailure(error: Throwable?) {}
                }), ShoppingCartResponse::class.java
            )
        )
    }

    fun removeCartItem(commerceItem: CommerceItem): Call<ShoppingCartResponse> {
        mCommerceItem = commerceItem
        val shoppingCartResponseCall = removeCartItem(commerceItem.commerceItemInfo.getCommerceId())
        shoppingCartResponseCall.enqueue(
            CompletionHandler<ShoppingCartResponse>(
                (object : IResponseListener<ShoppingCartResponse> {
                    override fun onSuccess(shoppingCartResponse: ShoppingCartResponse?) {
                        try {
                            if (shoppingCartResponse!!.httpCode == 200) {
                                val cartResponse =
                                    convertResponseToCartResponseObject(shoppingCartResponse)
                                updateCart(cartResponse, commerceItem)
                                if (cartResponse!!.cartItems != null) {
                                    if (cartResponse.cartItems.isEmpty()) onRemoveSuccess()
                                } else {
                                    onRemoveSuccess()
                                }
                            } else {
                                if (cartProductAdapter != null) resetItemDelete(true)
                            }
                            enableItemDelete(false)
                        } catch (ex: Exception) {
                            logException(ex)
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        val activity: Activity? = activity
                        activity?.runOnUiThread(Runnable {
                            if (cartProductAdapter != null) {
                                onRemoveItemLoadFail(commerceItem, true)
                                onRemoveItemFailed = true
                                enableItemDelete(false)
                            }
                            mErrorHandlerView!!.showToast()
                        })
                    }
                }), ShoppingCartResponse::class.java
            )
        )
        return shoppingCartResponseCall
    }

    fun removeAllCartItem(commerceItem: CommerceItem?): Call<ShoppingCartResponse> {
        mRemoveAllItemFromCartTapped = true
        onRemoveItem(true)
        val shoppingCartResponseCall = removeAllCartItems()
        shoppingCartResponseCall.enqueue(
            CompletionHandler<ShoppingCartResponse>(
                (object : IResponseListener<ShoppingCartResponse> {
                    override fun onSuccess(shoppingCartResponse: ShoppingCartResponse?) {
                        try {
                            if (shoppingCartResponse!!.httpCode == 200) {
                                val cartResponse =
                                    convertResponseToCartResponseObject(shoppingCartResponse)
                                mRemoveAllItemFromCartTapped = false
                                updateCart(cartResponse, commerceItem)
                                updateCartSummary(0)
                                onRemoveSuccess()
                            } else {
                                onRemoveItem(false)
                            }
                            Utils.deliveryLocationEnabled(
                                activity, true, deliveryLocationConstLayout
                            )
                        } catch (ex: Exception) {
                            if (ex.message != null) Log.e(TAG, ex.message!!)
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        val activity: Activity? = activity
                        activity?.runOnUiThread(Runnable {
                            mRemoveAllItemFailed = true
                            onRemoveItem(false)
                            mErrorHandlerView!!.hideErrorHandler()
                            mErrorHandlerView!!.showToast()
                        })
                    }
                }), ShoppingCartResponse::class.java
            )
        )
        return shoppingCartResponseCall
    }

    private fun removeItemProgressBar(commerceItem: CommerceItem?, visibility: Boolean) {
        if (commerceItem == null) {
            onRemoveItem(visibility)
        }
    }

    private fun onRemoveItemLoadFail(commerceItem: CommerceItem, state: Boolean) {
        mCommerceItem = commerceItem
        resetItemDelete(true)
    }

    fun convertResponseToCartResponseObject(response: ShoppingCartResponse?): CartResponse? {
        var cartResponse: CartResponse? = null
        if (response == null) return null
        try {
            displayUpSellMessage(response.data[0])
            cartResponse = CartResponse()
            cartResponse.httpCode = response.httpCode
            val data = response.data[0]
            cartResponse.orderSummary = data.orderSummary
            cartResponse.voucherDetails = data.voucherDetails
            cartResponse.productCountMap = data.productCountMap // set delivery location
            if (cartResponse.orderSummary.fulfillmentDetails != null) {
                Utils.savePreferredDeliveryLocation(ShoppingDeliveryLocation(cartResponse.orderSummary.fulfillmentDetails))
            }
            setDeliveryLocation(Utils.getPreferredDeliveryLocation())
            val itemsObject = JSONObject(Gson().toJson(data.items))
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
                Log.e("giftWithPurchase", key)
                if (key.contains(ProductType.DEFAULT.value)) cartItemGroup.setType(ProductType.DEFAULT.shortHeader) else if (key.contains(
                        ProductType.GIFT_COMMERCE_ITEM.value
                    )
                ) cartItemGroup.setType(ProductType.GIFT_COMMERCE_ITEM.shortHeader) else if (key.contains(
                        ProductType.HOME_COMMERCE_ITEM.value
                    )
                ) cartItemGroup.setType(ProductType.HOME_COMMERCE_ITEM.shortHeader) else if (key.contains(
                        ProductType.FOOD_COMMERCE_ITEM.value
                    )
                ) cartItemGroup.setType(ProductType.FOOD_COMMERCE_ITEM.shortHeader) else if (key.contains(
                        ProductType.CLOTHING_COMMERCE_ITEM.value
                    )
                ) cartItemGroup.setType(ProductType.CLOTHING_COMMERCE_ITEM.shortHeader) else if (key.contains(
                        ProductType.PREMIUM_BRAND_COMMERCE_ITEM.value
                    )
                ) cartItemGroup.setType(ProductType.PREMIUM_BRAND_COMMERCE_ITEM.shortHeader) else cartItemGroup.setType(
                    ProductType.OTHER_ITEMS.shortHeader
                )
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
                    }
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
        val activity: Activity? = activity
        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.CART_LIST)
        activity?.registerReceiver(
            mConnectionBroadcast,
            IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        )
        val lastDeliveryLocation = Utils.getPreferredDeliveryLocation()
        lastDeliveryLocation?.let { setDeliveryLocation(it) }
    }

    override fun onPause() {
        super.onPause()
        val activity: Activity? = activity
        if (activity != null && mConnectionBroadcast != null) {
            activity.unregisterReceiver(mConnectionBroadcast)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == CustomPopUpWindow.CART_DEFAULT_ERROR_TAPPED || resultCode == ActionSheetDialogFragment.DIALOG_REQUEST_CODE) {
            val activity: Activity? = activity
            if (activity != null) {
                activity.setResult(CustomPopUpWindow.CART_DEFAULT_ERROR_TAPPED)
                activity.finish()
                activity.overridePendingTransition(R.anim.slide_down_anim, R.anim.stay)
                return
            }
        }
        if (requestCode == SSOActivity.SSOActivityResult.LAUNCH.rawValue()) {
            if (SessionUtilities.getInstance().isUserAuthenticated) {
                if (resultCode == Activity.RESULT_OK) {
                    // Checkout completed successfully
                    val lastDeliveryLocation = Utils.getPreferredDeliveryLocation()
                    if (lastDeliveryLocation != null) {

                        // Show loading state
                        rlCheckOut!!.visibility = View.GONE
                        pBar!!.visibility = View.VISIBLE
                        if (cartProductAdapter != null) {
                            cartProductAdapter!!.clear()
                        }
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
            checkLocationChangeAndReload()
        }
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                BottomNavigationActivity.PDP_REQUEST_CODE -> {
                    val activity: FragmentActivity = activity ?: return
                    loadShoppingCart(false)
                    loadShoppingCartAndSetDeliveryLocation()
                    val productCountMap = Utils.jsonStringToObject(
                        data!!.getStringExtra("ProductCountMap"), ProductCountMap::class.java
                    ) as ProductCountMap
                    val itemsCount = data.getIntExtra("ItemsCount", 0)
                    if (isDeliveryOptionClickAndCollect() && productCountMap.quantityLimit!!.foodLayoutColour != null) {
                        showItemsLimitToastOnAddToCart(
                            (rlCheckOut)!!,
                            productCountMap,
                            activity,
                            itemsCount,
                            false
                        )
                    } else {
                        buildAddToCartSuccessToast((rlCheckOut)!!, false, activity, null)
                    }
                }
                REQUEST_SUBURB_CHANGE -> loadShoppingCartAndSetDeliveryLocation()
                REDEEM_VOUCHERS_REQUEST_CODE, APPLY_PROMO_CODE_REQUEST_CODE -> {
                    val shoppingCartResponse = Utils.strToJson(
                        data!!.getStringExtra("ShoppingCartResponse"),
                        ShoppingCartResponse::class.java
                    ) as ShoppingCartResponse
                    updateCart(convertResponseToCartResponseObject(shoppingCartResponse), null)
                    if (requestCode == REDEEM_VOUCHERS_REQUEST_CODE) showVouchersOrPromoCodeAppliedToast(
                        getString(
                            if (getAppliedVouchersCount(
                                    voucherDetails!!.vouchers
                                ) > 0
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
                CheckOutFragment.RESULT_RELOAD_CART -> checkLocationChangeAndReload()
                Activity.RESULT_OK -> requireActivity().onBackPressed()
            }
        }
        if (requestCode == REQUEST_SUBURB_CHANGE) {
            loadShoppingCartAndSetDeliveryLocation()
        }
        if (requestCode == ScreenManager.CART_LAUNCH_VALUE && resultCode == SSOActivity.SSOActivityResult.STATE_MISMATCH.rawValue()) {
            // login screen opens on cart and user closes it without login then move tab to last opened tab.
            val activity: Activity? = activity
            if (activity != null && activity is BottomNavigationActivity) {
                val previousTabIndex = activity.previousTabIndex
                activity.bottomNavigationById.currentItem = previousTabIndex
            }
        }

        // Retry callback when saved address api fails
        if (resultCode == ErrorHandlerActivity.RESULT_RETRY) {
            callSavedAddress()
        }
    }

    private fun checkLocationChangeAndReload() {
        //TODO: need to refactor
        /* ShoppingDeliveryLocation deliveryLocation = Utils.getPreferredDeliveryLocation();
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
        }*/
    }

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
            mErrorHandlerView!!.hideErrorHandler()
            loadShoppingCart(true)
            return
        }
        if (mRemoveAllItemFailed) {
            removeAllCartItem(null)
            mRemoveAllItemFailed = false
            return
        }
        if (changeQuantityWasClicked) {
            if (cartProductAdapter != null) {
                cartProductAdapter!!.onChangeQuantityLoad(mCommerceItem)
            }
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
                val cartItems = cartProductAdapter!!.cartItems
                for (cartItemGroup: CartItemGroup in cartItems) {
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

    fun initInventoryRequest(
        storeId: String?,
        multiSku: String?
    ): Call<SkusInventoryForStoreResponse> {
        val skuInventoryForStoreResponseCall = getInventorySkuForStore(
            (storeId)!!, (multiSku)!!
        )
        skuInventoryForStoreResponseCall.enqueue(
            CompletionHandler<SkusInventoryForStoreResponse>(
                (object : IResponseListener<SkusInventoryForStoreResponse> {
                    override fun onSuccess(skusInventoryForStoreResponse: SkusInventoryForStoreResponse?) {
                        if (skusInventoryForStoreResponse!!.httpCode == 200) {
                            mSkuInventories!![skusInventoryForStoreResponse.storeId] =
                                skusInventoryForStoreResponse.skuInventory
                            if (mSkuInventories!!.size == mapStoreIdWithCommerceItems!!.size) {
                                updateCartListWithAvailableStock(mSkuInventories)
                            }
                        } else {
                            isAllInventoryAPICallSucceed = false
                            if (!errorMessageWasPopUp) {
                                val activity: Activity? = activity
                                if (skusInventoryForStoreResponse.response == null || activity == null) return
                                if (TextUtils.isEmpty(skusInventoryForStoreResponse.response.desc)) return
                                Utils.displayValidationMessage(
                                    activity,
                                    CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                    skusInventoryForStoreResponse.response.desc
                                )
                                errorMessageWasPopUp = true
                            }
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        val activity: Activity? = activity
                        disableQuantitySelector(error, activity)
                    }
                }), SkusInventoryForStoreResponse::class.java
            )
        )
        return skuInventoryForStoreResponseCall
    }

    private fun disableQuantitySelector(error: Throwable?, activity: Activity?) {
        if (activity == null || !isAdded) return
        activity.runOnUiThread(Runnable {
            if (error is SocketTimeoutException) {
                if (cartProductAdapter != null && btnCheckOut != null) {
                    val cartItems: ArrayList<CartItemGroup> = cartProductAdapter!!.cartItems
                    for (cartItemGroup: CartItemGroup in cartItems) {
                        for (commerceItem: CommerceItem in cartItemGroup.commerceItems) {
                            if (!commerceItem.isStockChecked) {
                                commerceItem.quantityInStock = -1
                                commerceItem.isStockChecked = true
                            }
                        }
                    }
                    cartProductAdapter!!.updateStockAvailability(cartItems)
                }
            } else if (error is ConnectException || error is UnknownHostException) {
                if (cartProductAdapter != null && btnCheckOut != null) {
                    val cartItems: ArrayList<CartItemGroup> = cartProductAdapter!!.cartItems
                    for (cartItemGroup: CartItemGroup in cartItems) {
                        for (commerceItem: CommerceItem in cartItemGroup.commerceItems) {
                            if (!commerceItem.isStockChecked) {
                                commerceItem.quantityInStock = -1
                            }
                        }
                    }
                    cartProductAdapter!!.updateStockAvailability(cartItems)
                }
            }
            enableEditCart()
            btnCheckOut!!.isEnabled = false
            rlCheckOut!!.isEnabled = false
        })
    }

    private fun updateCartListWithAvailableStock(mSkuInventories: HashMap<String, List<SkuInventory>>?) {
        isAllInventoryAPICallSucceed = true
        for (cartItemGroup: CartItemGroup in cartItems!!) {
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
        if (cartProductAdapter != null) cartProductAdapter!!.updateStockAvailability(cartItems)
    }

    // If CommerceItem quantity in cart is more then inStock Update quantity to match stock
    private fun updateItemQuantityToMatchStock() {
        var isAnyItemNeedsQuantityUpdate = false
        val itemsTobeRemovedFromCart = ArrayList<CommerceItem>()
        for (cartItemGroup: CartItemGroup in cartItems!!) {
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
        if (!btnCheckOut!!.isEnabled && isAllInventoryAPICallSucceed && !isAnyItemNeedsQuantityUpdate) {
            fadeCheckoutButton(false)
            if (voucherDetails != null && isAdded) showAvailableVouchersToast(voucherDetails!!.activeVouchersCount)
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
        Utils.fadeInFadeOutAnimation(btnCheckOut, value)
    }

    fun deliveryLocationEnabled(isEditMode: Boolean) {
        Utils.deliveryLocationEnabled(activity, isEditMode, deliveryLocationConstLayout)
    }

    override fun onToastButtonClicked(currentState: String) {
        when (currentState) {
            TAG_ADDED_TO_LIST_TOAST -> {
                val activity: FragmentActivity = activity ?: return
                val intent = Intent()
                intent.putExtra("count", mNumberOfListSelected)
                if (mNumberOfListSelected == 1) {
                    val woolworthsApplication = WoolworthsApplication.getInstance() ?: return
                    val globalState = woolworthsApplication.wGlobalState
                    val shoppingListRequest = globalState.shoppingListRequest
                    if (shoppingListRequest != null) {
                        for (shoppingList: ShoppingList in shoppingListRequest) {
                            if (shoppingList.shoppingListRowWasSelected) {
                                intent.putExtra("listId", shoppingList.listId)
                                intent.putExtra("listName", shoppingList.listName)
                            }
                        }
                    }
                }
                activity.setResult(MOVE_TO_LIST_ON_TOAST_VIEW_CLICKED, intent)
                activity.finish()
                activity.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
            }
            TAG_AVAILABLE_VOUCHERS_TOAST -> {
                val activity: FragmentActivity = activity ?: return
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.Cart_ovr_popup_view,
                    activity
                )
                navigateToAvailableVouchersPage()
            }
            else -> {}
        }
    }

    fun setDeliveryLocation(shoppingDeliveryLocation: ShoppingDeliveryLocation?) {
        //TODO: Redesign data mapping
        requireActivity().apply {
            /*setDeliveryAddressView(
                this,
                (shoppingDeliveryLocation)!!,
                (tvDeliveryTitle)!!,
                (tvDeliveryLocation)!!,
                deliverLocationIcon
            )*/
        }
    }

    private fun enableEditCart(enable: Boolean) {
        Utils.fadeInFadeOutAnimation(btnEditCart, enable)
        btnEditCart!!.isEnabled = !enable
    }

    fun enableEditCart() {
        Utils.fadeInFadeOutAnimation(btnEditCart, false)
        btnEditCart!!.isEnabled = true
    }

    private fun showEditDeliveryLocationFeatureWalkthrough() {
        if ((!AppInstanceObject.get().featureWalkThrough.showTutorials || AppInstanceObject.get().featureWalkThrough.deliveryLocation
                    || (bottomNavigationActivity == null) || (deliverLocationIcon == null) || !isAdded || !isVisible)
        ) return
        val activity = bottomNavigationActivity
        setCrashlyticsString(
            getString(R.string.crashlytics_materialshowcase_key),
            this.javaClass.simpleName
        )
        activity!!.walkThroughPromtView =
            WMaterialShowcaseView.Builder(activity, WMaterialShowcaseView.Feature.DELIVERY_LOCATION)
                .setTarget(deliverLocationIcon)
                .setTitle(R.string.your_delivery_location)
                .setDescription(R.string.walkthrough_delivery_location_desc)
                .setActionText(R.string.tips_edit_delivery_location)
                .setImage(R.drawable.tips_tricks_ic_stores)
                .setAction(this)
                .setShapePadding(24)
                .setArrowPosition(WMaterialShowcaseView.Arrow.TOP_LEFT)
                .setMaskColour(resources.getColor(R.color.semi_transparent_black)).build()
        activity.walkThroughPromtView.show(activity)
    }

    override fun onWalkthroughActionButtonClick(feature: WMaterialShowcaseView.Feature) {
        if (feature == WMaterialShowcaseView.Feature.DELIVERY_LOCATION) onClick((deliveryLocationConstLayout)!!)
    }

    override fun onPromptDismiss(feature: WMaterialShowcaseView.Feature) {
        isMaterialPopUpClosed = true
        if (voucherDetails != null && isAdded) showAvailableVouchersToast(voucherDetails!!.activeVouchersCount)
    }

    private fun displayUpSellMessage(data: Data?) {
        if ((data == null) || (data.globalMessages == null) || mRemoveAllItemFromCartTapped) return
        val globalMessages = data.globalMessages
        if (globalMessages.qualifierMessages == null || globalMessages.qualifierMessages.isEmpty()) return
        val qualifierMessage = globalMessages.qualifierMessages[0]
        upSellMessageTextView!!.text = qualifierMessage
        upSellMessageTextView!!.visibility =
            if (TextUtils.isEmpty(qualifierMessage)) View.GONE else View.VISIBLE
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

    fun showRedeemVoucherFeatureWalkthrough() {
        val activity = bottomNavigationActivity
        if ((activity == null) || !isAdded || !isVisible) {
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
            .setMaskColour(resources.getColor(R.color.semi_transparent_black)).build()
        activity.walkThroughPromtView.show(activity)
    }

    fun showAvailableVouchersToast(availableVouchersCount: Int) {
        if (availableVouchersCount < 1 || !isMaterialPopUpClosed) return
        mToastUtils!!.activity = activity
        mToastUtils!!.currentState = TAG_AVAILABLE_VOUCHERS_TOAST
        mToastUtils!!.cartText = getString(R.string.available)
        mToastUtils!!.pixel = (btnCheckOut!!.height * 2.5).toInt()
        mToastUtils!!.view = btnCheckOut
        mToastUtils!!.message =
            availableVouchersCount.toString() + getString(if (availableVouchersCount > 1) R.string.available_vouchers_toast_message else R.string.available_voucher_toast_message)
        mToastUtils!!.setAllCapsUpperCase(true)
        mToastUtils!!.viewState = true
        mToastUtils!!.build()
    }

    fun showVouchersOrPromoCodeAppliedToast(message: String?) {
        if (isAdded) {
            mToastUtils!!.activity = activity
            mToastUtils!!.currentState = TAG
            mToastUtils!!.pixel = (btnCheckOut!!.height * 2.5).toInt()
            mToastUtils!!.view = btnCheckOut
            mToastUtils!!.message = message
            mToastUtils!!.viewState = false
            mToastUtils!!.buildCustomToast()
        }
    }

    override fun onViewVouchers() {
        navigateToAvailableVouchersPage()
    }

    fun navigateToAvailableVouchersPage() {
        val intent = Intent(context, AvailableVouchersToRedeemInCart::class.java)
        intent.putExtra(
            "VoucherDetails", Utils
                .toJson(voucherDetails)
        )
        startActivityForResult(
            intent, REDEEM_VOUCHERS_REQUEST_CODE
        )
    }

    override fun updateOrderTotal() {
        if (orderSummary != null) {
            orderTotal!!.text = formatAmountToRandAndCentWithSpace(
                orderSummary!!.total
            )
        }
    }

    override fun onEnterPromoCode() {
        val activity: FragmentActivity = activity ?: return
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.Cart_promo_enter, activity)
        navigateToApplyPromoCodePage()
    }

    override fun onRemovePromoCode(promoCode: String) {
        val activity: FragmentActivity = activity ?: return
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.Cart_promo_remove, activity)
        showProgressBar()
        removePromoCode(CouponClaimCode(promoCode)).enqueue(
            CompletionHandler<ShoppingCartResponse>(
                (object : IResponseListener<ShoppingCartResponse> {
                    override fun onSuccess(response: ShoppingCartResponse?) {
                        hideProgressBar()
                        when (response!!.httpCode) {
                            200 -> {
                                updateCart(convertResponseToCartResponseObject(response), null)
                                if (voucherDetails!!.promoCodes == null || voucherDetails!!.promoCodes.size == 0) showVouchersOrPromoCodeAppliedToast(
                                    getString(R.string.promo_code_removed_toast_message)
                                )
                            }
                            502 -> if (response.response != null) Utils.displayValidationMessage(
                                getActivity(),
                                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                response.response.desc,
                                true
                            )
                            440 -> {
                                SessionUtilities.getInstance()
                                    .setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                                SessionExpiredUtilities.getInstance().showSessionExpireDialog(
                                    getActivity() as AppCompatActivity?,
                                    this@CartFragment
                                )
                            }
                            else -> if (response.response != null) Utils.displayValidationMessage(
                                getActivity(),
                                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                getString(R.string.general_error_desc),
                                true
                            )
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        hideProgressBar()
                        Utils.displayValidationMessage(
                            getActivity(),
                            CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                            getString(R.string.general_error_desc),
                            true
                        )
                    }
                }!!), ShoppingCartResponse::class.java
            )
        )
    }

    fun navigateToApplyPromoCodePage() {
        val intent = Intent(context, AvailableVouchersToRedeemInCart::class.java)
        startActivityForResult(
            intent, APPLY_PROMO_CODE_REQUEST_CODE
        )
    }

    private fun hideProgressBar() {
        if (activity != null) {
            pBar!!.visibility = View.GONE
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    private fun showProgressBar() {
        if (activity != null) {
            pBar!!.visibility = View.VISIBLE
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        }
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
        val activity: Activity? = activity
        if (activity != null && isAdded) {
            updateItemLimitsBanner(
                productCountMap,
                (itemLimitsBanner)!!,
                (itemLimitsMessage)!!,
                (itemLimitsCounter)!!,
                getPreferredDeliveryType() === Delivery.CNC
            )
        }
    }

    fun enableItemDelete(enable: Boolean) {
        enableEditCart(enable)
        fadeCheckoutButton(enable)
        deliveryLocationEnabled(!enable)
    }

    companion object {
        private val CART_BACK_PRESSED_CODE = 9
        private val PDP_LOCATION_CHANGED_BACK_PRESSED_CODE = 18
        val REQUEST_PAYMENT_STATUS = 4775
        private val REQUEST_SUBURB_CHANGE = 143
        val REDEEM_VOUCHERS_REQUEST_CODE = 1979
        val APPLY_PROMO_CODE_REQUEST_CODE = 1989
        val MOVE_TO_LIST_ON_TOAST_VIEW_CLICKED = 1020
        private var isMaterialPopUpClosed = true
    }
}