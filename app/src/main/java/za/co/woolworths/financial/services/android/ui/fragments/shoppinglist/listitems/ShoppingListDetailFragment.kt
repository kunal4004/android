package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems

import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ShoppingListDetailFragmentBinding
import com.google.gson.Gson
import com.google.gson.JsonElement
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.Call
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.ScreenNames.Companion.SHOPPING_LIST_ITEMS
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.contracts.IToastInterface
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.isEnhanceSubstitutionFeatureAvailable
import za.co.woolworths.financial.services.android.geolocation.GeoUtils.Companion.getPlaceId
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.UpdateScreenLiveData
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.presentation.addtolist.AddToListFragment
import za.co.woolworths.financial.services.android.presentation.common.confirmationdialog.ConfirmationBottomsheetDialogFragment
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Product
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Recommendation
import za.co.woolworths.financial.services.android.recommendations.presentation.RecommendationLoader
import za.co.woolworths.financial.services.android.recommendations.presentation.RecommendationLoaderImpl
import za.co.woolworths.financial.services.android.recommendations.presentation.RecommendationLoadingNotifier
import za.co.woolworths.financial.services.android.shoppinglist.listener.MyShoppingListItemClickListener
import za.co.woolworths.financial.services.android.shoppinglist.model.EditOptionType
import za.co.woolworths.financial.services.android.shoppinglist.model.RemoveItemApiRequest
import za.co.woolworths.financial.services.android.shoppinglist.service.network.CopyItemToListRequest
import za.co.woolworths.financial.services.android.shoppinglist.service.network.ItemDetail
import za.co.woolworths.financial.services.android.shoppinglist.service.network.MoveItemApiRequest
import za.co.woolworths.financial.services.android.shoppinglist.view.MoreOptionDialogFragment
import za.co.woolworths.financial.services.android.shoppinglist.view.ShoppingListErrorView
import za.co.woolworths.financial.services.android.shoptoggle.common.UnsellableAccess
import za.co.woolworths.financial.services.android.shoptoggle.common.UnsellableAccess.Companion.resetUnsellableLiveData
import za.co.woolworths.financial.services.android.shoptoggle.common.UnsellableAccess.Companion.updateUnsellableLiveData
import za.co.woolworths.financial.services.android.shoptoggle.presentation.ShopToggleActivity
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity.EXTRA_LIST_ID
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity.EXTRA_SEARCH_TEXT_HINT
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity.PRODUCT_SEARCH_ACTIVITY_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListItemsAdapter
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderFailure
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderLoading
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.IOnConfirmDeliveryLocationActionListener
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog.ConfirmDeliveryLocationFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants.EVENT_PAGE_TYPE_SHOPPING_LIST
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants.EVENT_TYPE_PAGEVIEW
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants.EVENT_TYPE_PRODUCT_THUMBNAIL_VIEW
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants.EVENT_URL_SHOPPING_LIST
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment.Companion.ADDED_TO_SHOPPING_LIST_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment.Companion.MY_LIST_LIST_ID
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment.Companion.MY_LIST_LIST_NAME
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment.Companion.MY_LIST_SEARCH_TERM
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment.Companion.REFRESH_SHOPPING_LIST_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.views.ToastFactory
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.buildAddToCartSuccessToast
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.buildShoppingListFromSearchResultToast
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.showItemsLimitToastOnAddToCart
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_SESSION_TIMEOUT_440
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_KEY_CONFIRMATION_DIALOG
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.CustomProgressBar
import za.co.woolworths.financial.services.android.util.CustomTypefaceSpan
import za.co.woolworths.financial.services.android.util.EmptyCartView
import za.co.woolworths.financial.services.android.util.EmptyCartView.EmptyCartInterface
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getPreferredDeliveryType
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.presentEditDeliveryGeoLocationActivity
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.setDeliveryAndLocation
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.showQuantityLimitErrror
import za.co.woolworths.financial.services.android.util.NetworkChangeListener
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.PostItemToCart
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.ToastUtils.ToastInterface
import za.co.woolworths.financial.services.android.util.UnsellableUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper.switchDeliverModeEvent
import za.co.woolworths.financial.services.android.util.wenum.Delivery


