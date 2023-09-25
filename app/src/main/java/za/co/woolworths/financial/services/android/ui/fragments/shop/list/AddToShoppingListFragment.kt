package za.co.woolworths.financial.services.android.ui.fragments.shop.list

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AddToListContentBinding
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Event
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Device
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Session
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.User
import za.co.woolworths.financial.services.android.ui.adapters.AddToShoppingListAdapter
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.PrepareChangeAttributeRequestEvent
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.Properties
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.ViewModel.DyChangeAttributeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.ORDER_ID
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.BUNDLE_WISHLIST_EVENT_DATA
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper
import za.co.woolworths.financial.services.android.util.analytics.dto.AddToWishListFirebaseEventData
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Context

class AddToShoppingListFragment : DepartmentExtensionFragment(R.layout.add_to_list_content), View.OnClickListener {

    private lateinit var binding: AddToListContentBinding

    private var mAddToListArgs: String? = null
    private var mShoppingListGroup: HashMap<String, ShoppingList>? = null
    private var mOrderId: String? = null
    private lateinit var mPostItemList: MutableList<String>
    private var mAddToShoppingListAdapter: AddToShoppingListAdapter? = null
    private var mErrorDialogDidAppear: Boolean = false
    private var mAutoConnect: AutoConnect? = null
    private var mAddToWishListEventData: AddToWishListFirebaseEventData? = null
    private var dyServerId: String? = null
    private var dySessionId: String? = null
    private var config: NetworkConfig? = null
    private lateinit var dyReportEventViewModel: DyChangeAttributeViewModel

    enum class AutoConnect {
        ADD_ORDER_TO_LIST,
        ADD_PRODUCT_TO_LIST;
    }

