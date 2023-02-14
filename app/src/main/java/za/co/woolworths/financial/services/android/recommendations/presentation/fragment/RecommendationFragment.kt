package za.co.woolworths.financial.services.android.recommendations.presentation.fragment

import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.RecommendationsLayoutBinding
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Action
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Product
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Event
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest
import za.co.woolworths.financial.services.android.recommendations.presentation.viewmodel.RecommendationViewModel
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.adapters.holder.RecyclerViewViewHolderItems
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import za.co.woolworths.financial.services.android.util.wenum.Delivery

class RecommendationFragment :
    BaseFragmentBinding<RecommendationsLayoutBinding>(RecommendationsLayoutBinding::inflate),
    RecommendationsProductListingListener {

    private var _recommendationsLayoutBinding: RecommendationsLayoutBinding? = null
    private val recommendationsLayoutBinding get() = _recommendationsLayoutBinding!!
    private val recommendationViewModel: RecommendationViewModel by activityViewModels()
    private var mProductCategoryAdapter: ProductCategoryAdapter? = null
    private var mProductListRecommendationAdapter: ProductListRecommendationAdapter? = null

    private var mStoreId = ""
    private var isUserBrowsing: Boolean = false

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
            _recommendationsLayoutBinding?.recommendationsCategoryRecyclerview?.layoutManager =
                LinearLayoutManager(it, RecyclerView.HORIZONTAL, false)
            mProductCategoryAdapter = ProductCategoryAdapter(actionItemList)
        }
        _recommendationsLayoutBinding?.recommendationsCategoryRecyclerview?.adapter =
            mProductCategoryAdapter

        actionItemList.get(0).products?.let { showRecProductsList(it) }

        mProductCategoryAdapter?.onItemClick = {
            showRecProductsList(it)
        }
    }

    private fun showRecProductsList(productsList: List<Product>?) {
        if (productsList.isNullOrEmpty()) {
            _recommendationsLayoutBinding?.recommendationsProductsRecyclerview?.visibility =
                View.GONE
        } else {

            _recommendationsLayoutBinding?.recommendationsProductsRecyclerview?.visibility =
                View.VISIBLE
            context?.let {
                _recommendationsLayoutBinding?.recommendationsProductsRecyclerview?.layoutManager =
                    LinearLayoutManager(it, RecyclerView.HORIZONTAL, false)

                mProductListRecommendationAdapter =
                    ProductListRecommendationAdapter(productsList, this, it)
            }
            _recommendationsLayoutBinding?.recommendationsProductsRecyclerview?.adapter =
                mProductListRecommendationAdapter
        }

    }


    private fun getRecommendationDetails() {
        val bundle = arguments?.getBundle(BundleKeysConstants.BUNDLE)
        val reccommendationsDataEventTypeFirst = bundle?.getParcelable<Event>(BundleKeysConstants.RECOMMENDATIONS_EVENT_DATA) as Event
        val reccommendationsDataEventTypeSecond = bundle?.getParcelable<Event>(BundleKeysConstants.RECOMMENDATIONS_EVENT_DATA_TYPE) as Event
        var recMonetateId: String? = null
        if (Utils.getMonetateId() != null) {
            recMonetateId = Utils.getMonetateId()
        }

        val recommendationRequest = RecommendationRequest(
            events = listOf(reccommendationsDataEventTypeFirst, reccommendationsDataEventTypeSecond),
            monetateId = recMonetateId
        )

        recommendationViewModel.getRecommendationResponse(recommendationRequest)

        recommendationViewModel.recommendationResponseData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                when (response.status) {
                    Status.SUCCESS -> {

                        if (!response.data?.monetateId.isNullOrEmpty()) {
                            Utils.saveMonetateId(response.data?.monetateId)
                        }
                        response.data?.actions?.let { response ->
                            showProductCategory(response)

                        }
                    }
                    Status.ERROR -> {
                        //TODO:
                    }
                    else -> {
                        // Nothing
                    }
                }
            }
        }

    }

    override fun onDestroyView() {
        _recommendationsLayoutBinding = null
        super.onDestroyView()
    }

    override fun openProductDetailView(productList: Product) {

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
            onShopTabSelected(bottomNavigationById.menu[BottomNavigationActivity.INDEX_PRODUCT])
        }
        ScreenManager.openProductDetailFragment(requireActivity(), productName, strProductList)
    }


    override fun addFoodProductTypeToCart(addItemToCart: AddItemToCart?) {
        val mAddItemsToCart = mutableListOf<AddItemToCart>()
        addItemToCart?.let { cartItem -> mAddItemsToCart.add(cartItem) }
        recommendationViewModel.callToAddItemsToCart(mAddItemsToCart)
    }

    override fun queryInventoryForStore(
        fulfilmentTypeId: String, addItemToCart: AddItemToCart?, productList: Product
    ) {
        recommendationViewModel.setAddItemToCart(addItemToCart)
        mStoreId =
            fulfilmentTypeId.let { it1 -> MyRecycleViewHolder.getFulFillmentStoreId(it1) }

        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSigninActivity(
                activity,
                AppConstant.REQUEST_CODE_QUERY_INVENTORY_FOR_STORE,
                true
            )
            return
        }

        recommendationViewModel.setProductList(productList)

        /*if (productList.isLiquor == true && !KotlinUtils.isCurrentSuburbDeliversLiquor() && !KotlinUtils.isLiquorModalShown()) {
            KotlinUtils.setLiquorModalShown()
           // showLiquorDialog()
            AppConfigSingleton.productItemForLiquorInventory = productList
            return
        }*/

        // Now first check for if delivery location and browsing location is same.
        // if same no issues. If not then show changing delivery location popup.
        if (!KotlinUtils.getDeliveryType()?.deliveryType.equals(Delivery.DASH.type)) {
            KotlinUtils.showChangeDeliveryTypeDialog(
                requireContext(), requireFragmentManager(),
                KotlinUtils.browsingDeliveryType
            )
            return
        }

        if (!KotlinUtils.getDeliveryType()?.deliveryType.equals(KotlinUtils.browsingDeliveryType?.type) && isUserBrowsing) {
            KotlinUtils.showChangeDeliveryTypeDialog(
                requireContext(), requireFragmentManager(),
                KotlinUtils.browsingDeliveryType
            )
            return
        }
        addToCart(addItemToCart)
    }

    private fun addToCart(addItemToCart: AddItemToCart?) {
        val fulfilmentTypeId = AppConfigSingleton.quickShopDefaultValues?.foodFulfilmentTypeId
        mStoreId =
            fulfilmentTypeId?.let { it1 -> RecyclerViewViewHolderItems.getFulFillmentStoreId(it1) }
                ?: ""

        /*if (mStoreId.isEmpty()) {
            addItemToCart?.catalogRefId?.let { skuId -> productOutOfStockErrorMessage(skuId) }
            return
        }*/
        recommendationViewModel.setAddItemToCart(addItemToCart)
        recommendationViewModel.fetchInventorySkuForStore(
            mStoreId, addItemToCart?.catalogRefId
                ?: ""
        )
    }

    override fun queryStoreFinderProductByFusedLocation(location: Location?) {
        val globalState = WoolworthsApplication.getInstance().wGlobalState
        with(globalState) {
            recommendationViewModel.callStoreFinder(
                sku = recommendationViewModel.productList.value?.sku ?: "",
                startRadius = startRadius.toString(),
                endRadius = endRadius.toString()
            )
        }
    }

    override fun openChangeFulfillmentScreen() {
        KotlinUtils.presentEditDeliveryGeoLocationActivity(
            requireActivity(),
            BundleKeysConstants.REQUEST_CODE,
            Delivery.DASH,
            KotlinUtils.getDeliveryType()?.address?.placeId ?: ""
        )
    }

}



