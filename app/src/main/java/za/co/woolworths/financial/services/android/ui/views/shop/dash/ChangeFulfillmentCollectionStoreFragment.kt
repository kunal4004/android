package za.co.woolworths.financial.services.android.ui.views.shop.dash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.LayoutDashCollectionStoreBinding
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidatePlace
import za.co.woolworths.financial.services.android.geolocation.view.adapter.StoreListAdapter
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.adapters.DepartmentAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.ShopFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.DepartmentExtensionFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getDeliveryType
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.geolocation.network.model.PlaceDetails
import za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel.ValidateStoreResponse
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.TAG_CHANGEFULLFILMENT_COLLECTION_STORE_FRAGMENT
import za.co.woolworths.financial.services.android.geolocation.view.PargoStoreInfoBottomSheetDialog
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.geolocation.view.FBHInfoBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderLoading
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess

class ChangeFulfillmentCollectionStoreFragment :
    DepartmentExtensionFragment(R.layout.layout_dash_collection_store),
    StoreListAdapter.OnStoreSelected, View.OnClickListener, TextWatcher {

    private lateinit var binding: LayoutDashCollectionStoreBinding
    private var updatedAddressStoreList: List<Store>? = mutableListOf()
    private var storeId: String? = null
    private var placeId: String? = null
    private val confirmAddressViewModel: ConfirmAddressViewModel by activityViewModels()
    private var parentFragment: ShopFragment? = null
    private var mDepartmentAdapter: DepartmentAdapter? = null
    private var saveInstanceState: Bundle? = null
    private var updatedPlace: PlaceDetails? = null
    private var isFragmentVisible: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LayoutDashCollectionStoreBinding.bind(view)

        parentFragment = (activity as? BottomNavigationActivity)?.currentFragment as? ShopFragment
        this.saveInstanceState = savedInstanceState
        addObserver()
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    override fun noConnectionLayout(isVisible: Boolean) {
        binding.let {
            it.layoutClickAndCollectStore.noClickAndCollectConnectionLayout.root.visibility =
                if (isVisible) View.VISIBLE else View.GONE
        }
    }

    fun init() {
        showCategoryList()
    }

    private fun addObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            confirmAddressViewModel.validateStoreInventoryData.collectLatest { validatePlaceResponse ->
                with(validatePlaceResponse) {
                    renderLoading {
                        if (isLoading) {
                            binding.cncProgressBar.visibility = View.VISIBLE
                        } else
                            binding.cncProgressBar.visibility = View.GONE

                    }
                    renderSuccess {
                        checkAndCallConfirmLocationApi(output)
                    }
                }
            }
        }
    }

    private fun setStoreCollectionData(validatePlace: ValidatePlace?) {
        if (validatePlace == null) {
            val mPlaceId = getDeliveryType()?.address?.placeId ?: return
            if (mPlaceId.isNotEmpty()) {
                /* if place id is not null means previously location is set but validate place api
                  is not called yet or not in sync. so need to call again */
                executeValidatePlaceApi(mPlaceId)
            } else {
                showSetLocationUi()
            }
            return
        }
        if (validatePlace.stores.isNullOrEmpty()) {
            showNoCollectionStoresUi()
            return
        }
        binding.layoutClickAndCollectStore.tvStoresNearMe.text = resources.getString(
            R.string.near_stores,
            validatePlace.stores?.size
        )
        binding.layoutClickAndCollectStore.tvAddress?.text =
            KotlinUtils.capitaliseFirstLetter(validatePlace.placeDetails?.address1)
        placeId = validatePlace.placeDetails?.placeId
        setStoreList(validatePlace.stores)
    }

    private fun executeValidatePlaceApi(mPlaceId: String) {
        lifecycleScope.launch {
            try {
                binding.cncProgressBar.visibility = View.VISIBLE
                val validateLocationResponse =
                    confirmAddressViewModel.getValidateLocation(mPlaceId)

                if (validateLocationResponse != null) {
                    when (validateLocationResponse.httpCode) {
                        AppConstant.HTTP_OK -> {
                            binding.cncProgressBar.visibility = View.GONE
                            binding.layoutClickAndCollectStore.tvStoresNearMe.text =
                                resources.getString(
                                    R.string.near_stores,
                                    validateLocationResponse?.validatePlace?.stores?.size
                                )
                            updatedAddressStoreList =
                                validateLocationResponse?.validatePlace?.stores
                            updatedPlace = validateLocationResponse?.validatePlace?.placeDetails
                            binding.layoutClickAndCollectStore.tvAddress.text =
                                KotlinUtils.capitaliseFirstLetter(validateLocationResponse?.validatePlace?.placeDetails?.address1)
                            placeId = validateLocationResponse?.validatePlace?.placeDetails?.placeId
                            setStoreList(validateLocationResponse?.validatePlace?.stores)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.cncProgressBar.visibility = View.GONE
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                binding.cncProgressBar.visibility = View.GONE
            }
        }
    }

    private fun setStoreList(stores: List<Store>?) {
        binding.layoutEdgeCaseScreen?.root?.visibility = View.GONE
        binding.layoutClickAndCollectStore?.root?.visibility = View.VISIBLE
        binding.layoutClickAndCollectStore?.topPaddingView?.visibility = View.VISIBLE
        binding.layoutClickAndCollectStore?.backButton?.visibility = View.GONE
        binding.layoutClickAndCollectStore.rvStoreList.layoutManager =
            activity?.let { activity -> LinearLayoutManager(activity) }
        if (stores?.isNotEmpty() == true) {
            binding.layoutClickAndCollectStore.tvConfirmStore.isEnabled = false
            val storesListWithHeaders =
                StoreUtils.getStoresListWithHeaders(StoreUtils.sortedStoreList(stores))
            if (storesListWithHeaders.isNotEmpty()) {
                binding.layoutClickAndCollectStore.rvStoreList.adapter = activity?.let { activity ->
                    StoreListAdapter(
                        activity,
                        storesListWithHeaders,
                        this
                    )
                }
                if (isFragmentVisible) {
                    binding.layoutClickAndCollectStore.rvStoreList.runWhenReady {
                        if (!AppInstanceObject.get().featureWalkThrough.new_fbh_cnc) {
                            firstTimeFBHCNCIntroDialog()
                        }
                    }
                }
            }
            binding.layoutClickAndCollectStore.rvStoreList.adapter?.notifyDataSetChanged()
        }

    }

    private fun showSetLocationUi() {
        binding.apply {
            layoutClickAndCollectStore?.root?.visibility = View.GONE
            layoutEdgeCaseScreen?.root?.visibility = View.VISIBLE
            layoutEdgeCaseScreen.imgView.setImageResource(R.drawable.ic_cnc_set_location)
            layoutEdgeCaseScreen.txtDashTitle.text = bindString(R.string.set_location_title)
            layoutEdgeCaseScreen.txtDashSubTitle.text =
                bindString(R.string.device_location_service_disabled_subTitle)
            layoutEdgeCaseScreen.btnDashSetAddress.text = bindString(R.string.set_location)
            layoutEdgeCaseScreen.btnDashSetAddress.setOnClickListener {
                navigateToConfirmAddressScreen()
            }
        }
    }

    private fun showNoCollectionStoresUi() {
        binding.apply {
            layoutClickAndCollectStore?.root?.visibility = View.GONE
            layoutEdgeCaseScreen?.root?.visibility = View.VISIBLE
            layoutEdgeCaseScreen.imgView.setImageResource(R.drawable.ic_cnc_set_location)
            layoutEdgeCaseScreen.txtDashTitle.text = bindString(R.string.collection_store_title)
            layoutEdgeCaseScreen.txtDashSubTitle.text =
                bindString(R.string.suburb_not_deliverable_description)
            layoutEdgeCaseScreen.btnDashSetAddress.text = bindString(R.string.change_location)
            layoutEdgeCaseScreen.btnDashSetAddress.setOnClickListener {
                navigateToConfirmAddressScreen()
            }
        }
    }

    private fun navigateToConfirmAddressScreen() {
        // navigate to confirm address screen
        KotlinUtils.isComingFromCncTab = true
        activity?.apply {
            KotlinUtils.presentEditDeliveryGeoLocationActivity(
                this,
                BundleKeysConstants.CNC_SET_ADDRESS_REQUEST_CODE,
                Delivery.CNC,
                null,
                false
            )
        }
    }

    fun scrollToTop() {
        if (::binding.isInitialized) {
            binding.layoutEdgeCaseScreen?.root?.scrollTo(0, 0)
        }
    }

    override fun onStoreSelected(store: Store?) {
        storeId = store?.storeId
        binding.layoutClickAndCollectStore.tvConfirmStore.isEnabled = true
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvConfirmStore -> {
                // first will call store inventory to get store data then after success will make confirmLocation API.
                callValidateStoreInventory()
            }

            R.id.btChange -> {
                navigateToConfirmAddressScreen()
            }
        }
    }

    private fun callValidateStoreInventory() {
        lifecycleScope.launch {
            if (placeId.isNullOrEmpty() && storeId.isNullOrEmpty()) {
                return@launch
            } else {
                confirmAddressViewModel.queryValidateStoreInventory(placeId!!, storeId!!)
            }
        }
    }

    private fun checkAndCallConfirmLocationApi(validateStoreResponse: ValidateStoreResponse) {
        if (validateStoreResponse.validatePlace != null) {
            setCnCStoreInValidateResponse(validateStoreResponse?.validatePlace?.stores)
        }
        if (Utils.getPreferredDeliveryLocation()?.fulfillmentDetails == null &&
            KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails == null
        ) {
            postConfirmLocationApi()
            return
        } else {
            /*location , fulfillment is already available so only browsing location need to be save */
            setBrowsingDataInformation()
            showCategoryList()
        }
    }

    private fun setCnCStoreInValidateResponse(browsingStoreData: ArrayList<Store>?) {
        val storeListData = WoolworthsApplication.getValidatePlaceDetails()?.stores
            ?: WoolworthsApplication.getCncBrowsingValidatePlaceDetails()?.stores
        if (!storeListData.isNullOrEmpty() && !browsingStoreData.isNullOrEmpty()) {
            storeListData?.forEach { listStore ->
                if (listStore.storeId == browsingStoreData[0].storeId) {
                    KotlinUtils.setCncStoreValidateResponse(browsingStoreData[0], listStore)
                    return@forEach
                }
            }
        }
    }

    private fun postConfirmLocationApi() {
        if (placeId?.isNullOrEmpty() == true) {
            return
        }
        lifecycleScope.launch {
            try {
                binding.cncProgressBar.visibility = View.VISIBLE
                val confirmLocationAddress =
                    ConfirmLocationAddress(placeId)
                val confirmLocationRequest =
                    ConfirmLocationRequest(BundleKeysConstants.CNC, confirmLocationAddress, storeId)
                val confirmLocationResponse =
                    confirmAddressViewModel.postConfirmAddress(confirmLocationRequest)
                if (confirmLocationResponse != null) {
                    when (confirmLocationResponse.httpCode) {
                        AppConstant.HTTP_OK -> {
                            binding.cncProgressBar.visibility = View.GONE
                            if (SessionUtilities.getInstance().isUserAuthenticated) {

                                KotlinUtils.placeId = placeId
                                if (Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId == null) {
                                    KotlinUtils.isLocationPlaceIdSame = true
                                } else {
                                    KotlinUtils.isLocationPlaceIdSame =
                                        placeId?.equals(Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId)
                                }

                                Utils.savePreferredDeliveryLocation(
                                    ShoppingDeliveryLocation(
                                        confirmLocationResponse.orderSummary?.fulfillmentDetails
                                    )
                                )
                                if (KotlinUtils.getAnonymousUserLocationDetails() != null)
                                    KotlinUtils.clearAnonymousUserLocationDetails()
                            } else {
                                KotlinUtils.placeId = placeId
                                if (KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.address?.placeId == null) {
                                    KotlinUtils.isLocationPlaceIdSame = true
                                } else {
                                    KotlinUtils.isLocationPlaceIdSame =
                                        placeId?.equals(Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId)
                                }
                                KotlinUtils.saveAnonymousUserLocationDetails(
                                    ShoppingDeliveryLocation(
                                        confirmLocationResponse.orderSummary?.fulfillmentDetails
                                    )
                                )
                            }

                            /* reset browsing data for cnc and dash both once fulfillment location is confirmed */

                            WoolworthsApplication.setValidatedSuburbProducts(WoolworthsApplication.getCncBrowsingValidatePlaceDetails())
                            setBrowsingDataInformation()
                            setDeliveryView()
//                            parentFragment?.showClickAndCollectToolTipUi(storeId) // TODO, this will be verified in the implementation and will be deleted permanently
                            showCategoryList()
                        }
                    }
                }
            } catch (e: Exception) {
                FirebaseManager.logException(e)
                binding.cncProgressBar.visibility = View.GONE
            }
        }
    }

    private fun openNextFragment(rootCategory: RootCategory): Fragment {
        val drillDownCategoryFragment = SubCategoryFragment()
        val bundle = Bundle()
        return when (rootCategory.hasChildren) {
            // navigate to drill down of categories
            true -> {
                bundle.putString(
                    SubCategoryFragment.KEY_ARGS_ROOT_CATEGORY,
                    Utils.toJson(rootCategory)
                )
                bundle.putBoolean(
                    AppConstant.Keys.EXTRA_SEND_DELIVERY_DETAILS_PARAMS,
                    arguments?.getBoolean(
                        AppConstant.Keys.EXTRA_SEND_DELIVERY_DETAILS_PARAMS,
                        false
                    ) ?: false
                )
                bundle.putString(SubCategoryFragment.KEY_ARGS_VERSION, version)
                bundle.putBoolean(
                    SubCategoryFragment.KEY_ARGS_IS_LOCATION_ENABLED,
                    if (context != null) Utils.isLocationEnabled(context) else false
                )
                bundle.putBoolean(
                    AppConstant.Keys.EXTRA_SEND_DELIVERY_DETAILS_PARAMS,
                    arguments?.getBoolean(
                        AppConstant.Keys.EXTRA_SEND_DELIVERY_DETAILS_PARAMS,
                        false
                    ) ?: false
                )
                //     location?.let { bundle.putParcelable(SubCategoryFragment.KEY_ARGS_LOCATION, it) }
                drillDownCategoryFragment.arguments = bundle
                return drillDownCategoryFragment
            }

            else -> ProductListingFragment.newInstance(
                ProductsRequestParams.SearchType.NAVIGATE,
                rootCategory.categoryName,
                rootCategory.dimValId,
                isBrowsing = true,
                sendDeliveryDetails = arguments?.getBoolean(
                    AppConstant.Keys.EXTRA_SEND_DELIVERY_DETAILS_PARAMS,
                    false
                )
            )
        }
    }

    private fun setBrowsingDataInformation() {
        if (WoolworthsApplication.getValidatePlaceDetails() != null) {
            WoolworthsApplication.setCncBrowsingValidatePlaceDetails(WoolworthsApplication.getValidatePlaceDetails())
            WoolworthsApplication.setDashBrowsingValidatePlaceDetails(WoolworthsApplication.getValidatePlaceDetails())
        }
        KotlinUtils.browsingCncStore =
            GeoUtils.getStoreDetails(
                storeId,
                WoolworthsApplication.getCncBrowsingValidatePlaceDetails()?.stores
            )
    }

    private fun showCategoryList() {
        binding.apply {
            parentFragment?.showSearchAndBarcodeUi()
            layoutClickAndCollectStore?.root?.visibility = View.GONE
            layoutEdgeCaseScreen?.root?.visibility = View.GONE
            binding.rvCategoryLayout?.root?.visibility = View.VISIBLE
            setUpCategoryRecyclerView(mutableListOf())
            initializeRootCategoryList()
        }
    }

    private fun initializeRootCategoryList() {
        if (parentFragment?.getCategoryResponseData()?.rootCategories != null) bindDepartment(
            mDepartmentAdapter,
            parentFragment
        ) else executeDepartmentRequest(mDepartmentAdapter, parentFragment)
    }

    private fun setUpCategoryRecyclerView(categories: MutableList<RootCategory>?) {
        binding.rvCategoryLayout?.root?.visibility = View.VISIBLE
        mDepartmentAdapter = DepartmentAdapter(
            categories,
            ::departmentItemClicked
        ) //{ rootCategory: RootCategory -> departmentItemClicked(rootCategory)}
        activity?.let {
            binding.rvCategoryLayout.rclDepartment.apply {
                layoutManager = LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
                adapter = mDepartmentAdapter
            }
        }
    }

    private fun setDeliveryView() {
        parentFragment?.setDeliveryView()
    }

    private fun departmentItemClicked(rootCategory: RootCategory) {
        (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(openNextFragment(rootCategory))
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // not required
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // not required
    }

    override fun afterTextChanged(s: Editable?) {
        val list = ArrayList<Store>()
        val stores: List<Store>? =
            WoolworthsApplication.getCncBrowsingValidatePlaceDetails()?.stores
        stores?.let {
            for (store in it) {
                if (store.storeName?.contains(
                        s.toString(),
                        true
                    ) == true || store.storeAddress?.contains(s.toString(), true) == true
                ) {
                    list.add(store)
                }
            }
        }
        setStoreList(list)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        this.saveInstanceState = outState
    }

    override fun onFirstTimePargo() {
        PargoStoreInfoBottomSheetDialog().show(
            parentFragmentManager,
            TAG_CHANGEFULLFILMENT_COLLECTION_STORE_FRAGMENT
        )
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        isFragmentVisible = isVisibleToUser
    }

    private fun firstTimeFBHCNCIntroDialog() {
        val fbh = FBHInfoBottomSheetDialog()
        activity?.supportFragmentManager?.let { fbh.show(it, AppConstant.TAG_FBH_CNC_FRAGMENT) }
    }

}