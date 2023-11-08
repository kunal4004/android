package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.OrderDetailsFragmentBinding
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import org.json.JSONObject
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.contracts.IToastInterface
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsItem
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Parameter
import za.co.woolworths.financial.services.android.onecartgetstream.OCChatActivity
import za.co.woolworths.financial.services.android.ui.activities.CancelOrderProgressActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_PRODUCT
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.adapters.OrderDetailsAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.shop.helpandsupport.HelpAndSupportFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.ProductTypeDetails
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.dto.AddToWishListFirebaseEventData
import za.co.woolworths.financial.services.android.util.analytics.dto.toAnalyticItemList
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import za.co.woolworths.financial.services.android.util.voc.VoiceOfCustomerManager

class OrderDetailsFragment : BaseFragmentBinding<OrderDetailsFragmentBinding>(OrderDetailsFragmentBinding::inflate), OrderDetailsAdapter.OnItemClick,
    CancelOrderConfirmationDialogFragment.ICancelOrderConfirmation,
    OrderHistoryErrorDialogFragment.IOrderHistoryErrorDialogDismiss, IToastInterface {

    companion object {
        val PROMO_NOTE_FOOD = "FOOD"

        fun getInstance(orderId: String, isNaviagtedFromMyAccount: Boolean = false) =
            OrderDetailsFragment().withArgs {
                putString(AppConstant.Keys.ARG_ORDER, orderId)
                putBoolean(AppConstant.NAVIGATED_FROM_MY_ACCOUNTS, isNaviagtedFromMyAccount)
            }

        fun getInstance(params: Parameter) = OrderDetailsFragment().withArgs {
            putParcelable(AppConstant.Keys.ARG_NOTIFICATION_PARAMETERS, params)
        }
    }

    private var orderItemList: ArrayList<CommerceItem> = ArrayList<CommerceItem>()
    private var dataList = arrayListOf<OrderDetailsItem>()
    private var argOrderId: String? = null
    private var orderDetailsResponse: OrderDetailsResponse? = null
    var isNavigatedFromMyAccounts: Boolean = false
    private var mBottomNavigator: BottomNavigator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            argOrderId = it.getString(AppConstant.Keys.ARG_ORDER, "")
            val notificationParams: Parameter? = it.getParcelable(AppConstant.Keys.ARG_NOTIFICATION_PARAMETERS) as? Parameter
            notificationParams?.let { parameter ->
                argOrderId = parameter.orderId
            }
            isNavigatedFromMyAccounts = it.getBoolean(AppConstant.NAVIGATED_FROM_MY_ACCOUNTS, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed({
            requireActivity().runOnUiThread {
                initViews()
            }
        }, 100)
        addFragmentResultListener()
    }

    private fun addFragmentResultListener() {
        KotlinUtils.setAddToListFragmentResultListener(
            activity = requireActivity(),
            lifecycleOwner = viewLifecycleOwner,
            toastContainerView = binding.mainLayout,
            onToastClick = {}
        )
    }


    private fun initViews() {
        when (isNavigatedFromMyAccounts) {
            true -> Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.Acc_My_Orders_DT,
                requireActivity()
            )
            false -> Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.Shop_My_Orders_DT,
                requireActivity()
            )
        }

        binding.apply {
            mBottomNavigator?.apply {
                removeToolbar()
            }
            tvSelectAll?.visibility = View.VISIBLE
            tvSelectAll?.text = getString(R.string.dash_help)
            btnBack?.setOnClickListener { requireActivity().onBackPressed() }
            orderDetails.layoutManager = LinearLayoutManager(activity)
            orderItemsBtn.setOnClickListener {
                (requireActivity() as? BottomNavigationActivity)?.pushFragment(
                    orderDetailsResponse?.let { AddOrderToCartFragment.getInstance(it) }
                )
            }
            tvSelectAll.setOnClickListener {
                (requireActivity() as? BottomNavigationActivity)?.pushFragment(
                    HelpAndSupportFragment.newInstance(orderDetailsResponse, orderItemList)
                )
            }
            argOrderId?.let { orderId -> requestOrderDetails(orderId) }
        }
    }

    private fun requestOrderDetails(orderId: String): Call<OrderDetailsResponse> {
        val orderDetailRequest = OneAppService().getOrderDetails(orderId)
        orderDetailRequest.enqueue(CompletionHandler(object :
            IResponseListener<OrderDetailsResponse> {
            override fun onSuccess(ordersResponse: OrderDetailsResponse?) {
                if (!isAdded) return
                when (ordersResponse?.httpCode) {
                    0 -> {
                        binding.mainLayout?.visibility = View.VISIBLE
                        binding.loadingBar?.visibility = View.GONE
                        orderDetailsResponse = ordersResponse
                        bindData(orderDetailsResponse!!)
                    }
                    502 -> {
                        binding.loadingBar.visibility = View.GONE
                        showErrorDialog(
                            ordersResponse.response?.desc
                                ?: getString(R.string.general_error_desc)
                        )
                    }
                }

            }

            override fun onFailure(error: Throwable?) {
            }

        }, OrderDetailsResponse::class.java))

        return orderDetailRequest
    }

    private fun bindData(ordersResponse: OrderDetailsResponse) {
        dataList = buildDataForOrderDetailsView(ordersResponse)
        binding.orderDetails.adapter = requireActivity().let { OrderDetailsAdapter(it, this, dataList) }
        VoiceOfCustomerManager().showVocSurveyIfNeeded(
            activity,
            KotlinUtils.vocShoppingHandling(orderDetailsResponse?.orderSummary?.fulfillmentDetails?.deliveryType)
        )
    }

    private fun buildDataForOrderDetailsView(ordersResponse: OrderDetailsResponse): ArrayList<OrderDetailsItem> {
        val dataList = arrayListOf<OrderDetailsItem>()

        dataList.add(OrderDetailsItem(ordersResponse, OrderDetailsItem.ViewType.ORDER_STATUS))
        // Endless Aisle Barcode
        ordersResponse.orderSummary?.let {
            if (it.endlessAisleOrder && it.state?.contains(
                    requireContext().getString(R.string.cancelled)
                ) == false
            ) {
                dataList.add(
                    OrderDetailsItem(
                        ordersResponse.orderSummary,
                        OrderDetailsItem.ViewType.ENDLESS_AISLE_BARCODE
                    )
                )
            }
        }
        ordersResponse.orderSummary?.apply {
            if (!taxNoteNumbers.isNullOrEmpty())
                dataList.add(OrderDetailsItem(null, OrderDetailsItem.ViewType.VIEW_TAX_INVOICE))
            if (orderCancellable && !requestCancellation)
                dataList.add(OrderDetailsItem(null, OrderDetailsItem.ViewType.CANCEL_ORDER))
            if (isChatEnabled)
                dataList.add(
                    OrderDetailsItem(
                        ordersResponse,
                        OrderDetailsItem.ViewType.CHAT_VIEW
                    )
                )
            if (isDriverTrackingEnabled && (!driverTrackingURL.isNullOrEmpty()))
                dataList.add(
                    OrderDetailsItem(
                        ordersResponse,
                        OrderDetailsItem.ViewType.TRACK_ORDER
                    )
                )
        }
        dataList.add(OrderDetailsItem(null, OrderDetailsItem.ViewType.ADD_TO_LIST_LAYOUT))
        dataList.add(OrderDetailsItem(ordersResponse, OrderDetailsItem.ViewType.ORDER_TOTAL))

        val itemsObject = JSONObject(Gson().toJson(ordersResponse.items))
        val keys = itemsObject.keys()
        while ((keys.hasNext())) {
            val key = keys.next()
            val productsArray = itemsObject.getJSONArray(key)
            val orderItemLength = productsArray.length()
            val orderDetailsItem = when {
                key.contains(ProductTypeDetails.DEFAULT.value) -> OrderDetailsItem(
                    ProductTypeDetails.DEFAULT.longHeader,
                    OrderDetailsItem.ViewType.HEADER,
                    orderItemLength
                )
                key.contains(ProductTypeDetails.HOME_COMMERCE_ITEM.value) -> OrderDetailsItem(
                    ProductTypeDetails.HOME_COMMERCE_ITEM.longHeader,
                    OrderDetailsItem.ViewType.HEADER,
                    orderItemLength
                )
                key.contains(ProductTypeDetails.FOOD_COMMERCE_ITEM.value) -> OrderDetailsItem(
                    ProductTypeDetails.FOOD_COMMERCE_ITEM.longHeader,
                    OrderDetailsItem.ViewType.HEADER,
                    orderItemLength
                )
                key.contains(ProductTypeDetails.CLOTHING_COMMERCE_ITEM.value) -> OrderDetailsItem(
                    ProductTypeDetails.CLOTHING_COMMERCE_ITEM.longHeader,
                    OrderDetailsItem.ViewType.HEADER,
                    orderItemLength
                )
                key.contains(ProductTypeDetails.PREMIUM_BRAND_COMMERCE_ITEM.value) -> OrderDetailsItem(
                    ProductTypeDetails.PREMIUM_BRAND_COMMERCE_ITEM.longHeader,
                    OrderDetailsItem.ViewType.HEADER,
                    orderItemLength
                )
                else -> OrderDetailsItem(
                    ProductTypeDetails.OTHER_ITEMS.longHeader,
                    OrderDetailsItem.ViewType.HEADER,
                    orderItemLength
                )
            }

            dataList.add(orderDetailsItem)

            if (orderItemLength > 0) {
                for (i in 0 until orderItemLength) {
                    try {
                        val commerceItem =
                            Gson().fromJson(
                                productsArray.getJSONObject(i).toString(),
                                CommerceItem::class.java
                            )
                        val fulfillmentStoreId = Utils.retrieveStoreId(commerceItem.fulfillmentType)
                        commerceItem.fulfillmentStoreId =
                            fulfillmentStoreId!!.replace("\"".toRegex(), "")
                        if (commerceItem.isGWP)
                            dataList.add(
                                OrderDetailsItem(
                                    commerceItem,
                                    OrderDetailsItem.ViewType.GIFT,
                                    orderItemLength
                                )
                            )
                        else
                            dataList.add(
                                OrderDetailsItem(
                                    commerceItem,
                                    OrderDetailsItem.ViewType.COMMERCE_ITEM,
                                    orderItemLength
                                )
                            )
                        orderItemList.add(commerceItem)
                    } catch (e: Exception) {
                        when (e) {
                            is IllegalStateException,
                            is JsonSyntaxException,
                            -> dataList.add(
                                OrderDetailsItem(
                                    CommerceItem(),
                                    OrderDetailsItem.ViewType.COMMERCE_ITEM,
                                    orderItemLength
                                )
                            )
                        }
                    }
                }
            }
        }
        return dataList
    }

    override fun onAddToList(commerceItemList: MutableList<AddToListRequest>) {
        val addToWishListEventData = AddToWishListFirebaseEventData(products = dataList.toAnalyticItemList())
        val listItems = ArrayList<AddToListRequest>(0)
        listItems.addAll(commerceItemList)
        KotlinUtils.openAddToListPopup(
            requireActivity(),
            requireActivity().supportFragmentManager,
            listItems,
            argOrderId,
            addToWishListEventData
        )
    }

    override fun onOpenProductDetail(commerceItem: CommerceItem) {

        // Move to shop tab.
        if (requireActivity() !is BottomNavigationActivity) {
            return
        }
        val bottomNavigationActivity = requireActivity() as BottomNavigationActivity
        bottomNavigationActivity.bottomNavigationById.currentItem =
            BottomNavigationActivity.INDEX_PRODUCT
        val productDetails = ProductDetails()

        val commerceItemInfo = commerceItem.commerceItemInfo
        productDetails.externalImageRefV2 = commerceItemInfo.externalImageRefV2
        productDetails.productName = commerceItemInfo.productDisplayName
        productDetails.fromPrice = commerceItem.priceInfo.getAmount().toFloat()
        productDetails.productId = commerceItemInfo.productId
        productDetails.sku = commerceItemInfo.catalogRefId
        openProductDetailFragment("", productDetails)
    }

    fun openProductDetailFragment(productName: String?, productDetails: ProductDetails?) {
        if (requireActivity() !is BottomNavigationActivity || !isAdded) {
            return
        }
        val strProductList = Gson().toJson(productDetails)
        // Move to shop tab first.
        (requireActivity() as? BottomNavigationActivity)?.apply {
            onShopTabSelected(bottomNavigationById.menu[INDEX_PRODUCT])
        }
        ScreenManager.openProductDetailFragment(requireActivity(), productName, strProductList)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigator) {
            mBottomNavigator = context
        }
    }

    override fun onViewTaxInvoice() {
        (requireActivity() as? BottomNavigationActivity)?.pushFragment(
            orderDetailsResponse?.orderSummary?.let {
                it.orderId?.let { it1 ->
                    TaxInvoiceLIstFragment.getInstance(
                        it1, it.taxNoteNumbers ?: ArrayList(0)
                    )
                }
            }
        )
    }

    fun triggerFirebaseEvent(properties: String) {
        val arguments = HashMap<String, String>()
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = properties
        Utils.triggerFireBaseEvents(
            if (isNavigatedFromMyAccounts) FirebaseManagerAnalyticsProperties.Acc_My_Orders_Cancel_Order else FirebaseManagerAnalyticsProperties.SHOP_MY_ORDERS_CANCEL_ORDER,
            arguments,
            requireActivity()
        )
    }

    override fun onCancelOrder() {

        triggerFirebaseEvent(FirebaseManagerAnalyticsProperties.PropertyNames.CANCEL_ORDER_TAP)

        requireActivity().apply {
            this@OrderDetailsFragment.childFragmentManager.apply {
                CancelOrderConfirmationDialogFragment.newInstance(
                    isNavigatedFromMyAccounts
                )
                    .show(this, CancelOrderConfirmationDialogFragment::class.java.simpleName)
            }
        }
    }

    override fun onOpenChatScreen(orderId: String?) {
        orderId?.let {
            startActivity(OCChatActivity.newIntent(requireActivity(), it))
        }
    }

    override fun onOpenTrackOrderScreen(orderTrackingURL: String) {
        activity?.apply {startActivity(OrderTrackingWebViewActivity.newIntent(this, orderTrackingURL))
            overridePendingTransition(
                R.anim.slide_from_right,
                R.anim.slide_out_to_left
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CancelOrderProgressFragment.REQUEST_CODE_CANCEL_ORDER
            && resultCode == CancelOrderProgressFragment.RESULT_CODE_CANCEL_ORDER_SUCCESS
        ) {
            requireActivity().onBackPressed()
            // move back to shop fragment and reload my order tab
            (requireActivity() as? BottomNavigationActivity)?.apply {
                (currentFragment as? ShopFragment)?.let {
                    onActivityResult(requestCode, resultCode, data)
                }
            }
            return
        }

        val fragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCancelOrderConfirmation() {
        requireActivity().apply {
            val intent = Intent(this, CancelOrderProgressActivity::class.java)
            intent.putExtra(CancelOrderProgressFragment.ORDER_ID, argOrderId)
            intent.putExtra(AppConstant.NAVIGATED_FROM_MY_ACCOUNTS, isNavigatedFromMyAccounts)
            intent.putExtra(AppConstant.ORDER_ITEM_LIST, orderItemList)
            intent.putExtra(AppConstant.ORDER_ITEM_TOTAL, orderDetailsResponse?.orderSummary?.total)
            intent.putExtra(AppConstant.ORDER_SHIPPING_TOTAL, orderDetailsResponse?.orderSummary?.estimatedDelivery)
            startActivityForResult(intent, CancelOrderProgressFragment.REQUEST_CODE_CANCEL_ORDER)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    fun showErrorDialog(errorMessage: String) {
        val dialog = OrderHistoryErrorDialogFragment.newInstance(errorMessage)
        requireActivity().apply {
            this@OrderDetailsFragment.childFragmentManager.beginTransaction()
                .let { fragmentTransaction ->
                    dialog.show(
                        fragmentTransaction,
                        OrderHistoryErrorDialogFragment::class.java.simpleName
                    )
                }
        }
    }

    override fun onErrorDialogDismiss() {
        requireActivity().onBackPressed()
    }

    override fun onToastButtonClicked(jsonElement: JsonElement?) {
        jsonElement?.let {
            NavigateToShoppingList.navigateToShoppingListOnToastClicked(
                requireActivity(),
                it
            )
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) (requireActivity() as? BottomNavigationActivity)?.showBottomNavigationMenu()
    }
}
