package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import kotlinx.android.synthetic.main.order_details_fragment.*
import org.json.JSONObject
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.contracts.IToastInterface
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Parameter
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity
import za.co.woolworths.financial.services.android.ui.activities.CancelOrderProgressActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_PRODUCT
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.adapters.OrderDetailsAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.shop.helpandsupport.HelpAndSupportFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import za.co.woolworths.financial.services.android.ui.views.ToastFactory
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.ProductTypeDetails
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils

class OrderDetailsFragment : Fragment(), OrderDetailsAdapter.OnItemClick,
    CancelOrderConfirmationDialogFragment.ICancelOrderConfirmation,
    OrderHistoryErrorDialogFragment.IOrderHistoryErrorDialogDismiss, IToastInterface {

    companion object {
        val ARG_PARAM = "order"

        fun getInstance(order: Order, isNaviagtedFromMyAccount: Boolean = false) =
            OrderDetailsFragment().withArgs {
                putString(ARG_PARAM, Utils.toJson(order))
                putBoolean(AppConstant.NAVIGATED_FROM_MY_ACCOUNTS, isNaviagtedFromMyAccount)
            }

        fun getInstance(params: Parameter) = OrderDetailsFragment().withArgs {
            putParcelable(AppConstant.Keys.ARG_NOTIFICATION_PARAMETERS, params)
        }
    }

    private var dataList = arrayListOf<OrderDetailsItem>()
    private var order: Order? = null
    private var notificationParams: Parameter? = null
    private var orderDetailsResponse: OrderDetailsResponse? = null
    var isNavigatedFromMyAccounts: Boolean = false
    private var mBottomNavigator: BottomNavigator? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.order_details_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            order = Utils.jsonStringToObject(it.getString("order"), Order::class.java) as Order?
            notificationParams = it.getParcelable(AppConstant.Keys.ARG_NOTIFICATION_PARAMETERS)
            isNavigatedFromMyAccounts = it.getBoolean(AppConstant.NAVIGATED_FROM_MY_ACCOUNTS, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler().postDelayed({
            activity?.runOnUiThread {
                initViews()
            }
        }, 100)
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

        mBottomNavigator?.apply {
            removeToolbar()
        }
        tvSelectAll?.visibility = View.VISIBLE
        tvSelectAll?.text = getString(R.string.dash_help)
        btnBack?.setOnClickListener { activity?.onBackPressed() }
        orderDetails.layoutManager = LinearLayoutManager(activity)
        orderItemsBtn.setOnClickListener {
            (activity as? BottomNavigationActivity)?.pushFragment(
                AddOrderToCartFragment.getInstance(orderDetailsResponse!!, order)
            )
        }
        tvSelectAll.setOnClickListener {
            (activity as? BottomNavigationActivity)?.pushFragment(
                HelpAndSupportFragment.newInstance()
            )
        }
        order?.orderId?.let { orderId -> requestOrderDetails(orderId) }
        // This will call only when Push Notification is received.
        notificationParams?.orderID?.let { orderId -> requestOrderDetails(orderId) }
    }

    private fun requestOrderDetails(orderId: String): Call<OrderDetailsResponse> {
        val orderDetailRequest = OneAppService.getOrderDetails(orderId)
        orderDetailRequest.enqueue(CompletionHandler(object :
            IResponseListener<OrderDetailsResponse> {
            override fun onSuccess(ordersResponse: OrderDetailsResponse?) {
                if (!isAdded) return
                when (ordersResponse?.httpCode) {
                    0 -> {
                        mainLayout?.visibility = View.VISIBLE
                        loadingBar?.visibility = View.GONE
                        orderDetailsResponse = ordersResponse
                        bindData(orderDetailsResponse!!)
                    }
                    502 -> {
                        loadingBar.visibility = View.GONE
                        showErrorDialog(
                            ordersResponse?.response?.desc
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
        orderDetails.adapter = activity?.let { OrderDetailsAdapter(it, this, dataList) }
    }

    private fun buildDataForOrderDetailsView(ordersResponse: OrderDetailsResponse): ArrayList<OrderDetailsItem> {
        val dataList = arrayListOf<OrderDetailsItem>()

        dataList.add(OrderDetailsItem(ordersResponse, OrderDetailsItem.ViewType.ORDER_STATUS))

        order?.apply {
            if (taxNoteNumbers.isNotEmpty())
                dataList.add(OrderDetailsItem(null, OrderDetailsItem.ViewType.VIEW_TAX_INVOICE))
            if (orderCancellable && !requestCancellation)
                dataList.add(OrderDetailsItem(null, OrderDetailsItem.ViewType.CANCEL_ORDER))
            if (ordersResponse.orderSummary?.isChatEnabled == true)
                dataList.add(OrderDetailsItem(ordersResponse,
                    OrderDetailsItem.ViewType.CHAT_VIEW))
            if (ordersResponse.orderSummary?.isDriverTrackingEnabled == true)
                dataList.add(OrderDetailsItem(null,
                    OrderDetailsItem.ViewType.TRACK_ORDER))
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
        NavigateToShoppingList.openShoppingList(activity, commerceItemList, order?.orderId, false)
    }

    override fun onOpenProductDetail(commerceItem: CommerceItem) {

        // Move to shop tab.
        if (!(getActivity() is BottomNavigationActivity)) {
            return
        }
        val bottomNavigationActivity = activity as BottomNavigationActivity
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
        if (activity !is BottomNavigationActivity || !isAdded) {
            return
        }
        val gson = Gson()
        val strProductList = gson.toJson(productDetails)
        // Move to shop tab first.
        (activity as BottomNavigationActivity).apply {
            onShopTabSelected(bottomNavigationById.menu[INDEX_PRODUCT])
        }
        ScreenManager.openProductDetailFragment(activity, productName, strProductList)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigator) {
            mBottomNavigator = context
        }
    }

    override fun onViewTaxInvoice() {
        (activity as? BottomNavigationActivity)?.pushFragment(
            TaxInvoiceLIstFragment.getInstance(
                order?.orderId!!, order?.taxNoteNumbers!!
            )
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

        activity?.apply {
            this@OrderDetailsFragment.childFragmentManager.apply {
                CancelOrderConfirmationDialogFragment.newInstance(isNavigatedFromMyAccounts)
                    .show(this, CancelOrderConfirmationDialogFragment::class.java.simpleName)
            }
        }
    }

    override fun onOpenChatScreen() {
        //TODO: open chat screen
    }

    override fun onOpenTrackOrderScreen() {
        //TODO: open track order screen
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CancelOrderProgressFragment.REQUEST_CODE_CANCEL_ORDER
            && resultCode == CancelOrderProgressFragment.RESULT_CODE_CANCEL_ORDER_SUCCESS
        ) {
            activity?.onBackPressed()
            // move back to shop fragment and reload my order tab
            (activity as? BottomNavigationActivity)?.apply {
                (currentFragment as? ShopFragment)?.let {
                    onActivityResult(requestCode, resultCode, data)
                }
            }
            return
        }

        if (requestCode == AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_REQUEST_CODE
            && resultCode == AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE
        ) {
            ToastFactory.buildShoppingListToast(
                requireActivity(),
                orderDetails, true, data, this
            )
            return
        }

        if (requestCode == BottomNavigationActivity.PDP_REQUEST_CODE && resultCode == AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE) {
            activity?.setResult(
                AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE,
                data
            )
            activity?.onBackPressed()
            activity?.overridePendingTransition(0, 0)
            return
        }
        val fragment = activity?.supportFragmentManager?.findFragmentById(R.id.fragmentContainer)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCancelOrderConfirmation() {
        activity?.apply {
            val intent = Intent(this, CancelOrderProgressActivity::class.java)
            intent.putExtra(CancelOrderProgressFragment.ORDER_ID, order?.orderId)
            intent.putExtra(AppConstant.NAVIGATED_FROM_MY_ACCOUNTS, isNavigatedFromMyAccounts)
            startActivityForResult(intent, CancelOrderProgressFragment.REQUEST_CODE_CANCEL_ORDER)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    fun showErrorDialog(errorMessage: String) {
        val dialog = OrderHistoryErrorDialogFragment.newInstance(errorMessage)
        activity?.apply {
            this@OrderDetailsFragment.childFragmentManager?.beginTransaction()
                ?.let { fragmentTransaction ->
                    dialog.show(
                        fragmentTransaction,
                        OrderHistoryErrorDialogFragment::class.java.simpleName
                    )
                }
        }
    }

    override fun onErrorDialogDismiss() {
        activity?.onBackPressed()
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
        if (!hidden) (activity as? BottomNavigationActivity)?.showBottomNavigationMenu()
    }
}
