package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
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
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ShoppingListDetailFragmentBinding
import com.google.gson.Gson
import com.google.gson.JsonElement
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.ScreenNames.Companion.SHOPPING_LIST_ITEMS
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.contracts.IToastInterface
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.isEnhanceSubstitutionFeatureAvailable
import za.co.woolworths.financial.services.android.geolocation.GeoUtils.Companion.getPlaceId
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Product
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Recommendation
import za.co.woolworths.financial.services.android.recommendations.presentation.RecommendationLoader
import za.co.woolworths.financial.services.android.recommendations.presentation.RecommendationLoaderImpl
import za.co.woolworths.financial.services.android.recommendations.presentation.RecommendationLoadingNotifier
import za.co.woolworths.financial.services.android.shoptoggle.presentation.ShopToggleActivity
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity.EXTRA_LIST_ID
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity.EXTRA_SEARCH_TEXT_HINT
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity.PRODUCT_SEARCH_ACTIVITY_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListItemsAdapter
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
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.buildAddToCartSuccessToast
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.buildShoppingListFromSearchResultToast
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.showItemsLimitToastOnAddToCart
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_SESSION_TIMEOUT_440
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
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
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.Delivery

@AndroidEntryPoint
class ShoppingListDetailFragment : Fragment(), View.OnClickListener, EmptyCartInterface,
    NetworkChangeListener, ToastInterface, ShoppingListItemsNavigator, IToastInterface,
    IOnConfirmDeliveryLocationActionListener, RecommendationLoadingNotifier, RecommendationLoader by RecommendationLoaderImpl() {

    private val viewModel: ShoppingListDetailViewModel by viewModels()

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
    private var mId: String  = ""
    private var mProductId: String = ""
    private var mCatalogRefId: String = ""
    private var mShouldUpdateShoppingList: Boolean = false

    private var _bindingListDetails: ShoppingListDetailFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val bindingListDetails get() = _bindingListDetails!!

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
    }

    private fun setUpToolbar(listName: String?) {
        bindingListDetails.shoppingListTitleTextView.text = listName
        (activity as? BottomNavigationActivity)?.apply {
            bindingListDetails.appbar.visibility = VISIBLE
            hideToolbar()
            showBackNavigationIcon(true)
            setToolbarBackgroundDrawable(R.drawable.appbar_background)
            bindingListDetails.btnBack.setOnClickListener { onBackPressed() }
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

    override fun onItemDeleteClick(
        id: String,
        productId: String,
        catalogRefId: String,
        shouldUpdateShoppingList: Boolean,
    ) {
        mId = id
        mProductId = productId
        mCatalogRefId = catalogRefId
        mShouldUpdateShoppingList = shouldUpdateShoppingList
        showDeleteConfirmationDialog(ON_CONFIRM_REMOVE_WITH_DELETE_ICON_PRESSED)
    }

    private fun onItemDeleteApiCall() {
        if (mId?.isEmpty() == true || mProductId?.isEmpty() == true || mCatalogRefId?.isEmpty() == true) {
            return
        }

        if (viewModel.listId.isEmpty()) {
            return
        }

        val listSize = shoppingListItemsAdapter?.shoppingListItems?.size ?: 0
        if (listSize == 1) {
            if (!mShouldUpdateShoppingList) {
                bindingListDetails.rlEmptyListView.visibility = VISIBLE
                bindingListDetails.rcvShoppingListItems.visibility = GONE
                showMenu = false
                val activity: Activity? = activity
                activity?.invalidateOptionsMenu()
                bindingListDetails.rlCheckOut.visibility = GONE
            }
        }
        val shoppingListItemsResponseCall = OneAppService().deleteShoppingListItem(
            viewModel.listId, mId, mProductId, mCatalogRefId
        )
        bindingListDetails.loadingBar.visibility = VISIBLE
        shoppingListItemsResponseCall.enqueue(
            CompletionHandler(
                object : IResponseListener<ShoppingListItemsResponse> {
                    override fun onSuccess(response: ShoppingListItemsResponse?) {
                        bindingListDetails.loadingBar.visibility = GONE
                        val currentList =
                            shoppingListItemsAdapter?.shoppingListItems ?: ArrayList(0)

                        val updatedList =
                            response?.listItems?.let { ArrayList(it) } ?: ArrayList(0)

                        when(updatedList.size) {
                            currentList.size.minus(1) -> {
                                shoppingListItemsAdapter?.deleteListItem(mCatalogRefId)
                            }
                            else -> {
                                viewModel.mShoppingListItems = updatedList
                                viewModel.onDeleteSyncList(currentList)
                                response?.let { onShoppingListItemDelete(it) }
                            }
                        }
                        onDeleteUIUpdate()
                    }

                    override fun onFailure(error: Throwable?) {
                        bindingListDetails.loadingBar.visibility = GONE
                        if (mShouldUpdateShoppingList) onDeleteItemFailed()
                    }
                }, ShoppingListItemsResponse::class.java
            )
        )
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

    private fun showDeleteConfirmationDialog(resultCode: String) {
        val customBottomSheetDialogFragment =
            CustomBottomSheetDialogFragment.newInstance(
                getString(R.string.are_you_sure),
                getString(R.string.delete_confirmation_list_text),
                getString(R.string.remove),
                getString(R.string.cancel),
                resultCode
            )
        customBottomSheetDialogFragment.show(
            requireFragmentManager(),
            CustomBottomSheetDialogFragment::class.java.simpleName
        )
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

    override fun onDeleteItemFailed() {
        if (!isAdded) return
        mErrorHandlerView?.showToast()
        updateList()
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
        }

        setFragmentResultListener(ON_CONFIRM_REMOVE_WITH_DELETE_ICON_PRESSED) { _, _ ->
            onItemDeleteApiCall()
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
        } else {
            bindingListDetails.rlCheckOut.visibility = GONE
            setScrollViewBottomMargin(0)
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
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            (activity as? BottomNavigationActivity)?.showBottomNavigationMenu()
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
}