@AndroidEntryPoint
class ShoppingListDetailFragment : Fragment(), View.OnClickListener, EmptyCartInterface,
    NetworkChangeListener, ToastInterface, ShoppingListItemsNavigator, IToastInterface,
    IOnConfirmDeliveryLocationActionListener, RecommendationLoadingNotifier, RecommendationLoader by RecommendationLoaderImpl(),
    MyShoppingListItemClickListener {

    private val viewModel: ShoppingListDetailViewModel by viewModels()
    private var customProgressDialog: CustomProgressBar? = null
    private val confirmAddressViewModel: ConfirmAddressViewModel by activityViewModels()

    val selectedItems  = ArrayList<ItemDetail>()
    val shoppingListId  = ArrayList<String>()
    val removalGiftItemIds  = ArrayList<String>()

    private val productSearchResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                // add to list from search result
                ADDED_TO_SHOPPING_LIST_RESULT_CODE -> {
                    val count = result.data?.getIntExtra(EXTRA_LIST_ITEMS, 0) ?: 0
                    buildShoppingListFromSearchResultToast(
                        requireActivity(), bindingListDetails.rlCheckOut, listName ?: "", count
                    )
                    viewModel.getShoppingListDetails()
                    val addedToListIntent = Bundle()
                    addedToListIntent.putInt(EXTRA_LIST_ITEMS, count)
                    setFragmentResult(REFRESH_SHOPPING_LIST_RESULT_CODE.toString(), addedToListIntent)
                }
                // searched details result
                PRODUCT_SEARCH_ACTIVITY_RESULT_CODE -> {
                    val searchResultFragment = SearchResultFragment()
                    result.data?.let { data ->
                        val bundle = bundleOf(
                            MY_LIST_SEARCH_TERM to data.getStringExtra(MY_LIST_LIST_NAME),
                            MY_LIST_LIST_ID to data.getStringExtra(MY_LIST_LIST_ID),
                            ARG_LIST_NAME to listName
                        )
                        searchResultFragment.arguments = bundle
                    }
                    (activity as? BottomNavigationActivity)?.pushFragment(searchResultFragment)
                }
            }
        }

    private var mErrorHandlerView: ErrorHandlerView? = null
    private var openFromMyList = false
    private var addedToCart = false
    private var errorMessageWasPopUp = false
    private var showMenu = false
    private var isSelectAll: Boolean = false
    private var listName: String? = null
    private var shoppingListItemsAdapter: ShoppingListItemsAdapter? = null
    private var mConnectionBroadcast: BroadcastReceiver? = null
    private var mPostAddToCart: Call<AddItemToCartResponse>? = null

    private var timer: CountDownTimer? = null

    private var _bindingListDetails: ShoppingListDetailFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val bindingListDetails get() = _bindingListDetails!!

    private var selectedItemsForRemoval = 0
    private var isSingleItemSelected = false
    private var singleShoppingListItem:ShoppingListItem? = null

    private var selectedShoppingList:ArrayList<ShoppingList>? = null
    private var listOfItems =  ArrayList<AddToListRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            listName = getString(ARG_LIST_NAME, "")
            openFromMyList = getBoolean(ARG_OPEN_FROM_MY_LIST, false)
        }
        Utils.updateStatusBarBackground(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _bindingListDetails = ShoppingListDetailFragmentBinding.inflate(inflater, container, false)
        return _bindingListDetails?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingListDetails = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(listName)
        setDeliveryLocation()
        initViewAndEvent()
        addSubscribeEvents()
        addFragmentListener()
    }

    private fun addSubscribeEvents() {
        // Shopping List items
        viewModel.shoppListDetails.observe(viewLifecycleOwner) {
            val response = it.peekContent().data
            setRecommendationDividerVisibility(visibility = false)
            when (it.peekContent().status) {
                Status.LOADING -> {
                    mErrorHandlerView?.hideErrorHandler()
                    bindingListDetails.apply {
                        rlEmptyListView.visibility = GONE
                        rcvShoppingListItems.visibility = GONE
                        loadingBar.visibility = VISIBLE
                        rlCheckOut.visibility = GONE
                    }
                }
                Status.SUCCESS -> {
                    onShoppingListItemsResponse(response)
                }
                Status.ERROR -> {
                    onShoppingListItemsResponseError(response)
                }
            }
        }

        viewModel.isListUpdated.observe(viewLifecycleOwner) {
            if(it) {
                bindingListDetails.loadingBar.visibility = GONE
                updateList()
            }
        }

        // Shopping List Items Inventory
        viewModel.inventoryDetails.observe(viewLifecycleOwner) {
            val response = it.peekContent().data
            when (it.peekContent().status) {
                Status.LOADING -> {
                    enableAdapterClickEvent(false)
                    mErrorHandlerView?.hideErrorHandler()
                    bindingListDetails.rlEmptyListView.visibility = GONE
                    bindingListDetails.rcvShoppingListItems.visibility = VISIBLE
                    bindingListDetails.loadingBar.visibility = VISIBLE
                }
                Status.SUCCESS -> {
                    enableAdapterClickEvent(true)
                    bindingListDetails.loadingBar.visibility = GONE
                    getInventoryForStoreSuccess(response)
                    updateCartCountButton()
                    manageSelectAllMenuVisibility()
                }
                Status.ERROR -> {
                    viewModel.inventoryCallFailed = true
                    viewModel.setOutOfStock()
                    if (!isAdded) return@observe
                    bindingListDetails.loadingBar.visibility = GONE
                    enableAdapterClickEvent(true)
                    updateList()
                }
            }
        }

       viewModel.shoppingListDetailsAfterDelete.observe(viewLifecycleOwner) {
            val response = it.peekContent().data
            when (it.peekContent().status) {
                Status.LOADING -> {
                    val message  = resources.getQuantityString(R.plurals.remove_item,selectedItemsForRemoval) + "\n" + listName
                    showLoadingProgress(this, message)
                    bindingListDetails.errorListView.visibility = GONE
                }
                Status.SUCCESS -> {
                    hideLoadingProgress()
                    bindingListDetails.errorListView.visibility = GONE
                    val currentList =
                        shoppingListItemsAdapter?.shoppingListItems ?: ArrayList(0)

                    val updatedList =
                        response?.listItems?.let { ArrayList(it) } ?: ArrayList(0)

                    viewModel.mShoppingListItems = updatedList
                    viewModel.onDeleteSyncList(currentList)
                    response?.let { onShoppingListItemDelete(it) }
                    onDeleteUIUpdate()
                    shoppingListItemsAdapter?.notifyDataSetChanged()
                    val message = HtmlCompat.fromHtml( "\t\t" + getFormatedString(
                        count = selectedItemsForRemoval, R.plurals.remove_list) +"\t\t" + listName , HtmlCompat.FROM_HTML_MODE_LEGACY)
                    ToastFactory.showToast(
                        requireActivity(),
                        bindingListDetails.rlCheckOut,
                        message.toString()
                    )
                    //refresh main myList fragment to show updated count.
                    setFragmentResult(REFRESH_SHOPPING_LIST_RESULT_CODE.toString(), bundleOf())
                }
                Status.ERROR -> {
                    hideLoadingProgress()
                    showErrorMessage(getString(R.string.remove_error_msg))
                }
            }
        }

        viewModel.copyItemsToList.observe(viewLifecycleOwner) {
            val response = it.peekContent().data
            when (it.peekContent().status) {
                Status.LOADING -> {
                    val message = if (selectedShoppingList?.size == 1) {
                        getFormatedString(selectedItems.size, R.plurals.copy_item) + "\n" + selectedShoppingList?.getOrNull(0)?.listName
                    } else {
                        getFormatedString(selectedItems.size, R.plurals.copy_item) + "\n" + getString(R.string.multiple_lists)
                    }
                    showLoadingProgress(this, message)
                    bindingListDetails.errorListView.visibility = GONE
                }

                Status.SUCCESS -> {
                    hideLoadingProgress()
                    val listName =  if (selectedShoppingList?.size == 1 ) {
                        selectedShoppingList?.getOrNull(0)?.listName?: ""
                    } else {
                        getString(R.string.multiple_lists)
                    }

                    val title = context?.resources?.getQuantityString(
                        R.plurals.copy_item_msg,
                        selectedItems.size, selectedItems.size, listName
                    ) ?: ""

                    shoppingListItemsAdapter?.resetSelection()
                    showSuccessMessage(listName, title)
                }
                Status.ERROR -> {
                    hideLoadingProgress()
                    showErrorMessage(getString(R.string.remove_copy_msg))
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.moveItemFromList.collectLatest { moveItemFromListResponse ->
                with(moveItemFromListResponse) {
                    renderLoading {
                        val message = if (selectedShoppingList?.size == 1) {
                            getFormatedString(selectedItems.size, R.plurals.move_item) + "\n" + selectedShoppingList?.getOrNull(0)?.listName
                        } else {
                            getFormatedString(selectedItems.size, R.plurals.move_item) + "\n" + getString(R.string.multiple_lists)
                        }
                        if (isLoading) {
                            showLoadingProgress(this@ShoppingListDetailFragment, message)
                        } else {
                            hideLoadingProgress()
                            bindingListDetails.errorListView.visibility = GONE
                        }
                    }
                    renderSuccess {
                        val listName =  if (selectedShoppingList?.size == 1 ) {
                            selectedShoppingList?.getOrNull(0)?.listName?: ""
                        } else {
                            getString(R.string.multiple_lists)
                        }

                        val title = context?.resources?.getQuantityString(
                            R.plurals.move_item_msg,
                            selectedItems.size, selectedItems.size, listName
                        ) ?: ""

                        updateUiForMovedItem()
                        showSuccessMessage(listName, title)
                    }
                    renderFailure {
                        showErrorMessage(getString(R.string.remove_move_msg))
                    }
                }
            }
        }
    }

    private fun updateUiForMovedItem() {
        val list : ArrayList<ShoppingListItem>? = viewModel.updateListForMoveItem() as? ArrayList<ShoppingListItem>?
        shoppingListItemsAdapter?.setList(list)
        shoppingListItemsAdapter?.notifyDataSetChanged()
        setUpView()
    }

    private fun showSuccessMessage(listName: String, title: String) {
        bindingListDetails.errorListView.visibility = GONE
        ToastFactory.buildItemsAddedToList(
            activity = requireActivity(),
            viewLocation = bindingListDetails.rlCheckOut,
            listName = listName,
            hasGiftProduct = false,
            count = selectedItems.size,
            title = title,
            onButtonClick = {
                if (selectedShoppingList?.size == 1) {
                    selectedShoppingList?.getOrNull(0)?.let {
                        ScreenManager.presentShoppingListDetailActivity(
                            activity,
                            it.listId,
                            it.listName,
                            false
                        )
                    }
                } else {
                    ScreenManager.presentMyListScreen(activity)
                }
            }
        )
        //refresh main myList fragment to show updated count.
        setFragmentResult(REFRESH_SHOPPING_LIST_RESULT_CODE.toString(), bundleOf())
    }

    private fun getFormatedString(count:Int ,msg:Int): String {
        return requireContext().resources.getQuantityString(
            msg,
            count,
            count
        )
    }

    private fun showLoadingProgress(fragment: Fragment, message: String) {
        if (customProgressDialog != null && customProgressDialog!!.isVisible)
            return
        customProgressDialog = CustomProgressBar.newInstance(
            message,
            getString(R.string.processing_your_request_desc)
        )
        customProgressDialog?.show(
            fragment.requireActivity().supportFragmentManager,
            CustomProgressBar::class.java.simpleName
        )
    }

    private fun hideLoadingProgress() {
        customProgressDialog?.dismiss()
    }

    private fun showErrorMessage(message: String) {
        bindingListDetails.errorListView.visibility = VISIBLE
        bindingListDetails.errorListView.setContent {
            ShoppingListErrorView(message)
        }
    }

    private fun setUpToolbar(listName: String?) {
        bindingListDetails.shoppingListTitleTextView.text = listName
        (activity as? BottomNavigationActivity)?.apply {
            bindingListDetails.appbar.visibility = VISIBLE
            hideToolbar()
            showBackNavigationIcon(true)
            setToolbarBackgroundDrawable(R.drawable.appbar_background)
            bindingListDetails.btnBack.setOnClickListener {
                onBackPressed()
            }
            toolbar().setNavigationOnClickListener { popFragment() }
            setTitle(listName)
        }
    }

    private fun initViewAndEvent() {
        mConnectionBroadcast = Utils.connectionBroadCast(activity, this)
        setUpAddToCartButton()
        with(bindingListDetails) {
            initList(rcvShoppingListItems)
            fulfilmentAndLocationLayout.root.setBackgroundColor(Color.WHITE)
            fulfilmentAndLocationLayout.layoutFulfilment.tvSubTitle.visibility = GONE
            fulfilmentAndLocationLayout.layoutLocation.ivLocation.visibility = GONE
            selectDeselectAllTextView.setOnClickListener(this@ShoppingListDetailFragment)
            fulfilmentAndLocationLayout.layoutFulfilment.root.setOnClickListener(this@ShoppingListDetailFragment)
            fulfilmentAndLocationLayout.layoutLocation.root.setOnClickListener(this@ShoppingListDetailFragment)
            textProductSearch.setOnClickListener(this@ShoppingListDetailFragment)
            blackToolTipLayout.closeWhiteBtn.setOnClickListener(this@ShoppingListDetailFragment)
            blackToolTipLayout.changeLocationButton.setOnClickListener(this@ShoppingListDetailFragment)

            btnRetry.setOnClickListener(this@ShoppingListDetailFragment)
            txtMoreOptions.setOnClickListener(this@ShoppingListDetailFragment)

            mErrorHandlerView = ErrorHandlerView(activity, noConnectionLayout)
            mErrorHandlerView?.setMargin(noConnectionLayout, 0, 0, 0, 0)
            val emptyCartView = EmptyCartView(root, this@ShoppingListDetailFragment)
            emptyCartView.setView(
                getString(R.string.title_empty_shopping_list),
                getString(R.string.description_empty_shopping_list),
                getString(R.string.button_empty_shopping_list),
                R.drawable.empty_list_icon
            )
        }

        // Show Bottom Navigation Menu
        (activity as? BottomNavigationActivity)?.showBottomNavigationMenu()
    }

    private fun showBlackToolTip() {
        val delivery = when (getPreferredDeliveryType()) {
            Delivery.CNC -> R.string.label_collection
            Delivery.DASH -> R.string.label_dash
            else -> return
        }

        bindingListDetails.blackToolTipLayout.deliveryCollectionTitle.text =
            buildSpannedString {
                val text = getText(R.string.title_mylist_dash_tooltip)
                append(text)
                append(getText(delivery))
                val typefaceBold = ResourcesCompat.getFont(requireContext(), R.font.futura_semi_bold)
                setSpan(
                    CustomTypefaceSpan("futura", typefaceBold),
                    text.length,
                    length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

        bindingListDetails.blackToolTipLayout?.root?.visibility = VISIBLE
        timer?.cancel()
        // Check the time from the config for blackToolTip dismiss.
        if (AppConfigSingleton.tooltipSettings?.isAutoDismissEnabled == true) {
            val timeDuration =
                AppConfigSingleton.tooltipSettings?.autoDismissDuration?.times(1000) ?: return
            timer = object : CountDownTimer(timeDuration, 100) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    hideBlackToolTip()
                }
            }.start()
        }
    }

    private fun hideBlackToolTip() {
        bindingListDetails?.blackToolTipLayout?.root?.visibility = GONE
        timer?.cancel()
    }

    private fun screenRefresh(){
        if(isVisible) {
            UpdateScreenLiveData.observe(viewLifecycleOwner) {
                if (it == updateUnsellableLiveData)
                {   viewModel.getShoppingListDetails()
                    setDeliveryLocation()
                    switchDeliverModeEvent(KotlinUtils.getDeliveryType()?.deliveryType)
                    UpdateScreenLiveData.value = resetUnsellableLiveData
                }
            }
        }
    }


    private fun setUpView() {
        bindingListDetails.rlEmptyListView.visibility =
            if (viewModel.mShoppingListItems.isEmpty()) VISIBLE else GONE
        // 1 to exclude header
        bindingListDetails.rcvShoppingListItems.visibility =
            if (viewModel.mShoppingListItems.isEmpty()) GONE else VISIBLE
        manageSelectAllMenuVisibility()
    }

    private fun initList(rcvShoppingListItems: RecyclerView) {
        if (!isAdded || !isVisible) return
        shoppingListItemsAdapter = ShoppingListItemsAdapter(viewModel.mShoppingListItems, this)
        val mLayoutManager = LinearLayoutManager(activity)
        mLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rcvShoppingListItems.layoutManager = mLayoutManager
        rcvShoppingListItems.adapter = shoppingListItemsAdapter
    }

    override fun onClick(view: View) {
        when (view.id) {
            bindingListDetails.fulfilmentAndLocationLayout.layoutFulfilment.root.id -> launchShopToggleScreen()
            bindingListDetails.fulfilmentAndLocationLayout.layoutLocation.root.id -> launchStoreOrLocationSelection()
            R.id.selectDeselectAllTextView -> onOptionsItemSelected()
            R.id.textProductSearch -> openProductSearchActivity()
            R.id.btnRetry -> if (NetworkManager.getInstance().isConnectedToNetwork(
                    activity
                )
            ) {
                errorMessageWasPopUp = false
                viewModel.getShoppingListDetails()
            }
            R.id.btnCheckOut -> addItemsToCart()
            R.id.changeLocationButton -> deliverySelectionIntent(DELIVERY_LOCATION_REQUEST)
            R.id.closeWhiteBtn -> hideBlackToolTip()
            R.id.txtMoreOptions -> {
                isSingleItemSelected = false
                openMoreOptionsDialog()
            }
            else -> {}
        }
    }

    private fun launchShopToggleScreen() {
        Intent(requireActivity(), ShopToggleActivity::class.java).apply {
            startActivityForResult(this, ShopToggleActivity.REQUEST_DELIVERY_TYPE)
        }
    }
    private fun launchStoreOrLocationSelection() {
        val delivery = Delivery.getType(KotlinUtils.getDeliveryType()?.deliveryType)
        if (delivery == Delivery.CNC) {
            launchStoreSelection()
        } else {
            launchGeoLocationFlow()
        }
    }
    private fun launchStoreSelection() {
        KotlinUtils.presentEditDeliveryGeoLocationActivity(
            activity,
            BundleKeysConstants.UPDATE_STORE_REQUEST,
            Delivery.getType(KotlinUtils.getDeliveryType()?.deliveryType)
                ?: KotlinUtils.browsingDeliveryType,
            KotlinUtils.getDeliveryType()?.address?.placeId ?: "",
            isFromNewToggleFulfilmentScreen = true,
            newDelivery = Delivery.CNC,
            needStoreSelection = true,
        )
    }

    private fun launchGeoLocationFlow() {
        KotlinUtils.presentEditDeliveryGeoLocationActivity(
            activity,
            BundleKeysConstants.UPDATE_LOCATION_REQUEST,
            Delivery.getType(KotlinUtils.getDeliveryType()?.deliveryType) ?: KotlinUtils.browsingDeliveryType,
            KotlinUtils.getDeliveryType()?.address?.placeId ?: "",
            isLocationUpdateRequest = true,
            newDelivery = Delivery.getType(KotlinUtils.getDeliveryType()?.deliveryType) ?: KotlinUtils.browsingDeliveryType
        )
    }

    private fun openMoreOptionsDialog() {
        bindingListDetails.rlCheckOut.visibility = GONE
        setScrollViewBottomMargin(0)
        val count = if (isSingleItemSelected) {
            1
        } else {
            shoppingListItemsAdapter?.addedItemsCount ?: 0
        }

        viewModel.mShoppingListItems.forEach {
           if (it.isSelected) {
               listOfItems.add(AddToListRequest(skuID = it.catalogRefId, catalogRefId = it.catalogRefId, quantity = "1"))
           }
        }

        val fragment = MoreOptionDialogFragment.newInstance(
            this@ShoppingListDetailFragment,
            count,
            viewModel.listId,
            viewModel.isCheckedDontAskAgain(),
            listOfItems
        )
        fragment.show(parentFragmentManager, MoreOptionDialogFragment::class.simpleName)
    }

    private fun openProductSearchActivity() {
        with(requireActivity()) {
            val openProductSearchActivity = Intent(this, ProductSearchActivity::class.java)
            openProductSearchActivity.putExtra(
                EXTRA_SEARCH_TEXT_HINT, requireContext().getString(R.string.shopping_search_hint)
            )
            openProductSearchActivity.putExtra(
                EXTRA_LIST_ID, viewModel.listId
            )
            productSearchResult.launch(openProductSearchActivity)
            overridePendingTransition(0, 0)
        }
    }

    private fun onShoppingListItemsResponse(shoppingListItemsResponse: ShoppingListItemsResponse?) {
        if (!isAdded || !isVisible) return
        when (shoppingListItemsResponse?.httpCode) {
            HTTP_OK -> {
                bindingListDetails.loadingBar.visibility = GONE
                viewModel.syncListWithAdapter(shoppingListItemsAdapter?.shoppingListItems)
                viewModel.makeInventoryCalls()
                if (viewModel.isShoppingListContainsUnavailableItems())
                    showBlackToolTip()
                else
                    hideBlackToolTip()

                setUpView()
                shoppingListItemsAdapter?.setList(viewModel.mShoppingListItems)
                showRecommendedProducts(viewModel.mShoppingListItems)
            }
            HTTP_SESSION_TIMEOUT_440 -> SessionUtilities.getInstance().setSessionState(
                SessionDao.SESSION_STATE.INACTIVE,
                shoppingListItemsResponse.response.stsParams,
                activity
            )
            else -> {
                onShoppingListItemsResponseError(shoppingListItemsResponse)
            }
        }
    }

    private fun showRecommendedProducts(shoppingListItems: ArrayList<ShoppingListItem>) {
        val products = shoppingListItems.filter { !it.productId.isNullOrEmpty() }.map { Product(productId = it.productId) }
        if(products.isEmpty()) {
            return
        }
        val bundle = Bundle()
        bundle.putParcelable(
            BundleKeysConstants.RECOMMENDATIONS_EVENT_DATA,
            Recommendation.PageView(eventType = EVENT_TYPE_PAGEVIEW, url = EVENT_URL_SHOPPING_LIST, pageType = EVENT_PAGE_TYPE_SHOPPING_LIST)
        )
        bundle.putParcelable(
            BundleKeysConstants.RECOMMENDATIONS_EVENT_DATA_TYPE,
            Recommendation.ShoppingListEvent(eventType = EVENT_TYPE_PRODUCT_THUMBNAIL_VIEW, products = products)
        )
        bundle.putBoolean(BundleKeysConstants.RECOMMENDATIONS_DYNAMIC_TITLE_REQUIRED, true)
        loadRecommendations(bundle = bundle, fragmentManager = childFragmentManager)
    }

    private fun onShoppingListItemsResponseError(response: ShoppingListItemsResponse?) {
        if (!isAdded || !isVisible) return

        bindingListDetails.loadingBar.visibility = GONE
        enableAdapterClickEvent(true)
        response?.response?.desc?.let {
            Utils.displayValidationMessage(
                requireActivity(),
                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                response.response?.desc
            )
        }
    }

    private fun enableAdapterClickEvent(clickable: Boolean) {
        shoppingListItemsAdapter?.adapterClickable(clickable)
    }

    override fun onItemSelectionChange(isSelected: Boolean) {
        if (!isAdded || !isVisible) return
        if (isSelected)
            hideBlackToolTip()
        updateCartCountButton()
        manageSelectAllMenuVisibility()
    }

    override fun onShoppingListItemDelete(shoppingListItemsResponse: ShoppingListItemsResponse) {
        updateList()
        enableAdapterClickEvent(true)
        if (!viewModel.isShoppingListContainsUnavailableItems()) {
            hideBlackToolTip()
        }
    }

    override fun onItemDeleteClick(shoppingListItem: ShoppingListItem) {
        singleShoppingListItem = shoppingListItem
        selectedItemsForRemoval = 1
        isSingleItemSelected = true
        showDeleteConfirmationDialog()
    }

    private fun onDeleteUIUpdate() {
        val isStockAvailable =
            viewModel.getIsStockAvailable()
        if (!isStockAvailable) {
            bindingListDetails.rlCheckOut.visibility = GONE
            manageSelectAllMenuVisibility()
        }
        //update on delete item
        updateCartCountButton()
    }

    private fun showDeleteConfirmationDialog() {
        if (viewModel.isCheckedDontAskAgain()) {
            removeItemFromList()
        } else {
            val bottomsheetConfirmationDialog = ConfirmationBottomsheetDialogFragment().also {
                it.arguments = bundleOf(
                    AppConstant.Keys.BUNDLE_KEY to AppConstant.RESULT_DELETE_ITEM_CONFIRMED,
                    AppConstant.Keys.BUNDLE_KEY_SCREEN_NAME to AppConstant.SCREEN_NAME_DELETE_ITEM_CONFIRMATION
                )
            }
            bottomsheetConfirmationDialog.show(
                requireActivity().supportFragmentManager,
                ConfirmationBottomsheetDialogFragment::class.java.simpleName
            )
        }
    }

    override fun onShoppingSearchClick() {
        openProductSearchActivity()
    }

    private fun onAddToCartPreExecute() {
        bindingListDetails.pbLoadingIndicator.visibility = VISIBLE
        enableAddToCartButton(GONE)
    }

    fun onAddToCartSuccess(addItemToCartResponse: AddItemToCartResponse?, size: Int) {
        if (!isAdded || !isVisible || addItemToCartResponse == null) return
        val resultIntent = Intent()
        if (addItemToCartResponse.data?.isNotEmpty() == true) {
            val successMessage = addItemToCartResponse.data[0].message
            resultIntent.putExtra("addedToCartMessage", successMessage)
            resultIntent.putExtra(
                "ProductCountMap", Utils.toJson(
                    addItemToCartResponse.data[0].productCountMap
                )
            )
            resultIntent.putExtra("ItemsCount", size)
        }

        // reset selection after items added to cart
        shoppingListItemsAdapter?.resetSelection()

        bindingListDetails.pbLoadingIndicator.visibility = GONE
        bindingListDetails.btnCheckOut.visibility = VISIBLE

        // Present toast on BottomNavigationMenu if shopping list detail was opened from my list
        addItemToCartResponse.data?.get(0)?.let { addedToCartDatum ->
            if (openFromMyList) {
                (activity as? BottomNavigationActivity)?.apply {
                    onBackPressed()
                    if (addItemToCartResponse.data?.isNotEmpty() == true) {
                        val itemAddToCartMessage = addedToCartDatum.message
                        val productCountMap = addedToCartDatum.productCountMap
                        setToast(itemAddToCartMessage, "", productCountMap, size)
                    }
                }
            } else {
                addedToCartDatum.productCountMap?.let { productCountMap ->
                    // else display shopping list toast
                    when (getPreferredDeliveryType()) {
                        Delivery.DASH, Delivery.CNC ->
                            if (productCountMap.quantityLimit?.foodLayoutColour != null) {
                                showItemsLimitToastOnAddToCart(
                                    bindingListDetails.rlCheckOut,
                                    productCountMap,
                                    requireActivity(),
                                    size,
                                    true
                                )
                            } else {
                                buildAddToCartSuccessToast(
                                    bindingListDetails.rlCheckOut, true, requireActivity(), this
                                )
                            }
                        else -> buildAddToCartSuccessToast(
                            bindingListDetails.rlCheckOut, true, requireActivity(), this
                        )
                    }
                }
            }
        }
    }

    private fun confirmDeliveryLocation() {
        if (!isAdded || !isVisible) return
        val fragmentManager = this.childFragmentManager

        val confirmDeliveryLocationFragment = ConfirmDeliveryLocationFragment.newInstance()
        confirmDeliveryLocationFragment.isCancelable = false
        confirmDeliveryLocationFragment.show(
            fragmentManager,
            ConfirmDeliveryLocationFragment::class.java.simpleName
        )
    }

    private fun enableAddToCartButton(visible: Int) {
        bindingListDetails.btnCheckOut.visibility = visible
    }

    fun onSessionTokenExpired(response: Response) {
        SessionUtilities.getInstance()
            .setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.stsParams, activity)
    }

    fun otherHttpCode(response: Response) {
        if (isAdded) {
            bindingListDetails.pbLoadingIndicator.visibility = GONE
            enableAddToCartButton(VISIBLE)
            Utils.displayValidationMessage(
                activity,
                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                response.desc
            )
        }
    }

    override fun openProductDetailFragment(productName: String, productList: ProductList) {
        if (activity is BottomNavigationActivity) {
            val fragment = ProductDetailsFragment.newInstance()
            val gson = Gson()
            val strProductList = gson.toJson(productList)
            val bundle = Bundle()
            bundle.putString(ProductDetailsFragment.STR_PRODUCT_LIST, strProductList)
            bundle.putString(ProductDetailsFragment.STR_PRODUCT_CATEGORY, productName)
            fragment.arguments = bundle
            val bottomNavigationActivity = activity as BottomNavigationActivity?
            bottomNavigationActivity!!.pushFragment(fragment)
        }
    }

    private fun addFragmentListener() {
        activity ?: return
        requireActivity().supportFragmentManager.setFragmentResultListener(
            ADDED_TO_SHOPPING_LIST_RESULT_CODE.toString(),
            requireActivity()
        ) { _, result: Bundle ->
            if (result.containsKey("listItems")) {
                val count = result.getInt("listItems", 0)
                buildShoppingListFromSearchResultToast(
                    requireActivity(), bindingListDetails.rlCheckOut, listName!!, count
                )
            }
            viewModel.getShoppingListDetails()
            setFragmentResult(REFRESH_SHOPPING_LIST_RESULT_CODE.toString(), result)
        }

        listenerForUnsellable()

        setFragmentResultListener(REQUEST_KEY_CONFIRMATION_DIALOG) { _, bundle ->
            val result = bundle.getString(AppConstant.Keys.BUNDLE_KEY)
            val isCheckedDontAskAgain =
                bundle.getBoolean(AppConstant.Keys.BUNDLE_KEY_DONT_ASK_AGAIN_CHECKED, false)
            viewModel.setIsCheckedDontAskAgain(isCheckedDontAskAgain)
            if (result == AppConstant.RESULT_DELETE_ITEM_CONFIRMED) {
                removeItemFromList()
            }
        }
        setFragmentResultListener(UnsellableUtils.ADD_TO_LIST_SUCCESS_RESULT_CODE) { _, _ ->
            UpdateScreenLiveData.value=updateUnsellableLiveData
        }

        setFragmentResultListener(MoreOptionDialogFragment.MORE_OPTION_CANCEL_CLICK_LISTENER.toString()) { _, _->
            if (viewModel.isItemSelected(shoppingListItemsAdapter?.shoppingListItems)) {
                bindingListDetails.rlCheckOut.visibility = VISIBLE
            }
        }

        setFragmentResultListener(AddToListFragment.ADD_TO_SHOPPING_LIST_REQUEST_CODE.toString()) { _, bundle ->
            val selectedLists: java.util.ArrayList<ShoppingList>? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelableArrayList(
                        AppConstant.Keys.KEY_LIST_DETAILS,
                        ShoppingList::class.java
                    )
                } else {
                    bundle.get(AppConstant.Keys.KEY_LIST_DETAILS) as?
                            java.util.ArrayList<ShoppingList>
                }

            val listName =
                if (selectedLists?.size == 1) {
                    selectedLists.getOrNull(0)?.listName ?: ""
                } else {
                    activity?.getString(R.string.multiple_lists)?: ""
                }
            val count =  bundle.getInt(AppConstant.Keys.KEY_COUNT, 0)
            val title = activity?.resources?.getQuantityString(
                R.plurals.added_to_list,
                count,
                count, listName
            )?: ""
            shoppingListItemsAdapter?.resetSelection()
            showSuccessMessage(listName = listName, title = title)
        }
    }

    override fun onDetach() {
        super.onDetach()
        cancelRequest(mPostAddToCart)
//        cancelRequest(mGetInventorySkusForStore)
    }

    private fun cancelRequest(call: Call<*>?) {
        if (call != null && !call.isCanceled) {
            call.cancel()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (activity == null || !isAdded) return
        if (showMenu) {
            bindingListDetails.selectDeselectAllTextView.visibility = VISIBLE
            bindingListDetails.selectDeselectAllTextView.text = requireContext().getString(
                if (isSelectAll)
                    R.string.select_all
                else
                    R.string.deselect_all
            )
        } else bindingListDetails.selectDeselectAllTextView.visibility = GONE

        super.onCreateOptionsMenu(menu, inflater)
    }

    fun onOptionsItemSelected() {
        if (!isAdded) return
        if (shouldUserSetSuburb()) {
            deliverySelectionIntent(DELIVERY_LOCATION_REQUEST_CODE_FROM_SELECT_ALL)
            return
        }
        if (TOOLBAR_SELECT_ALL.equals(
                bindingListDetails.selectDeselectAllTextView.text.toString(),
                ignoreCase = true
            )
        ) {
            selectAllListItems(true)
            bindingListDetails.selectDeselectAllTextView.text = getString(R.string.deselect_all)
        } else {
            selectAllListItems(false)
            bindingListDetails.selectDeselectAllTextView.text = getString(R.string.select_all)
        }
    }

    override fun onEmptyCartRetry() {
        openProductSearchActivity()
    }

    private fun setUpAddToCartButton() {
        bindingListDetails.btnCheckOut.setOnClickListener(this)
        updateCartCountButton()
    }

    private fun updateCartCountButton() {
        val itemWasSelected = viewModel.isItemSelected(shoppingListItemsAdapter?.shoppingListItems)

        // if no item then hide bottom view
        if (viewModel.mShoppingListItems.size == 0) {
            bindingListDetails.rlCheckOut.visibility = GONE
            setScrollViewBottomMargin(0)
            return
        }

        if (itemWasSelected) {
            bindingListDetails.rlCheckOut.visibility = VISIBLE
            val count = shoppingListItemsAdapter?.addedItemsCount ?: 0
            bindingListDetails.btnCheckOut.text =
                requireContext().resources.getQuantityString(
                    R.plurals.plural_add_to_cart,
                    count,
                    count
                )
            bindingListDetails.selectDeselectAllTextView.visibility = VISIBLE
            setScrollViewBottomMargin(Utils.dp2px(60f))
            (activity as? BottomNavigationActivity)?.hideBottomNavigationMenu()
        } else {
            bindingListDetails.rlCheckOut.visibility = GONE
            setScrollViewBottomMargin(0)
            (activity as? BottomNavigationActivity)?.showBottomNavigationMenu()
        }
    }

    private fun setScrollViewBottomMargin(margin: Int) {
        val layoutParams : ConstraintLayout.LayoutParams? = bindingListDetails.nestedScrollView.layoutParams as? ConstraintLayout.LayoutParams
        layoutParams?.bottomMargin = margin
    }

    private fun executeAddToCart(items: ArrayList<ShoppingListItem>?) {
        onAddToCartPreExecute()
        val selectedItems: MutableList<AddItemToCart> = ArrayList(0)
        for (item in items!!) {
            if (item.isSelected && item.quantityInStock > 0) selectedItems.add(
                if (isEnhanceSubstitutionFeatureAvailable()) {
                    AddItemToCart(
                        item.productId,
                        item.catalogRefId,
                        item.userQuantity,
                        SubstitutionChoice.SHOPPER_CHOICE.name,
                        ""
                    )
                } else {
                    AddItemToCart(
                        item.productId,
                        item.catalogRefId,
                        item.userQuantity
                    )
                }

            )
        }
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.SHOP_MY_LIST_ADD_TO_CART,
            activity
        )
        mPostAddToCart = postAddItemToCart(ArrayList(selectedItems))
    }

    fun manageSelectAllMenuVisibility() {
        showMenu = false
        isSelectAll = false
        viewModel.mShoppingListItems
            .filter { it.quantityInStock > 0 }
            .forEach { shoppingListItem ->
                // This condition to verify to show select all or Deselect All
                if (!shoppingListItem.isSelected) {
                    isSelectAll = true
                    showMenu = true
                    return@forEach
                }
                showMenu = true
            }
        requireActivity().invalidateOptionsMenu()
    }

    private fun selectAllListItems(setSelection: Boolean) {
        shoppingListItemsAdapter?.apply {
            shoppingListItems ?: return
            shoppingListItems?.forEach { item ->
                if (item.quantityInStock > 0) {
                    item.isSelected = setSelection
                    item.userQuantity = item.userQuantity.coerceAtLeast(1)
                }
            }
            viewModel.mShoppingListItems = ArrayList(shoppingListItems)
            notifyDataSetChanged()
            onItemSelectionChange(setSelection)
        }
    }

    override fun onConnectionChanged() {
        if (viewModel.inventoryCallFailed) {
            viewModel.makeInventoryCalls()
            if (viewModel.isShoppingListContainsUnavailableItems())
                showBlackToolTip()
            else
                hideBlackToolTip()
        }
        if (addedToCart()) {
            addItemsToCart()
            addedToCartFail(false)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().apply {
            Utils.setScreenName(this, SHOPPING_LIST_ITEMS)
            registerReceiver(
                mConnectionBroadcast,
                IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
            )
        }
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(mConnectionBroadcast)
        UpdateScreenLiveData.removeObservers(viewLifecycleOwner)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            (activity as? BottomNavigationActivity)?.showBottomNavigationMenu()
           listenerForUnsellable()
            arguments?.apply {
                listName = getString(ARG_LIST_NAME, "")
                openFromMyList = getBoolean(ARG_OPEN_FROM_MY_LIST, false)
            }
            setUpToolbar(listName)
        }
    }

    override fun onToastButtonClicked(currentState: String) {}

    private fun addItemsToCart() {
        executeAddToCart(viewModel.mShoppingListItems)
    }

    private fun getInventoryForStoreSuccess(skusInventoryForStoreResponse: SkusInventoryForStoreResponse?) {
        if (skusInventoryForStoreResponse?.httpCode == HTTP_OK) {
            shoppingListItemsAdapter?.setList(
                viewModel.mShoppingListItems
            )
            enableAdapterClickEvent(true)
        } else {
            onInventoryError(skusInventoryForStoreResponse)
        }
    }

    private fun onInventoryError(response: SkusInventoryForStoreResponse?) {
        // Hide quantity progress bar indicator
        viewModel.setOutOfStock()
        updateList()
        enableAdapterClickEvent(true)
        if (!errorMessageWasPopUp) {
            response?.response?.desc?.let {
                errorMessageWasPopUp = true
                Utils.displayValidationMessage(
                    activity,
                    CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                    it
                )
            }
        }
    }

    private fun updateList() {
        setUpView()
        shoppingListItemsAdapter?.setList(
            viewModel.mShoppingListItems
        )
    }

    override fun openSetSuburbProcess(shoppingListItem: ShoppingListItem) {
        viewModel.mOpenShoppingListItem = shoppingListItem
        deliverySelectionIntent(DELIVERY_LOCATION_REQUEST)
    }

    override fun onAddListItemCount(shoppingListItem: ShoppingListItem) {
        viewModel.mOpenShoppingListItem = shoppingListItem
        viewModel.setItem(shoppingListItem)
        updateCartCountButton()
    }

    override fun onSubstractListItemCount(listItem: ShoppingListItem) {
        viewModel.mOpenShoppingListItem = listItem
        viewModel.setItem(listItem)
        updateCartCountButton()
    }

    override fun showListBlackToolTip() {
        showBlackToolTip()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            DELIVERY_LOCATION_REQUEST_CODE_FROM_SELECT_ALL,
            DELIVERY_LOCATION_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    setDeliveryLocation()
                    if (viewModel.isShoppingListContainsUnavailableItems())
                        showBlackToolTip()
                    else
                        hideBlackToolTip()
                    viewModel.getShoppingListDetails()
                }
            }
            ShopToggleActivity.REQUEST_DELIVERY_TYPE, BundleKeysConstants.UPDATE_LOCATION_REQUEST, BundleKeysConstants.UPDATE_STORE_REQUEST ->{
                if (resultCode == RESULT_OK) {

                    val toggleFulfilmentResultWithUnsellable= UnsellableAccess.getToggleFulfilmentResultWithUnSellable(data)
                    if(toggleFulfilmentResultWithUnsellable!=null){
                        screenRefresh()
                        UnsellableAccess.navigateToUnsellableItemsFragment(ArrayList(toggleFulfilmentResultWithUnsellable.unsellableItemsList),
                            toggleFulfilmentResultWithUnsellable.deliveryType,confirmAddressViewModel,
                            bindingListDetails.loadingBar,this,parentFragmentManager)
                    }

                    setDeliveryLocation()
                    if (viewModel.isShoppingListContainsUnavailableItems())
                        showBlackToolTip()
                    else
                        hideBlackToolTip()
                    viewModel.getShoppingListDetails()
                }
            }
        }
    }

    private fun setDeliveryLocation() {
        val shoppingDeliveryLocation = Utils.getPreferredDeliveryLocation()
        if (shoppingDeliveryLocation?.fulfillmentDetails == null || !isAdded || !isVisible) return
        view?.apply {
            setDeliveryAndLocation(
                activity,
                shoppingDeliveryLocation.fulfillmentDetails,
                bindingListDetails.fulfilmentAndLocationLayout.layoutFulfilment.tvTitle,
                bindingListDetails.fulfilmentAndLocationLayout.layoutLocation.tvTitle,
            )
        }
    }

    private fun deliverySelectionIntent(resultCode: Int) {
        val activity = activity ?: return
        presentEditDeliveryGeoLocationActivity(
            activity, resultCode, getPreferredDeliveryType(), getPlaceId(),
            isFromDashTab = false,
            isComingFromCheckout = false,
            isComingFromSlotSelection = false,
            isMixedBasket = false,
            isFBHOnly = false,
            savedAddressResponse = null,
            defaultAddress = null,
            whoISCollecting = null,
            liquorCompliance = null
        )
    }

    private fun postAddItemToCart(addItemToCart: ArrayList<AddItemToCart>): Call<AddItemToCartResponse> {
        onAddToCartPreExecute()
        addedToCartFail(false)
        val postItemToCart = PostItemToCart()
        return postItemToCart.make(
            addItemToCart.toMutableList(),
            object : IResponseListener<AddItemToCartResponse> {
                override fun onSuccess(response: AddItemToCartResponse?) {
                    addedToCartFail(false)
                    if (!isAdded || !isVisible) return
                    when (response?.httpCode) {
                        HTTP_OK -> onAddToCartSuccess(
                            response,
                            getTotalItemQuantity(addItemToCart)
                        )
                        AppConstant.HTTP_EXPECTATION_FAILED_417 -> {                         // Preferred Delivery Location has been reset on server
                            // As such, we give the user the ability to set their location again
                            if (response.response != null) confirmDeliveryLocation()
                        }
                        HTTP_SESSION_TIMEOUT_440 -> {
                            response.response?.let { onSessionTokenExpired(it) }
                        }
                        AppConstant.HTTP_EXPECTATION_FAILED_502 -> {
                            if (response.response.code == AppConstant.RESPONSE_ERROR_CODE_1235) {
                                bindingListDetails.pbLoadingIndicator.visibility = GONE
                                showQuantityLimitErrror(
                                    requireActivity().supportFragmentManager,
                                    response.response.desc,
                                    "",
                                    context
                                )
                                enableAddToCartButton(VISIBLE)
                            }
                        }
                        else -> response?.response?.let {
                            otherHttpCode(it)
                        }
                    }
                }

                override fun onFailure(error: Throwable?) {
                    addedToCartFail(true)
                    if (!isAdded || !isVisible) return
                    bindingListDetails.pbLoadingIndicator.visibility = GONE
                    bindingListDetails.btnCheckOut.visibility = VISIBLE

                }
            })
    }

    fun addedToCartFail(addedToCart: Boolean) {
        this.addedToCart = addedToCart
    }

    fun addedToCart(): Boolean {
        return addedToCart
    }

    override fun onToastButtonClicked(jsonElement: JsonElement?) {
        //Nav stack change since not using Cart activity anymore
        // Pass back result to BottomNavigation activity.
        activity?.apply {
            setResult(BottomNavigationActivity.RESULT_OK_OPEN_CART_FROM_SHOPPING_DETAILS)
            finishActivity(ScreenManager.SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE)
            overridePendingTransition(0, 0)
        }
    }

    private fun shouldUserSetSuburb(): Boolean {
        val shoppingDeliveryLocation = Utils.getPreferredDeliveryLocation()
        return shoppingDeliveryLocation == null
    }

    override fun onConfirmLocation() {
        addItemsToCart()
    }

    override fun onSetNewLocation() {
        deliverySelectionIntent(DELIVERY_LOCATION_REQUEST)
    }

    fun getTotalItemQuantity(addItemToCart: ArrayList<AddItemToCart>): Int {
        var totalQuantity = 0
        for (item in addItemToCart) {
            totalQuantity += item.quantity
        }
        return totalQuantity
    }

    override fun onDestroy() {
        (activity as? BottomNavigationActivity)?.showToolbar()
        super.onDestroy()
    }

    override fun onRecommendationsLoadedSuccessfully() {
        if(isAdded) {
            setRecommendationDividerVisibility(visibility = !shoppingListItemsAdapter?.shoppingListItems.isNullOrEmpty())
        }
    }

    private fun setRecommendationDividerVisibility(visibility: Boolean) {
        bindingListDetails.viewRecommendationDivider.visibility = if(visibility) VISIBLE else GONE
    }

    companion object {
        const val QUANTITY_CHANGED_FROM_LIST = 2010
        const val ADD_TO_CART_SUCCESS_RESULT = 2000
        private const val DELIVERY_LOCATION_REQUEST_CODE_FROM_SELECT_ALL = 1222
        private const val DELIVERY_LOCATION_REQUEST = 2
        private const val TOOLBAR_SELECT_ALL: String = "SELECT ALL"
        const val ARG_LIST_NAME: String = "listName"
        private const val ARG_OPEN_FROM_MY_LIST: String = "openFromMyList"
        private const val EXTRA_LIST_ITEMS: String = "listItems"

        // constants for deletion confirmation.
        private const val ON_CONFIRM_REMOVE_WITH_DELETE_ICON_PRESSED =
            "remove_with_delete_icon_pressed"
    }
    private fun listenerForUnsellable(){
        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_DISMISS_RESULT) { _, bundle ->
            val resultCode =
                bundle.getString(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT)
            if (resultCode == UnsellableUtils.ADD_TO_LIST_SUCCESS_RESULT_CODE) {
                UpdateScreenLiveData.value=updateUnsellableLiveData
            }
        }
        setFragmentResultListener(UnsellableUtils.ADD_TO_LIST_SUCCESS_RESULT_CODE) { _, _ ->
            UpdateScreenLiveData.value=updateUnsellableLiveData

        }
    }
    override fun itemEditOptionsClick(editOptionType: EditOptionType) {
        when (editOptionType) {

            is EditOptionType.RemoveItemFromList -> {
                removeItemFromList()
            }
            is EditOptionType.CopyItemFromList -> {
                copytemFromList(editOptionType.list)
            }
            is EditOptionType.MoveItemFromList -> {
                moveItemFromList(editOptionType.list)
            }
            else -> {}
        }
    }

    private fun removeItemFromList() {
        val selectedItems  = ArrayList<String>()
        if (isSingleItemSelected) {
            singleShoppingListItem?.Id?.let {
                selectedItems.add(it)
            }
            selectedItemsForRemoval = 1
        } else {
            for (item in viewModel.mShoppingListItems) {
                if (item.isSelected == true) {
                    selectedItems.add(item.Id)
                }
            }
            selectedItemsForRemoval = selectedItems.size
        }
        val removeItemApiRequest = RemoveItemApiRequest(selectedItems)
        viewModel.removeMultipleItemsFromList(viewModel.listId, removeItemApiRequest)
    }

    override fun naviagteToMoreOptionDialog(shoppingListItem: ShoppingListItem) {
        isSingleItemSelected = true
        singleShoppingListItem = shoppingListItem
        openMoreOptionsDialog()
    }

    private fun copytemFromList(shoppingList: ArrayList<ShoppingList>) {
        prepareRequestForCopyOrMoveItem(shoppingList)
        val copyItemToListRequest =
            CopyItemToListRequest(items = selectedItems, giftListIds = shoppingListId)
        viewModel.copyMultipleItemsFromList(copyItemToListRequest)
    }

    private fun moveItemFromList(shoppingList: ArrayList<ShoppingList>) {
        prepareRequestForCopyOrMoveItem(shoppingList)
        val moveItemApiRequest = MoveItemApiRequest(
            items = selectedItems,
            giftListIds = shoppingListId,
            removalGiftItemIds = removalGiftItemIds,
            sourceGiftListId = viewModel.listId
        )
        lifecycleScope.launch {
            viewModel.moveItemsFromList(moveItemApiRequest)
        }
    }

    private fun prepareRequestForCopyOrMoveItem(shoppingList: ArrayList<ShoppingList>) {
        shoppingListId.clear()
        selectedItems.clear()
        removalGiftItemIds.clear()
        selectedShoppingList = shoppingList
        shoppingList.forEach {
            shoppingListId.add(it.listId)
        }
        if (isSingleItemSelected) {
            singleShoppingListItem?.let {
                selectedItems.add(ItemDetail(
                    skuID = it.catalogRefId,
                    catalogRefId = it.catalogRefId,
                    quantity = "1"
                ))
                removalGiftItemIds.add(it.Id)
            }
        } else {
            for (item in viewModel.mShoppingListItems) {
                if (item.isSelected == true) {
                    selectedItems.add(ItemDetail(skuID = item.catalogRefId, catalogRefId = item.catalogRefId, quantity = "1"))
                    removalGiftItemIds.add(item.Id)
                }
            }
        }
    }
}