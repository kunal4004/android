package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.google.gson.Gson
import org.json.JSONObject
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.rest.product.GetOrderDetailsRequest
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.adapters.OrderDetailsAdapter
import za.co.woolworths.financial.services.android.util.OnEventListener
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import kotlinx.android.synthetic.main.order_details_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnOrderItemsClicked

class OrderDetailsFragment : Fragment(), OrderDetailsAdapter.OnItemClick {

    private var dataList = arrayListOf<OrderDetailsItem>()
    private var order: Order? = null
    private var orderDetailsResponse: OrderDetailsResponse? = null
    private lateinit var listener: OnOrderItemsClicked

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.order_details_fragment, container, false)
    }

    companion object {
        private val ARG_PARAM = "order"

        fun getInstance(order: Order) = OrderDetailsFragment().withArgs {
            putSerializable(ARG_PARAM, order)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            order = arguments.getSerializable(ARG_PARAM) as Order
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        orderDetails.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        orderItemsBtn.setOnClickListener {
            listener.onOrderItemsClicked(orderDetailsResponse!!)
        }
        requestOrderDetails(order?.orderId!!).execute()
    }

    private fun requestOrderDetails(orderId: String): GetOrderDetailsRequest {
        return GetOrderDetailsRequest(activity, orderId, object : OnEventListener<OrderDetailsResponse> {
            override fun onSuccess(ordersResponse: OrderDetailsResponse) {
                orderDetailsResponse = ordersResponse
                bindData(orderDetailsResponse!!)
            }

            override fun onFailure(e: String?) {

            }

        })

    }

    private fun bindData(ordersResponse: OrderDetailsResponse) {
        dataList = buildDataForOrderDetailsView(ordersResponse)
        orderDetails.adapter = OrderDetailsAdapter(activity, this, dataList)
    }

    private fun buildDataForOrderDetailsView(ordersResponse: OrderDetailsResponse): ArrayList<OrderDetailsItem> {
        val dataList = arrayListOf<OrderDetailsItem>()
        dataList.add(OrderDetailsItem(order, OrderDetailsItem.ViewType.ORDER_STATUS))
        dataList.add(OrderDetailsItem(null, OrderDetailsItem.ViewType.ADD_TO_LIST_LAYOUT))
        val itemsObject = JSONObject(Gson().toJson(ordersResponse.items))
        val keys = itemsObject.keys()
        while ((keys.hasNext())) {
            val key = keys.next()
            if (key.contains("default"))
                dataList.add(OrderDetailsItem("YOUR GENERAL ITEMS", OrderDetailsItem.ViewType.HEADER))
            else if (key.contains("homeCommerceItem"))
                dataList.add(OrderDetailsItem("YOUR HOME ITEMS", OrderDetailsItem.ViewType.HEADER))
            else if (key.contains("foodCommerceItem"))
                dataList.add(OrderDetailsItem("YOUR FOOD ITEMS", OrderDetailsItem.ViewType.HEADER))
            else if (key.contains("clothingCommerceItem"))
                dataList.add(OrderDetailsItem("YOUR CLOTHING ITEMS", OrderDetailsItem.ViewType.HEADER))
            else if (key.contains("premiumBrandCommerceItem"))
                dataList.add(OrderDetailsItem("YOUR PREMIUM BRAND ITEMS", OrderDetailsItem.ViewType.HEADER))
            else
                dataList.add(OrderDetailsItem("YOUR OTHER ITEMS", OrderDetailsItem.ViewType.HEADER))

            val productsArray = itemsObject.getJSONArray(key)
            if (productsArray.length() > 0) {
                for (i in 0 until productsArray.length()) {
                    var commerceItem = CommerceItem()
                    commerceItem = Gson().fromJson(productsArray.getJSONObject(i).toString(), CommerceItem::class.java)
                    val fulfillmentStoreId = Utils.retrieveStoreId(commerceItem.fulfillmentType)
                    commerceItem.fulfillmentStoreId = fulfillmentStoreId!!.replace("\"".toRegex(), "")
                    dataList.add(OrderDetailsItem(commerceItem, OrderDetailsItem.ViewType.COMMERCE_ITEM))
                }
            }
        }
        return dataList
    }

    override fun onAddToList(commerceItemList: MutableList<AddToListRequest>) {
        activity?.apply {
            val intentAddToList = Intent(this, AddToShoppingListActivity::class.java)
            intentAddToList.putExtra("addToListRequest", Gson().toJson(commerceItemList))
            startActivity(intentAddToList)
            overridePendingTransition(0, 0)
        }
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


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnOrderItemsClicked) {
            listener = context
        } else {
            throw ClassCastException(context.toString() + " must implement OnOrderItemsClicked.")
        }
    }
}