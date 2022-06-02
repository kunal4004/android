package za.co.woolworths.financial.services.android.ui.views.shop.dash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.fragment_click_and_collect_stores.*
import kotlinx.android.synthetic.main.fragment_click_and_collect_stores.view.*
import kotlinx.android.synthetic.main.fragment_shop_department.*
import kotlinx.android.synthetic.main.layout_dash_collection_store.*
import kotlinx.android.synthetic.main.layout_dash_set_address_fragment.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidatePlace
import za.co.woolworths.financial.services.android.geolocation.view.adapter.StoreListAdapter
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
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
import za.co.woolworths.financial.services.android.util.wenum.Delivery

class ChangeFullfilmentCollectionStoreFragment(var validatePlace: ValidatePlace?) :
    DepartmentExtensionFragment(), OnMapReadyCallback,
    StoreListAdapter.OnStoreSelected, View.OnClickListener, TextWatcher {

    private var storeId: String? = null
    private var placeId: String? = null
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var confirmAddressViewModel: ConfirmAddressViewModel
    private var parentFragment: ShopFragment? = null
    private var mDepartmentAdapter: DepartmentAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.layout_dash_collection_store, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentFragment = (activity as? BottomNavigationActivity)?.currentFragment as? ShopFragment
        setUpViewModel()
        init()
    }

    private fun setUpViewModel() {
        confirmAddressViewModel = ViewModelProvider(
            this,
            GeoLocationViewModelFactory(GeoLocationApiHelper())
        ).get(ConfirmAddressViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        etEnterNewAddress?.addTextChangedListener(this)
    }

    fun init() {
        mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        tvConfirmStore?.setOnClickListener(this)
        btChange?.setOnClickListener(this)

        tvAddress?.visibility = View.GONE

        var isPermissionGranted = false
        activity?.apply {
            isPermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (isPermissionGranted && Utils.isLocationEnabled(context)) {

            if (WoolworthsApplication.getCncBrowsingValidatePlaceDetails() == null && getDeliveryType() == null) {
                // when user comes first time i.e. no location , no fullfillment type
                // navigate to geo location flow
                showSetLocationdUi()
            } else if (WoolworthsApplication.getCncBrowsingValidatePlaceDetails() != null && KotlinUtils.browsingCncStore == null) {
                /*when user comes with location but no store is selected yet*/
                setStoreCollectionData(WoolworthsApplication.getCncBrowsingValidatePlaceDetails())
            } else if (KotlinUtils.browsingCncStore == null) {
                setStoreCollectionData(WoolworthsApplication.getCncBrowsingValidatePlaceDetails())
            } else {
                showCategoryList()
            }
        } else {
            if (WoolworthsApplication.getCncBrowsingValidatePlaceDetails() == null && getDeliveryType() == null) {
                // when user comes first time i.e. no location , no fullfillment type
                // navigate to geo location flow
                showSetLocationdUi()
            } else if (WoolworthsApplication.getCncBrowsingValidatePlaceDetails() != null && KotlinUtils.browsingCncStore == null) {
                /*when user comes with location but no store is selected yet*/
                setStoreCollectionData(WoolworthsApplication.getCncBrowsingValidatePlaceDetails())
            } else if (KotlinUtils.browsingCncStore == null) {
                setStoreCollectionData(WoolworthsApplication.getCncBrowsingValidatePlaceDetails())
            } else {
                showCategoryList()
            }
        }
    }

    private fun setStoreCollectionData(validatePlace: ValidatePlace?) {
        if (validatePlace == null ) {
            showSetLocationdUi()
            return
        }
        if (validatePlace?.stores?.isNullOrEmpty() == true) {
            showNoCollectionStoresUi()
            return
        }
        tvStoresNearMe?.text = resources.getString(
            R.string.near_stores,
            validatePlace?.stores?.size
        )
        tvAddress?.text =
            KotlinUtils.capitaliseFirstLetter(validatePlace?.placeDetails?.address1)
        placeId = validatePlace?.placeDetails?.placeId
        setStoreList(validatePlace?.stores)
    }

    private fun setStoreList(stores: List<Store>?) {
        layoutEdgeCaseScreen?.visibility = View.GONE
        layoutClickAndCollectStore?.visibility = View.VISIBLE
        layoutClickAndCollectStore?.ivCross?.visibility = View.GONE
        rvStoreList.layoutManager =
            activity?.let { activity -> LinearLayoutManager(activity) }
        rvStoreList.adapter = activity?.let { activity ->
            StoreListAdapter(
                activity,
                stores,
                this
            )
        }
        rvStoreList.adapter?.notifyDataSetChanged()
    }

    private fun showSetLocationdUi() {
        layoutClickAndCollectStore?.visibility = View.GONE
        layoutEdgeCaseScreen?.visibility = View.VISIBLE
        img_view?.setImageResource(R.drawable.location_disabled)
        txt_dash_title?.text = bindString(R.string.set_location_title)
        txt_dash_sub_title?.text = bindString(R.string.device_location_service_disabled_subTitle)
        btn_dash_set_address?.text = bindString(R.string.set_location)
        btn_dash_set_address?.setOnClickListener {
            navigateToConfirmAddressScreen()
        }
    }

    private fun showNoCollectionStoresUi() {
        layoutClickAndCollectStore?.visibility = View.GONE
        layoutEdgeCaseScreen?.visibility = View.VISIBLE
        img_view?.setImageResource(R.drawable.location_disabled)
        txt_dash_title?.text = bindString(R.string.collection_store_title)
        txt_dash_sub_title?.text = bindString(R.string.suburb_not_deliverable_description)
        btn_dash_set_address?.text = bindString(R.string.change_location)
        btn_dash_set_address?.setOnClickListener {
            navigateToConfirmAddressScreen()
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
        layoutEdgeCaseScreen?.scrollTo(0, 0)
    }

    override fun onStoreSelected(store: Store?) {
        storeId = store?.storeId
        tvConfirmStore?.isEnabled = true
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.uiSettings?.setAllGesturesEnabled(false)
        val addressStorList = WoolworthsApplication.getCncBrowsingValidatePlaceDetails()?.stores
        GeoUtils.showFirstFourLocationInMap(addressStorList, googleMap, requireContext())
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvConfirmStore -> {
                callConfirmLocationApi()
            }
            R.id.btChange -> {
                navigateToConfirmAddressScreen()
            }
        }
    }

    private fun callConfirmLocationApi() {
        if (Utils.getPreferredDeliveryLocation()?.fulfillmentDetails == null &&
            KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails == null
        ) {
            postConfirmLocationApi()
            return
        } else {
            /*location , fullfillment is already available so only browsing location need to be save */
            setBrowsingDataInformation()
            showCategoryList()
        }
    }

    private fun postConfirmLocationApi() {
        lifecycleScope.launch {
            try {
                cncProgressBar.visibility = View.VISIBLE
                val confirmLocationAddress =
                    ConfirmLocationAddress(placeId)
                val confirmLocationRequest =
                    ConfirmLocationRequest(BundleKeysConstants.CNC, confirmLocationAddress, storeId)
                val confirmLocationResponse =
                    confirmAddressViewModel.postConfirmAddress(confirmLocationRequest)
                if (confirmLocationResponse != null) {
                    when (confirmLocationResponse.httpCode) {
                        AppConstant.HTTP_OK -> {
                            cncProgressBar.visibility = View.GONE
                            if (SessionUtilities.getInstance().isUserAuthenticated) {

                                KotlinUtils.placeId = placeId
                                if (Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId == null) {
                                    KotlinUtils.isLocationSame = true
                                } else {
                                    KotlinUtils.isLocationSame =
                                        placeId?.equals(Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId)
                                }

                                KotlinUtils.isCncTabClicked =
                                    placeId?.equals(Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId)

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
                                    KotlinUtils.isLocationSame = true
                                } else {
                                    KotlinUtils.isLocationSame =
                                        placeId?.equals(Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId)
                                }
                                KotlinUtils.isCncTabClicked =
                                    placeId?.equals(KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.address?.placeId)
                                KotlinUtils.saveAnonymousUserLocationDetails(
                                    ShoppingDeliveryLocation(
                                        confirmLocationResponse.orderSummary?.fulfillmentDetails
                                    )
                                )
                            }

                            /* reset browsing data for cnc and dash both once fullfillment location is comfirmed */

                            WoolworthsApplication.setValidatedSuburbProducts(validatePlace)
                            setBrowsingDataInformation()
                            setDeliveryView()
                            showCategoryList()
                        }
                    }
                }
            } catch (e: HttpException) {
                e.printStackTrace()
                cncProgressBar?.visibility = View.GONE
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
                bundle.putString(SubCategoryFragment.KEY_ARGS_VERSION, version)
                bundle.putBoolean(
                    SubCategoryFragment.KEY_ARGS_IS_LOCATION_ENABLED,
                    if (context != null) Utils.isLocationEnabled(context) else false
                )
                //     location?.let { bundle.putParcelable(SubCategoryFragment.KEY_ARGS_LOCATION, it) }
                drillDownCategoryFragment.arguments = bundle
                return drillDownCategoryFragment
            }
            else -> ProductListingFragment.newInstance(
                ProductsRequestParams.SearchType.NAVIGATE,
                rootCategory.categoryName,
                rootCategory.dimValId,
                true
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
                WoolworthsApplication.getCncBrowsingValidatePlaceDetails().stores
            )
    }

    private fun showCategoryList() {
        parentFragment?.showSerachAndBarcodeUi()
        layoutClickAndCollectStore?.visibility = View.GONE
        rv_category_layout?.visibility = View.VISIBLE
        setUpCategoryRecyclerView(mutableListOf())
        initializeRootCategoryList()
    }

    private fun initializeRootCategoryList() {
        if (parentFragment?.getCategoryResponseData() != null) bindDepartment(
            mDepartmentAdapter,
            parentFragment
        ) else executeDepartmentRequest(mDepartmentAdapter, parentFragment)
    }

    private fun setUpCategoryRecyclerView(categories: MutableList<RootCategory>?) {
        rv_category_layout?.visibility = View.VISIBLE
        mDepartmentAdapter = DepartmentAdapter(
            categories,
            ::departmentItemClicked,
            ::onDashBannerClicked
        ) //{ rootCategory: RootCategory -> departmentItemClicked(rootCategory)}
        activity?.let {
            rclDepartment?.apply {
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

    private fun onDashBannerClicked() {
        /*todo need to delete this one */
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // not required
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // not required
    }

    override fun afterTextChanged(s: Editable?) {
        val list = ArrayList<Store>()
        val stores: List<Store>? = WoolworthsApplication.getCncBrowsingValidatePlaceDetails()?.stores
        stores?.let {
            for (store in it) {
                if (store.storeName?.contains(s.toString(), true) == true || store.storeAddress?.contains(s.toString(), true)==true) {
                    list.add(store)
                }
            }
        }
        setStoreList(list)
    }
}