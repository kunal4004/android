package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ShoppingListFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.contracts.IShoppingList
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.presentation.createlist.CreateListFragment
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.adapters.ViewShoppingListAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.DepartmentExtensionFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE_CREATE_LIST
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.RESULT_CODE
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.GetCartSummary
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

@AndroidEntryPoint
class MyListsFragment : DepartmentExtensionFragment(R.layout.shopping_list_fragment), View.OnClickListener, IShoppingList {

    private lateinit var binding: ShoppingListFragmentBinding

    private var mAddToShoppingListAdapter: ViewShoppingListAdapter? = null
    private var mGetShoppingListRequest: Call<ShoppingListsResponse>? = null
    private var isMyListsFragmentVisible: Boolean = false
    private var isFragmentVisible: Boolean = false
    private var shoppingListsResponse: ShoppingListsResponse? = null

    private var mBottomNavigator: BottomNavigator? = null

    companion object {
        private const val MY_LIST_SIGN_IN_REQUEST_CODE = 7878
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ShoppingListFragmentBinding.bind(view)

        initUI()
        authenticateUser(true)
        setListener()
        addFragmentResultListener()
    }

    private fun addFragmentResultListener() {
        setFragmentResultListener(
            REQUEST_CODE_CREATE_LIST.toString()
        ) { _, bundle ->

            when(bundle.getInt(RESULT_CODE)) {
                REQUEST_CODE_CREATE_LIST -> {
                    getShoppingList(true)
                }
                else ->{}
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MY_LIST_SIGN_IN_REQUEST_CODE  -> {
                setYourDeliveryLocation()
                getShoppingList(true)
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setupToolbar()
            getShoppingList(false)
        }
    }

    override fun noConnectionLayout(isVisible: Boolean) {
        binding.incConnectionLayout?.root?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun initUI() {
        setupToolbar()
        activity?.let {
            val itemDecorator = DividerItemDecoration(it, DividerItemDecoration.VERTICAL)
            ContextCompat.getDrawable(it, R.drawable.inset_divider_24dp)
                ?.let { it1 -> itemDecorator.setDrawable(it1) }
            binding.rcvShoppingLists?.addItemDecoration(itemDecorator)
            binding.rcvShoppingLists?.layoutManager =
                LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
            mAddToShoppingListAdapter = ViewShoppingListAdapter(mutableListOf(), this)
            binding.rcvShoppingLists?.adapter = mAddToShoppingListAdapter
        }
    }

    private fun setupToolbar() {
        mBottomNavigator?.apply {
            setTitle(bindString(R.string.my_lists))
            displayToolbar()
            showBackNavigationIcon(true)
        }
    }

    private fun setListener() {
        binding.apply {
            locationSelectedLayout.setOnClickListener(this@MyListsFragment)
            binding.includeSignOutTemplate.btnGoToProduct.setOnClickListener(this@MyListsFragment)
            rlCreateAList.setOnClickListener(this@MyListsFragment)
            binding.incConnectionLayout.btnRetry.setOnClickListener(this@MyListsFragment)
            swipeToRefresh.setOnRefreshListener { getShoppingList(true) }
        }
    }

     fun getShoppingList(isPullToRefresh: Boolean) {
        if (isPullToRefresh) binding.swipeToRefresh.isRefreshing = true else loadShoppingList(true)
        noNetworkConnectionLayout(false)
        mGetShoppingListRequest = OneAppService().getShoppingLists().apply {
            enqueue(CompletionHandler(object : IResponseListener<ShoppingListsResponse> {
                override fun onSuccess(response: ShoppingListsResponse?) {
                    activity?.let {
                        response?.apply {
                            when (httpCode) {
                                200 -> {
                                    shoppingListsResponse = response
                                    bindShoppingListToUI()
                                }
                                440 -> {
                                    shoppingListsResponse = null
                                    mAddToShoppingListAdapter?.notifyDataSetChanged()
                                    SessionUtilities.getInstance()
                                        .setSessionState(SessionDao.SESSION_STATE.INACTIVE)
                                    showSignOutView()
                                    QueryBadgeCounter.instance.clearBadge()
                                    if (isFragmentVisible)
                                        activity?.let {
                                            SessionExpiredUtilities.getInstance()
                                                .showSessionExpireDialog(it as? AppCompatActivity?)
                                        }
                                }
                                else -> {
                                    loadShoppingList(false)
                                    showErrorDialog(this.response?.desc!!)
                                }
                            }
                            if (isPullToRefresh) binding.swipeToRefresh.isRefreshing =
                                false else loadShoppingList(false)
                        }
                    }
                }

                override fun onFailure(error: Throwable?) {
                    activity?.let {
                        it.runOnUiThread {
                            if (isPullToRefresh) binding.swipeToRefresh.isRefreshing =
                                false else loadShoppingList(false)
                            loadShoppingList(false)
                            noNetworkConnectionLayout(true)
                        }
                    }
                }
            }, ShoppingListsResponse::class.java))
        }
    }

    private fun bindShoppingListToUI() {
        val shoppingList: MutableList<ShoppingList> = shoppingListsResponse?.lists
            ?: mutableListOf()
        shoppingList?.let {
            when (it.size) {
                0 -> showEmptyShoppingListView() //no list found

                else -> {
                    binding.rlCreateAList?.visibility = VISIBLE
                    binding.includeSignOutTemplate.root.visibility = GONE
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
        binding.loadingBar?.visibility = if (state) VISIBLE else GONE
        binding.rcvShoppingLists?.visibility = if (state) GONE else VISIBLE
        if (state) mAddToShoppingListAdapter?.clear()
    }

    private fun setYourDeliveryLocation() {
        if (Utils.getPreferredDeliveryLocation() == null) {
            GetCartSummary().getCartSummary(object : IResponseListener<CartSummaryResponse> {
                override fun onSuccess(response: CartSummaryResponse?) {
                    when (response?.httpCode) {
                        AppConstant.HTTP_OK -> {
                            activity?.let {
                                KotlinUtils.getDeliveryType()?.let { fulfillmentDetails ->
                                    KotlinUtils.setDeliveryAddressView(
                                        it,
                                        fulfillmentDetails,
                                        binding.tvDeliveringTo,
                                        binding.tvDeliveryLocation,
                                        binding.deliverLocationIcon
                                    )
                                }
                            }
                        }
                    }
                }

                override fun onFailure(error: Throwable?) {
                }
            })
        } else {
            activity?.let {
                KotlinUtils.getDeliveryType()?.let { fulfillmentDetails ->
                    KotlinUtils.setDeliveryAddressView(
                        it,
                        fulfillmentDetails,
                        binding.tvDeliveringTo,
                        binding.tvDeliveryLocation,
                        binding.deliverLocationIcon
                    )
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {

            R.id.locationSelectedLayout -> {
                locationSelectionClicked()
            }
            R.id.btnGoToProduct -> {
                when (binding.includeSignOutTemplate.btnGoToProduct.tag) {
                    0 -> activity?.let { ScreenManager.presentSSOSignin(it, MY_LIST_SIGN_IN_REQUEST_CODE) }
                    1 -> navigateToCreateListFragment()
                }
            }

            R.id.btnRetry -> {
                if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                    getShoppingList(false)
                }
            }

            R.id.rlCreateAList -> navigateToCreateListFragment()
        }
    }

    private fun navigateToCreateListFragment() {
        activity?.apply {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.SHOP_MY_LIST_NEW_LIST,
                this)
        }
        (requireActivity() as? BottomNavigationActivity)?.apply {
            pushFragmentSlideUp(CreateListFragment())
        }
    }

    private fun locationSelectionClicked() {
        KotlinUtils.presentEditDeliveryGeoLocationActivity(
            requireActivity(),
            0,
            KotlinUtils.getPreferredDeliveryType(),
            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
        )
    }

    private fun showEmptyShoppingListView() {
        binding.apply {
            rlCreateAList?.visibility = GONE
            includeSignOutTemplate.clSignOutTemplate.visibility = VISIBLE
            includeSignOutTemplate.clSignOutTemplate.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
            Utils.getPreferredDeliveryLocation()?.apply {
                activity?.let {
                    KotlinUtils.setDeliveryAddressView(
                        it,
                        this.fulfillmentDetails,
                        binding.tvDeliveringTo,
                        binding.tvDeliveryLocation,
                        binding.deliverLocationIcon
                    )
                }
            }
            includeSignOutTemplate.apply {
                imEmptyIcon.setImageResource(R.drawable.empty_list_icon)
                imEmptyIcon.alpha = 1.0f
                txtEmptyStateTitle.text = getString(R.string.title_no_shopping_lists)
                txtEmptyStateDesc.text = getString(R.string.description_no_shopping_lists)
                btnGoToProduct.text = getString(R.string.button_no_shopping_lists)
                btnGoToProduct.tag = 1
                btnGoToProduct.visibility = VISIBLE
                locationSelectedLayout.visibility = VISIBLE
            }
        }
    }

    private fun hideEmptyOverlay() {
        binding.includeSignOutTemplate.root.visibility = GONE
    }

    private fun showSignOutView() {
        binding.apply {
            includeSignOutTemplate.apply {
                clSignOutTemplate.visibility = VISIBLE
                imEmptyIcon.setImageResource(R.drawable.ic_shopping_list_sign_out)
                txtEmptyStateTitle.text = getString(R.string.shop_sign_out_order_title)
                txtEmptyStateDesc.text = getString(R.string.shop_sign_out_order_desc)
                btnGoToProduct.visibility = VISIBLE
                btnGoToProduct.tag = 0
                btnGoToProduct.text = getString(R.string.sign_in)
                locationSelectedLayout.visibility = GONE
            }
        }
    }

    fun authenticateUser(isNewSession: Boolean) {
        hideEmptyOverlay()
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            shoppingListsResponse = null
            getShoppingList(isNewSession)
        } else {
            showSignOutView()
        }
    }

    private fun noNetworkConnectionLayout(state: Boolean) {
        binding.incConnectionLayout?.root?.visibility = if (state) VISIBLE else GONE
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
        val deleteShoppingList = OneAppService().deleteShoppingList(shoppingList.listId)
        deleteShoppingList.enqueue(CompletionHandler(object :
            IResponseListener<ShoppingListsResponse> {
            override fun onSuccess(response: ShoppingListsResponse?) {
                response?.apply {
                    when (httpCode) {
                        200 -> {
                            shoppingListsResponse = this
                            if (mAddToShoppingListAdapter?.getShoppingList()?.size == 0)
                                showEmptyShoppingListView()
                        }
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                activity?.let { it.runOnUiThread { ErrorHandlerView(it).showToast() } }
            }

        }, ShoppingListsResponse::class.java))
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
        activity?.let {
            ScreenManager.presentShoppingListDetailActivity(it,
                shoppingList.listId,
                shoppingList.listName,
                true)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        isFragmentVisible = isVisibleToUser
        if (!isVisibleToUser && mGetShoppingListRequest != null)
            cancelRequest(mGetShoppingListRequest)
    }

    fun scrollToTop() {
        if (binding.nestedScrollview != null)
            binding.nestedScrollview.scrollTo(0, 0)
    }

}
