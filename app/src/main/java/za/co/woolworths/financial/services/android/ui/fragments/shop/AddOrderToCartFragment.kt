package za.co.woolworths.financial.services.android.ui.fragments.shop


import android.app.Activity.RESULT_OK
import android.content.Context
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
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity
import za.co.woolworths.financial.services.android.ui.activities.DeliveryLocationSelectionActivity
import za.co.woolworths.financial.services.android.ui.adapters.AddOrderToCartAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.FragmentsEventsListner
import za.co.woolworths.financial.services.android.util.MultiMap
import za.co.woolworths.financial.services.android.util.PostItemToCart
import za.co.woolworths.financial.services.android.util.Utils


class AddOrderToCartFragment : Fragment(), AddOrderToCartAdapter.OnItemClick {

    private var orderDetailsResponse: OrderDetailsResponse? = null
    private var dataList = arrayListOf<OrderDetailsItem>()
    private var addOrderToCartAdapter: AddOrderToCartAdapter? = null
    var updateQuantityPosition: Int = 0
    private var isAnyItemSelected: Boolean = false
    private var tvSelectAll: TextView? = null
    private var mFulFillmentStoreId: String? = null
    private var mMapStoreFulFillmentKeyValue: MutableMap<String, String> = hashMapOf()
    private var mWoolWorthsApplication: WoolworthsApplication? = null
    private var isSelectAllReadyToShow = false
    private lateinit var listener: FragmentsEventsListner


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_order_to_cart, container, false)
    }

    companion object {
        private val ARG_PARAM = "orderDetailsResponse"
        const val QUANTITY_CHANGED = 2019
        const val REQUEST_SUBURB_CHANGE = 1550
        fun getInstance(orderDetailsResponse: OrderDetailsResponse) = AddOrderToCartFragment().withArgs {
            putString(ARG_PARAM, Utils.objectToJson(orderDetailsResponse))
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{
            orderDetailsResponse = Utils.jsonStringToObject(it.getString(ARG_PARAM), OrderDetailsResponse::class.java) as OrderDetailsResponse
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mWoolWorthsApplication = activity?.application as WoolworthsApplication
        initViews()
    }

    private fun initViews() {
        tvSelectAll = activity?.findViewById(R.id.tvSelectAll)
        rvItemsToCart.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        addToCartButton.isEnabled = isAnyItemSelected
        tvSelectAll?.setOnClickListener { onSelectAll() }
        addToCartButton.setOnClickListener { addItemsToCart() }
        setSelectAllTextVisibility(isSelectAllReadyToShow)
        bindData()
    }


    fun bindData() {
        dataList = buildDataForOrderDetailsView(orderDetailsResponse!!)
        addOrderToCartAdapter = activity?.let { AddOrderToCartAdapter(it, this, dataList) }
        rvItemsToCart.adapter = addOrderToCartAdapter
        makeInventoryCall()
    }

    fun makeInventoryCall() {
        val shoppingDeliveryLocation = Utils.getPreferredDeliveryLocation()
        if (shoppingDeliveryLocation == null) {
            addOrderToCartAdapter?.adapterClickable(true)
            return
        }
        shoppingListInventory()
    }

    override fun onItemSelectionChanged(dataList: ArrayList<OrderDetailsItem>) {

        if (isAdded) {
            isAnyItemSelected = getButtonStatus(dataList)
            addToCartButton.isEnabled = isAnyItemSelected
            if (dataList.size > 0)
                tvSelectAll?.setText(getString(if (getSelectAllMenuVisibility(dataList)) R.string.deselect else R.string.select_all))
            else
                tvSelectAll?.visibility = View.GONE
        }
    }

    override fun onQuantityUpdate(position: Int, item: OrderHistoryCommerceItem) {
        updateQuantityPosition = position
        navigateFromQuantity()
        activity?.apply {
            val editQuantityIntent = Intent(this, ConfirmColorSizeActivity::class.java)
            editQuantityIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, ConfirmColorSizeActivity.QUANTITY)
            editQuantityIntent.putExtra("ORDER_QUANTITY_IN_STOCK", item.quantityInStock)
            startActivityForResult(editQuantityIntent, QUANTITY_CHANGED)
            overridePendingTransition(0, 0)
        }
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
                val updatedQuantity = bundleUpdatedQuantity!!.getInt("QUANTITY_CHANGED")
                if (updatedQuantity > 0) {
                    if (addOrderToCartAdapter == null) return
                    val listItems = addOrderToCartAdapter?.getListItems()
                    var item = listItems?.get(updateQuantityPosition)?.item as OrderHistoryCommerceItem
                    item.userQuantity = updatedQuantity
                    item.isSelected = true
                    addOrderToCartAdapter?.updateList(listItems)

                }
            }
        } else if (requestCode == REQUEST_SUBURB_CHANGE && resultCode == RESULT_OK) {
            makeInventoryCall()
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
                executeGetInventoryForStore(mFulFillmentStoreId!!, multiSKUS)
            } else {
                for (sku in skuIds) {
                    for (orderDetailsItem in dataList) {
                        if (orderDetailsItem.type == OrderDetailsItem.ViewType.COMMERCE_ITEM) {
                            if ((orderDetailsItem.item as OrderHistoryCommerceItem).commerceItemInfo.catalogRefId.equals(sku))
                                orderDetailsItem.item.inventoryCallCompleted = true
                        }
                    }
                }
                addOrderToCartAdapter?.adapterClickable(true)
            }
        }
        return false
    }

    private fun executeGetInventoryForStore(storeId: String, multiSku: String) {
        setSelectAllTextVisibility(false)
        getInventoryStockForStore(storeId, multiSku)

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
                if (!(orderDetailsItem.item as OrderHistoryCommerceItem).isSelected)
                    return false
        }
        return true
    }

    private fun updateShoppingList() {
        manageSelectAllMenuVisibility()
        updateList()
    }

    private fun navigateFromQuantity() {
        if (mWoolWorthsApplication != null) {
            val wGlobalState = mWoolWorthsApplication?.getWGlobalState()
            if (wGlobalState != null) {
                wGlobalState!!.navigateFromQuantity(QUANTITY_CHANGED)
            }
        }
    }

    private fun manageSelectAllMenuVisibility() {
        isSelectAllReadyToShow = false
        for (dataListItem in dataList) {
            if (dataListItem.type == OrderDetailsItem.ViewType.COMMERCE_ITEM) {
                var item = dataListItem.item as OrderHistoryCommerceItem
                if (item.quantityInStock > 0) {
                    isSelectAllReadyToShow = true
                    break
                }
            }
        }
        setSelectAllTextVisibility(isSelectAllReadyToShow)
    }

    private fun updateList() {
        addOrderToCartAdapter?.adapterClickable(true)
        addOrderToCartAdapter?.updateList(dataList)
    }

    private fun getInventoryStockForStore(storeId: String, multiSku: String): Call<SkusInventoryForStoreResponse> {
      val skusInventoryForStoreRequest =    OneAppService.getInventorySkuForStore(storeId, multiSku)
        skusInventoryForStoreRequest.enqueue(CompletionHandler(object: RequestListener<SkusInventoryForStoreResponse>{
            override fun onSuccess(skusInventoryForStoreResponse: SkusInventoryForStoreResponse?) {
                when (skusInventoryForStoreResponse?.httpCode) {
                    200 -> {
                        var fulFillmentType: String? = null
                        val storeId = skusInventoryForStoreResponse.storeId
                        for ((key, value) in mMapStoreFulFillmentKeyValue!!) {
                            if (storeId.equals(value, ignoreCase = true)) {
                                fulFillmentType = key
                            }
                        }
                        val skuInventory = skusInventoryForStoreResponse.skuInventory
                        // skuInventory is empty or null
                        if (skuInventory.isEmpty()) {
                            for (inventoryItems in dataList) {
                                if (inventoryItems.type == OrderDetailsItem.ViewType.COMMERCE_ITEM) {
                                    var item = inventoryItems.item as OrderHistoryCommerceItem
                                    if (TextUtils.isEmpty(item.fulfillmentType)) continue
                                    if (item.fulfillmentType.equals(fulFillmentType!!, ignoreCase = true)) {
                                        item.inventoryCallCompleted = true
                                        item.quantityInStock = -1
                                    }
                                }
                            }
                        }

                        if (skuInventory.size > 0) {
                            for (dataListItem in dataList) {
                                if (dataListItem.type == OrderDetailsItem.ViewType.COMMERCE_ITEM) {
                                    var item = dataListItem.item as OrderHistoryCommerceItem
                                    if (item.fulfillmentType.equals(fulFillmentType!!, ignoreCase = true)) {
                                        val otherSkuId = item.commerceItemInfo.catalogRefId
                                        item.inventoryCallCompleted = true
                                        item.quantityInStock = -1
                                        for (inventorySku in skusInventoryForStoreResponse.skuInventory) {
                                            if (otherSkuId.equals(inventorySku.sku, ignoreCase = true)) {
                                                item.quantityInStock = inventorySku.quantity
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        updateShoppingList()

                    }
                }
            }

            override fun onFailure(error: Throwable?) {
            }

        },SkusInventoryForStoreResponse::class.java))

        return skusInventoryForStoreRequest
    }

    private fun setSelectAllTextVisibility(state: Boolean) {
        tvSelectAll?.visibility = if (state) View.VISIBLE else View.GONE
    }

    private fun selectAllItems(isSelected: Boolean) {
        if (addOrderToCartAdapter != null && dataList != null && dataList.size > 0) {
            for (listItem in dataList) {
                if (listItem.type == OrderDetailsItem.ViewType.COMMERCE_ITEM) {
                    var item = listItem.item as OrderHistoryCommerceItem
                    if (item.quantityInStock != 0) {
                        item.isSelected = isSelected
                        val quantity = if (item.userQuantity > 1) item.userQuantity else 1 // Click -> Select all - when one item quantity is > 1
                        item.userQuantity = if (isSelected) quantity else 0
                    }
                }
            }
            addOrderToCartAdapter?.updateList(dataList)
        }
    }

    private fun onSelectAll() {
        if (tvSelectAll?.getText().toString().equals("SELECT ALL", ignoreCase = true)) {
            selectAllItems(true)
            tvSelectAll?.setText(getString(R.string.deselect))
        } else {
            selectAllItems(false)
            tvSelectAll?.setText(getString(R.string.select_all))
        }
    }

    private fun addItemsToCart() {
        executeAddToCart(dataList)
    }

    private fun executeAddToCart(listItems: List<OrderDetailsItem>) {
        onAddToCartPreExecute()
        val selectedItems = ArrayList<AddItemToCart>()
        for (listItem in listItems) {
            if (listItem.type == OrderDetailsItem.ViewType.COMMERCE_ITEM) {
                var item = listItem.item as OrderHistoryCommerceItem
                if (item.isSelected && item.quantityInStock > 0)
                    selectedItems.add(AddItemToCart(item.commerceItemInfo.productId, item.commerceItemInfo.catalogRefId, item.userQuantity))
            }
        }

        postAddItemToCart(selectedItems)
    }

    private fun onAddToCartPreExecute() {
        tvAddToCart.visibility = View.GONE
        loadingBar.visibility = View.VISIBLE
    }

    private fun postAddItemToCart(addItemToCart: MutableList<AddItemToCart>): Call<AddItemToCartResponse> {
        val postItemToCart = PostItemToCart()
        return postItemToCart.make(addItemToCart, object : RequestListener<AddItemToCartResponse> {
            override fun onSuccess(addItemToCartResponse: AddItemToCartResponse?) {
                addItemToCartResponse?.let { onAddToCartSuccess(it) }
            }

            override fun onFailure(error: Throwable?) {
            }

        })
    }

    fun onAddToCartSuccess(addItemToCartResponse: AddItemToCartResponse) {
        listener.onItemsAddedToCart(addItemToCartResponse)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is FragmentsEventsListner) {
            listener = context
        } else {
            throw ClassCastException(context.toString() + " must implement FragmentsEventsListner.")
        }
    }

    override fun openSetSuburbProcess() {
        activity?.apply {
            val openDeliveryLocationSelectionActivity = Intent(this, DeliveryLocationSelectionActivity::class.java)
            startActivityForResult(openDeliveryLocationSelectionActivity, REQUEST_SUBURB_CHANGE)
            overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay)
        }
    }

}
