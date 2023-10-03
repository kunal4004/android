package za.co.woolworths.financial.services.android.ui.fragments.shop


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentAddOrderToCartBinding
import com.google.gson.Gson
import org.json.JSONObject
import retrofit2.Call
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.isEnhanceSubstitutionFeatureAvailable
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity
import za.co.woolworths.financial.services.android.ui.adapters.AddOrderToCartAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.FragmentsEventsListner
import za.co.woolworths.financial.services.android.ui.views.ToastFactory
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.RESPONSE_ERROR_CODE_1235
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding


class AddOrderToCartFragment : BaseFragmentBinding<FragmentAddOrderToCartBinding>(FragmentAddOrderToCartBinding::inflate), AddOrderToCartAdapter.OnItemClick {

    private var orderDetailsResponse: OrderDetailsResponse? = null
    private var dataList = arrayListOf<OrderDetailsItem>()
    private var addOrderToCartAdapter: AddOrderToCartAdapter? = null
    var updateQuantityPosition: Int = 0
    private var isAnyItemSelected: Boolean = false
    private var mFulFillmentStoreId: String? = null
    private var mMapStoreFulFillmentKeyValue: MutableMap<String, String> = hashMapOf()
    private var mWoolWorthsApplication: WoolworthsApplication? = null
    private var isSelectAllReadyToShow = false
    private lateinit var listener: FragmentsEventsListner

    private var order: Order? = null
    private var orderText: String = ""

