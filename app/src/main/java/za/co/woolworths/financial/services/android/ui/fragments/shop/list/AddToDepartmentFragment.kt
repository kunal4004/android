package za.co.woolworths.financial.services.android.ui.fragments.shop.list

import android.content.Intent
import android.os.Bundle
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
import za.co.woolworths.financial.services.android.ui.activities.OrderDetailsActivity.Companion.ORDER_ID
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import za.co.woolworths.financial.services.android.util.*

class AddToDepartmentFragment : DepartmentExtensionFragment(), View.OnClickListener {

    private var mAddToListArgs: String? = null
    private var isRetrievingShoppingItem = false
    private var isPostingShoppingItem = false
    private var mShoppingListGroup: HashMap<String, ShoppingList>? = null
    private var mOrderId: String? = null
    private var isPostingToOrderShoppingList: Boolean? = null
    private lateinit var mPostItemList: MutableList<String>
    private var mAddToShoppingListAdapter: AddToShoppingListAdapter? = null
    private var mErrorDialogDidAppear: Boolean = false

    companion object {
        const val POST_ADD_TO_SHOPPING_LIST = "POST_ADD_TO_SHOPPING_LIST"
        fun newInstance(postListRequest: String?, order_id: String?) = AddToDepartmentFragment().apply {
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
        if (savedInstanceState == null) {
            getBundleArguments()
            initAndConfigureUI()
            setListener()
            getShoppingList()
            networkConnectivityStatus()
        }
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
        isRetrievingShoppingItem = state
        imCreateList.alpha = if (state) 0.5f else 1.0f
        imCreateList.isEnabled = !state
    }

    private fun noNetworkConnection(state: Boolean) {
        if (isRetrievingShoppingItem) {
            no_connection_layout.visibility = if (state) GONE else VISIBLE
            flCancelButton.visibility = if (state) VISIBLE else GONE
        }
    }

    private fun getShoppingList() {
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
                                if (mErrorDialogDidAppear) {
                                    mErrorDialogDidAppear = true
                                    showErrorDialog(this.response?.message!!)
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
                    }
                }
            }
        }).execute()
    }


    private fun bindShoppingListToUI(shoppingList: MutableList<ShoppingList>) {
        activity?.apply {
            // dynamic RecyclerView height
            val viewGroupParams = rclAddToList.layoutParams
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
            mAddToShoppingListAdapter?.setShoppingList(shoppingList)
            mAddToShoppingListAdapter?.notifyDataSetChanged()
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

    private fun shoppingListItemWasSelected(): Boolean = shoppingListSelectedItemGroup()?.size!! > 0

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnPostShoppingList -> {
                if (shoppingListItemWasSelected()) {
                    // contain selected item
                    if (!mOrderId.isNullOrEmpty()) {
                        createOrderShoppingListRequest()
                    } else {
                        createProductShoppingList()
                    }
                } else {
                    //TODO:: Implement elegant transition animation
                    activity?.finish()
                    activity?.overridePendingTransition(0, 0)
                }
            }
            R.id.btnRetry -> {
                if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                    no_connection_layout.visibility = GONE
                    flCancelButton.visibility = VISIBLE
                    getShoppingList()
                }
            }

            R.id.imCreateList -> {
                mShoppingListGroup = shoppingListSelectedItemGroup()
                navigateToCreateShoppingListFragment(false)
            }
        }
    }

    private fun createOrderShoppingListRequest() {
        mShoppingListGroup = shoppingListSelectedItemGroup()
        mShoppingListGroup?.forEach { (key, shoppingList) ->
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
                    noNetworkConnection(hasConnection)
                    retryPostAddToShoppingList()
                }
            })
        }
    }

    private fun retryPostAddToShoppingList() {
        if (isPostingShoppingItem) {
            createProductShoppingList()
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
        isPostingShoppingItem = true
        PostAddToShoppingList(listId, addToListRequest, object : AsyncAPIResponse.ResponseDelegate<ShoppingListItemsResponse> {
            override fun onSuccess(response: ShoppingListItemsResponse) {
                response.apply {
                    addProductResponseHandler(listId, response)
                    isPostingShoppingItem = false
                }
            }

            override fun onFailure(errorMessage: String) {
                shoppingListPostProgress(false)
                noNetworkConnection(false)
            }

        }).execute()
    }

    private fun addOrderToShoppingList(orderId: String?, shoppingList: ShoppingList) {
        isPostingToOrderShoppingList = true
        shoppingListPostProgress(true)
        val orderRequestList = OrderToShoppingListRequestBody(shoppingList.listId, shoppingList.listName)
        PostOrderToShoppingList(orderId, orderRequestList, object : AsyncAPIResponse.ResponseDelegate<OrderToListReponse> {
            override fun onSuccess(ordersResponse: OrderToListReponse) {
                ordersResponse.apply {
                    addOrderResponseHandler(orderRequestList.shoppingListId, ordersResponse)
                    isPostingToOrderShoppingList = false
                }
            }

            override fun onFailure(errorMessage: String) {
                activity?.apply {
                    runOnUiThread {
                        shoppingListPostProgress(false)
                        noNetworkConnection(false)
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
                enterAnimation = R.anim.stay,
                exitAnimation = R.anim.slide_down_anim,
                popEnterAnimation = R.anim.fade_in,
                popExitAnimation = R.anim.stay
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            SSOActivity.SSOActivityResult.SUCCESS.rawValue() -> {
                getShoppingList()
            }
            SSOActivity.SSOActivityResult.STATE_MISMATCH.rawValue() -> {
                activity?.onBackPressed()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }

    fun closeFragment() {
        activity?.onBackPressed()
    }
}