package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ShoppingListDetailFragmentBinding
import com.google.gson.Gson
import com.google.gson.JsonElement
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.ScreenNames.Companion.SHOPPING_LIST_ITEMS
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.contracts.IToastInterface
import za.co.woolworths.financial.services.android.geolocation.GeoUtils.Companion.getPlaceId
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.item_limits.ProductCountMap
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService.deleteShoppingListItem
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity
import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListItemsAdapter
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.IOnConfirmDeliveryLocationActionListener
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog.ConfirmDeliveryLocationFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.buildAddToCartSuccessToast
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.buildShoppingListFromSearchResultToast
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.showItemsLimitToastOnAddToCart
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_SESSION_TIMEOUT_440
import za.co.woolworths.financial.services.android.util.EmptyCartView.EmptyCartInterface
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getPreferredDeliveryType
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getPreferredPlaceId
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.presentEditDeliveryGeoLocationActivity
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.setDeliveryAddressView
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.showQuantityLimitErrror
import za.co.woolworths.financial.services.android.util.ToastUtils.ToastInterface
import za.co.woolworths.financial.services.android.util.wenum.Delivery

@AndroidEntryPoint
class ShoppingListDetailFragment : Fragment(), View.OnClickListener, EmptyCartInterface,
    NetworkChangeListener, ToastInterface, ShoppingListItemsNavigator, IToastInterface,
    IOnConfirmDeliveryLocationActionListener {

    private val viewModel: ShoppingListDetailViewModel by viewModels(
        ownerProducer = { this }
    )

    private var mErrorHandlerView: ErrorHandlerView? = null
    private var openFromMyList = false
    private var addedToCart = false
    private var errorMessageWasPopUp = false
    private var showMenu = false
    private var isSelectAll: Boolean = false
    private var listName: String? = null
    private var listId: String? = ""
    private var shoppingListItemsAdapter: ShoppingListItemsAdapter? = null
    private var mConnectionBroadcast: BroadcastReceiver? = null
    private var mPostAddToCart: Call<AddItemToCartResponse>? = null

    private var _bindingListDetails: ShoppingListDetailFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val bindingListDetails get() = _bindingListDetails!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            listId = getString(ARG_LIST_ID, "")
            listName = getString(ARG_LIST_NAME, "")
            openFromMyList = getBoolean(ARG_OPEN_FROM_MY_LIST, false)
        }
        Utils.updateStatusBarBackground(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _bindingListDetails = ShoppingListDetailFragmentBinding.inflate(inflater, container, false)
        return _bindingListDetails?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingListDetails = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(listName)
        setDeliveryLocation()
        initViewAndEvent()
        initGetShoppingListItems()
        addSubscribeEvents()
        addFragmentListener()
    }

    private fun addSubscribeEvents() {
        // Shopping List items
        viewModel.shoppListDetails.observe(viewLifecycleOwner) {
            val response = it.peekContent().data
            when (it.peekContent().status) {
                Status.LOADING -> {
                    mErrorHandlerView?.hideErrorHandler()
                    bindingListDetails.apply {
                        rlEmptyListView.visibility = GONE
                        rcvShoppingListItems.visibility = GONE
                        loadingBar.visibility = VISIBLE
                        rlCheckOut.visibility = GONE
                    }
                }
                Status.SUCCESS -> {
                    onShoppingListItemsResponse(response)
                }
                Status.ERROR -> {
                    onShoppingListItemsResponseError(response)
                }
            }
        }

        // Shopping List Items Inventory
        viewModel.inventoryDetails.observe(viewLifecycleOwner) {
            val response = it.peekContent().data
            when (it.peekContent().status) {
                Status.LOADING -> {
                    enableAdapterClickEvent(false)
                    mErrorHandlerView?.hideErrorHandler()
                    bindingListDetails.rlEmptyListView.visibility = GONE
                    bindingListDetails.rcvShoppingListItems.visibility = VISIBLE
                    bindingListDetails.loadingBar.visibility = VISIBLE
                }
                Status.SUCCESS -> {
                    enableAdapterClickEvent(true)
                    bindingListDetails.loadingBar.visibility = GONE
                    getInventoryForStoreSuccess(response)
                }
                Status.ERROR -> {
                    viewModel.inventoryCallFailed = true
                    viewModel.setOutOfStock()
                    if (!isAdded || !isVisible) return@observe
                    enableAdapterClickEvent(true)
                    mErrorHandlerView?.showToast()
                    updateList()
                }
            }
        }
    }

    private fun setUpToolbar(listName: String?) {
        bindingListDetails.shoppingListTitleTextView.text = listName
        (activity as? BottomNavigationActivity)?.apply {
            bindingListDetails.appbar.visibility = VISIBLE
            hideToolbar()
            showBackNavigationIcon(true)
            setToolbarBackgroundDrawable(R.drawable.appbar_background)
            bindingListDetails.btnBack.setOnClickListener { onBackPressed() }
            toolbar().setNavigationOnClickListener { popFragment() }
            setTitle(listName)
        }
    }

    private fun initViewAndEvent() {
        mConnectionBroadcast = Utils.connectionBroadCast(activity, this)
        setUpAddToCartButton()
        with(bindingListDetails) {
            initList(rcvShoppingListItems)

            selectDeselectAllTextView.setOnClickListener(this@ShoppingListDetailFragment)
            deliveryLocationConstLayout.setOnClickListener(this@ShoppingListDetailFragment)
            textProductSearch.setOnClickListener(this@ShoppingListDetailFragment)
            btnRetry.setOnClickListener(this@ShoppingListDetailFragment)

            mErrorHandlerView = ErrorHandlerView(activity, noConnectionLayout)
            mErrorHandlerView?.setMargin(noConnectionLayout, 0, 0, 0, 0)
            val emptyCartView = EmptyCartView(root, this@ShoppingListDetailFragment)
            emptyCartView.setView(
                getString(R.string.title_empty_shopping_list),
                getString(R.string.description_empty_shopping_list),
                getString(R.string.button_empty_shopping_list),
                R.drawable.empty_list_icon
            )
        }

        // Show Bottom Navigation Menu
        (activity as? BottomNavigationActivity)?.showBottomNavigationMenu()
    }

    private fun setUpView() {
        bindingListDetails.rlEmptyListView.visibility =
            if (viewModel.mShoppingListItems.isEmpty()) VISIBLE else GONE
        // 1 to exclude header
        bindingListDetails.rcvShoppingListItems.visibility =
            if (viewModel.mShoppingListItems.isEmpty()) GONE else VISIBLE
        manageSelectAllMenuVisibility()
    }

    private fun initList(rcvShoppingListItems: RecyclerView) {
        if (!isAdded || !isVisible) return
        shoppingListItemsAdapter = ShoppingListItemsAdapter(viewModel.mShoppingListItems, this)
        val mLayoutManager = LinearLayoutManager(activity)
        mLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rcvShoppingListItems.layoutManager = mLayoutManager
        rcvShoppingListItems.adapter = shoppingListItemsAdapter
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.deliveryLocationConstLayout -> deliverySelectionIntent(DELIVERY_LOCATION_REQUEST)
            R.id.selectDeselectAllTextView -> onOptionsItemSelected()
            R.id.textProductSearch -> openProductSearchActivity()
            R.id.btnRetry -> if (NetworkManager.getInstance().isConnectedToNetwork(
                    activity
                )
            ) {
                errorMessageWasPopUp = false
                initGetShoppingListItems()
            }
            R.id.btnCheckOut -> addItemsToCart()
            else -> {}
        }
    }

    private fun openProductSearchActivity() {
        requireActivity().apply {
            val openProductSearchActivity = Intent(activity, ProductSearchActivity::class.java)
            openProductSearchActivity.putExtra(
                "SEARCH_TEXT_HINT",
                getString(R.string.shopping_search_hint)
            )
            openProductSearchActivity.putExtra("listID", listId)
            startActivityForResult(
                openProductSearchActivity,
                ProductSearchActivity.PRODUCT_SEARCH_ACTIVITY_REQUEST_CODE
            )
            overridePendingTransition(0, 0)
        }
    }

    private fun onShoppingListItemsResponse(shoppingListItemsResponse: ShoppingListItemsResponse?) {
        if (!isAdded || !isVisible) return
        when (shoppingListItemsResponse?.httpCode) {
            HTTP_OK -> {
                bindingListDetails.loadingBar.visibility = GONE
                viewModel.makeInventoryCalls()
                updateList()
            }
            HTTP_SESSION_TIMEOUT_440 -> SessionUtilities.getInstance().setSessionState(
                SessionDao.SESSION_STATE.INACTIVE,
                shoppingListItemsResponse.response.stsParams,
                activity
            )
            else -> {
                onShoppingListItemsResponseError(shoppingListItemsResponse)
            }
        }
    }

    private fun onShoppingListItemsResponseError(response: ShoppingListItemsResponse?) {
        if (!isAdded || !isVisible) return

        bindingListDetails.loadingBar.visibility = GONE
        enableAdapterClickEvent(true)
        response?.response?.desc?.let {
            Utils.displayValidationMessage(
                requireActivity(),
                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                response.response?.desc
            )
        }
    }

    private fun enableAdapterClickEvent(clickable: Boolean) {
        shoppingListItemsAdapter?.adapterClickable(clickable)
    }

    override fun onItemSelectionChange() {
        if (!isAdded || !isVisible) return
        updateCartCountButton()
        manageSelectAllMenuVisibility()
    }

    override fun onShoppingListItemDelete(shoppingListItemsResponse: ShoppingListItemsResponse) {
        viewModel.mShoppingListItems =
            shoppingListItemsResponse.listItems?.let { ArrayList(it) } ?: ArrayList(0)
        updateList()
        enableAdapterClickEvent(true)
    }

    override fun onItemDeleteClick(
        id: String,
        productId: String,
        catalogRefId: String,
        shouldUpdateShoppingList: Boolean
    ) {
        val listSize = shoppingListItemsAdapter?.shoppingListItems?.size ?: 0
        if (listSize == 1) {
            if (!shouldUpdateShoppingList) {
                bindingListDetails.rlEmptyListView.visibility = VISIBLE
                bindingListDetails.rcvShoppingListItems.visibility = GONE
                showMenu = false
                val activity: Activity? = activity
                activity?.invalidateOptionsMenu()
                bindingListDetails.rlCheckOut.visibility = GONE
            }
        }
        val shoppingListItemsResponseCall = deleteShoppingListItem(
            listId!!, id, productId, catalogRefId
        )
        shoppingListItemsResponseCall.enqueue(
            CompletionHandler(
                object : IResponseListener<ShoppingListItemsResponse> {
                    override fun onSuccess(response: ShoppingListItemsResponse?) {

                        val currentList =
                            shoppingListItemsAdapter?.shoppingListItems ?: ArrayList(0)

                        for (shoppingListItem in currentList) {
                            for (updatedList in response?.listItems ?: ArrayList(0)) {
                                if (shoppingListItem.catalogRefId.equals(
                                        updatedList.catalogRefId,
                                        ignoreCase = true
                                    )
                                ) {
                                    // Since the location is not changed.
                                    updatedList.inventoryCallCompleted =
                                        shoppingListItem.inventoryCallCompleted
                                    updatedList.unavailable = shoppingListItem.unavailable
                                    updatedList.quantityInStock = shoppingListItem.quantityInStock
                                }
                            }
                        }

                        response?.let { onShoppingListItemDelete(it) }

                        response?.listItems?.let {
                            val isStockAvailable =
                                getIsStockAvailable(ArrayList(response.listItems))
                            if (!isStockAvailable) {
                                bindingListDetails.rlCheckOut.visibility = GONE
                                manageSelectAllMenuVisibility()
                            }
                        }
                        //requirement: to show count on add to cart
                        //update on delete item
                        updateCartCountButton()
                    }

                    override fun onFailure(error: Throwable?) {
                        if (shouldUpdateShoppingList) onDeleteItemFailed()
                    }
                }, ShoppingListItemsResponse::class.java
            )
        )
    }

    private fun getIsStockAvailable(list: ArrayList<ShoppingListItem?>): Boolean {
        var isAvailable = false
        for (listItem in list) {
            if ((listItem?.quantityInStock ?: 0) > 0) {
                isAvailable = true
                break
            }
        }
        return isAvailable
    }

    override fun onShoppingSearchClick() {
        openProductSearchActivity()
    }

    private fun onAddToCartPreExecute() {
        bindingListDetails.pbLoadingIndicator.visibility = VISIBLE
        enableAddToCartButton(GONE)
    }

    fun onAddToCartSuccess(addItemToCartResponse: AddItemToCartResponse?, size: Int) {
        if (!isAdded || !isVisible || addItemToCartResponse == null) return
        val resultIntent = Intent()
        if (addItemToCartResponse.data?.isNotEmpty() == true) {
            val successMessage = addItemToCartResponse.data[0].message
            resultIntent.putExtra("addedToCartMessage", successMessage)
            resultIntent.putExtra(
                "ProductCountMap", Utils.toJson(
                    addItemToCartResponse.data[0].productCountMap
                )
            )
            resultIntent.putExtra("ItemsCount", size)
        }

        // reset selection after items added to cart
        shoppingListItemsAdapter?.resetSelection()

        bindingListDetails.pbLoadingIndicator.visibility = GONE
        bindingListDetails.btnCheckOut.visibility = VISIBLE

        // Present toast on BottomNavigationMenu if shopping list detail was opened from my list
        addItemToCartResponse.data?.get(0)?.let { addedToCartDatum ->
            if (openFromMyList) {
                (activity as? BottomNavigationActivity)?.apply {
                    onBackPressed()
                    if (addItemToCartResponse.data?.isNotEmpty() == true) {
                        val itemAddToCartMessage = addedToCartDatum.message
                        val productCountMap = addedToCartDatum.productCountMap
                        setToast(itemAddToCartMessage, "", productCountMap, size)
                    }
                }
            } else {
                addedToCartDatum.productCountMap?.let { productCountMap ->
                    // else display shopping list toast
                    when (getPreferredDeliveryType()) {
                        Delivery.DASH, Delivery.CNC ->
                            if (productCountMap.quantityLimit?.foodLayoutColour != null) {
                                showItemsLimitToastOnAddToCart(
                                    bindingListDetails.rlCheckOut,
                                    productCountMap,
                                    requireActivity(),
                                    size,
                                    true
                                )
                            } else {
                                buildAddToCartSuccessToast(
                                    bindingListDetails.rlCheckOut, true, requireActivity(), this
                                )
                            }
                        else -> buildAddToCartSuccessToast(
                            bindingListDetails.rlCheckOut, true, requireActivity(), this
                        )
                    }
                }
            }
        }
    }

    private fun confirmDeliveryLocation() {
        if (!isAdded || !isVisible) return
        val fragmentManager = this.childFragmentManager

        val confirmDeliveryLocationFragment = ConfirmDeliveryLocationFragment.newInstance()
        confirmDeliveryLocationFragment.isCancelable = false
        confirmDeliveryLocationFragment.show(
            fragmentManager,
            ConfirmDeliveryLocationFragment::class.java.simpleName
        )
    }

    private fun enableAddToCartButton(visible: Int) {
        bindingListDetails.btnCheckOut.visibility = visible
    }

    fun onSessionTokenExpired(response: Response) {
        SessionUtilities.getInstance()
            .setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.stsParams, activity)
    }

    fun otherHttpCode(response: Response) {
        if (isAdded) {
            bindingListDetails.pbLoadingIndicator.visibility = GONE
            enableAddToCartButton(VISIBLE)
            Utils.displayValidationMessage(
                activity,
                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                response.desc
            )
        }
    }

    override fun onDeleteItemFailed() {
        if (!isAdded) return
        mErrorHandlerView?.showToast()
        updateList()
    }

    override fun openProductDetailFragment(productName: String, productList: ProductList) {
        if (activity is BottomNavigationActivity) {
            val fragment = ProductDetailsFragment.newInstance()
            val gson = Gson()
            val strProductList = gson.toJson(productList)
            val bundle = Bundle()
            bundle.putString(ProductDetailsFragment.STR_PRODUCT_LIST, strProductList)
            bundle.putString(ProductDetailsFragment.STR_PRODUCT_CATEGORY, productName)
            fragment.arguments = bundle
            val bottomNavigationActivity = activity as BottomNavigationActivity?
            bottomNavigationActivity!!.pushFragment(fragment)
        }
    }

    private fun initGetShoppingListItems() {
        listId?.let { viewModel.getShoppingListDetails(it) }
    }

    private fun addFragmentListener() {
        activity ?: return
        activity?.supportFragmentManager?.setFragmentResultListener(
            SearchResultFragment.ADDED_TO_SHOPPING_LIST_RESULT_CODE.toString(),
            activity!!
        ) { requestKey: String?, result: Bundle ->
            if (result.containsKey("listItems")) {
                val count = result.getInt("listItems", 0)
                buildShoppingListFromSearchResultToast(
                    activity!!, bindingListDetails.rlCheckOut, listName!!, count
                )
            }
            initGetShoppingListItems()
        }
    }

    override fun onDetach() {
        super.onDetach()
        cancelRequest(mPostAddToCart)
//        cancelRequest(mGetInventorySkusForStore)
    }

    private fun cancelRequest(call: Call<*>?) {
        if (call != null && !call.isCanceled) {
            call.cancel()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (activity == null || !isAdded) return
        if (showMenu) {
            bindingListDetails.selectDeselectAllTextView.visibility = VISIBLE
            bindingListDetails.selectDeselectAllTextView.text = requireContext().getString(
                if (isSelectAll)
                    R.string.select_all
                else
                    R.string.deselect_all
            )
        } else bindingListDetails.selectDeselectAllTextView.visibility = GONE

        super.onCreateOptionsMenu(menu, inflater)
    }

    fun onOptionsItemSelected() {
        if (!isAdded) return
        if (shouldUserSetSuburb()) {
            deliverySelectionIntent(DELIVERY_LOCATION_REQUEST_CODE_FROM_SELECT_ALL)
            return
        }
        if (TOOLBAR_SELECT_ALL.equals(
                bindingListDetails.selectDeselectAllTextView.text.toString(),
                ignoreCase = true
            )
        ) {
            selectAllListItems(true)
            bindingListDetails.selectDeselectAllTextView.text = getString(R.string.deselect_all)
        } else {
            selectAllListItems(false)
            bindingListDetails.selectDeselectAllTextView.text = getString(R.string.select_all)
        }
    }

    override fun onEmptyCartRetry() {
        openProductSearchActivity()
    }

    private fun setUpAddToCartButton() {
        bindingListDetails.btnCheckOut.setOnClickListener(this)
        updateCartCountButton()
    }

    private fun updateCartCountButton() {
        val itemWasSelected =
            shoppingListItemsAdapter?.shoppingListItems?.let { viewModel.isItemSelected(it) }
                ?: false

        if (itemWasSelected) {
            bindingListDetails.rlCheckOut.visibility = VISIBLE
            val count = shoppingListItemsAdapter?.addedItemsCount ?: 0
            bindingListDetails.btnCheckOut.text =
                requireContext().resources.getQuantityString(
                    R.plurals.plural_add_to_cart,
                    count,
                    count
                )
            bindingListDetails.selectDeselectAllTextView.visibility = VISIBLE
            Utils.setRecyclerViewMargin(bindingListDetails.rcvShoppingListItems, Utils.dp2px(60f))
        } else {
            bindingListDetails.rlCheckOut.visibility = GONE
            Utils.setRecyclerViewMargin(bindingListDetails.rcvShoppingListItems, 0)
        }
    }

    private fun executeAddToCart(items: ArrayList<ShoppingListItem>?) {
        onAddToCartPreExecute()
        val selectedItems: MutableList<AddItemToCart> = ArrayList(0)
        for (item in items!!) {
            if (item.isSelected && item.quantityInStock > 0) selectedItems.add(
                AddItemToCart(
                    item.productId,
                    item.catalogRefId,
                    item.userQuantity
                )
            )
        }
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.SHOP_MY_LIST_ADD_TO_CART,
            activity
        )
        mPostAddToCart = postAddItemToCart(ArrayList(selectedItems))
    }

    fun manageSelectAllMenuVisibility() {
        showMenu = false
        isSelectAll = false
        for (shoppingListItem in viewModel.mShoppingListItems) {
            if (shoppingListItem.quantityInStock > 0) {
                // This condition to verify to show select all or Deselect All
                if (!shoppingListItem.isSelected) {
                    isSelectAll = true
                    showMenu = true
                    break
                }
                showMenu = true
                break
            }
        }
        requireActivity().invalidateOptionsMenu()
    }

    private fun selectAllListItems(setSelection: Boolean) {
        shoppingListItemsAdapter?.apply {
            shoppingListItems ?: return
            for (item in shoppingListItems) {
                if (item.quantityInStock > 0) {
                    item.isSelected = setSelection
                    item.userQuantity = item.userQuantity.coerceAtLeast(1)
                }
            }
            notifyDataSetChanged()
            viewModel.mShoppingListItems = shoppingListItems
            onItemSelectionChange()
        }
    }

    override fun onConnectionChanged() {
        if (viewModel.inventoryCallFailed) {
            viewModel.makeInventoryCalls()
        }
        if (addedToCart()) {
            addItemsToCart()
            addedToCartFail(false)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().apply {
            Utils.setScreenName(this, SHOPPING_LIST_ITEMS)
            registerReceiver(
                mConnectionBroadcast,
                IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
            )
        }
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(mConnectionBroadcast)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            (activity as? BottomNavigationActivity)?.showBottomNavigationMenu()
        }
    }

    override fun onToastButtonClicked(currentState: String) {}

    private fun addItemsToCart() {
        executeAddToCart(viewModel.mShoppingListItems)
    }

    private fun getInventoryForStoreSuccess(skusInventoryForStoreResponse: SkusInventoryForStoreResponse?) {
        if (skusInventoryForStoreResponse?.httpCode == HTTP_OK) {
            updateList()
            enableAdapterClickEvent(true)
        } else {
            onInventoryError(skusInventoryForStoreResponse)
        }
    }

    private fun onInventoryError(response: SkusInventoryForStoreResponse?) {
        // Hide quantity progress bar indicator
        viewModel.setOutOfStock()
        updateList()
        enableAdapterClickEvent(true)
        if (!errorMessageWasPopUp) {
            response?.response?.desc?.let {
                errorMessageWasPopUp = true
                Utils.displayValidationMessage(
                    activity,
                    CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                    it
                )
            }
        }
    }

    private fun updateList() {
        setUpView()
        shoppingListItemsAdapter?.updateList(
            viewModel.mShoppingListItems
        )
    }

    override fun openSetSuburbProcess(shoppingListItem: ShoppingListItem) {
        viewModel.mOpenShoppingListItem = shoppingListItem
        deliverySelectionIntent(DELIVERY_LOCATION_REQUEST)
    }

    override fun onAddListItemCount(shoppingListItem: ShoppingListItem) {
        viewModel.mOpenShoppingListItem = shoppingListItem
        viewModel.setItem(shoppingListItem)
        updateCartCountButton()
    }

    override fun onSubstractListItemCount(listItem: ShoppingListItem) {
        viewModel.mOpenShoppingListItem = listItem
        viewModel.setItem(listItem)
        updateCartCountButton()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // add to list from search result
        if (requestCode == ProductSearchActivity.PRODUCT_SEARCH_ACTIVITY_REQUEST_CODE && resultCode == SearchResultFragment.ADDED_TO_SHOPPING_LIST_RESULT_CODE) {
            val count = data!!.getIntExtra("listItems", 0)
            buildShoppingListFromSearchResultToast(
                activity!!, bindingListDetails.rlCheckOut, listName!!, count
            )
            initGetShoppingListItems()
            return
        }
        if (requestCode == BottomNavigationActivity.PDP_REQUEST_CODE && resultCode == AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE
            || requestCode == ProductSearchActivity.PRODUCT_SEARCH_ACTIVITY_REQUEST_CODE && resultCode == AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE
        ) {
            activity?.setResult(
                AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE,
                data
            )
            activity?.onBackPressed()
            return
            // response from search product from shopping list
        }

        when(requestCode) {
            DELIVERY_LOCATION_REQUEST_CODE_FROM_SELECT_ALL,
            DELIVERY_LOCATION_REQUEST -> {
                if(resultCode == RESULT_OK) {
                    setDeliveryLocation()
                    viewModel.makeInventoryCalls()
                }
            }
        }

        if (requestCode == BottomNavigationActivity.PDP_REQUEST_CODE && resultCode == RESULT_OK) {
            val activity = activity ?: return
            val productCountMap = Utils.jsonStringToObject(
                data?.getStringExtra("ProductCountMap"), ProductCountMap::class.java
            ) as? ProductCountMap
            val itemsCount = data?.getIntExtra("ItemsCount", 0)
            when {
                ((getPreferredDeliveryType() == Delivery.DASH || getPreferredDeliveryType() == Delivery.CNC)
                        && productCountMap?.quantityLimit?.foodLayoutColour != null) -> {
                    showItemsLimitToastOnAddToCart(
                        bindingListDetails.rlCheckOut,
                        productCountMap,
                        activity,
                        itemsCount ?: 0,
                        true
                    )
                }
                else -> buildAddToCartSuccessToast(
                    bindingListDetails.rlCheckOut, true, activity, this
                )
            }
        }
    }

    private fun setDeliveryLocation() {
        val shoppingDeliveryLocation = Utils.getPreferredDeliveryLocation()
        if (shoppingDeliveryLocation?.fulfillmentDetails == null || !isAdded || !isVisible) return
        view?.apply {
            setDeliveryAddressView(
                activity,
                shoppingDeliveryLocation.fulfillmentDetails,
                bindingListDetails.tvDeliveryTitle,
                bindingListDetails.tvDeliverySubtitle,
                bindingListDetails.imgDelivery
            )
        }
    }

    private fun deliverySelectionIntent(resultCode: Int) {
        val activity = activity ?: return
        presentEditDeliveryGeoLocationActivity(
            activity, resultCode, getPreferredDeliveryType(), getPlaceId(),
            isFromDashTab = false,
            isComingFromCheckout = false,
            isComingFromSlotSelection = false,
            savedAddressResponse = null,
            defaultAddress = null,
            whoISCollecting = null,
            liquorCompliance = null
        )
    }

    private fun postAddItemToCart(addItemToCart: ArrayList<AddItemToCart>): Call<AddItemToCartResponse> {
        onAddToCartPreExecute()
        addedToCartFail(false)
        val postItemToCart = PostItemToCart()
        return postItemToCart.make(
            addItemToCart.toMutableList(),
            object : IResponseListener<AddItemToCartResponse> {
                override fun onSuccess(response: AddItemToCartResponse?) {
                    addedToCartFail(false)
                    if (!isAdded || !isVisible) return
                    when (response?.httpCode) {
                        HTTP_OK -> onAddToCartSuccess(
                            response,
                            getTotalItemQuantity(addItemToCart)
                        )
                        AppConstant.HTTP_EXPECTATION_FAILED_417 -> {                         // Preferred Delivery Location has been reset on server
                            // As such, we give the user the ability to set their location again
                            if (response.response != null) confirmDeliveryLocation()
                        }
                        HTTP_SESSION_TIMEOUT_440 -> {
                            response.response?.let { onSessionTokenExpired(it) }
                        }
                        AppConstant.HTTP_EXPECTATION_FAILED_502 -> {
                            if (response.response.code == AppConstant.RESPONSE_ERROR_CODE_1235) {
                                bindingListDetails.pbLoadingIndicator.visibility = GONE
                                showQuantityLimitErrror(
                                    requireActivity().supportFragmentManager,
                                    response.response.desc,
                                    "",
                                    context
                                )
                                enableAddToCartButton(VISIBLE)
                            }
                        }
                        else -> response?.response?.let {
                            otherHttpCode(it)
                        }
                    }
                }

                override fun onFailure(error: Throwable?) {
                    addedToCartFail(true)
                    if (!isAdded || !isVisible) return
                    bindingListDetails.pbLoadingIndicator.visibility = GONE
                    bindingListDetails.btnCheckOut.visibility = VISIBLE

                }
            })
    }

    fun addedToCartFail(addedToCart: Boolean) {
        this.addedToCart = addedToCart
    }

    fun addedToCart(): Boolean {
        return addedToCart
    }

    override fun onToastButtonClicked(jsonElement: JsonElement?) {
        //Nav stack change since not using Cart activity anymore
        // Pass back result to BottomNavigation activity.
        activity?.apply {
            setResult(BottomNavigationActivity.RESULT_OK_OPEN_CART_FROM_SHOPPING_DETAILS)
            finishActivity(ScreenManager.SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE)
            overridePendingTransition(0, 0)
        }
    }

    private fun shouldUserSetSuburb(): Boolean {
        val shoppingDeliveryLocation = Utils.getPreferredDeliveryLocation()
        return shoppingDeliveryLocation == null
    }

    override fun onConfirmLocation() {
        addItemsToCart()
    }

    override fun onSetNewLocation() {
        deliverySelectionIntent(DELIVERY_LOCATION_REQUEST)
    }

    fun getTotalItemQuantity(addItemToCart: ArrayList<AddItemToCart>): Int {
        var totalQuantity = 0
        for (item in addItemToCart) {
            totalQuantity += item.quantity
        }
        return totalQuantity
    }

    override fun onDestroy() {
        (activity as? BottomNavigationActivity)?.showToolbar()
        super.onDestroy()
    }

    companion object {
        const val QUANTITY_CHANGED_FROM_LIST = 2010
        const val ADD_TO_CART_SUCCESS_RESULT = 2000
        private const val DELIVERY_LOCATION_REQUEST_CODE_FROM_SELECT_ALL = 1222
        private const val DELIVERY_LOCATION_REQUEST = 2
        private const val TOOLBAR_SELECT_ALL: String = "SELECT ALL"
        private const val ARG_LIST_ID: String = "listId"
        private const val ARG_LIST_NAME: String = "listName"
        private const val ARG_OPEN_FROM_MY_LIST: String = "openFromMyList"
    }
}