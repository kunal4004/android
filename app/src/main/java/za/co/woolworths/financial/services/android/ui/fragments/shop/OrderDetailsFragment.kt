package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.json.JSONObject
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.ui.adapters.OrderDetailsAdapter
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import kotlinx.android.synthetic.main.order_details_fragment.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.CancelOrderProgressActivity
import za.co.woolworths.financial.services.android.ui.activities.OrderDetailsActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.FragmentsEventsListner
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import java.lang.IllegalStateException

class OrderDetailsFragment : Fragment(), OrderDetailsAdapter.OnItemClick, CancelOrderConfirmationDialogFragment.ICancelOrderConfirmation, OrderHistoryErrorDialogFragment.IOrderHistoryErrorDialogDismiss {

    companion object {
        private val ARG_PARAM = "order"
        fun getInstance(order: Order) = OrderDetailsFragment().withArgs {
            putString(ARG_PARAM, Utils.toJson(order))
        }
    }

    private var dataList = arrayListOf<OrderDetailsItem>()
    private var order: Order? = null
    private var orderDetailsResponse: OrderDetailsResponse? = null
    private lateinit var listener: FragmentsEventsListner
    private var tvSelectAll: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.order_details_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            order = Utils.jsonStringToObject(it.getString("order"),Order::class.java) as Order?
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
        tvSelectAll = activity?.findViewById(R.id.tvSelectAll)
        tvSelectAll?.visibility = View.GONE
        orderDetails.layoutManager = LinearLayoutManager(activity)
        orderItemsBtn.setOnClickListener {
            listener.onOrderItemsClicked(orderDetailsResponse!!)
        }
        order?.orderId?.let { orderId -> requestOrderDetails(orderId) }
    }

    private fun requestOrderDetails(orderId: String): Call<OrderDetailsResponse> {
        val orderDetailRequest = OneAppService.getOrderDetails(orderId)
        orderDetailRequest.enqueue(CompletionHandler(object : IResponseListener<OrderDetailsResponse> {
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
                        showErrorDialog(ordersResponse?.response?.desc
                                ?: getString(R.string.general_error_desc))
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
        }
        dataList.add(OrderDetailsItem(null, OrderDetailsItem.ViewType.ADD_TO_LIST_LAYOUT))
        val itemsObject = JSONObject(Gson().toJson(ordersResponse.items))
        val keys = itemsObject.keys()
        while ((keys.hasNext())) {
            val key = keys?.next()?.apply {
                when {
                    contains("default") -> dataList.add(OrderDetailsItem("YOUR GENERAL ITEMS", OrderDetailsItem.ViewType.HEADER))
                    contains("homeCommerceItem") -> dataList.add(OrderDetailsItem("YOUR HOME ITEMS", OrderDetailsItem.ViewType.HEADER))
                    contains("foodCommerceItem") -> dataList.add(OrderDetailsItem("YOUR FOOD ITEMS", OrderDetailsItem.ViewType.HEADER))
                    contains("clothingCommerceItem") -> dataList.add(OrderDetailsItem("YOUR CLOTHING ITEMS", OrderDetailsItem.ViewType.HEADER))
                    contains("premiumBrandCommerceItem") -> dataList.add(OrderDetailsItem("YOUR PREMIUM BRAND ITEMS", OrderDetailsItem.ViewType.HEADER))
                    else -> dataList.add(OrderDetailsItem("YOUR OTHER ITEMS", OrderDetailsItem.ViewType.HEADER))
                }
            }

            val productsArray = itemsObject.getJSONArray(key)
            if (productsArray.length() > 0) {
                for (i in 0 until productsArray.length()) {
                    try {
                        val commerceItem = Gson().fromJson(productsArray.getJSONObject(i).toString(), CommerceItem::class.java)
                        val fulfillmentStoreId = Utils.retrieveStoreId(commerceItem.fulfillmentType)
                        commerceItem.fulfillmentStoreId = fulfillmentStoreId!!.replace("\"".toRegex(), "")
                        dataList.add(OrderDetailsItem(commerceItem, OrderDetailsItem.ViewType.COMMERCE_ITEM))
                    } catch (e: Exception) {
                        when (e) {
                            is IllegalStateException,
                            is JsonSyntaxException -> dataList.add(OrderDetailsItem(CommerceItem(), OrderDetailsItem.ViewType.COMMERCE_ITEM))
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
        val productList = ProductDetails()
        val commerceItemInfo = commerceItem.commerceItemInfo
        productList.externalImageRef = commerceItemInfo.externalImageURL
        productList.productName = commerceItemInfo.productDisplayName
        productList.fromPrice = commerceItem.priceInfo.getAmount().toFloat()
        productList.productId = commerceItemInfo.productId
        productList.sku = commerceItemInfo.catalogRefId
        val gson = Gson()
        val strProductList = gson.toJson(productList)
        val bundle = Bundle()
        bundle.putString("strProductList", strProductList)
        bundle.putString("strProductCategory", "")
        ScreenManager.presentProductDetails(activity, bundle)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentsEventsListner) {
            listener = context
        } else {
            throw ClassCastException("$context must implement FragmentsEventsListner.")
        }
    }

    override fun onViewTaxInvoice() {
        listener.openTaxInvoices()
    }

    override fun onCancelOrder() {
        (activity as? OrderDetailsActivity)?.triggerFirebaseEvent(FirebaseManagerAnalyticsProperties.PropertyNames.CANCEL_ORDER_TAP)
        activity?.apply {
            this@OrderDetailsFragment.childFragmentManager.apply {
                CancelOrderConfirmationDialogFragment.newInstance().show(this, CancelOrderConfirmationDialogFragment::class.java.simpleName)
            }
        }

    }

    override fun onCancelOrderConfirmation() {
        activity?.apply {
            val isNavigatedFromMyAccounts  = (this as? OrderDetailsActivity)?.isNavigatedFromMyAccounts
            val intent = Intent(this, CancelOrderProgressActivity::class.java)
            intent.putExtra(CancelOrderProgressFragment.ORDER_ID, order?.orderId)
            intent.putExtra(OrderDetailsActivity.NAVIGATED_FROM_MY_ACCOUNTS,isNavigatedFromMyAccounts)
            startActivityForResult(intent, CancelOrderProgressFragment.REQUEST_CODE_CANCEL_ORDER)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    fun showErrorDialog(errorMessage: String) {
        val dialog = OrderHistoryErrorDialogFragment.newInstance(errorMessage)
        activity?.apply {
            this@OrderDetailsFragment.childFragmentManager?.beginTransaction()?.let { fragmentTransaction -> dialog.show(fragmentTransaction, OrderHistoryErrorDialogFragment::class.java.simpleName) }
        }
    }

    override fun onErrorDialogDismiss() {
        activity?.onBackPressed()
    }
}