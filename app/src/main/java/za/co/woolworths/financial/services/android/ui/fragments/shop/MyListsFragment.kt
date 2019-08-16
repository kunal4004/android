package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.ui.adapters.ViewShoppingListAdapter
import kotlinx.android.synthetic.main.no_connection_handler.*
import kotlinx.android.synthetic.main.sign_out_template.*
import kotlinx.android.synthetic.main.shopping_list_fragment.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.DeliveryLocationSelectionActivity
import za.co.woolworths.financial.services.android.contracts.IShoppingList
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.DepartmentExtensionFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import za.co.woolworths.financial.services.android.util.*

class MyListsFragment : DepartmentExtensionFragment(), View.OnClickListener, IShoppingList {

    private var mAddToShoppingListAdapter: ViewShoppingListAdapter? = null
    private var mGetShoppingListRequest: Call<ShoppingListsResponse>? = null
    private var mSuburbName: String? = null
    private var mProvinceName: String? = null
    private var isMyListsFragmentVisible: Boolean = false
    private var isFragmentVisible: Boolean = false
    private var parentFragment: ShopFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.shopping_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isFragmentVisible) {
            initUI()
            authenticateUser(false)
            setListener()
        }
    }

    private fun initUI() {
        activity?.let {
            val itemDecorator = DividerItemDecoration(it, DividerItemDecoration.VERTICAL)
            ContextCompat.getDrawable(it, R.drawable.divider)?.let { it1 -> itemDecorator.setDrawable(it1) }
            rcvShoppingLists.addItemDecoration(itemDecorator)
            rcvShoppingLists.layoutManager = LinearLayoutManager(it, LinearLayout.VERTICAL, false)
            mAddToShoppingListAdapter = ViewShoppingListAdapter(mutableListOf(), this)
            rcvShoppingLists.adapter = mAddToShoppingListAdapter
        }
    }

    private fun setListener() {
        locationSelectedLayout.setOnClickListener(this)
        btnGoToProduct.setOnClickListener(this)
        rlCreateAList.setOnClickListener(this)
        btnRetry.setOnClickListener(this)
        rlDeliveryLocationLayout.setOnClickListener(this)
        swipeToRefresh.setOnRefreshListener { getShoppingList(true) }
    }

    private fun getShoppingList(isPullToRefresh: Boolean) {
        if (isPullToRefresh) swipeToRefresh.isRefreshing = true else loadShoppingList(true)
        noNetworkConnectionLayout(false)
        mGetShoppingListRequest = OneAppService.getShoppingLists().apply {
            enqueue(CompletionHandler(object : RequestListener<ShoppingListsResponse> {
                override fun onSuccess(response: ShoppingListsResponse?) {
                    activity?.let {
                        response?.apply {
                            when (httpCode) {
                                200 -> {
                                    parentFragment?.setShoppingListResponseData(response)
                                    bindShoppingListToUI()
                                }
                                440 -> {
                                    SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                                    showSignOutView()
                                    QueryBadgeCounter.getInstance().clearBadge()
                                    if (isFragmentVisible)
                                        activity?.let { SessionExpiredUtilities.getInstance().showSessionExpireDialog(it as? AppCompatActivity?) }
                                }
                                else -> {
                                    loadShoppingList(false)
                                    showErrorDialog(this.response?.desc!!)
                                }
                            }
                            if (isPullToRefresh) swipeToRefresh.isRefreshing = false else loadShoppingList(false)
                        }
                    }
                }

                override fun onFailure(error: Throwable?) {
                    activity?.let {
                        it.runOnUiThread {
                            if (isPullToRefresh) swipeToRefresh.isRefreshing = false else loadShoppingList(false)
                            loadShoppingList(false)
                            noNetworkConnectionLayout(true)
                        }
                    }
                }
            },ShoppingListsResponse::class.java))
        }
    }

    private fun bindShoppingListToUI() {
        val shoppingList: MutableList<ShoppingList>? = parentFragment?.getShoppingListResponseData()?.lists ?: mutableListOf()
        shoppingList.let {
            when (it?.size) {
                0 -> showEmptyShoppingListView() //no list found

                else -> {
                    mAddToShoppingListAdapter?.setShoppingList(shoppingList!!)
                    mAddToShoppingListAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setYourDeliveryLocation()
    }

    private fun loadShoppingList(state: Boolean) {
        loadingBar?.visibility = if (state) VISIBLE else GONE
        rcvShoppingLists?.visibility = if (state) GONE else VISIBLE
        if (state) mAddToShoppingListAdapter?.clear()
    }

    private fun setYourDeliveryLocation() {
        Utils.getPreferredDeliveryLocation()?.apply {
            mSuburbName = suburb?.name ?: ""
            mProvinceName = province?.name ?: ""
            mSuburbName?.isNotEmpty().apply { manageDeliveryLocationUI("$mSuburbName , $mProvinceName") }
        }
    }

    private fun manageDeliveryLocationUI(deliveryLocation: String) {
        tvDeliveringTo.text = getString(R.string.delivering_to)
        tvDeliveringEmptyTo.text = getString(R.string.delivering_to)
        tvDeliveryLocation.visibility = VISIBLE
        tvDeliveryEmptyLocation.visibility = VISIBLE
        tvDeliveryLocation.text = deliveryLocation
        tvDeliveryEmptyLocation.text = deliveryLocation
    }

    override fun onClick(view: View?) {
        when (view?.id) {

            R.id.locationSelectedLayout, R.id.rlDeliveryLocationLayout -> {
                locationSelectionClicked()
            }
            R.id.btnGoToProduct -> {
                when (btnGoToProduct.tag) {
                    0 -> activity?.let { ScreenManager.presentSSOSignin(it) }
                    1 -> navigateToCreateListFragment(mutableListOf())
                }
            }

            R.id.btnRetry -> {
                if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                    getShoppingList(false)
                }
            }

            R.id.rlCreateAList -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPNEWLIST)
                navigateToCreateListFragment(mutableListOf())
            }
        }
    }

    private fun navigateToCreateListFragment(commerceItemList: MutableList<AddToListRequest>) {
        NavigateToShoppingList.openShoppingList(activity, commerceItemList, "", true)
    }

    private fun locationSelectionClicked() {
        activity?.apply {
            val openDeliveryLocationSelectionActivity = Intent(this, DeliveryLocationSelectionActivity::class.java)
            openDeliveryLocationSelectionActivity.putExtra("suburbName", mSuburbName)
            openDeliveryLocationSelectionActivity.putExtra("provinceName", mProvinceName)
            startActivity(openDeliveryLocationSelectionActivity)
            overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay)
        }
    }

    private fun showEmptyShoppingListView() {
        clSignOutTemplate.visibility = VISIBLE
        imEmptyIcon.setImageResource(R.drawable.emptylists)
        imEmptyIcon.alpha = 1.0f
        txtEmptyStateTitle.text = getString(R.string.title_no_shopping_lists)
        txtEmptyStateDesc.text = getString(R.string.description_no_shopping_lists)
        btnGoToProduct.text = getString(R.string.button_no_shopping_lists)
        btnGoToProduct.tag = 1
        btnGoToProduct.visibility = VISIBLE
        rlDeliveryLocationLayout.visibility = VISIBLE
    }

    private fun hideEmptyOverlay() {
        clSignOutTemplate?.visibility = GONE
    }

    private fun showSignOutView() {
        clSignOutTemplate.visibility = VISIBLE
        imEmptyIcon.setImageResource(R.drawable.ic_shopping_list_sign_out)
        txtEmptyStateTitle.text = getString(R.string.shop_sign_out_order_title)
        txtEmptyStateDesc.text = getString(R.string.shop_sign_out_order_desc)
        btnGoToProduct.visibility = VISIBLE
        btnGoToProduct.tag = 0
        btnGoToProduct.text = getString(R.string.sign_in)
        rlDeliveryLocationLayout.visibility = GONE
    }

    fun authenticateUser(isNewSession: Boolean) {
        parentFragment = (activity as? BottomNavigationActivity)?.currentFragment as? ShopFragment
        hideEmptyOverlay()
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            if (parentFragment?.getShoppingListResponseData() != null && !isNewSession && !parentFragment?.isDifferentUser()!!) bindShoppingListToUI() else {
                parentFragment?.clearCachedData()
                getShoppingList(isNewSession)
            }
        } else {
            showSignOutView()
        }
    }

    private fun noNetworkConnectionLayout(state: Boolean) {
        incConnectionLayout?.visibility = if (state) VISIBLE else GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelRequest(mGetShoppingListRequest)
    }

    override fun setMenuVisibility(visible: Boolean) {
        super.setMenuVisibility(visible)
        isMyListsFragmentVisible = (visible && isResumed)
    }

    private fun deleteShoppingListItem(shoppingList: ShoppingList) {
        val deleteShoppingList =  OneAppService.deleteShoppingList(shoppingList.listId)
        deleteShoppingList.enqueue(CompletionHandler(object: RequestListener<ShoppingListsResponse>{
            override fun onSuccess(shoppingListsResponse: ShoppingListsResponse?) {
                shoppingListsResponse?.apply {
                    when (httpCode) {
                        200 -> {
                            parentFragment?.setShoppingListResponseData(this)
                            if (mAddToShoppingListAdapter?.getShoppingList()?.size == 0)
                                showEmptyShoppingListView()
                        }
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                activity?.let { it.runOnUiThread { ErrorHandlerView(it).showToast() } }
            }

        },ShoppingListsResponse::class.java))
    }

    override fun onShoppingListItemDeleted(shoppingList: ShoppingList, position: Int) {
        if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
            mAddToShoppingListAdapter?.getShoppingList().let {
                it?.remove(shoppingList)
                mAddToShoppingListAdapter?.notifyItemRemoved(position)
                mAddToShoppingListAdapter?.notifyItemRangeChanged(0, it!!.size)
                deleteShoppingListItem(shoppingList)
            }
        }
    }

    override fun onShoppingListItemSelected(shoppingList: ShoppingList) {
        activity?.let { ScreenManager.presentShoppingListDetailActivity(it, shoppingList.listId, shoppingList.listName,true) }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        isFragmentVisible = isVisibleToUser
        if (!isVisibleToUser && mGetShoppingListRequest != null)
            cancelRequest(mGetShoppingListRequest)
    }

    fun scrollToTop() {
        if (nested_scrollview != null)
            nested_scrollview.scrollTo(0, 0)
    }

}
