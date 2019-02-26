package za.co.woolworths.financial.services.android.ui.fragments.shop.list

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.create_new_list.*
import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.CreateShoppingList
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddToShoppingList
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostOrderToShoppingList
import za.co.woolworths.financial.services.android.ui.activities.OrderDetailsActivity
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import za.co.woolworths.financial.services.android.util.*
import java.util.HashMap

class CreateShoppingListFragment : DepartmentExtensionFragment(), View.OnClickListener {

    private var mShoppingListGroup: HashMap<String, ShoppingList>? = null
    private var mAddToListRequest: MutableList<AddToListRequest>? = null
    private var mPostShoppingList: HttpAsyncTask<String, String, ShoppingListItemsResponse>? = null
    private var mCreateShoppingList: HttpAsyncTask<String, String, ShoppingListsResponse>? = null
    private var mShouldDisplayCreateListOnly: Boolean = false
    private var mOrderId: String? = null
    private var isOrderIdNullOrEmpty: Boolean = false
    private var mDialogErrorMessageDidAppear = false
    private var mAutoConnect: AutoConnect? = null

    enum class AutoConnect {
        CREATE_LIST,
        ADD_ORDER_TO_LIST,
        ADD_PRODUCT_TO_LIST;
    }

    companion object {
        private const val HIDE_KEYBOARD_DELAY_MILIS: Long = 400
        private const val SHOW_KEYBOARD_DELAY_MILIS: Long = 290
        private const val SHOPPING_LIST_SELECTED_LIST_ID = "SHOPPING_LIST_SELECTED_LIST_ID"
        private const val SHOPPING_LIST_SELECTED_GROUP = "SHOPPING_LIST_SELECTED_GROUP"
        private const val DISPLAY_CREATE_LIST_ONLY = "DISPLAY_CREATE_LIST_ONLY"


        fun newInstance(listOfIds: HashMap<String, ShoppingList>?, selectedListGroup: String?) = CreateShoppingListFragment().apply {
            arguments = Bundle(2).apply {
                putSerializable(SHOPPING_LIST_SELECTED_LIST_ID, listOfIds)
                putString(SHOPPING_LIST_SELECTED_GROUP, selectedListGroup)
            }
        }

        fun newInstance(listOfIds: HashMap<String, ShoppingList>?, selectedListGroup: String?, shouldDisplayCreateList: Boolean, orderId: String?) = CreateShoppingListFragment().apply {
            arguments = Bundle(4).apply {
                putSerializable(SHOPPING_LIST_SELECTED_LIST_ID, listOfIds)
                putString(SHOPPING_LIST_SELECTED_GROUP, selectedListGroup)
                putBoolean(DISPLAY_CREATE_LIST_ONLY, shouldDisplayCreateList)
                putString(OrderDetailsActivity.ORDER_ID, orderId)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getBundleArguments()
        return if (mShouldDisplayCreateListOnly)
            inflater!!.inflate(R.layout.create_list_from_shopping_list_view, container, false)
        else
            inflater!!.inflate(R.layout.create_new_list, container, false)

    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarIconVisibility()
        clickListener()
        textChangeListener()
        enableCancelButton(false)
        networkConnectivityStatus()
    }

    private fun networkConnectivityStatus() {
        activity?.let {
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(it, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    if (hasConnection) {
                        mAutoConnect?.let { connect ->
                            when (connect) {
                                AutoConnect.CREATE_LIST -> {
                                    btnCancel.isEnabled = true
                                    btnCancel.performClick()
                                }
                                AutoConnect.ADD_PRODUCT_TO_LIST -> {
                                    btnCancel.isEnabled = true
                                    btnCancel.performClick()
                                }

                                AutoConnect.ADD_ORDER_TO_LIST -> {
                                    btnCancel.isEnabled = true
                                    btnCancel.performClick()
                                }
                            }
                            mAutoConnect = null
                        }
                    }
                }
            })
        }
    }

    private fun getBundleArguments() {
        mAddToListRequest = mutableListOf()
        arguments.apply {

            if (this.containsKey(SHOPPING_LIST_SELECTED_LIST_ID)) {
                mShoppingListGroup = this.getSerializable(SHOPPING_LIST_SELECTED_LIST_ID) as HashMap<String, ShoppingList>?
            }

            if (this.containsKey(SHOPPING_LIST_SELECTED_GROUP)) {
                mAddToListRequest = convertStringToObject(this.getString(SHOPPING_LIST_SELECTED_GROUP))
            }

            if (this.containsKey(DISPLAY_CREATE_LIST_ONLY)) {
                mShouldDisplayCreateListOnly = this.getBoolean(DISPLAY_CREATE_LIST_ONLY, false)
            }

            if (this.containsKey(OrderDetailsActivity.ORDER_ID)) {
                mOrderId = this.getString(OrderDetailsActivity.ORDER_ID)
            }
        }
    }