    companion object {
        private const val ARG_PARAM = "orderDetailsResponse"
        const val QUANTITY_CHANGED = 2019
        const val REQUEST_SUBURB_CHANGE = 1550
        fun getInstance(orderDetailsResponse: OrderDetailsResponse) = AddOrderToCartFragment().withArgs {
            putString(ARG_PARAM, Utils.objectToJson(orderDetailsResponse))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{ it ->
            orderDetailsResponse = Utils.jsonStringToObject(it.getString(ARG_PARAM), OrderDetailsResponse::class.java) as OrderDetailsResponse
            order = orderDetailsResponse?.orderSummary?.let { orderSummary ->
                Order(
                    completedDate = orderSummary.completedDate ?: "",
                    orderCancellable = orderSummary.orderCancellable,
                    state = orderSummary.state ?: "",
                    orderId = orderSummary.orderId ?: "",
                    submittedDate = orderSummary.submittedDate ?: "",
                    total = orderSummary.total,
                    taxNoteNumbers = orderSummary.taxNoteNumbers ?: ArrayList(0),
                    requestCancellation = orderSummary.requestCancellation,
                    deliveryDates = orderSummary.deliveryDates,
                    clickAndCollectOrder = orderSummary.clickAndCollectOrder,
                    deliveryStatus = null // TODO: update oderSummary.deliveryStatus's type, remove null and use value from orderSummary, and test if no regression happens
                )
            }
            orderText = getString(R.string.order_page_title_prefix) + order?.orderId
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mWoolWorthsApplication = activity?.application as WoolworthsApplication
        binding.initViews()
    }

    private fun FragmentAddOrderToCartBinding.initViews() {
        toolbarText?.text = orderText
        btnBack?.setOnClickListener { activity?.onBackPressed() }
        rvItemsToCart.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        addToCartButton.isEnabled = isAnyItemSelected
        tvSelectAllAddToCart?.setOnClickListener { onSelectAll() }
        addToCartButton.setOnClickListener { addItemsToCart() }
        setSelectAllTextVisibility(isSelectAllReadyToShow)
        bindData()
    }

    fun bindData() {
        dataList = buildDataForOrderDetailsView(orderDetailsResponse!!)
        addOrderToCartAdapter = activity?.let { AddOrderToCartAdapter(it, this, dataList) }
        binding.rvItemsToCart.adapter = addOrderToCartAdapter
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
        binding.apply {
            if (isAdded) {
                isAnyItemSelected = getButtonStatus(dataList)
                addToCartButton.isEnabled = isAnyItemSelected
                if (dataList.size > 0)
                    tvSelectAllAddToCart?.setText(getString(if (getSelectAllMenuVisibility(dataList)) R.string.deselect else R.string.select_all))
                else
                    tvSelectAllAddToCart?.visibility = View.GONE
            }
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
            val productsArray = itemsObject.getJSONArray(key)
            val orderItemLength = productsArray.length()

            val orderDetailsHeaderItem = when {
                key.contains(ProductType.DEFAULT.value) -> OrderDetailsItem(ProductType.DEFAULT.longHeader, OrderDetailsItem.ViewType.HEADER, orderItemLength)
                key.contains(ProductType.HOME_COMMERCE_ITEM.value) -> OrderDetailsItem(ProductType.HOME_COMMERCE_ITEM.longHeader, OrderDetailsItem.ViewType.HEADER, orderItemLength)
                key.contains(ProductType.FOOD_COMMERCE_ITEM.value) -> OrderDetailsItem(ProductType.FOOD_COMMERCE_ITEM.longHeader, OrderDetailsItem.ViewType.HEADER, orderItemLength)
                key.contains(ProductType.CLOTHING_COMMERCE_ITEM.value) -> OrderDetailsItem(ProductType.CLOTHING_COMMERCE_ITEM.longHeader, OrderDetailsItem.ViewType.HEADER, orderItemLength)
                key.contains(ProductType.PREMIUM_BRAND_COMMERCE_ITEM.value) -> OrderDetailsItem(ProductType.PREMIUM_BRAND_COMMERCE_ITEM.longHeader, OrderDetailsItem.ViewType.HEADER, orderItemLength)
                else -> OrderDetailsItem(ProductType.OTHER_ITEMS.longHeader, OrderDetailsItem.ViewType.HEADER, orderItemLength)
            }

            val orderDetailCommerceItem = arrayListOf<OrderDetailsItem>()
            if (orderItemLength > 0) {
                for (i in 0 until orderItemLength) {
                    try {
                        val commerceItem: OrderHistoryCommerceItem = Gson().fromJson(productsArray.getJSONObject(i).toString(), OrderHistoryCommerceItem::class.java)
                        val fulfillmentStoreId = Utils.retrieveStoreId(commerceItem.fulfillmentType)
                        commerceItem.fulfillmentStoreId = fulfillmentStoreId!!.replace("\"".toRegex(), "")
                        if (!commerceItem.isGWP)
                            orderDetailCommerceItem.add(OrderDetailsItem(commerceItem, OrderDetailsItem.ViewType.COMMERCE_ITEM, orderItemLength))
                    } catch (e:Exception) {
                        FirebaseManager.logException(e)
                    }
                }
            }

            orderDetailsHeaderItem.orderItemLength = orderDetailCommerceItem.size
            dataList.add(orderDetailsHeaderItem)
            orderDetailCommerceItem.forEach { orderDetailsItem -> dataList.add(orderDetailsItem)}
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
      val skusInventoryForStoreRequest =    OneAppService().getInventorySkuForStore(
          storeId,
          multiSku,
          false
      )
        skusInventoryForStoreRequest.enqueue(CompletionHandler(object: IResponseListener<SkusInventoryForStoreResponse> {
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
        binding.tvSelectAllAddToCart?.visibility = if (state) View.VISIBLE else View.GONE
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
        binding.apply {
            if (tvSelectAllAddToCart?.getText().toString()
                    .equals("SELECT ALL", ignoreCase = true)
            ) {
                selectAllItems(true)
                tvSelectAllAddToCart?.setText(getString(R.string.deselect))
            } else {
                selectAllItems(false)
                tvSelectAllAddToCart?.setText(getString(R.string.select_all))
            }
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
                    selectedItems.add(
                        if (isEnhanceSubstitutionFeatureAvailable()) {
                            AddItemToCart(item.commerceItemInfo.productId, item.commerceItemInfo.catalogRefId, item.userQuantity, SubstitutionChoice.SHOPPER_CHOICE.name, "")
                        } else {
                            AddItemToCart(item.commerceItemInfo.productId, item.commerceItemInfo.catalogRefId, item.userQuantity)
                        }
                    )
            }
        }

        postAddItemToCart(selectedItems)
    }

    private fun onAddToCartPreExecute() {
        binding.apply {
            tvAddToCart.visibility = View.GONE
            loadingBar.visibility = View.VISIBLE
        }
    }

    private fun postAddItemToCart(addItemToCart: MutableList<AddItemToCart>): Call<AddItemToCartResponse> {
        val postItemToCart = PostItemToCart()
        return postItemToCart.make(addItemToCart, object : IResponseListener<AddItemToCartResponse> {
            override fun onSuccess(addItemToCartResponse: AddItemToCartResponse?) {
                addItemToCartResponse?.let { onAddToCartSuccess(it, addItemToCart.sumBy { item -> item.quantity }) }
            }

            override fun onFailure(error: Throwable?) {
            }

        })
    }

    fun onAddToCartSuccess(addItemToCartResponse: AddItemToCartResponse, size: Int) {
      onItemsAddedToCart(addItemToCartResponse, size)
    }

     fun onItemsAddedToCart(addItemToCartResponse: AddItemToCartResponse, size:Int) {
        when (addItemToCartResponse.httpCode) {
            200 -> {

                activity?.onBackPressed()

                if ((KotlinUtils.isDeliveryOptionClickAndCollect() || KotlinUtils.isDeliveryOptionDash())
                    && addItemToCartResponse.data[0]?.productCountMap?.quantityLimit?.foodLayoutColour != null) {
                    addItemToCartResponse.data[0]?.productCountMap?.let {
                        ToastFactory.showItemsLimitToastOnAddToCart(binding.fragmentAddToOrder, it, requireActivity(), size) }
                } else {
                    ToastFactory.buildAddToCartSuccessToast(binding.fragmentAddToOrder, true, requireActivity())
                }
            }
            440 -> {
                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, addItemToCartResponse.response.stsParams, requireActivity())
            }

            AppConstant.HTTP_EXPECTATION_FAILED_502 -> {
                if (addItemToCartResponse.response.code == RESPONSE_ERROR_CODE_1235 ) {
                    binding.loadingBar?.visibility = View.GONE
                    binding.tvAddToCart?.visibility = View.VISIBLE
                    KotlinUtils.showQuantityLimitErrror(
                        activity?.supportFragmentManager,
                        addItemToCartResponse.response.desc,
                        "",
                        context
                    )
                }
            }
        }
    }

    override fun openSetSuburbProcess() {
        activity?.apply {
            KotlinUtils.presentEditDeliveryGeoLocationActivity(this, REQUEST_SUBURB_CHANGE)
        }
    }
}
