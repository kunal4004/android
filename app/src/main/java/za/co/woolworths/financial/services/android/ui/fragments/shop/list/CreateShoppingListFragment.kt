package za.co.woolworths.financial.services.android.ui.fragments.shop.list

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.KeyListener
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CreateListFromShoppingListViewBinding
import com.awfs.coordination.databinding.CreateNewListBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
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
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.Companion.ADD_TO_SHOPPING_LIST_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Device
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Session
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.User
import za.co.woolworths.financial.services.android.ui.adapters.DepartmentAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.PrepareChangeAttributeRequestEvent
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.Properties
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.ViewModel.DyChangeAttributeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.ShopFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_SESSION_TIMEOUT_440
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.BUNDLE_WISHLIST_EVENT_DATA
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper
import za.co.woolworths.financial.services.android.util.analytics.dto.AddToWishListFirebaseEventData

@AndroidEntryPoint
class CreateShoppingListFragment : Fragment(), View.OnClickListener {

    private lateinit var bindingCreateListFromShoppingView: CreateListFromShoppingListViewBinding
    private lateinit var bindingCreateList: CreateNewListBinding

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
    private var mKeyListener: KeyListener? = null
    private var mAddToWishListEventData: AddToWishListFirebaseEventData? = null
    private var dyServerId: String? = null
    private var dySessionId: String? = null
    private var config: NetworkConfig? = null
    private lateinit var dyReportEventViewModel: DyChangeAttributeViewModel

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
                putString(AppConstant.ORDER_ID, orderId)
            }
        }

        fun newInstance(listOfIds: HashMap<String, ShoppingList>?, selectedListGroup: String?, shouldDisplayCreateList: Boolean, orderId: String?, addToWishListEventData: AddToWishListFirebaseEventData? = null) = CreateShoppingListFragment().apply {
            arguments = Bundle(4).apply {
                putSerializable(SHOPPING_LIST_SELECTED_LIST_ID, listOfIds)
                putString(SHOPPING_LIST_SELECTED_GROUP, selectedListGroup)
                putBoolean(DISPLAY_CREATE_LIST_ONLY, shouldDisplayCreateList)
                putString(AppConstant.ORDER_ID, orderId)
                putParcelable(BUNDLE_WISHLIST_EVENT_DATA, addToWishListEventData)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getBundleArguments()
        return if (mShouldDisplayCreateListOnly) {
            bindingCreateListFromShoppingView = CreateListFromShoppingListViewBinding.inflate(inflater, container, false)
            return bindingCreateListFromShoppingView.root
        } else {
            bindingCreateList = CreateNewListBinding.inflate(inflater, container, false)
            return bindingCreateList.root
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarIconVisibility()
        clickListener()
        textChangeListener()

        mKeyListener = if (mShouldDisplayCreateListOnly) {
            bindingCreateListFromShoppingView.etNewList?.keyListener
        } else {
            bindingCreateList.etNewList?.keyListener
        }

        // Disable create list button when tap from create a list row
        if (!mShouldDisplayCreateListOnly) enableCancelButton(true) else enableCancelButton(false)
        networkConnectivityStatus()
        dyReportEventViewModel()
    }

    private fun networkConnectivityStatus() {
        activity?.let {
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(it, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    if (hasConnection) {
                        mAutoConnect?.let { connect ->
                            when (connect) {
                                AutoConnect.CREATE_LIST -> {
                                    (if (mShouldDisplayCreateListOnly) {
                                        bindingCreateListFromShoppingView.btnCancel
                                    } else {
                                        bindingCreateList.btnCancel
                                    }).apply {
                                        isEnabled = true
                                        performClick()
                                    }
                                }
                                AutoConnect.ADD_PRODUCT_TO_LIST -> {
                                    (if (mShouldDisplayCreateListOnly) {
                                        bindingCreateListFromShoppingView.btnCancel
                                    } else {
                                        bindingCreateList.btnCancel
                                    }).apply {
                                        isEnabled = true
                                        performClick()
                                    }
                                }

                                AutoConnect.ADD_ORDER_TO_LIST -> {
                                    (if (mShouldDisplayCreateListOnly) {
                                        bindingCreateListFromShoppingView.btnCancel
                                    } else {
                                        bindingCreateList.btnCancel
                                    }).apply {
                                        isEnabled = true
                                        performClick()
                                    }
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
            if (this.containsKey(AppConstant.ORDER_ID)) {
                mOrderId = this.getString(AppConstant.ORDER_ID)
            }

            mAddToWishListEventData = getParcelable(BUNDLE_WISHLIST_EVENT_DATA)
        }
    }

    private fun clickListener() {
        if (mShouldDisplayCreateListOnly) {
            bindingCreateListFromShoppingView.apply {
                imBack?.setOnClickListener(this@CreateShoppingListFragment)
                imCloseIcon?.setOnClickListener(this@CreateShoppingListFragment)
                btnCancel?.setOnClickListener(this@CreateShoppingListFragment)
                etNewList?.setOnEditorActionListener { _, actionId, event ->
                    if ((actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_DONE || event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                        createListRequest()
                    }
                    true
                }
            }
        } else {
            bindingCreateList.apply {
                imBack?.setOnClickListener(this@CreateShoppingListFragment)
                imCloseIcon?.setOnClickListener(this@CreateShoppingListFragment)
                btnCancel?.setOnClickListener(this@CreateShoppingListFragment)
                etNewList?.setOnEditorActionListener { _, actionId, event ->
                    if ((actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_DONE || event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                        createListRequest()
                    }
                    true
                }
            }
        }
    }

    private fun textChangeListener() {
        (if (mShouldDisplayCreateListOnly) {
            bindingCreateListFromShoppingView.etNewList
        } else {
            bindingCreateList.etNewList
        })?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val isEditTextNotEmpty = (if (mShouldDisplayCreateListOnly) {
                    bindingCreateListFromShoppingView.etNewList
                } else {
                    bindingCreateList.etNewList
                }).text.toString().trim { it <= ' ' }.isNotEmpty()
                if (!mShouldDisplayCreateListOnly) {
                    bindingCreateList.clBottomView?.isEnabled = true;
                    bindingCreateList.btnCancel.isEnabled = true;
                    if (isEditTextNotEmpty) {
                        bindingCreateList.clBottomView.background = bindDrawable(R.drawable.black_button_drawable_state)
                        bindingCreateList.btnCancel?.text = getString(R.string.create_list_and_add)
                        bindingCreateList.btnCancel.setTextColor(Color.WHITE)
                    } else {
                        bindingCreateList.clBottomView.setBackgroundColor(Color.TRANSPARENT)
                        bindingCreateList.btnCancel.setTextColor(Color.BLACK)
                        bindingCreateList.btnCancel?.text = getString(R.string.cancel)
                    }
                    bindingCreateList.tvOnErrorLabel?.visibility = GONE
                } else {
                    enableCancelButton(isEditTextNotEmpty)
                    bindingCreateListFromShoppingView.tvOnErrorLabel?.visibility = GONE
                }
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })
    }

    override fun onResume() {
        super.onResume()
        view?.postDelayed({ showKeyboard((if (mShouldDisplayCreateListOnly) {
            bindingCreateListFromShoppingView.etNewList
        } else {
            bindingCreateList.etNewList
        })) }, SHOW_KEYBOARD_DELAY_MILIS)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelRequest(mPostShoppingList)
        cancelRequest(mCreateShoppingList)
        hideKeyboard()
    }

    private fun toolbarIconVisibility() {
        val entryCount: Int? = getFragmentBackStackEntryCount()

        (if (mShouldDisplayCreateListOnly) {
            bindingCreateListFromShoppingView.apply {
                imBack.visibility = if (entryCount == 0) GONE else VISIBLE
                imCloseIcon.visibility = if (entryCount == 0) VISIBLE else GONE

                if (mShouldDisplayCreateListOnly) {
                    imBack.visibility = VISIBLE
                    imCloseIcon.visibility = GONE
                }

                //triggered when add to list is empty
                if (mDisplayCloseIcon) {
                    imBack.visibility = GONE
                    imCloseIcon.visibility = VISIBLE
                }
            }
        } else {
            bindingCreateList.apply {
                imBack.visibility = if (entryCount == 0) GONE else VISIBLE
                imCloseIcon.visibility = if (entryCount == 0) VISIBLE else GONE

                if (mShouldDisplayCreateListOnly) {
                    imBack.visibility = VISIBLE
                    imCloseIcon.visibility = GONE
                }

                //triggered when add to list is empty
                if (mDisplayCloseIcon) {
                    imBack.visibility = GONE
                    imCloseIcon.visibility = VISIBLE
                }
            }
        })
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
        val etNewList = (if (mShouldDisplayCreateListOnly) {
            bindingCreateListFromShoppingView.etNewList
        } else {
            bindingCreateList.etNewList
        })
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
        val etNewList = (if (mShouldDisplayCreateListOnly) {
            bindingCreateListFromShoppingView.etNewList
        } else {
            bindingCreateList.etNewList
        })
        val listName = etNewList?.text?.toString()
        val createListRequest = buildFirstRequest(listName)
        etNewList?.keyListener = null
        mCreateShoppingList = OneAppService().createList(createListRequest)
        mCreateShoppingList?.enqueue(CompletionHandler(object : IResponseListener<ShoppingListsResponse> {
            override fun onSuccess(response: ShoppingListsResponse?) {
                etNewList?.keyListener = mKeyListener
                response?.apply {
                    when (httpCode) {
                        HTTP_OK -> {
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
                        HTTP_SESSION_TIMEOUT_440 -> {
                            shoppingListPostProgress(false)
                            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                            activity?.let { ScreenManager.presentSSOSignin(it) }
                        }
                        else -> {
                            shoppingListPostProgress(false)
                            if (mShouldDisplayCreateListOnly) {
                                bindingCreateListFromShoppingView.apply {
                                    tvOnErrorLabel.text = response.response?.desc ?: ""
                                    tvOnErrorLabel.visibility = VISIBLE
                                }
                            } else {
                                bindingCreateList.apply {
                                    tvOnErrorLabel.text = response.response?.desc ?: ""
                                    tvOnErrorLabel.visibility = VISIBLE
                                }
                            }
                        }

                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                activity?.let {
                    it.runOnUiThread {
                        etNewList?.keyListener = mKeyListener
                        if (!networkConnectionAvailable(it)) {
                            mAutoConnect = AutoConnect.CREATE_LIST
                            displayNoConnectionToast()
                        }
                        shoppingListPostProgress(false)
                    }
                }
            }
        }, ShoppingListsResponse::class.java))
    }

    private fun addProductToShoppingList(listId: String?) {
        var size: String? = null
        var skuID: String? = null
        // Update listId value from dto AddToListRequest
        mAddToListRequest?.forEach { item ->
            item.giftListId = listId
            item.listId = null // remove list id from request body
            size = item.size
            skuID = item.skuID
        }

        mPostShoppingList = listId?.let { listID -> mAddToListRequest?.let { listItem -> OneAppService().addToList(listItem, listID) } }

        mPostShoppingList?.enqueue(CompletionHandler(object : IResponseListener<ShoppingListItemsResponse> {
            override fun onSuccess(shoppingListsResponse: ShoppingListItemsResponse?) {
                shoppingListsResponse?.apply {
                    when (httpCode) {
                        HTTP_OK -> addToListWasSendSuccessfully(listId)
                        HTTP_SESSION_TIMEOUT_440 -> sessionExpiredHandler()
                        else -> response?.let { otherHttpCodeHandler(it) }
                    }
                }
                AppConfigSingleton.dynamicYieldConfig?.apply {
                    if (isDynamicYieldEnabled == true)
                        prepareDyAddToWishListRequestEvent(skuID, size)
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

        }, ShoppingListItemsResponse::class.java))
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
        val context =
            za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Context(
                device,
                null,
                Utils.DY_CHANNEL
            )
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

    private fun addToListWasSendSuccessfully(listId: String?) {
        mShoppingListGroup?.apply {
            callAddToWishlistFirebaseEvent(listId)
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

    private fun displayNoConnectionToast() {
        activity?.let { ErrorHandlerView(it).showToast() }
    }

    private fun shoppingListPostProgress(state: Boolean) {
        enableCancelButton(!state)
        if (mShouldDisplayCreateListOnly) {
            bindingCreateListFromShoppingView.pbCreateList.visibility = if (state) VISIBLE else GONE
        } else {
            bindingCreateList.pbCreateList.visibility = if (state) VISIBLE else GONE
        }
    }

    private fun enableCancelButton(isEditTextNotEmpty: Boolean) {
        if (mShouldDisplayCreateListOnly) {
            bindingCreateListFromShoppingView.btnCancel?.isEnabled = isEditTextNotEmpty
            bindingCreateListFromShoppingView.clBottomView?.isEnabled = isEditTextNotEmpty
        } else {
            bindingCreateList.btnCancel?.isEnabled = isEditTextNotEmpty
            bindingCreateList.clBottomView?.isEnabled = isEditTextNotEmpty
        }
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

        orderId?.let { orderID ->
            val addOrderToListRequest = OneAppService().addOrderToList(orderID, orderRequestList)
            addOrderToListRequest.enqueue(CompletionHandler(object : IResponseListener<OrderToListReponse> {
                override fun onSuccess(orderToListReponse: OrderToListReponse?) {
                    orderToListReponse?.apply {
                        response.apply {
                            when (httpCode) {
                                0 -> addToListWasSendSuccessfully(orderRequestList.shoppingListId)
                                HTTP_SESSION_TIMEOUT_440 -> sessionExpiredHandler()
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

            }, OrderToListReponse::class.java))
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

    fun showKeyboard(editText: EditText) {
        editText.requestFocus()
        activity?.let {
            editText.requestFocus()
            editText.isFocusableInTouchMode = true
            val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun hideKeyboard() {
        activity?.apply {
            val inputManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // check if no view has focus:
            val currentFocusedView = currentFocus
            if (currentFocusedView != null) {
                inputManager.hideSoftInputFromWindow(
                    currentFocusedView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }
    }

    fun convertStringToObject(mAddToListArgs: String?) =
        Gson().fromJson<MutableList<AddToListRequest>>(
            mAddToListArgs,
            object : TypeToken<MutableList<AddToListRequest>>() {}.type
        )!!

    fun showErrorDialog(message: String) {
        activity?.let {
            val fm = it.supportFragmentManager
            val singleButtonDialogFragment = SingleButtonDialogFragment.newInstance(message)
            singleButtonDialogFragment?.show(fm, SingleButtonDialogFragment::class.java.simpleName)
        }
    }

    fun cancelRequest(call: Call<*>?) {
        call?.apply {
            if (!isCanceled)
                cancel()
        }
    }

    fun bindDepartment(mDepartmentAdapter: DepartmentAdapter?, parentFragment: ShopFragment?) {
        mDepartmentAdapter?.setRootCategories(parentFragment?.getCategoryResponseData()?.rootCategories)
        mDepartmentAdapter?.notifyDataSetChanged()
    }

    fun networkConnectionAvailable(it: FragmentActivity) =
        NetworkManager.getInstance().isConnectedToNetwork(it)

    private fun dyReportEventViewModel() {
        dyReportEventViewModel = ViewModelProvider(this)[DyChangeAttributeViewModel::class.java]
    }
}