    companion object {
        const val POST_ADD_TO_SHOPPING_LIST = "POST_ADD_TO_SHOPPING_LIST"
        fun newInstance(postListRequest: String?, order_id: String?, addToWishListEventData: AddToWishListFirebaseEventData? = null) = AddToShoppingListFragment().apply {
            arguments = Bundle(2).apply {
                putString(POST_ADD_TO_SHOPPING_LIST, postListRequest)
                putString(ORDER_ID, order_id)
                putParcelable(BUNDLE_WISHLIST_EVENT_DATA, addToWishListEventData)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AddToListContentBinding.bind(view)

        getBundleArguments()
        initAndConfigureUI()
        setListener()
        displayListItem()
        networkConnectivityStatus()
        dyReportEventViewModel()
    }

    override fun noConnectionLayout(isVisible: Boolean) {
        binding.noConnectionLayout?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun initAndConfigureUI() {
        activity?.apply {
            binding.rclAddToList?.layoutManager = LinearLayoutManager(this)
            mAddToShoppingListAdapter = AddToShoppingListAdapter(mutableListOf()) { shoppingListItemClicked() }
            binding.rclAddToList?.adapter = mAddToShoppingListAdapter
        }
    }

    private fun getBundleArguments() {
        arguments?.let {
            mAddToListArgs = arguments?.getString(POST_ADD_TO_SHOPPING_LIST)
            mOrderId = arguments?.getString(ORDER_ID)
            mAddToWishListEventData = it.getParcelable(BUNDLE_WISHLIST_EVENT_DATA)
        }
    }

    private fun setListener() {
        binding.btnPostShoppingList.setOnClickListener(this)
        binding.btnRetry.setOnClickListener(this)
        binding.imCreateList.setOnClickListener(this)
    }

    private fun loadShoppingList(state: Boolean) {
        recyclerViewMaximumHeight(binding.rclAddToList.layoutParams)
        binding.relProgressBar.visibility = if (state) VISIBLE else GONE
        disableCreateListButton(state)
    }

    private fun noNetworkConnection(state: Boolean) {
        binding.noConnectionLayout.visibility = if (state) VISIBLE else GONE
        binding.flCancelButton.visibility = if (state) GONE else VISIBLE
        disableCreateListButton(state)
    }

    private fun disableCreateListButton(state: Boolean) {
        binding.imCreateList.alpha = if (state) 0.5f else 1.0f
        binding.imCreateList.isEnabled = !state
    }

    private fun retrieveShoppingList() {
        loadShoppingList(true)
        val shoppingListRequest = OneAppService().getShoppingLists()
        shoppingListRequest.enqueue(CompletionHandler(object : IResponseListener<ShoppingListsResponse> {
            override fun onSuccess(shoppingListResponse: ShoppingListsResponse?) {
                activity?.let {
                    shoppingListResponse?.apply {
                        when (httpCode) {

                            AppConstant.HTTP_OK -> bindShoppingListToUI(lists)
                            AppConstant.HTTP_SESSION_TIMEOUT_440 -> {
                                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                                ScreenManager.presentSSOSignin(it)
                            }
                            else -> {
                                if (!mErrorDialogDidAppear) {
                                    mErrorDialogDidAppear = true
                                    response?.desc?.let { showErrorDialog(it) }
                                }
                            }
                        }
                        loadShoppingList(false)
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                activity?.let {
                    it.runOnUiThread {
                        loadShoppingList(false)
                        if (!networkConnectionAvailable(it)) {
                            noNetworkConnection(true)
                        }
                    }
                }
            }

        },ShoppingListsResponse::class.java))
    }

    private fun bindShoppingListToUI(shoppingList: MutableList<ShoppingList>) {
        activity?.apply {
            // dynamic RecyclerView height
            setRecyclerViewHeight(shoppingList, binding.rclAddToList.layoutParams)
            saveShoppingListInstance(shoppingList)
            mAddToShoppingListAdapter?.setShoppingList(shoppingList)
            mAddToShoppingListAdapter?.notifyDataSetChanged()
        }
    }

    private fun setRecyclerViewHeight(shoppingList: MutableList<ShoppingList>, viewGroupParams: ViewGroup.LayoutParams) {
        shoppingList.let {
            when (it.size) {
                0 -> navigateToCreateShoppingListFragment(false, this)   // pop up create list fragment
                else -> recyclerViewMaximumHeight(viewGroupParams)
            }
        }
    }

    private fun recyclerViewMaximumHeight(viewGroupParams: ViewGroup.LayoutParams) {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        viewGroupParams.height = 2 * displayMetrics.heightPixels / 5
        binding.rclAddToList.layoutParams = viewGroupParams
    }

    private fun shoppingListItemClicked() {
        mAddToShoppingListAdapter?.notifyDataSetChanged()
        binding.btnPostShoppingList.text = if (shoppingListItemWasSelected()) getString(R.string.ok) else getString(R.string.cancel)
    }

    private fun shoppingListSelectedItemGroup(): HashMap<String, ShoppingList>? {
        val hmSelectedShoppingList = HashMap<String, ShoppingList>()
        mAddToShoppingListAdapter?.getShoppingList().apply {
            this?.forEach {
                if (it.shoppingListRowWasSelected) {
                    hmSelectedShoppingList[it.listId] = it
                }
            }
        }
        return hmSelectedShoppingList
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnPostShoppingList -> {
                mAddToShoppingListAdapter?.getShoppingList()?.let { saveShoppingListInstance(it) }
                if (shoppingListItemWasSelected()) {
                    // contain selected item
                    if (!mOrderId.isNullOrEmpty()) {
                        createOrderShoppingListRequest()
                    } else {
                        createProductShoppingList()
                    }
                } else {
                    activity?.onBackPressed()
                }
            }
            R.id.btnRetry -> {
                if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                    noNetworkConnection(false)
                    displayListItem()
                }
            }

            R.id.imCreateList -> {
                mAddToShoppingListAdapter?.getShoppingList()?.let { saveShoppingListInstance(it) }
                mShoppingListGroup = shoppingListSelectedItemGroup()
                navigateToCreateShoppingListFragment(false)
            }
        }
    }

    private fun createOrderShoppingListRequest() {
        mShoppingListGroup = shoppingListSelectedItemGroup()
        mShoppingListGroup?.forEach { (_, shoppingList) ->
            if (!shoppingList.wasSentToServer)
                mOrderId?.let { addOrderToShoppingList(it, shoppingList) }
        }
    }

    private fun createProductShoppingList() {
        var size: String? = null
        var skuID: String? = null
        mPostItemList = mutableListOf()
        mShoppingListGroup = shoppingListSelectedItemGroup()
        val constructAddToListRequest = convertStringToObject(mAddToListArgs)
        mShoppingListGroup?.forEach { (listId,valueWasPosted) ->
            constructAddToListRequest.forEach {
                it.giftListId = listId
                it.listId = null // remove list id from request body
                size = it.size
                skuID = it.skuID
            }
            if (!valueWasPosted.wasSentToServer) {
                addProductToShoppingList(constructAddToListRequest, listId)
            }
        }
        AppConfigSingleton.dynamicYieldConfig?.apply {
            if (isDynamicYieldEnabled == true)
                prepareDyAddToWishListRequestEvent(skuID, size)
        }
    }

    private fun prepareDyAddToWishListRequestEvent(skuID: String?, size: String?) {
        config = NetworkConfig(AppContextProviderImpl())
        if (Utils.getSessionDaoDyServerId(SessionDao.KEY.DY_SERVER_ID) != null)
            dyServerId = Utils.getSessionDaoDyServerId(SessionDao.KEY.DY_SERVER_ID)
        if (Utils.getSessionDaoDySessionId(SessionDao.KEY.DY_SESSION_ID) != null)
            dySessionId = Utils.getSessionDaoDySessionId(SessionDao.KEY.DY_SESSION_ID)
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(Utils.IPAddress,config?.getDeviceModel())
        val context = Context(device,null, Utils.DY_CHANNEL)
        val properties = Properties(null,null,
            Utils.ADD_TO_WISH_LIST_DY_TYPE,null,null,null,null,skuID,null,null,null,size,null,null,null,null,null,null)
        val eventsDyChangeAttribute = Event(null,null,null,null,null,null,null,null,null,null,null,null,
            Utils.ADD_TO_WISH_LIST_EVENT_NAME,properties)
        val events = ArrayList<Event>()
        events.add(eventsDyChangeAttribute);
        val prepareAddToWishListRequestEvent = PrepareChangeAttributeRequestEvent(
            context,
            events,
            session,
            user
        )
        dyReportEventViewModel.createDyChangeAttributeRequest(prepareAddToWishListRequestEvent)
    }

    private fun networkConnectivityStatus() {
        activity?.let {
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(it, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    if (hasConnection) {
                        mAutoConnect?.let { connect ->
                            when (connect) {
                                AutoConnect.ADD_PRODUCT_TO_LIST -> {
                                    disableCreateListButton(false)
                                    binding.btnPostShoppingList.performClick()
                                }

                                AutoConnect.ADD_ORDER_TO_LIST -> {
                                    disableCreateListButton(false)
                                    binding.btnPostShoppingList.performClick()
                                }
                            }
                            mAutoConnect = null
                        }
                    }
                }
            })
        }
    }

    private fun shoppingListPostProgress(state: Boolean) {
        binding.apply {
            btnPostShoppingList.isEnabled = !state
            rclAddToList.isEnabled = !state
            pbAddToList.visibility = if (state) VISIBLE else GONE
            imCreateList.isEnabled = !state
            imCreateList.alpha = if (state) 0.5f else 1.0f
        }
    }

    private fun addProductToShoppingList(addToListRequest: MutableList<AddToListRequest>, listId: String) {
        shoppingListPostProgress(true)
       val shoppingListResponseCall =  OneAppService().addToList(addToListRequest, listId)
        shoppingListResponseCall.enqueue(CompletionHandler(object : IResponseListener<ShoppingListItemsResponse> {
            override fun onSuccess(shoppingListResponse: ShoppingListItemsResponse?) {
                shoppingListResponse?.apply {
                    addProductResponseHandler(listId, this)
                }
            }

            override fun onFailure(error: Throwable?) {
                activity?.let {
                    it.runOnUiThread {
                        mAutoConnect = AutoConnect.ADD_PRODUCT_TO_LIST
                        disableCreateListButton(true)
                        shoppingListPostProgress(false)
                        ErrorHandlerView(it).showToast()
                    }
                }
            }

        },ShoppingListItemsResponse::class.java))
    }

    private fun addOrderToShoppingList(orderId: String, shoppingList: ShoppingList) {
        shoppingListPostProgress(true)
        val orderRequestList = OrderToShoppingListRequestBody(shoppingList.listId, shoppingList.listName)
        val orderRequest =  OneAppService().addOrderToList(orderId,orderRequestList)
        orderRequest.enqueue(CompletionHandler(object: IResponseListener<OrderToListReponse> {
            override fun onSuccess(orderDetailsResponse: OrderToListReponse?) {
                orderDetailsResponse?.apply {
                    addOrderResponseHandler(orderRequestList.shoppingListId, this)
                }
            }

            override fun onFailure(error: Throwable?) {
                activity?.let {
                    it.runOnUiThread {
                        mAutoConnect = AutoConnect.ADD_ORDER_TO_LIST
                        disableCreateListButton(true)
                        shoppingListPostProgress(false)
                        ErrorHandlerView(it).showToast()
                    }
                }
            }
        },OrderToListReponse::class.java))
    }

    private fun addOrderResponseHandler(listId: String, ordersResponse: OrderToListReponse) {
        ordersResponse.apply {
            when (httpCode) {
                0 -> {
                    mShoppingListGroup?.apply {
                        callAddToWishlistFirebaseEvent(listId)
                        // Will replace the value of an existing key and will create it if doesn't exist
                        val shopList = get(listId)
                        shopList!!.wasSentToServer = true
                        put(listId, shopList)
                        //Check all values are true, implying that all request was sent
                        var allShoppingListPostExecuteCompleted = true
                        values.forEach {
                            if (!it.wasSentToServer) {
                                allShoppingListPostExecuteCompleted = false
                            }
                        }

                        if (allShoppingListPostExecuteCompleted) {
                            shoppingListPostProgress(false)
                            showShoppingListSuccessToast()
                        }
                    }
                }
                AppConstant.HTTP_SESSION_TIMEOUT_440 -> {
                    SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                    SessionExpiredUtilities.getInstance().showSessionExpireDialog(activity as? AppCompatActivity)
                    shoppingListPostProgress(false)
                }
                else -> {
                    shoppingListPostProgress(false)
                    if (mErrorDialogDidAppear) {
                        mErrorDialogDidAppear = true
                        showErrorDialog(this.response?.message!!)
                    }
                }
            }
        }
    }

    fun addProductResponseHandler(listId: String?, shoppingResponse: ShoppingListItemsResponse) {
        shoppingResponse.apply {
            when (httpCode) {
                AppConstant.HTTP_OK -> {
                    mShoppingListGroup?.apply {
                        callAddToWishlistFirebaseEvent(listId)
                        // Will replace the value of an existing key and will create it if doesn't exist
                        val shopList = get(listId)
                        shopList!!.wasSentToServer = true
                        put(shopList.listId, shopList)

                        //Check all values are true, implying that all request was sent
                        var allRequestPostToServer = true
                        values.forEach {
                            if (!it.wasSentToServer) {
                                allRequestPostToServer = false
                            }
                        }

                        if (allRequestPostToServer) {
                            shoppingListPostProgress(false)
                            showShoppingListSuccessToast()
                        }
                    }
                }
                AppConstant.HTTP_SESSION_TIMEOUT_440 -> {
                    SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                    SessionExpiredUtilities.getInstance().showSessionExpireDialog(activity as? AppCompatActivity)
                    shoppingListPostProgress(false)
                }
                else -> {
                    shoppingListPostProgress(false)
                    if (mErrorDialogDidAppear) {
                        mErrorDialogDidAppear = true
                        showErrorDialog(this.response?.message!!)
                    }
                }
            }
        }
    }

    private fun callAddToWishlistFirebaseEvent(shoppingListId: String?) {
        if (shoppingListId.isNullOrEmpty()){
            return
        }
        mAddToWishListEventData?.let { eventData ->
            mShoppingListGroup?.let { shoppingListGroup ->
                val shoppingListName = shoppingListGroup[shoppingListId]?.listName
                if (!shoppingListName.isNullOrEmpty()){
                    eventData.shoppingListName = shoppingListName
                    FirebaseAnalyticsEventHelper.addToWishlistEvent(eventData)
                }
            }
        }
    }

    private fun showShoppingListSuccessToast() {
        NavigateToShoppingList.requestToastOnNavigateBack(activity, POST_ADD_TO_SHOPPING_LIST, mShoppingListGroup)
    }

    private fun navigateToCreateShoppingListFragment(state: Boolean) {
        replaceFragment(
                fragment = CreateShoppingListFragment.newInstance(mShoppingListGroup, mAddToListArgs, state, mOrderId, addToWishListEventData = mAddToWishListEventData),
                tag = CreateShoppingListFragment::class.java.simpleName,
                containerViewId = R.id.flShoppingListContainer,
                allowStateLoss = false,
                enterAnimation = R.anim.enter_from_right,
                exitAnimation = R.anim.exit_to_left,
                popEnterAnimation = R.anim.enter_from_left,
                popExitAnimation = R.anim.exit_to_right)
    }

    private fun navigateToCreateShoppingListFragment(state: Boolean, removeFragment: Fragment) {
        // remove current fragment from fragment stack before popping next up
        activity?.let {
            it.supportFragmentManager.apply {
                beginTransaction()
                        .remove(removeFragment)
                        .commitAllowingStateLoss()
                popBackStack()
            }

            (it as? AppCompatActivity)?.addFragment(
                    fragment = CreateShoppingListFragment.newInstance(mShoppingListGroup, mAddToListArgs, state, mOrderId, addToWishListEventData = mAddToWishListEventData),
                    tag = CreateShoppingListFragment::class.java.simpleName,
                    containerViewId = R.id.flShoppingListContainer,
                    allowStateLoss = false,
                    enterAnimation = R.anim.enter_from_right,
                    exitAnimation = R.anim.exit_to_left,
                    popEnterAnimation = R.anim.enter_from_left,
                    popExitAnimation = R.anim.exit_to_right)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            SSOActivity.SSOActivityResult.SUCCESS.rawValue() -> {
                displayListItem()
            }
            SSOActivity.SSOActivityResult.STATE_MISMATCH.rawValue() -> {
                activity?.onBackPressed()
            }
        }
    }

    private fun displayListItem() {
        if (getLatestShoppingList() == null) {
            activity?.let { if (networkConnectionAvailable(it)) retrieveShoppingList() else noNetworkConnection(true) }
        } else {
            val cachedShoppingList = getLatestShoppingList()
            var atLeastOneShoppingListItemSelected = false
            cachedShoppingList?.forEach {
                if (it.shoppingListRowWasSelected)
                    atLeastOneShoppingListItemSelected = true
            }
            binding.btnPostShoppingList.text = cachedShoppingList?.let { if (atLeastOneShoppingListItemSelected) getString(R.string.ok) else getString(R.string.cancel) }
            cachedShoppingList?.let { setRecyclerViewHeight(it, binding.rclAddToList.layoutParams) }
            cachedShoppingList?.let { mAddToShoppingListAdapter?.setShoppingList(it) }
            mAddToShoppingListAdapter?.notifyDataSetChanged()
        }
    }

    //Recreate list with selected option on fragment back navigation
    private fun saveShoppingListInstance(shoppingList: MutableList<ShoppingList>) {
        if (activity is AddToShoppingListActivity)
            (activity as? AddToShoppingListActivity)?.setLatestShoppingList(shoppingList)
    }

    private fun getLatestShoppingList(): MutableList<ShoppingList>? = (activity as? AddToShoppingListActivity)?.getLatestShoppingList()

    private fun shoppingListItemWasSelected(): Boolean = shoppingListSelectedItemGroup()?.size!! > 0

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }

    fun closeFragment() {
        activity?.onBackPressed()
    }

    private fun dyReportEventViewModel() {
        dyReportEventViewModel = ViewModelProvider(this).get(DyChangeAttributeViewModel::class.java)
    }
}