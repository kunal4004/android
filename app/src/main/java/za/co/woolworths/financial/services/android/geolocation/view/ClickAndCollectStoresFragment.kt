package za.co.woolworths.financial.services.android.geolocation.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_click_and_collect_stores.*
import kotlinx.android.synthetic.main.fragment_click_and_collect_stores.dynamicMapView
import kotlinx.android.synthetic.main.fragment_stores_nearby1.*
import kotlinx.android.synthetic.main.geo_location_delivery_address.*
import kotlinx.android.synthetic.main.no_connection.view.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.view.adapter.StoreListAdapter
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
import za.co.woolworths.financial.services.android.geolocation.viewmodel.StoreLiveData
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.maps.DynamicMapDelegate
import za.co.woolworths.financial.services.android.ui.views.maps.model.DynamicMapMarker
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_CONFIRM_ADD
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_FROM_STORE_LOCATOR
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_PLACE_ID
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.VALIDATE_RESPONSE
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@AndroidEntryPoint
class ClickAndCollectStoresFragment : DialogFragment(), DynamicMapDelegate,
    StoreListAdapter.OnStoreSelected, View.OnClickListener, TextWatcher, VtoTryAgainListener {

    private var mValidateLocationResponse: ValidateLocationResponse? = null
    private lateinit var confirmAddressViewModel: ConfirmAddressViewModel
    private var dataStore: Store? = null
    private var bundle: Bundle? = null
    private  var validateLocationResponse: ValidateLocationResponse? = null
    private var placeId: String? = null
    private var isComingFromConfirmAddress: Boolean? = false
    @Inject
    lateinit var vtoErrorBottomSheetDialog: VtoErrorBottomSheetDialog

    companion object {
        fun newInstance(validateLocationResponse: ValidateLocationResponse?) =
            ClickAndCollectStoresFragment().withArgs {
                putSerializable(VALIDATE_RESPONSE, validateLocationResponse)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle(BUNDLE)
        bundle?.apply {
            placeId = this.getString(KEY_PLACE_ID, "")
            isComingFromConfirmAddress = getBoolean(IS_COMING_CONFIRM_ADD,false)
            if(containsKey(VALIDATE_RESPONSE)){
                getSerializable(VALIDATE_RESPONSE)?.let {
                    mValidateLocationResponse =
                        it as ValidateLocationResponse
                }

            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_click_and_collect_stores, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        dynamicMapView?.initializeMap(savedInstanceState, this)
        tvConfirmStore?.setOnClickListener(this)
        ivCross?.setOnClickListener(this)
        btChange?.setOnClickListener(this)
        etEnterNewAddress?.addTextChangedListener(this)
        dialog?.window
            ?.attributes?.windowAnimations = R.style.DialogFragmentAnimation
        if (isComingFromConfirmAddress == true) {
            placeId?.let {
                if (confirmAddressViewModel.isConnectedToInternet(requireActivity())) {
                    getDeliveryDetailsFromValidateLocation(it)
                    noClickAndCollectConnectionLayout?.no_connection_layout?.visibility = View.GONE
                } else {
                    noClickAndCollectConnectionLayout?.no_connection_layout?.visibility = View.VISIBLE
                }
            }
        } else {
            setAddressUI(mValidateLocationResponse?.validatePlace?.stores,
                mValidateLocationResponse)
        }
    }

    override fun onMapReady() {
        dynamicMapView?.setAllGesturesEnabled(false)
        showFirstFourLocationInMap(mValidateLocationResponse?.validatePlace?.stores)
    }

    private fun showFirstFourLocationInMap(addressStoreList: List<Store>?) {
        addressStoreList?.let {
            for (i in 0..3) {
                dynamicMapView?.addMarker(
                    requireContext(),
                    latitude = addressStoreList?.get(i)?.latitude,
                    longitude = addressStoreList?.get(i)?.longitude,
                    icon = R.drawable.pin
                )
            }
        }
        //after plotting all the markers pointing the camera to nearest store
        val store:Store?=addressStoreList?.get(0)
        store?.let{
            dynamicMapView?.moveCamera(
                latitude = it.latitude,
                longitude =it.longitude,
                zoom = 11f
            )
        }
    }

    private fun setAddressUI(
        address: List<Store>?,
        mValidateLocationResponse: ValidateLocationResponse?
    ) {
        tvStoresNearMe?.text = resources.getString(R.string.near_stores, address?.size)
        tvAddress?.text = mValidateLocationResponse?.validatePlace?.placeDetails?.address1
        setStoreList(address)
    }

    private fun setStoreList(address: List<Store>?) {
        rvStoreList.layoutManager =
            activity?.let { activity -> LinearLayoutManager(activity) }
        rvStoreList.adapter = activity?.let { activity ->
            StoreListAdapter(
                activity,
                address,
                this
            )
        }
        rvStoreList.adapter?.notifyDataSetChanged()
    }

    override fun onStoreSelected(mStore: Store?) {
        dataStore = mStore
        tvConfirmStore?.isEnabled = true
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvConfirmStore -> {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.SHOP_CONFIRM_STORE,
                    hashMapOf(
                        FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_CONFIRM_STORE
                    ),
                    activity)
                navigateToFulfillmentScreen()
            }
            R.id.ivCross -> {
               dismiss()
            }
            R.id.btChange -> {
                IS_FROM_STORE_LOCATOR = true
                findNavController().navigate(
                    R.id.action_clickAndCollectStoresFragment_to_confirmAddressLocationFragment,
                    bundleOf(BUNDLE to bundle)
                )
            }
        }
    }

    private fun navigateToFulfillmentScreen() {
        if (IS_FROM_STORE_LOCATOR) {
            dataStore?.let { StoreLiveData.value = it }
            bundle?.putString(
               KEY_PLACE_ID, placeId)
            IS_FROM_STORE_LOCATOR = false
            findNavController().navigate(
                R.id.action_clickAndCollectStoresFragment_to_deliveryAddressConfirmationFragment,
                bundleOf(BUNDLE to bundle)
            )
        } else {
            dataStore?.let { StoreLiveData.value = it }
            dismiss()
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // not required
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // not required
    }

    override fun afterTextChanged(s: Editable?) {
        val list = ArrayList<Store>()
        mValidateLocationResponse?.validatePlace?.stores?.let {
            for (store in it) {
                if (store.storeName?.contains(s.toString(), true) == true || store.storeAddress?.contains(s.toString(), true)==true) {
                    list.add(store)
                }
            }
        }
        setStoreList(list)
    }

    private fun getDeliveryDetailsFromValidateLocation(placeId: String) {
        if (placeId.isNullOrEmpty())
            return
        viewLifecycleOwner.lifecycleScope.launch {
            clickCollectProgress?.visibility = View.VISIBLE
            try {
                validateLocationResponse =
                    confirmAddressViewModel.getValidateLocation(placeId)
                clickCollectProgress?.visibility = View.GONE
                geoDeliveryView?.visibility = View.VISIBLE
                if (validateLocationResponse != null) {
                    when (validateLocationResponse?.httpCode) {
                        AppConstant.HTTP_OK -> {
                            setAddressUI(validateLocationResponse?.validatePlace?.stores, validateLocationResponse)
                        }
                        else -> {
                         showErrorDialog()
                        }
                    }
                }
            } catch (e: HttpException) {
                FirebaseManager.logException(e)
                clickCollectProgress?.visibility = View.GONE
                showErrorDialog()
            }
        }
    }

    private fun setUpViewModel() {
        confirmAddressViewModel = ViewModelProvider(
            this,
            GeoLocationViewModelFactory(GeoLocationApiHelper())
        ).get(ConfirmAddressViewModel::class.java)
    }

    private fun showErrorDialog() {
        requireActivity().resources?.apply {
            vtoErrorBottomSheetDialog.showErrorBottomSheetDialog(
                this@ClickAndCollectStoresFragment,
                requireActivity(),
                getString(R.string.vto_generic_error),
                "",
                getString(R.string.retry_label)
            )
        }
    }
    override fun tryAgain() {
        if(confirmAddressViewModel.isConnectedToInternet(requireActivity()))
        placeId?.let { getDeliveryDetailsFromValidateLocation(it) }
    }

    override fun onMarkerClicked(marker: DynamicMapMarker) { }

    override fun onResume() {
        super.onResume()
        dynamicMapView?.onResume()
    }

    override fun onPause() {
        dynamicMapView?.onPause()
        super.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        dynamicMapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        dynamicMapView?.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        dynamicMapView?.onDestroy()
        super.onDestroyView()
    }
}