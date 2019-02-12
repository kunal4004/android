package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import org.json.JSONObject
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsItem
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsResponse
import za.co.woolworths.financial.services.android.models.rest.product.GetOrderDetailsRequest
import za.co.woolworths.financial.services.android.util.OnEventListener
import za.co.woolworths.financial.services.android.util.Utils
import kotlinx.android.synthetic.main.order_details_activity.*
import za.co.woolworths.financial.services.android.ui.adapters.OrderDetailsAdapter

class OrderDetailsActivity : AppCompatActivity() {

    private var dataList = arrayListOf<OrderDetailsItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_details_activity)
        Utils.updateStatusBarBackground(this)
        configureUI()
    }

    private fun configureUI() {
        orderDetails.layoutManager = LinearLayoutManager(this)
        requestOrderDetails("o264740019").execute()
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
        dataList.add(OrderDetailsItem(null, OrderDetailsItem.ViewType.ORDER_STATUS))
        dataList.add(OrderDetailsItem(null, OrderDetailsItem.ViewType.ADD_TO_LIST_LAYOUT))
        val itemsObject = JSONObject(Gson().toJson(ordersResponse.items))
        val keys = itemsObject.keys()
        while ((keys.hasNext())) {
            val key = keys.next()
            if (key.contains("default"))
                dataList.add(OrderDetailsItem("GENERAL", OrderDetailsItem.ViewType.HEADER))
            else if (key.contains("homeCommerceItem"))
                dataList.add(OrderDetailsItem("HOME", OrderDetailsItem.ViewType.HEADER))
            else if (key.contains("foodCommerceItem"))
                dataList.add(OrderDetailsItem("FOOD", OrderDetailsItem.ViewType.HEADER))
            else if (key.contains("clothingCommerceItem"))
                dataList.add(OrderDetailsItem("CLOTHING", OrderDetailsItem.ViewType.HEADER))
            else if (key.contains("premiumBrandCommerceItem"))
                dataList.add(OrderDetailsItem("PREMIUM BRAND", OrderDetailsItem.ViewType.HEADER))
            else
                dataList.add(OrderDetailsItem("OTHER", OrderDetailsItem.ViewType.HEADER))

            val productsArray = itemsObject.getJSONArray(key)
            if (productsArray.length() > 0) {
                for (i in 0 until productsArray.length()) {
                    val commerceItem = Gson().fromJson(productsArray.getJSONObject(i).toString(), CommerceItem::class.java)
                    val fulfillmentStoreId = Utils.retrieveStoreId(commerceItem.fulfillmentType)
                    commerceItem.fulfillmentStoreId = fulfillmentStoreId!!.replace("\"".toRegex(), "")
                    dataList.add(OrderDetailsItem(commerceItem, OrderDetailsItem.ViewType.COMMERCE_ITEM))
                }
            }
        }
        return dataList
    }
}