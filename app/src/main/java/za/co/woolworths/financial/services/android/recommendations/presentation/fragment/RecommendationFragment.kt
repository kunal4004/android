package za.co.woolworths.financial.services.android.recommendations.presentation.fragment

import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.RecommendationsLayoutBinding
import com.google.gson.Gson
import com.skydoves.balloon.balloon
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.isEnhanceSubstitutionFeatureAvailable
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.app_config.EnhanceSubstitution
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Action
import za.co.woolworths.financial.services.android.recommendations.data.response.request.CommonRecommendationEvent
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationEvent
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest
import za.co.woolworths.financial.services.android.recommendations.presentation.RecommendationEventHandler
import za.co.woolworths.financial.services.android.recommendations.presentation.RecommendationLoadingNotifier
import za.co.woolworths.financial.services.android.recommendations.presentation.adapter.ProductCategoryAdapter
import za.co.woolworths.financial.services.android.recommendations.presentation.adapter.ProductListRecommendationAdapter
import za.co.woolworths.financial.services.android.recommendations.presentation.viewmodel.RecommendationViewModel
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.adapters.holder.RecyclerViewViewHolderItems
import za.co.woolworths.financial.services.android.ui.extension.isConnectedToNetwork
import za.co.woolworths.financial.services.android.ui.views.AddedToCartBalloonFactory
import za.co.woolworths.financial.services.android.ui.views.ToastFactory
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SelectYourQuantityFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.*

