package za.co.woolworths.financial.services.android.ui.fragments.shop.list

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.add_to_list_content.*
import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.GetShoppingList
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.ui.adapters.AddToShoppingListAdapter
import java.util.*
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddToShoppingList
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.util.ConnectionBroadcastReceiver
import za.co.woolworths.financial.services.android.util.NetworkManager

class AddToShoppingListFragment : ShoppingListExtensionFragment(), View.OnClickListener {

    private var mAddToListArgs: String? = null
    private var isRetrievingShoppingItem = false
    private var isPostingShoppingItem = false
    private var mShoppingListGroup: HashMap<String, Boolean>? = null

    private lateinit var mPostItemList: MutableList<String>
    private lateinit var mAddToShoppingListAdapter: AddToShoppingListAdapter

    companion object {
        private const val POST_ADD_TO_SHOPPING_LIST = "POST_ADD_TO_SHOPPING_LIST"
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
        btnCancel.setOnClickListener(this)
        btnRetry.setOnClickListener(this)
        imCreateList.setOnClickListener(this)
    }

    private fun loadShoppingList(state: Boolean) {
        recyclerViewMaximumHeight(rclAddToList.layoutParams)
        relProgressBar.visibility = if (state) VISIBLE else GONE
        isRetrievingShoppingItem = state
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
                            else -> {
                                showErrorDialog(this.response?.desc!!)
                            }
                        }
                        loadShoppingList(false)
                    }
                }
            }

            override fun onFailure(errorMessage: String) {
                loadShoppingList(false)

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
                        navigateToCreateShoppingListFragment()
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
        btnCancel.text = if (shoppingListItemWasSelected()) getString(R.string.ok) else getString(R.string.cancel)
    }

    private fun shoppingListSelectedItemGroup(): HashMap<String, Boolean>? {
        val hmSelectedShoppingList = HashMap<String, Boolean>()
        mAddToShoppingListAdapter.getShoppingList().apply {
            forEach {
                if (it.shoppingListRowWasSelected) {
                    hmSelectedShoppingList[it.listId] = false
                }
            }
        }
        return hmSelectedShoppingList
    }

    private fun shoppingListItemWasSelected(): Boolean = shoppingListSelectedItemGroup()?.size!! > 0

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnCancel -> {
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
                navigateToCreateShoppingListFragment()
            }
        }
    }

    private fun buildShoppingListRequest() {
        mPostItemList = mutableListOf()
        mShoppingListGroup = shoppingListSelectedItemGroup()
        val constructAddToListRequest = convertStringToObject(mAddToListArgs)
        mShoppingListGroup?.forEach { (key, valueWasPosted) ->
            constructAddToListRequest.forEach { it.listId = key }
            if (!valueWasPosted)
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
                                listId?.let { put(it, true) }

                                //Check all values are true, implying that all request was sent
                                if (false !in values) {
                                    shoppingListPostProgress(false)
                                    showShoppingListSuccessToast()
                                }
                            }
                        }
                        440 -> {
                            shoppingListPostProgress(false)

                        }
                        else -> {
                            // TODO :: display dialog error message
                            shoppingListPostProgress(false)
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
        Toast.makeText(activity, "TODO(not implement Toast)", Toast.LENGTH_SHORT).show()
    }

    fun navigateToCreateShoppingListFragment() {
        replaceFragment(
                fragment = CreateShoppingListFragment.newInstance(mShoppingListGroup, mAddToListArgs),
                tag = CreateShoppingListFragment::class.java.simpleName,
                containerViewId = R.id.flShoppingListContainer,
                allowStateLoss = true,
                enterAnimation = R.anim.stay,
                exitAnimation = R.anim.slide_down_anim,
                popEnterAnimation = R.anim.fade_in,
                popExitAnimation = R.anim.stay
        )
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }
}