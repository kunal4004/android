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
import kotlinx.android.synthetic.main.create_new_list.*
import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.GetShoppingList
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.ui.adapters.AddToShoppingListAdapter
import java.util.*
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddToShoppingList
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.ShoppingListToastNavigation
import za.co.woolworths.financial.services.android.util.*

class AddToShoppingListFragment : ShoppingListExtensionFragment(), View.OnClickListener {

    private var mAddToListArgs: String? = null
    private var isRetrievingShoppingItem = false
    private var isPostingShoppingItem = false
    private var mShoppingListGroup: HashMap<String, ShoppingList>? = null

    private lateinit var mPostItemList: MutableList<String>
    private lateinit var mAddToShoppingListAdapter: AddToShoppingListAdapter

    companion object {
        public const val POST_ADD_TO_SHOPPING_LIST = "POST_ADD_TO_SHOPPING_LIST"
        fun newInstance(postListRequest: String?) = AddToShoppingListFragment().apply {
            arguments = Bundle(1).apply {
                putString(POST_ADD_TO_SHOPPING_LIST, postListRequest)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.add_to_list_content, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            //alwaysHideWindowSoftInputMode()
            getBundleArguments()
            setListener()
            getShoppingList()
            networkConnectivityStatus()
        }
    }

    private fun getBundleArguments() {
        mAddToListArgs = arguments?.getString(POST_ADD_TO_SHOPPING_LIST)
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
                                showErrorDialog(this.response?.message!!)
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
            rclAddToList.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
            mAddToShoppingListAdapter = AddToShoppingListAdapter(shoppingList) { shoppingListItemClicked() }
            rclAddToList.adapter = mAddToShoppingListAdapter
        }
    }

    private fun recyclerViewMaximumHeight(viewGroupParams: ViewGroup.LayoutParams) {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        viewGroupParams.height = 2 * displayMetrics.heightPixels / 5
        rclAddToList.layoutParams = viewGroupParams
    }

    private fun shoppingListItemClicked() {
        mAddToShoppingListAdapter.notifyDataSetChanged()
        btnPostShoppingList.text = if (shoppingListItemWasSelected()) getString(R.string.ok) else getString(R.string.cancel)
    }

    private fun shoppingListSelectedItemGroup(): HashMap<String, ShoppingList>? {
        val hmSelectedShoppingList = HashMap<String, ShoppingList>()
        mAddToShoppingListAdapter.getShoppingList().apply {
            forEach {
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
                    buildShoppingListRequest()
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

    private fun buildShoppingListRequest() {
        mPostItemList = mutableListOf()
        mShoppingListGroup = shoppingListSelectedItemGroup()
        val constructAddToListRequest = convertStringToObject(mAddToListArgs)
        mShoppingListGroup?.forEach { (key, valueWasPosted) ->
            constructAddToListRequest.forEach { it.listId = key }
            if (!valueWasPosted.wasSentToServer)
                postToShoppingList(constructAddToListRequest, key)
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
            buildShoppingListRequest()
        }
    }

    private fun shoppingListPostProgress(state: Boolean) {
        btnCancel.isEnabled = !state
        rclAddToList.isEnabled = !state
        pbAddToList.visibility = if (state) VISIBLE else GONE
    }

    private fun postToShoppingList(addToListRequest: MutableList<AddToListRequest>?, listId: String?) {
        shoppingListPostProgress(true)
        isPostingShoppingItem = true
        PostAddToShoppingList(listId, addToListRequest, object : AsyncAPIResponse.ResponseDelegate<ShoppingListItemsResponse> {
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
                            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                            SessionExpiredUtilities.getInstance().showSessionExpireDialog(activity as? AppCompatActivity)
                            shoppingListPostProgress(false)
                        }
                        else -> {
                            shoppingListPostProgress(false)
                            tvOnErrorLabel.text = response.response?.desc ?: ""
                            tvOnErrorLabel.visibility = VISIBLE
                        }
                    }
                    isPostingShoppingItem = false
                }
            }

            override fun onFailure(errorMessage: String) {
                shoppingListPostProgress(false)
                noNetworkConnection(false)
            }

        }).execute()
    }

    private fun showShoppingListSuccessToast() {
        ShoppingListToastNavigation.requestToastOnNavigateBack(activity, POST_ADD_TO_SHOPPING_LIST, mShoppingListGroup)
    }

    private fun navigateToCreateShoppingListFragment(state: Boolean) {
        replaceFragment(
                fragment = CreateShoppingListFragment.newInstance(mShoppingListGroup, mAddToListArgs, state),
                tag = CreateShoppingListFragment::class.java.simpleName,
                containerViewId = R.id.flShoppingListContainer,
                allowStateLoss = true,
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
                //TODO:: Work on the animation
                activity?.finish()
                activity?.overridePendingTransition(0, 0)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }

    fun closeFragment() {
        //TODO:: slide down and dismiss animation
        activity?.let {
            it.finish()
            it.overridePendingTransition(0, 0)
        }
    }
}