@AndroidEntryPoint
class RecommendationFragment :
    BaseFragmentBinding<RecommendationsLayoutBinding>(RecommendationsLayoutBinding::inflate),
    IProductListing {

    companion object {
        private const val QUERY_INVENTORY_FOR_STORE_REQUEST_CODE = 3343
        const val ITEM_TYPE_FOOD = "Food"
    }

    private var isUserBrowsing: Boolean = false
    private var oneTimeInventoryErrorDialogDisplay: Boolean = false
    private var _recommendationsLayoutBinding: RecommendationsLayoutBinding? = null
    private val recommendationsLayoutBinding get() = _recommendationsLayoutBinding!!
    private val recommendationViewModel: RecommendationViewModel by viewModels(ownerProducer = { requireParentFragment().requireParentFragment() })
    private var mProductCategoryAdapter: ProductCategoryAdapter? = null
    private var mProductListRecommendationAdapter: ProductListRecommendationAdapter? = null
    private var recommendationLayoutManager: LinearLayoutManager? = null
    private var isViewItemListEventTriggeredOnPageLoad = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _recommendationsLayoutBinding =
            RecommendationsLayoutBinding.inflate(inflater, container, false)
        return recommendationsLayoutBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getRecommendationDetails()
    }

    private fun showProductCategory(actionItemList: List<Action>) {
        context?.let {
            recommendationsLayoutBinding?.recommendationsCategoryRecyclerview?.layoutManager =
                LinearLayoutManager(it, RecyclerView.HORIZONTAL, false)
            mProductCategoryAdapter = ProductCategoryAdapter(actionItemList)
        }
        recommendationsLayoutBinding?.recommendationsCategoryRecyclerview?.adapter =
            mProductCategoryAdapter

        actionItemList.getOrNull(0)?.products?.let {
            if(!isViewItemListEventTriggeredOnPageLoad) {
                submitViewItemListFirebaseEvent(actionItemList, 0)
                isViewItemListEventTriggeredOnPageLoad = true
            }
            showRecProductsList(it)
        }

        mProductCategoryAdapter?.onItemClick = { position, products ->
            recommendationViewModel.setCurrentSelectedTab(position)
            submitViewItemListFirebaseEvent(actionItemList, position)
            showRecProductsList(products)
        }
    }

    private fun submitViewItemListFirebaseEvent(actionItemList: List<Action>, position: Int) {
        if (actionItemList.isNotEmpty() && position >= 0 && position < actionItemList.size) {
            FirebaseAnalyticsEventHelper.viewItemListRecommendations(
                products = actionItemList[position].products,
                category = actionItemList[position].componentName
            )
        }
    }

    private fun showRecProductsList(productsList: List<ProductList>?) {
        if (productsList.isNullOrEmpty()) {
            recommendationsLayoutBinding?.recommendationsProductsRecyclerview?.visibility =
                View.GONE
        } else {
            recommendationsLayoutBinding.recommendationsProductsRecyclerview.clearOnScrollListeners()
            recommendationsLayoutBinding?.recommendationsProductsRecyclerview?.addOnScrollListener(
                recommendationProductsScrollListener
            )
            recommendationsLayoutBinding?.recommendationsProductsRecyclerview?.visibility =
                View.VISIBLE
            context?.let {
                recommendationLayoutManager =
                    LinearLayoutManager(it, LinearLayoutManager.HORIZONTAL, false)
                recommendationsLayoutBinding.recommendationsProductsRecyclerview.layoutManager =
                    recommendationLayoutManager

                mProductListRecommendationAdapter =
                    ProductListRecommendationAdapter(productsList, this, activity)
            }
            recommendationsLayoutBinding?.recommendationsProductsRecyclerview?.adapter =
                mProductListRecommendationAdapter
        }
    }

    private fun getRecommendationDetails() {
        recommendationViewModel.clearSubmittedRecImpressions()
        val bundle = arguments?.getBundle(BundleKeysConstants.BUNDLE)
        val reccommendationsDataEventTypeFirst =
            bundle?.getParcelable<RecommendationEvent>(BundleKeysConstants.RECOMMENDATIONS_EVENT_DATA) as RecommendationEvent
        val reccommendationsDataEventTypeSecond =
            bundle?.getParcelable<RecommendationEvent>(BundleKeysConstants.RECOMMENDATIONS_EVENT_DATA_TYPE) as RecommendationEvent
        val dynamicTitleRequired = bundle.getBoolean(BundleKeysConstants.RECOMMENDATIONS_DYNAMIC_TITLE_REQUIRED, false)

        var recMonetateId: String? = null
        if (Utils.getMonetateId() != null) {
            recMonetateId = Utils.getMonetateId()
        }

        val recommendationRequest = RecommendationRequest(
            events = listOf(
                reccommendationsDataEventTypeFirst,
                reccommendationsDataEventTypeSecond,
            ).plus(CommonRecommendationEvent.commonRecommendationEvents()),
            monetateId = recMonetateId
        )

        recommendationViewModel.getRecommendationResponse(recommendationRequest)

        recommendationViewModel.recommendationResponseData.observe(viewLifecycleOwner) { actionItems ->
            if (actionItems.isNullOrEmpty()) {
                recommendationsLayoutBinding.recommendationsMainLayout.visibility = View.GONE
            } else {
                recommendationsLayoutBinding.recommendationsMainLayout.visibility = View.VISIBLE
                recommendationsLayoutBinding.recommendationsText.text = getDynamicTitle(dynamicTitleRequired)
                val eventHandler = parentFragment?.parentFragment
                if(eventHandler is RecommendationLoadingNotifier) {
                    eventHandler.onRecommendationsLoadedSuccessfully()
                }
                showProductCategory(actionItems)
            }
        }

        recommendationViewModel.visibleRecommendationItemRequest.observe(viewLifecycleOwner) { visibleProductsRequested ->
            if (visibleProductsRequested == true) {
                getVisibleProductsPosition()?.let {
                    recommendationViewModel.visibleRecommendationProducts(
                        it
                    )
                }
            }
        }
    }

    private fun getDynamicTitle(dynamicTitleRequired: Boolean): String {
        val title = recommendationViewModel.recommendationTitle()
        return if(dynamicTitleRequired && !title.isNullOrEmpty()) {
            title
        } else {
            getString(R.string.recommendations_title)
        }
    }

    override fun onDestroyView() {
        _recommendationsLayoutBinding = null
        super.onDestroyView()
    }

    override fun openProductDetailView(productList: ProductList) {
        if(isConnectedToNetwork() == true) {
            WoolworthsApplication.getInstance().recommendationAnalytics.submitRecClicks(products = listOf(productList))
        }
        // Move to shop tab.
        if (requireActivity() !is BottomNavigationActivity) {
            return
        }
        val bottomNavigationActivity = requireActivity() as BottomNavigationActivity
        bottomNavigationActivity.bottomNavigationById.currentItem =
            BottomNavigationActivity.INDEX_PRODUCT
        val productDetails = ProductDetails()
        productDetails.externalImageRefV2 = productList.externalImageRefV2
        productDetails.productName = productList.productName
        productDetails.productId = productList.productId
        productDetails.sku = "6009223457053"
        openProductDetailFragment("", productDetails)
    }

    fun openProductDetailFragment(productName: String?, productDetails: ProductDetails?) {
        if (requireActivity() !is BottomNavigationActivity || !isAdded) {
            return
        }
        val strProductList = Gson().toJson(productDetails)
        // Move to shop tab first.
        (requireActivity() as? BottomNavigationActivity)?.apply {
            BottomNavigationActivity.preventShopTooltip = true
            onShopTabSelected(bottomNavigationById.menu[BottomNavigationActivity.INDEX_PRODUCT])
        }
        ScreenManager.openProductDetailFragment(requireActivity(), productName, strProductList)
    }

    override fun queryInventoryForStore(
        fulfilmentTypeId: String,
        addItemToCart: AddItemToCart?,
        productList: ProductList,
    ) {
        if (_recommendationsLayoutBinding?.incCenteredProgress?.root?.visibility == View.VISIBLE) return // ensure one api runs at a time

        val storeId =
            fulfilmentTypeId.let { it1 -> RecyclerViewViewHolderItems.getFulFillmentStoreId(it1) }
                ?: ""
        val activity = activity ?: return

        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSigninActivity(
                activity,
                QUERY_INVENTORY_FOR_STORE_REQUEST_CODE,
                isUserBrowsing
            )
            return
        }

        if (storeId.isEmpty()) {
            addItemToCart?.catalogRefId?.let { skuId -> productOutOfStockErrorMessage() }
            return
        }

        showProgressBar()
        OneAppService().getInventorySkuForStore(
            storeId, addItemToCart?.catalogRefId
                ?: "", isUserBrowsing
        ).enqueue(CompletionHandler(object : IResponseListener<SkusInventoryForStoreResponse> {
            override fun onSuccess(skusInventoryForStoreResponse: SkusInventoryForStoreResponse?) {
                if (!isAdded) return
                dismissProgressBar()
                oneTimeInventoryErrorDialogDisplay = false
                with(activity.supportFragmentManager.beginTransaction()) {
                    when (skusInventoryForStoreResponse?.httpCode) {
                        AppConstant.HTTP_OK -> {
                            val skuInventoryList = skusInventoryForStoreResponse.skuInventory
                            if (skuInventoryList.size == 0 || skuInventoryList[0].quantity == 0) {
                                addItemToCart?.catalogRefId?.let { skuId ->
                                    FirebaseManager.setCrashlyticsString(
                                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.PRODUCT_ID,
                                        productList.productId
                                    )
                                    FirebaseManager.setCrashlyticsString(
                                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.PRODUCT_NAME,
                                        productList.productName
                                    )
                                    FirebaseManager.setCrashlyticsString(
                                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.DELIVERY_LOCATION,
                                        KotlinUtils.getPreferredDeliveryAddressOrStoreName()
                                    )
                                    FirebaseManager.setCrashlyticsString(
                                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.PRODUCT_SKU,
                                        productList.sku
                                    )
                                    FirebaseManager.setCrashlyticsString(
                                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.FULFILLMENT_ID,
                                        fulfilmentTypeId
                                    )
                                    FirebaseManager.setCrashlyticsString(
                                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.STORE_ID,
                                        storeId
                                    )
                                    FirebaseManager.setCrashlyticsString(
                                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.DELIVERY_TYPE,
                                        KotlinUtils.getPreferredDeliveryType().toString()
                                    )
                                    FirebaseManager.setCrashlyticsString(
                                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.IS_USER_AUTHENTICATED,
                                        SessionUtilities.getInstance().isUserAuthenticated.toString()
                                    )
                                    Utils.getLastSavedLocation()?.let {
                                        FirebaseManager.setCrashlyticsString(
                                            FirebaseManagerAnalyticsProperties.CrashlyticsKeys.LAST_KNOWN_LOCATION,
                                            "${it.latitude}, ${it.longitude}"
                                        )
                                    }
                                    FirebaseManager.logException(
                                        Exception(
                                            FirebaseManagerAnalyticsProperties.CrashlyticsExceptionName.PRODUCT_LIST_FIND_IN_STORE
                                        )
                                    )

                                    productOutOfStockErrorMessage()
                                }
                            } else if (skuInventoryList[0].quantity == 1) {
                                if (isEnhanceSubstitutionFeatureAvailable()) {
                                    addFoodProductTypeToCart(
                                        AddItemToCart(
                                            addItemToCart?.productId,
                                            addItemToCart?.catalogRefId,
                                            1,
                                            SubstitutionChoice.SHOPPER_CHOICE.name,
                                            ""

                                        )
                                    )
                                } else {
                                    addFoodProductTypeToCart(
                                        AddItemToCart(
                                            addItemToCart?.productId,
                                            addItemToCart?.catalogRefId,
                                            1
                                        )
                                    )
                                }

                            } else {
                                val cartItem =
                                    if (isEnhanceSubstitutionFeatureAvailable()) {
                                        AddItemToCart(
                                            addItemToCart?.productId
                                                ?: "", addItemToCart?.catalogRefId
                                                ?: "", skuInventoryList[0].quantity,
                                            SubstitutionChoice.SHOPPER_CHOICE.name,
                                            ""
                                        )
                                    } else {
                                        AddItemToCart(
                                            addItemToCart?.productId
                                                ?: "", addItemToCart?.catalogRefId
                                                ?: "", skuInventoryList[0].quantity
                                        )
                                    }

                                try {
                                    val selectYourQuantityFragment =
                                        SelectYourQuantityFragment.newInstance(
                                            cartItem,
                                            this@RecommendationFragment
                                        )
                                    selectYourQuantityFragment.show(
                                        this,
                                        SelectYourQuantityFragment::class.java.simpleName
                                    )
                                } catch (ex: IllegalStateException) {
                                    FirebaseManager.logException(ex)
                                }
                            }
                        }

                        else -> {
                            if (!oneTimeInventoryErrorDialogDisplay) {
                                oneTimeInventoryErrorDialogDisplay = true
                                skusInventoryForStoreResponse?.response?.desc?.let { desc ->
                                    Utils.displayValidationMessage(
                                        activity,
                                        CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                        desc
                                    )
                                }
                            } else return
                        }
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                if (!isAdded) return
                activity.runOnUiThread {
                    dismissProgressBar()
                    error?.let { onFailureHandler(it) }
                }
            }
        }, SkusInventoryForStoreResponse::class.java))
    }

    override fun showLiquorDialog() {
        //No implementation is required for now
    }

    private fun productOutOfStockErrorMessage() {
        activity?.let { activity ->
            Utils.displayValidationMessage(
                activity,
                CustomPopUpWindow.MODAL_LAYOUT.NO_STOCK,
                ""
            )
        }
    }

    private fun showProgressBar() {
        // Show progress bar
        _recommendationsLayoutBinding?.incCenteredProgress?.root?.visibility = View.VISIBLE
    }

    private fun dismissProgressBar() {
        // hide progress bar
        _recommendationsLayoutBinding?.incCenteredProgress?.root?.visibility = View.GONE
    }

    override fun addFoodProductTypeToCart(addItemToCart: AddItemToCart?) {
        showProgressBar()
        val addItemsToCart = mutableListOf<AddItemToCart>()
        addItemToCart?.let { cartItem -> addItemsToCart.add(cartItem) }
        PostItemToCart().make(addItemsToCart, object : IResponseListener<AddItemToCartResponse> {
            override fun onSuccess(response: AddItemToCartResponse?) {
                if (!isAdded) return
                activity?.apply {
                    dismissProgressBar()
                    when (response?.httpCode) {
                        AppConstant.HTTP_OK -> {
                            // Preferred Delivery Location has been reset on server
                            // As such, we give the user the ability to set their location again
                            val addToCartList = response.data
                            if (addToCartList != null && addToCartList.size > 0 && addToCartList[0].formexceptions != null) {
                                val formException = addToCartList[0].formexceptions[0]
                                if (formException != null) {
                                    if (formException.message.toLowerCase(Locale.getDefault())
                                            .contains("unfortunately this product is now out of stock, please try again tomorrow")
                                    ) {
                                        productOutOfStockErrorMessage()
                                    } else {
                                        response.response.desc = formException.message
                                        Utils.displayValidationMessage(
                                            this,
                                            CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                            response.response.desc
                                        )
                                    }
                                    return
                                }
                            }
                            if ((KotlinUtils.isDeliveryOptionClickAndCollect() || KotlinUtils.isDeliveryOptionDash())
                                && response.data[0]?.productCountMap?.quantityLimit?.foodLayoutColour != null
                            ) {
                                response.data[0]?.productCountMap?.let {
                                    addItemToCart?.quantity?.let { it1 ->
                                        _recommendationsLayoutBinding?.recommendationsProductsRecyclerview?.let { view ->
                                            val notified = notifyItemAddedToCart()
                                            ToastFactory.showItemsLimitToastOnAddToCart(
                                                view,
                                                it,
                                                this,
                                                it1,
                                                viewButtonVisible = !notified
                                            )
                                        }
                                    }
                                }
                            } else {

                                showAddToCartSuccess(addItemToCart)
                            }
                        }

                        AppConstant.HTTP_EXPECTATION_FAILED_417 -> resources?.let {
                            activity?.let { activity ->
                                Utils.displayValidationMessage(
                                    activity,
                                    CustomPopUpWindow.MODAL_LAYOUT.NO_STOCK,
                                    ""
                                )
                            }
                        }
                        AppConstant.HTTP_SESSION_TIMEOUT_440 -> {
                            activity?.let { activity ->
                                Utils.displayValidationMessage(
                                    activity,
                                    CustomPopUpWindow.MODAL_LAYOUT.NO_STOCK,
                                    ""
                                )
                            }
                        }

                        AppConstant.HTTP_EXPECTATION_FAILED_502 -> {
                            response.response.desc?.let {
                                KotlinUtils.showQuantityLimitErrror(
                                    activity?.supportFragmentManager,
                                    it,
                                    "",
                                    context
                                )
                            }
                        }

                        else -> response?.response?.desc?.let { desc ->
                            Utils.displayValidationMessage(
                                this,
                                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                desc
                            )
                        }
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                if (!isAdded) return
                activity?.runOnUiThread { dismissProgressBar() }
            }
        })
    }

    private fun notifyItemAddedToCart(): Boolean {
        val eventHandler = try {
            requireParentFragment().requireParentFragment()
        } catch (e: IllegalStateException) {
            false
        }

        if (eventHandler is RecommendationEventHandler) {
            eventHandler.onItemAddedToCart()
            return true
        }
        return false
    }

    private fun showAddToCartSuccess(addItemToCart: AddItemToCart?) {
        val isNotifiedItemAddedToCart = notifyItemAddedToCart()

        val addToCartBalloon by balloon<AddedToCartBalloonFactory>()
        val bottomView =
            (activity as? BottomNavigationActivity)?.bottomNavigationById
        val buttonView: Button =
            addToCartBalloon.getContentView().findViewById(R.id.btnView)
        val tvAddedItem: TextView = addToCartBalloon.getContentView()
            .findViewById(R.id.tvAddedItem)
        val quantityAdded = addItemToCart?.quantity?.toString()
        val quantityDesc =
            "$quantityAdded ITEM${if ((addItemToCart?.quantity ?: 0) >= 1) "" else "s"}"
        tvAddedItem.text = quantityDesc

        if (isNotifiedItemAddedToCart) {
            buttonView.visibility = View.INVISIBLE
        } else {
            buttonView.visibility = View.VISIBLE
            buttonView.setOnClickListener {
                openCartActivity()
                addToCartBalloon.dismiss()
            }
        }

        bottomView?.let { bottomNavigationView ->
            addToCartBalloon.showAlignBottom(
                bottomNavigationView,
                0,
                16
            )
        }
        Handler().postDelayed({
            addToCartBalloon.dismiss()
        }, 3000)
    }

    override fun queryStoreFinderProductByFusedLocation(location: Location?) {
        activity?.let { activity ->
            Utils.displayValidationMessage(
                activity,
                CustomPopUpWindow.MODAL_LAYOUT.NO_STOCK,
                ""
            )
        }
    }

    override fun openChangeFulfillmentScreen() {
        // No implementation is required for now
    }

    override fun openBrandLandingPage() {
        // No implementation is required for now
    }

    private fun openCartActivity() {
        (activity as? BottomNavigationActivity)?.apply {
            bottomNavigationById?.currentItem = BottomNavigationActivity.INDEX_CART
        }
    }

    private fun onFailureHandler(error: Throwable) {
        activity?.let { activity ->
            when (error) {
                is ConnectException, is UnknownHostException -> {
                    ErrorHandlerView(activity).showToast(getString(R.string.no_connection))
                }
                else -> return
            }
        }
    }

    private val recommendationProductsScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> {
                    getVisibleProductsPosition()?.let {
                        recommendationViewModel.visibleRecommendationProducts(
                            it
                        )
                    }
                }
            }
        }
    }

    private fun getVisibleProductsPosition(): List<Int>? {
        recommendationLayoutManager?.let { layoutManager ->
            val firstItem = layoutManager.findFirstVisibleItemPosition()
            val lastItem = layoutManager.findLastVisibleItemPosition()
            val list = arrayListOf<Int>()
            for (i in firstItem..lastItem) {
                list.add(i)
            }
            return list
        }
        return null
    }
}