    private fun clickListener() {
        imBack.setOnClickListener(this)
        imCloseIcon.setOnClickListener(this)
        btnCancel.setOnClickListener(this)
        etNewList.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if ((actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_DONE || event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                    activity?.beginCreateListExecution()
                }
                return true
            }
        })
    }

    private fun textChangeListener() {
        etNewList?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val isEditTextNotEmpty = etNewList.text.toString().trim { it <= ' ' }.isNotEmpty()
                if (!mShouldDisplayCreateListOnly) {
                    btnCancel.isEnabled = isEditTextNotEmpty
                    btnCancel.text = if (isEditTextNotEmpty) getString(R.string.ok) else getString(R.string.cancel)
                    btnCancel.setTextColor(Color.BLACK)
                } else {
                    enableCancelButton(isEditTextNotEmpty)
                }
                tvOnErrorLabel.visibility = GONE
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })
    }

    override fun onResume() {
        super.onResume()
        view?.postDelayed({ showKeyboard(etNewList) }, SHOW_KEYBOARD_DELAY_MILIS)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelRequest(mPostShoppingList)
        cancelRequest(mCreateShoppingList)
        hideKeyboard()
    }

    private fun toolbarIconVisibility() {
        val entryCount: Int? = getFragmentBackStackEntryCount()
        imBack.visibility = if (entryCount == 0) GONE else VISIBLE
        imCloseIcon.visibility = if (entryCount == 0) VISIBLE else GONE
        if (mShouldDisplayCreateListOnly) {
            imBack.visibility = VISIBLE
            imCloseIcon.visibility = GONE
        }
    }

    private fun getFragmentBackStackEntryCount() = activity?.supportFragmentManager?.backStackEntryCount

    override fun onClick(view: View?) {
        activity?.apply {
            when (view?.id) {
                R.id.imBack -> {
                    hideKeyboard()
                    closeFragment(view)
                }

                R.id.imCloseIcon -> {
                    closeFragment(view)
                }

                R.id.btnCancel -> {
                    isOrderIdNullOrEmpty = mOrderId.isNullOrEmpty()
                    shoppingListPostProgress(true)
                    activity?.beginCreateListExecution()
                }
            }
        }
    }

    private fun FragmentActivity.closeFragment(view: View) {
        view.postDelayed({ onBackPressed() }, HIDE_KEYBOARD_DELAY_MILIS)
    }

    private fun FragmentActivity.beginCreateListExecution() {
        shoppingListPostProgress(true)
        val listName = etNewList.text.toString()
        if (listName.isNotEmpty()) {
            showKeyboard(etNewList)
            if (networkConnectionAvailable(activity)) {
                createShoppingListRequest()
            } else {
                ErrorHandlerView(activity).showToast()
            }
        } else {
            onBackPressed()
        }
    }

    private fun createShoppingListRequest() {
        val listName = etNewList?.text?.toString()
        val createListRequest = buildFirstRequest(listName)
        mCreateShoppingList = CreateShoppingList(createListRequest, object : AsyncAPIResponse.ResponseDelegate<ShoppingListsResponse> {
            override fun onSuccess(response: ShoppingListsResponse) {
                response.apply {
                    when (httpCode) {
                        200 -> {
                            if (mShoppingListGroup?.size!! > 0 && !mShouldDisplayCreateListOnly) {
                                //add new list to shopping list group
                                val createdList = lists[0]
                                createdList.wasSentToServer = isOrderIdNullOrEmpty
                                mShoppingListGroup?.put(createdList.listId, createdList)

                                mShoppingListGroup?.forEach {
                                    val listId = it.key
                                    val shopList = it.value
                                    // Post item not sent to server only, false mean item not send yet
                                    if (!shopList.wasSentToServer && isOrderIdNullOrEmpty) {
                                        // Update listId value from dto AddToListRequest
                                        mAddToListRequest?.forEach { item -> item.listId = listId }
                                        addProductToShoppingList(mAddToListRequest, it.key)
                                    } else {
                                        addOrderToShoppingList(mOrderId, shopList)
                                    }
                                }
                            } else {
                                val createdList = lists[0]
                                createdList.wasSentToServer = isOrderIdNullOrEmpty
                                mShoppingListGroup?.put(createdList.listId, createdList)

                                // post order to list
                                if (!isOrderIdNullOrEmpty) {
                                    mShoppingListGroup?.forEach {
                                        val shopList = it.value
                                        // Post item not sent to server only, false mean item not send yet
                                        if (!shopList.wasSentToServer) {
                                            // Update listId value from dto AddToListRequest
                                            addOrderToShoppingList(mOrderId, shopList)
                                        }
                                    }
                                    return
                                }

                                shoppingListPostProgress(false)
                                showShoppingListSuccessToast()
                            }
                        }
                        440 -> {
                            shoppingListPostProgress(false)
                            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                            activity?.let { ScreenManager.presentSSOSignin(it) }
                        }
                        else -> {
                            shoppingListPostProgress(false)
                            tvOnErrorLabel.text = response.response?.desc ?: ""
                            tvOnErrorLabel.visibility = VISIBLE
                        }
                    }
                }
            }

            override fun onFailure(errorMessage: String) {
                activity?.let {
                    it.runOnUiThread {
                        if (!networkConnectionAvailable(it)) {
                            mAutoConnect = AutoConnect.CREATE_LIST
                            displayNoConnectionToast()
                        }
                        shoppingListPostProgress(false)
                    }
                }
            }
        }).execute() as HttpAsyncTask<String, String, ShoppingListsResponse>
    }

    private fun addProductToShoppingList(addToListRequest: MutableList<AddToListRequest>?, listId: String?) {
        mPostShoppingList = PostAddToShoppingList(listId, addToListRequest, object : AsyncAPIResponse.ResponseDelegate<ShoppingListItemsResponse> {
            override fun onSuccess(response: ShoppingListItemsResponse) {
                response.apply {
                    when (httpCode) {
                        200 -> {
                            mShoppingListGroup?.apply {
                                // Will replace the value of an existing key and will create it if doesn't exist
                                val shopList = get(listId)
                                shopList!!.wasSentToServer = true
                                listId?.let { put(it, shopList) }

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
                            shoppingListPostProgress(false)

                        }
                        else -> {
                            shoppingListPostProgress(false)
                        }
                    }
                }
            }

            override fun onFailure(errorMessage: String) {
                activity?.let {
                    it.runOnUiThread {
                        mAutoConnect = AutoConnect.ADD_PRODUCT_TO_LIST
                        shoppingListPostProgress(false)
                        displayNoConnectionToast()
                    }
                }
            }

        }).execute() as HttpAsyncTask<String, String, ShoppingListItemsResponse>
    }

    private fun displayNoConnectionToast() {
        activity?.let { ErrorHandlerView(it).showToast() }
    }

    private fun shoppingListPostProgress(state: Boolean) {
        enableCancelButton(!state)
        pbCreateList.visibility = if (state) VISIBLE else GONE
    }

    private fun enableCancelButton(isEditTextNotEmpty: Boolean) {
        btnCancel.isEnabled = isEditTextNotEmpty
        clBottomView.isEnabled = isEditTextNotEmpty
    }

    private fun buildFirstRequest(listName: String?): CreateList {
        // create list and post order to the new list
        return if (mOrderId.isNullOrEmpty()) CreateList(listName, mAddToListRequest) else CreateList(listName, mutableListOf())
    }

    private fun showShoppingListSuccessToast() {
        if (mShoppingListGroup?.size == 1) {
            activity?.let { NavigateToShoppingList.requestToastOnNavigateBack(it, AddToShoppingListFragment.POST_ADD_TO_SHOPPING_LIST, mShoppingListGroup) }
        } else {
            if (!mShouldDisplayCreateListOnly)
                activity?.let { NavigateToShoppingList.requestToastOnNavigateBack(it, AddToShoppingListFragment.POST_ADD_TO_SHOPPING_LIST, mShoppingListGroup) }
            else
                activity?.let { NavigateToShoppingList.displayBottomNavigationToast(it, AddToShoppingListFragment.POST_ADD_TO_SHOPPING_LIST, mShoppingListGroup) }
        }
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
                        if (!networkConnectionAvailable(it)) {
                            mAutoConnect = AutoConnect.ADD_ORDER_TO_LIST
                            shoppingListPostProgress(false)
                            ErrorHandlerView(it).showToast()
                        }
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
                    activity?.let { SessionExpiredUtilities.getInstance().showSessionExpireDialog(it as? AppCompatActivity) }
                    shoppingListPostProgress(false)
                }
                else -> errorDialogDisplay(response)
            }
        }
    }

    private fun errorDialogDisplay(response: Response?) {
        if (!mDialogErrorMessageDidAppear) {
            mDialogErrorMessageDidAppear = true
            shoppingListPostProgress(false)
            response?.desc?.let { showErrorDialog(it) }
        }
    }
}