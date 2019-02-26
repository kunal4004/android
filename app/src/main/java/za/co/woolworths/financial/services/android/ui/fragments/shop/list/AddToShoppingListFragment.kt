package za.co.woolworths.financial.services.android.ui.fragments.shop.list

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.add_to_list_content.*
import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.GetShoppingList
import za.co.woolworths.financial.services.android.ui.adapters.AddToShoppingListAdapter
import java.util.*
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddToShoppingList
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostOrderToShoppingList
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity
import za.co.woolworths.financial.services.android.ui.activities.OrderDetailsActivity.Companion.ORDER_ID
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import za.co.woolworths.financial.services.android.util.*

class AddToShoppingListFragment : DepartmentExtensionFragment(), View.OnClickListener {

    private var mAddToListArgs: String? = null
    private var mShoppingListGroup: HashMap<String, ShoppingList>? = null
    private var mOrderId: String? = null
    private lateinit var mPostItemList: MutableList<String>
    private var mAddToShoppingListAdapter: AddToShoppingListAdapter? = null
    private var mErrorDialogDidAppear: Boolean = false
    private var mReConnect: ReConnect? = null

    enum class ReConnect {
        ADD_ORDER_TO_LIST,
        ADD_PRODUCT_TO_LIST;
    }

    companion object {
        const val POST_ADD_TO_SHOPPING_LIST = "POST_ADD_TO_SHOPPING_LIST"
        fun newInstance(postListRequest: String?, order_id: String?) = AddToShoppingListFragment().apply {
            arguments = Bundle(2).apply {
                putString(POST_ADD_TO_SHOPPING_LIST, postListRequest)
                putString(ORDER_ID, order_id)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.add_to_list_content, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getBundleArguments()
        initAndConfigureUI()
        setListener()
        displayListItem()
        networkConnectivityStatus()
    }

    private fun initAndConfigureUI() {
        activity?.apply {
            rclAddToList.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
            mAddToShoppingListAdapter = AddToShoppingListAdapter(mutableListOf()) { shoppingListItemClicked() }
            rclAddToList.adapter = mAddToShoppingListAdapter
        }
    }

    private fun getBundleArguments() {
        arguments?.let {
            mAddToListArgs = arguments?.getString(POST_ADD_TO_SHOPPING_LIST)
            mOrderId = arguments?.getString(ORDER_ID)
        }
    }

    private fun setListener() {
        btnPostShoppingList.setOnClickListener(this)
        btnRetry.setOnClickListener(this)
        imCreateList.setOnClickListener(this)
    }

    private fun loadShoppingList(state: Boolean) {
        recyclerViewMaximumHeight(rclAddToList.layoutParams)
        relProgressBar.visibility = if (state) VISIBLE else GONE
        disableCreateListButton(state)
    }

    private fun noNetworkConnection(state: Boolean) {
        no_connection_layout.visibility = if (state) VISIBLE else GONE
        flCancelButton.visibility = if (state) GONE else VISIBLE
        disableCreateListButton(state)
    }

    private fun disableCreateListButton(state: Boolean) {
        imCreateList.alpha = if (state) 0.5f else 1.0f
        imCreateList.isEnabled = !state
    }

    private fun retrieveShoppingList() {
        loadShoppingList(true)
        GetShoppingList(object : AsyncAPIResponse.ResponseDelegate<ShoppingListsResponse> {
            override fun onSuccess(response: ShoppingListsResponse) {
                activity?.let {
                    response.apply {
                        when (httpCode) {
                            200 -> {
                                bindShoppingListToUI(lists)
                            }
                            440 -> {
                                loadShoppingList(false)
                                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                                ScreenManager.presentSSOSignin(it)
                            }
                            else -> {
                                loadShoppingList(false)
                                if (!mErrorDialogDidAppear) {
                                    mErrorDialogDidAppear = true
                                    response.response?.desc?.let { showErrorDialog(it) }
                                }
                            }
                        }
                        loadShoppingList(false)
                    }
                }
            }

            override fun onFailure(errorMessage: String) {
                activity?.let {
                    it.runOnUiThread {
                        loadShoppingList(false)
                        if (!networkConnectionAvailable(it)) {
                            noNetworkConnection(true)
                        }
                    }
                }
            }
        }).execute()
    }

    private fun networkConnectionAvailable(it: FragmentActivity) = NetworkManager.getInstance().isConnectedToNetwork(it)


    private fun bindShoppingListToUI(shoppingList: MutableList<ShoppingList>) {
        activity?.apply {
            // dynamic RecyclerView height
            setRecyclerViewHeight(shoppingList, rclAddToList.layoutParams)
            saveShoppingListInstance(shoppingList)
            mAddToShoppingListAdapter?.setShoppingList(shoppingList)
            mAddToShoppingListAdapter?.notifyDataSetChanged()
        }
    }

    private fun setRecyclerViewHeight(shoppingList: MutableList<ShoppingList>, viewGroupParams: ViewGroup.LayoutParams) {
        shoppingList.apply {
            when {
                size == 0 -> {
                    // pop up create list fragment
                    navigateToCreateShoppingListFragment(false)
                }
                size < 4 -> {
                    viewGroupParams.height = RecyclerView.LayoutParams.WRAP_CONTENT
                    rclAddToList.layoutParams = viewGroupParams
                }
                else -> {
                    recyclerViewMaximumHeight(viewGroupParams)
                }
            }
        }
    }

    private fun recyclerViewMaximumHeight(viewGroupParams: ViewGroup.LayoutParams) {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        viewGroupParams.height = 2 * displayMetrics.heightPixels / 5
        rclAddToList.layoutParams = viewGroupParams
    }

    private fun shoppingListItemClicked() {
        mAddToShoppingListAdapter?.notifyDataSetChanged()
        btnPostShoppingList.text = if (shoppingListItemWasSelected()) getString(R.string.ok) else getString(R.string.cancel)
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
                addOrderToShoppingList(mOrderId, shoppingList)
        }
    }

    private fun createProductShoppingList() {
        mPostItemList = mutableListOf()
        mShoppingListGroup = shoppingListSelectedItemGroup()
        val constructAddToListRequest = convertStringToObject(mAddToListArgs)
        mShoppingListGroup?.forEach { (key, valueWasPosted) ->
            constructAddToListRequest.forEach { it.listId = key }
            if (!valueWasPosted.wasSentToServer)
                addProductToShoppingList(constructAddToListRequest, key)
        }
    }

    private fun networkConnectivityStatus() {
        activity?.let {
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(it, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    if (hasConnection) {
                        mReConnect?.let { connect ->
                            when (connect) {
                                ReConnect.ADD_PRODUCT_TO_LIST -> {
                                    disableCreateListButton(false)
                                    btnPostShoppingList.performClick()
                                }

                                ReConnect.ADD_ORDER_TO_LIST -> {
                                    disableCreateListButton(false)
                                    btnPostShoppingList.performClick()
                                }
                            }
                            mReConnect = null
                        }
                    }
                }
            })
        }
    }

    private fun shoppingListPostProgress(state: Boolean) {
        btnPostShoppingList.isEnabled = !state
        rclAddToList.isEnabled = !state
        pbAddToList.visibility = if (state) VISIBLE else GONE
        imCreateList.isEnabled = !state
        imCreateList.alpha = if (state) 0.5f else 1.0f
    }

    private fun addProductToShoppingList(addToListRequest: MutableList<AddToListRequest>?, listId: String?) {
        shoppingListPostProgress(true)
        PostAddToShoppingList(listId, addToListRequest, object : AsyncAPIResponse.ResponseDelegate<ShoppingListItemsResponse> {
            override fun onSuccess(response: ShoppingListItemsResponse) {
                response.apply {
                    addProductResponseHandler(listId, response)
                }
            }

            override fun onFailure(errorMessage: String) {
                activity?.let {
                    it.runOnUiThread {
                        mReConnect = ReConnect.ADD_PRODUCT_TO_LIST
                        disableCreateListButton(true)
                        shoppingListPostProgress(false)
                        ErrorHandlerView(it).showToast()
                    }
                }
            }

        }).execute()
    }

    private fun addOrderToShoppingList(orderId: String?, shoppingList: ShoppingList) {
        shoppingListPostProgress(true)
        val orderRequestList = OrderToShoppingListRequestBody(shoppingList.listId, shoppingList.listName)
        PostOrderToShoppingList(orderId, orderRequestList, object : AsyncAPIResponse.ResponseDelegate<OrderToListReponse> {
            override fun onSuccess(response: OrderToListReponse) {
                response.apply {
                    addOrderResponseHandler(orderRequestList.shoppingListId, response)
                }
            }

            override fun onFailure(errorMessage: String) {
                activity?.let {
                    it.runOnUiThread {
                        mReConnect = ReConnect.ADD_ORDER_TO_LIST
                        disableCreateListButton(true)
                        shoppingListPostProgress(false)
                        ErrorHandlerView(it).showToast()
                    }
                }
            }
        }).execute()
    }

    private fun addOrderResponseHandler(listId: String, ordersResponse: OrderToListReponse) {
        ordersResponse.apply {
            when (httpCode) {
                0 -> {
                    mShoppingListGroup?.apply {
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
                440 -> {
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
                200 -> {
                    mShoppingListGroup?.apply {
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
                440 -> {
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

    private fun showShoppingListSuccessToast() {
        NavigateToShoppingList.requestToastOnNavigateBack(activity, POST_ADD_TO_SHOPPING_LIST, mShoppingListGroup)
    }

    private fun navigateToCreateShoppingListFragment(state: Boolean) {
        replaceFragment(
                fragment = CreateShoppingListFragment.newInstance(mShoppingListGroup, mAddToListArgs, state, mOrderId),
                tag = CreateShoppingListFragment::class.java.simpleName,
                containerViewId = R.id.flShoppingListContainer,
                allowStateLoss = false,
                enterAnimation = R.anim.enter_from_right,
                exitAnimation = R.anim.exit_to_left,
                popEnterAnimation = R.anim.enter_from_left,
                popExitAnimation = R.anim.exit_to_right)
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
            if (networkConnectionAvailable(activity)) retrieveShoppingList() else noNetworkConnection(true)
        } else {
            val cachedShoppingList = getLatestShoppingList()
            btnPostShoppingList.text = cachedShoppingList?.let { if (it.size > 0) getString(R.string.ok) else getString(R.string.cancel) }
            cachedShoppingList?.let { setRecyclerViewHeight(it, rclAddToList.layoutParams) }
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
}