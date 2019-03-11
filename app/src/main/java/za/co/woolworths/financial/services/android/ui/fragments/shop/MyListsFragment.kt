package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.GetShoppingList
import za.co.woolworths.financial.services.android.ui.adapters.ViewShoppingListAdapter
import android.support.v7.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.no_connection_handler.*
import kotlinx.android.synthetic.main.sign_out_template.*
import kotlinx.android.synthetic.main.shopping_list_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.DeliveryLocationSelectionActivity
import za.co.woolworths.financial.services.android.contracts.IShoppingList
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.DeleteShoppingLists
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.DepartmentExtensionFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListItemsFragment
import za.co.woolworths.financial.services.android.util.*

class MyListsFragment : DepartmentExtensionFragment(), View.OnClickListener, IShoppingList {

    private var mAddToShoppingListAdapter: ViewShoppingListAdapter? = null
    private var mGetShoppingListRequest: HttpAsyncTask<String, String, ShoppingListsResponse>? = null
    private var mSuburbName: String? = null
    private var mProvinceName: String? = null
    private var isMyListsFragmentVisible: Boolean = false
    private var isFragmentVisible: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.shopping_list_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isFragmentVisible) {
            initUI()
            authenticateUser()
            setListener()
        }
    }

    private fun initUI() {
        activity?.let {
            val itemDecorator = DividerItemDecoration(it, DividerItemDecoration.VERTICAL)
            itemDecorator.setDrawable(ContextCompat.getDrawable(it, R.drawable.divider))
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
    }

    private fun getShoppingList() {
        loadShoppingList(true)
        noNetworkConnectionLayout(false)
        mGetShoppingListRequest = GetShoppingList(object : AsyncAPIResponse.ResponseDelegate<ShoppingListsResponse> {
            override fun onSuccess(response: ShoppingListsResponse) {
                activity?.let {
                    response.apply {
                        when (httpCode) {
                            200 -> {
                                bindShoppingListToUI(lists)
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
                        loadShoppingList(false)
                    }
                }
            }

            override fun onFailure(errorMessage: String) {
                activity?.let {
                    it.runOnUiThread {
                        loadShoppingList(false)
                        noNetworkConnectionLayout(true)
                    }
                }
            }
        }).execute() as HttpAsyncTask<String, String, ShoppingListsResponse>
    }

    private fun bindShoppingListToUI(shoppingList: MutableList<ShoppingList>) {
        shoppingList.let {
            when (it.size) {
                0 -> showEmptyShoppingListView() //no list found

                else -> {
                    mAddToShoppingListAdapter?.setShoppingList(shoppingList)
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

            R.id.locationSelectedLayout,R.id.rlDeliveryLocationLayout -> {
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
                    getShoppingList()
                }
            }

            R.id.rlCreateAList -> {
                navigateToCreateListFragment(mutableListOf())
            }
        }
    }

    private fun navigateToCreateListFragment(commerceItemList: MutableList<AddToListRequest>) {
        val navigate = NavigateToShoppingList()
        navigate.openShoppingList(activity, commerceItemList, "", true)
    }

    private fun locationSelectionClicked() {
        val openDeliveryLocationSelectionActivity = Intent(activity, DeliveryLocationSelectionActivity::class.java)
        openDeliveryLocationSelectionActivity.putExtra("suburbName", mSuburbName)
        openDeliveryLocationSelectionActivity.putExtra("provinceName", mProvinceName)
        startActivity(openDeliveryLocationSelectionActivity)
        activity?.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay)
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

    fun authenticateUser() {
        hideEmptyOverlay()
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            getShoppingList()
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
        DeleteShoppingLists(shoppingList.listId, object : AsyncAPIResponse.ResponseDelegate<ShoppingListsResponse> {
            override fun onSuccess(response: ShoppingListsResponse) {
                when (response.httpCode) {
                    200 -> {

                        if (mAddToShoppingListAdapter?.getShoppingList()?.size == 0)
                            showEmptyShoppingListView()
                    }
                }
            }

            override fun onFailure(errorMessage: String) {
                activity?.let { it.runOnUiThread { ErrorHandlerView(it).showToast() } }
            }

        }).execute()
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
        val bundle = Bundle()
        bundle.putString("listName", shoppingList.listName)
        bundle.putString("listId", shoppingList.listId)
        val shoppingListItemsFragment = ShoppingListItemsFragment()
        shoppingListItemsFragment.arguments = bundle
        (activity as? BottomNavigationActivity)?.pushFragment(shoppingListItemsFragment)
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
