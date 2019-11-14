package za.co.woolworths.financial.services.android.ui.fragments.shop.list

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.create_new_list.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.Companion.ADD_TO_SHOPPING_LIST_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.OrderDetailsActivity
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import za.co.woolworths.financial.services.android.util.*
import java.util.HashMap

class CreateShoppingListFragment : DepartmentExtensionFragment(), View.OnClickListener {

    private var mShoppingListGroup: HashMap<String, ShoppingList>? = null
    private var mAddToListRequest: MutableList<AddToListRequest>? = null
    private var mPostShoppingList: Call<ShoppingListItemsResponse>? = null
    private var mCreateShoppingList: Call<ShoppingListsResponse>? = null
    private var mShouldDisplayCreateListOnly: Boolean = false
    private var mOrderId: String? = null
    private var isOrderIdNullOrEmpty: Boolean = false
    private var mDialogErrorMessageDidAppear = false
    private var mAutoConnect: AutoConnect? = null
    private var mDisplayCloseIcon: Boolean = false

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
        private const val DISPLAY_CLOSE_ICON = "DISPLAY_CLOSE_ICON"

        fun newInstance(listOfIds: HashMap<String, ShoppingList>?, selectedListGroup: String?, shouldDisplayCreateList: Boolean, orderId: String?, closeIconVisibility: Boolean) = CreateShoppingListFragment().apply {
            arguments = Bundle(5).apply {
                putSerializable(SHOPPING_LIST_SELECTED_LIST_ID, listOfIds)
                putString(SHOPPING_LIST_SELECTED_GROUP, selectedListGroup)
                putBoolean(DISPLAY_CREATE_LIST_ONLY, shouldDisplayCreateList)
                putBoolean(DISPLAY_CLOSE_ICON, closeIconVisibility)
                putString(OrderDetailsActivity.ORDER_ID, orderId)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getBundleArguments()
        return if (mShouldDisplayCreateListOnly)
            inflater?.inflate(R.layout.create_list_from_shopping_list_view, container, false)
        else
            inflater?.inflate(R.layout.create_new_list, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarIconVisibility()
        clickListener()
        textChangeListener()
        // Disable create list button when tap from create a list row
        if (!mShouldDisplayCreateListOnly) enableCancelButton(true) else enableCancelButton(false)
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
        arguments?.apply {

            if (this.containsKey(SHOPPING_LIST_SELECTED_LIST_ID)) {
                mShoppingListGroup = this.getSerializable(SHOPPING_LIST_SELECTED_LIST_ID) as HashMap<String, ShoppingList>?
                        ?: hashMapOf()
            }

            if (this.containsKey(SHOPPING_LIST_SELECTED_GROUP)) {
                mAddToListRequest = convertStringToObject(this.getString(SHOPPING_LIST_SELECTED_GROUP))
            }

            if (this.containsKey(DISPLAY_CREATE_LIST_ONLY)) {
                mShouldDisplayCreateListOnly = this.getBoolean(DISPLAY_CREATE_LIST_ONLY, false)
            }

            if (this.containsKey(DISPLAY_CLOSE_ICON)) {
                mDisplayCloseIcon = this.getBoolean(DISPLAY_CLOSE_ICON, false)
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
        etNewList.setOnEditorActionListener { v, actionId, event ->
            if ((actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_DONE || event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                createListRequest()
            }
            true
        }
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
        } else {
            btnCancel.setTextColor(Color.BLACK)
        }

        //triggered when add to list is empty
        if (mDisplayCloseIcon) {
            imBack.visibility = GONE
            imCloseIcon.visibility = VISIBLE
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
                    hideKeyboard()
                    view.postDelayed({ (activity as? AddToShoppingListActivity)?.exitActivityAnimation() }, HIDE_KEYBOARD_DELAY_MILIS)
                }

                R.id.btnCancel -> createListRequest()

            }
        }
    }

    private fun createListRequest() {
        isOrderIdNullOrEmpty = mOrderId.isNullOrEmpty()
        shoppingListPostProgress(true)
        activity?.beginCreateListExecution()
    }

    private fun FragmentActivity.closeFragment(view: View) {
        view.postDelayed({ onBackPressed() }, HIDE_KEYBOARD_DELAY_MILIS)
    }

    private fun FragmentActivity.beginCreateListExecution() {
        shoppingListPostProgress(true)
        val listName = etNewList.text.toString()
        if (listName.isNotEmpty()) {
            showKeyboard(etNewList)
            activity?.let {
                if (networkConnectionAvailable(it)) {
                    createShoppingListRequest()
                } else {
                    ErrorHandlerView(it).showToast()
                }
            }
        } else {
            onBackPressed()
        }
    }

    private fun createShoppingListRequest() {
        val listName = etNewList?.text?.toString()
        val createListRequest = buildFirstRequest(listName)
        mCreateShoppingList = OneAppService.createList(createListRequest)
        mCreateShoppingList?.enqueue(CompletionHandler(object:RequestListener<ShoppingListsResponse>{
            override fun onSuccess(response: ShoppingListsResponse?) {
                response?.apply {
                    when (httpCode) {
                        200 -> {
                            // Add to List from MyListFragment
                            if (mShouldDisplayCreateListOnly) {
                                response.lists[0]?.apply {
                                    shoppingListPostProgress(false)
                                    ScreenManager.presentShoppingListDetailActivity(activity, listId, listName)
                                    activity?.apply {
                                        setResult(ADD_TO_SHOPPING_LIST_REQUEST_CODE)
                                        finish()
                                        overridePendingTransition(0, 0)
                                    }
                                }
                                return
                            }

                            // Add to list from other sections
                            val createdList = lists[0]
                            createdList.wasSentToServer = false
                            mShoppingListGroup?.put(createdList.listId, createdList)
                            //add new list to shopping list group
                            mShoppingListGroup?.forEach {
                                // Post product/commerceItem item to server, wasSentToServer -> false mean product item not send
                                val listId = it.key
                                val value = it.value
                                if (isOrderIdNullOrEmpty) {
                                    if (!value.wasSentToServer)
                                        addProductToShoppingList(listId)
                                } else {
                                    addOrderToShoppingList(mOrderId, value)
                                }
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

            override fun onFailure(error: Throwable?) {
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
        },ShoppingListsResponse::class.java))
    }

    private fun addProductToShoppingList(listId: String?) {
        // Update listId value from dto AddToListRequest
        mAddToListRequest?.forEach { item ->
            item.giftListId = listId
            item.listId = null // remove list id from request body
        }

        mPostShoppingList = listId?.let {listID -> mAddToListRequest?.let { listItem -> OneAppService.addToList(listItem, listID) } }

        mPostShoppingList?.enqueue(CompletionHandler(object:RequestListener<ShoppingListItemsResponse>{
            override fun onSuccess(shoppingListsResponse: ShoppingListItemsResponse?) {
                shoppingListsResponse?.apply {
                    when (httpCode) {
                        200 -> addToListWasSendSuccessfully(listId)
                        440 -> sessionExpiredHandler()
                        else -> response?.let { otherHttpCodeHandler(it) }
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                activity?.let {
                    it.runOnUiThread {
                        mAutoConnect = AutoConnect.ADD_PRODUCT_TO_LIST
                        shoppingListPostProgress(false)
                        displayNoConnectionToast()
                    }
                }
            }

        },ShoppingListItemsResponse::class.java))
    }

    private fun addToListWasSendSuccessfully(listId: String?) {
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

        orderId?.let {
             orderID ->
           val addOrderToListRequest =  OneAppService.addOrderToList(orderID, orderRequestList)
            addOrderToListRequest.enqueue(CompletionHandler(object:RequestListener<OrderToListReponse>{
                override fun onSuccess(orderToListReponse: OrderToListReponse?) {
                    orderToListReponse?.apply {
                        response.apply {
                            when (httpCode) {
                                0 -> addToListWasSendSuccessfully(orderRequestList.shoppingListId)
                                440 -> sessionExpiredHandler()
                                else -> response?.let { otherHttpCodeHandler(it) }
                            }
                        }
                    }
                }

                override fun onFailure(error: Throwable?) {
                    activity?.let { activity ->
                        activity.runOnUiThread {
                            if (!networkConnectionAvailable(activity)) {
                                mAutoConnect = AutoConnect.ADD_ORDER_TO_LIST
                                shoppingListPostProgress(false)
                                ErrorHandlerView(activity).showToast()
                            }
                        }
                    }
                }

            },OrderToListReponse::class.java))
        }
    }

    private fun otherHttpCodeHandler(response: Response) {
        shoppingListPostProgress(false)
        if (!mDialogErrorMessageDidAppear) {
            mDialogErrorMessageDidAppear = true
            shoppingListPostProgress(false)
            response.desc?.let { desc -> showErrorDialog(desc) }
        }
    }

    private fun sessionExpiredHandler() {
        SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
        activity?.let { ScreenManager.presentSSOSignin(it) }
        shoppingListPostProgress(false)
    }
}