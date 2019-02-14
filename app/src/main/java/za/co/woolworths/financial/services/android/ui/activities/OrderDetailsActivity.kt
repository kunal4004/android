package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import org.json.JSONObject
import za.co.woolworths.financial.services.android.models.rest.product.GetOrderDetailsRequest
import za.co.woolworths.financial.services.android.util.OnEventListener
import za.co.woolworths.financial.services.android.util.Utils
import kotlinx.android.synthetic.main.order_details_activity.*
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.ui.adapters.OrderDetailsAdapter
import za.co.woolworths.financial.services.android.util.ScreenManager

class OrderDetailsActivity : AppCompatActivity(), OrderDetailsAdapter.OnItemClick {

    private var dataList = arrayListOf<OrderDetailsItem>()
    private var order: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_details_activity)
        Utils.updateStatusBarBackground(this)
        configureUI()
    }

    private fun configureUI() {
        order = intent.getSerializableExtra("order") as Order?
        orderDetails.layoutManager = LinearLayoutManager(this)
        requestOrderDetails(order?.orderId!!).execute()
    }

    private fun requestOrderDetails(orderId: String): GetOrderDetailsRequest {
        return GetOrderDetailsRequest(this, orderId, object : OnEventListener<OrderDetailsResponse> {
            override fun onSuccess(ordersResponse: OrderDetailsResponse) {
                bindData(ordersResponse)
            }

            override fun onFailure(e: String?) {

            }

        })

    }

    private fun bindData(ordersResponse: OrderDetailsResponse) {
        dataList = buildDataForOrderDetailsView(ordersResponse)
        orderDetails.adapter = OrderDetailsAdapter(this, dataList)
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

    override fun onAddToList() {
        Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.ORDER_ADD_TO_LIST, order?.orderId)
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
        ScreenManager.presentProductDetails(this, bundle)
    }
}