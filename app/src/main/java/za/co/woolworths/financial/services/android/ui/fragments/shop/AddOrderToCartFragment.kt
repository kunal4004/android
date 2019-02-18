package za.co.woolworths.financial.services.android.ui.fragments.shop


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_add_order_to_cart.*
import org.json.JSONObject
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsItem
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.OrderHistoryCommerceItem
import za.co.woolworths.financial.services.android.ui.adapters.AddOrderToCartAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.MultiMap
import za.co.woolworths.financial.services.android.util.Utils


class AddOrderToCartFragment : Fragment(), AddOrderToCartAdapter.OnItemClick {

    private var orderDetailsResponse: OrderDetailsResponse? = null
    private var dataList = arrayListOf<OrderDetailsItem>()
    val QUANTITY_CHANGED = 2019
    private var addOrderToCartAdapter: AddOrderToCartAdapter? = null
    var updateQuantityPosition: Int = 0
    private var isAnyItemSelected: Boolean = false
    private var tvSelectAll: TextView? = null
    private var mFulFillmentStoreId: String? = null
    private var mMapStoreFulFillmentKeyValue: MutableMap<String, String>? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_order_to_cart, container, false)
    }

    companion object {
        private val ARG_PARAM = "orderDetailsResponse"

        fun getInstance(orderDetailsResponse: OrderDetailsResponse) = AddOrderToCartFragment().withArgs {
            putSerializable(ARG_PARAM, orderDetailsResponse)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            orderDetailsResponse = arguments.getSerializable(ARG_PARAM) as OrderDetailsResponse
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        tvSelectAll = activity.findViewById(R.id.tvSelectAll)
        rvItemsToCart.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        addToCartButton.setOnClickListener { }
        bindData()
    }

    fun bindData() {
        dataList = buildDataForOrderDetailsView(orderDetailsResponse!!)
        addOrderToCartAdapter = AddOrderToCartAdapter(activity, this, dataList)
        rvItemsToCart.adapter = addOrderToCartAdapter
    }

    override fun onItemSelectionChanged(dataList: ArrayList<OrderDetailsItem>) {
        isAnyItemSelected = getButtonStatus(dataList)
        val activity = activity ?: return
        addToCartButton.isEnabled = isAnyItemSelected

        if (isAdded) {
            if (dataList.size > 0)
                tvSelectAll?.setText(getString(if (getSelectAllMenuVisibility(dataList)) R.string.deselect_all else R.string.select_all))
            else
                tvSelectAll?.visibility = View.GONE
        }
    }

    override fun onQuantityUpdate(position: Int) {
        updateQuantityPosition = position
    }

    private fun buildDataForOrderDetailsView(ordersResponse: OrderDetailsResponse): ArrayList<OrderDetailsItem> {
        val dataList = arrayListOf<OrderDetailsItem>()
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
                    var commerceItem = OrderHistoryCommerceItem()
                    commerceItem = Gson().fromJson(productsArray.getJSONObject(i).toString(), OrderHistoryCommerceItem::class.java)
                    val fulfillmentStoreId = Utils.retrieveStoreId(commerceItem.fulfillmentType)
                    commerceItem.fulfillmentStoreId = fulfillmentStoreId!!.replace("\"".toRegex(), "")
                    dataList.add(OrderDetailsItem(commerceItem, OrderDetailsItem.ViewType.COMMERCE_ITEM))
                }
            }
        }
        return dataList
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == QUANTITY_CHANGED) {
            if (resultCode == QUANTITY_CHANGED) {
                val bundleUpdatedQuantity = data?.getExtras()
                val updatedQuantity = bundleUpdatedQuantity!!.getInt("QUANTITY_CHANGED_FROM_LIST")
                if (updatedQuantity > 0) {
                    if (addOrderToCartAdapter == null) return
                    val listItems = addOrderToCartAdapter?.getListItems()
                    var item = listItems?.get(updateQuantityPosition)?.item as OrderHistoryCommerceItem
                    item.userQuantity = updatedQuantity
                    item.isSelected = true
                    addOrderToCartAdapter?.updateList(listItems)

                }
            }
        }
    }

    private fun shoppingListInventory(): Boolean {

        val multiListItem: MultiMap<String, OrderHistoryCommerceItem> = MultiMap.create()
        for (listItem in dataList) {
            if (listItem.type == OrderDetailsItem.ViewType.COMMERCE_ITEM) {
                var item = listItem.item as OrderHistoryCommerceItem
                if (!item.inventoryCallCompleted && !TextUtils.isEmpty(item.commerceItemInfo.catalogRefId))
                    multiListItem.put(item.fulfillmentType, item)
            }
        }

        val collectOtherSkuId = HashMap<String, String>()
        val collections = multiListItem.getEntries()
        for (collectionEntry in collections.entries) {
            val collectionEntryValue = collectionEntry.value
            val fulFillmentTypeIdCollection = collectionEntry.key
            val skuIds = ArrayList<String>()
            for (item in collectionEntryValue) {
                skuIds.add(item.commerceItemInfo.catalogRefId)
            }
            val multiSKUS = TextUtils.join("-", skuIds)
            collectOtherSkuId[fulFillmentTypeIdCollection] = multiSKUS
            mFulFillmentStoreId = Utils.retrieveStoreId(fulFillmentTypeIdCollection)
            if (!TextUtils.isEmpty(mFulFillmentStoreId)) {
                mFulFillmentStoreId = mFulFillmentStoreId?.replace("\"".toRegex(), "")
                mMapStoreFulFillmentKeyValue?.put(fulFillmentTypeIdCollection, mFulFillmentStoreId!!)
                //executeGetInventoryForStore(mFulFillmentStoreId, multiSKUS)
            } else {
                for (inventoryItems in dataList) {
                    if (inventoryItems.type == OrderDetailsItem.ViewType.COMMERCE_ITEM) {
                        (inventoryItems.item as OrderHistoryCommerceItem).inventoryCallCompleted = true
                    }
                }
                addOrderToCartAdapter?.adapterClickable(true)
            }
        }
        return false
    }

    private fun getButtonStatus(items: List<OrderDetailsItem>): Boolean {
        for (orderDetailsItem in items) {
            if (orderDetailsItem.type == OrderDetailsItem.ViewType.COMMERCE_ITEM)
                if ((orderDetailsItem.item as OrderHistoryCommerceItem).isSelected)
                    return true
        }
        return false
    }

    private fun getSelectAllMenuVisibility(items: ArrayList<OrderDetailsItem>): Boolean {
        for (orderDetailsItem in items) {
            if (orderDetailsItem.type == OrderDetailsItem.ViewType.COMMERCE_ITEM)
                if (!(orderDetailsItem as OrderHistoryCommerceItem).isSelected)
                    return false
        }
        return true
    }

}
