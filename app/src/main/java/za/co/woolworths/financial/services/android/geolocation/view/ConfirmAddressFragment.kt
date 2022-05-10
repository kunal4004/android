package za.co.woolworths.financial.services.android.geolocation.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.confirm_address_bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.current_location_row_layout.*
import kotlinx.android.synthetic.main.no_connection.*
import kotlinx.android.synthetic.main.no_connection.view.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutAddressConfirmationListAdapter
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.model.MapData
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.view.adapter.SavedAddressAdapter
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.shop.DepartmentsFragment.Companion.DEPARTMENT_LOGIN_REQUEST
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.ADDRESS
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DEFAULT_ADDRESS
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DELIVERY_TYPE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_CONFIRM_ADD
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_CHECKOUT
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_SLOT_SELECTION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_FROM_STORE_LOCATOR
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_PLACE_ID
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import java.io.IOException
import java.util.*

class ConfirmAddressFragment : Fragment(), SavedAddressAdapter.OnAddressSelected,
    View.OnClickListener {
    private var mPosition: Int = 0
    private var savedAddressResponse: SavedAddressResponse? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mLastLocation: Location? = null
    private var selectedAddress = Address()
    private var bundle: Bundle? = null
    private var isComingFromCheckout: Boolean = false
    private var isComingFromSlotSelection: Boolean = false
    private var deliveryType: String? = null

    companion object {
        fun newInstance() = ConfirmAddressFragment()

    }

    private lateinit var confirmAddressViewModel: ConfirmAddressViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle(BUNDLE)
        bundle?.apply {
            isComingFromCheckout = this.getBoolean(
                IS_COMING_FROM_CHECKOUT, false)
            isComingFromSlotSelection = this.getBoolean(
                IS_COMING_FROM_SLOT_SELECTION, false)
            deliveryType =  this.getString(
                DELIVERY_TYPE, "")
        }
        hideBottomNav()
    }

    private fun hideBottomNav() {
        (activity as? BottomNavigationActivity)?.apply {
            hideBottomNavigationMenu()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.confirm_address_bottom_sheet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        initViews()
    }

    override fun onResume() {
        super.onResume()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        if (checkPermissions()) {
            getLastLocation()
        } else {
            hideCurrentLocation()
        }
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            inSavedAddress?.visibility = View.GONE
            tvConfirmAddress?.visibility = View.VISIBLE
            if (confirmAddressViewModel.isConnectedToInternet(requireActivity()))
                fetchAddress()
            else {
                no_connection_layout?.visibility = View.VISIBLE
            }
            rvSavedAddressList?.visibility = View.VISIBLE
        } else {
            inSavedAddress?.visibility = View.VISIBLE
            tvConfirmAddress?.visibility = View.GONE
            rvSavedAddressList?.visibility = View.GONE
        }
    }

    private fun initViews() {
        tvConfirmAddress?.setOnClickListener(this)
        inCurrentLocation?.setOnClickListener(this)
        inSavedAddress?.setOnClickListener(this)
        backButton?.setOnClickListener(this)
        enterNewAddress?.setOnClickListener(this)

        if (SessionUtilities.getInstance().isUserAuthenticated) {
            inSavedAddress?.visibility = View.GONE
            tvConfirmAddress?.visibility = View.VISIBLE
            if (confirmAddressViewModel.isConnectedToInternet(requireActivity()))
                fetchAddress()
            else {
                no_connection_layout?.visibility = View.VISIBLE
            }
        } else {
            inSavedAddress?.visibility = View.VISIBLE
            tvConfirmAddress?.visibility = View.GONE
        }
        setButtonUI(false)
         no_connection_layout?.btnRetry?.setOnClickListener {
            initViews()
        }

    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnCompleteListener(activity as Activity) { task ->
                if (task.isSuccessful) {
                    mLastLocation = task.result
                    mLastLocation?.let {
                        try {
                            val addresses = Geocoder(
                                activity,
                                Locale.getDefault()
                            ).getFromLocation(it.latitude, it.longitude, 1)
                            tvCurrentLocation.text = addresses[0].getAddressLine(0)
                        } catch (io: IOException) {
                            FirebaseManager.logException(io)
                        }
                    }
                    if (mLastLocation == null) {
                        hideCurrentLocation()
                    }
                } else {
                    hideCurrentLocation()
                }
            }
    }

    private fun setUpViewModel() {
        confirmAddressViewModel = ViewModelProvider(
            this,
            GeoLocationViewModelFactory(GeoLocationApiHelper())
        ).get(ConfirmAddressViewModel::class.java)
    }

    private fun fetchAddress() {
        lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE
            try {
                savedAddressResponse = confirmAddressViewModel.getSavedAddress()
                savedAddressResponse?.addresses?.let { setAddressUI(it) }
                savedAddressResponse?.defaultAddressNickname?.let {
                    setButtonUI(it.length > 1)
                }
                progressBar?.visibility = View.GONE
            } catch (e: HttpException) {
                FirebaseManager.logException(e)
                progressBar?.visibility = View.GONE
            }

        }
    }

    private fun setAddressUI(address: ArrayList<Address>) {
        rvSavedAddressList?.layoutManager =
            activity?.let { activity -> LinearLayoutManager(activity) }
        rvSavedAddressList?.adapter = activity?.let { activity ->
            SavedAddressAdapter(
                activity,
                address,
                savedAddressResponse?.defaultAddressNickname,
                this,
            )
        }
    }

    private fun setButtonUI(activated: Boolean) {
        if (activated) {
            tvConfirmAddress?.setBackgroundColor(ContextCompat.getColor(requireActivity(),R.color.black))
        } else {
            tvConfirmAddress?.setBackgroundColor(ContextCompat.getColor(requireActivity(),R.color.color_A9A9A9))
        }

    }


    private fun checkPermissions(): Boolean {
        val permissionState = activity?.let {
            ActivityCompat.checkSelfPermission(
                it,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun hideCurrentLocation() {
        inCurrentLocation.visibility = View.GONE
        currentLocDiv.visibility = View.GONE
    }

    override fun onAddressSelected(address: Address, position: Int) {
        selectedAddress = address
        mPosition = position
        setButtonUI(true)
        if (address.verified) {
            tvConfirmAddress.text = getString(R.string.confirm)
        } else {
            tvConfirmAddress.text = getString(R.string.update_address)
        }
    }

    override fun onEditAddress(address: Address, position: Int) {
        selectedAddress = address
        mPosition = position
        savedAddressResponse?.let { navigateToUpdateAddress(it) }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.tvConfirmAddress -> {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.SHOP_CONFIRM_LOCATION,
                    hashMapOf(
                        FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_CONFIRM_LOCATION
                    ),
                    activity)

                if (progressBar.visibility == View.GONE
                    && selectedAddress != null
                    && tvConfirmAddress.text == getString(R.string.update_address)){
                    savedAddressResponse?.let {

                        Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.SHOP_UPDATE_ADDRESS,
                            hashMapOf(
                                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_UPDATE_ADDRESS
                            ),
                            activity)

                        navigateToUpdateAddress(it)
                    }
                    return
                }
                if (progressBar.visibility == View.GONE
                    && selectedAddress != null
                    && tvConfirmAddress.text == getString(R.string.confirm))
                {
                    selectedAddress?.let {
                        if (it.placesId != null) {

                            bundle?.putString(
                               KEY_PLACE_ID, it.placesId)

                            bundle?.putString(
                                ADDRESS, it.address1)

                            bundle?.putSerializable(
                                DEFAULT_ADDRESS, it)

                            bundle?.putBoolean(
                                IS_COMING_CONFIRM_ADD, true)

                            if (IS_FROM_STORE_LOCATOR) {
                                findNavController().navigate(
                                    R.id.actionClickAndCollectStoresFragment,
                                    bundleOf(BUNDLE to bundle)
                                )
                            } else {
                                findNavController().navigate(
                                    R.id.actionToDeliveryAddressConfirmationFragment,
                                    bundleOf(BUNDLE to bundle)
                                )
                            }

                        }
                        else
                            return
                    }
                }
            }
            R.id.inCurrentLocation -> {

                if (isComingFromCheckout && deliveryType == Delivery.STANDARD.name) {
                    navigateToAddAddress(savedAddressResponse)
                } else if (isComingFromCheckout && deliveryType == Delivery.CNC.name) {
                    //Navigate to map screen with delivery type or checkout type
                    navigateToConfirmAddressForStoreLocator(mLastLocation?.latitude, mLastLocation?.longitude,false)
                } else {
                    val getMapData =
                        getDataForMapView(mLastLocation?.latitude, mLastLocation?.longitude,
                            isAddAddress = false,
                            isComingFromCheckout = false)
                    val directions =
                        ConfirmAddressFragmentDirections.actionToConfirmAddressMapFragment(
                            getMapData
                        )
                    findNavController().navigate(directions)
                }
            }
            R.id.inSavedAddress -> {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.SHOP_SAVED_PLACES,
                    hashMapOf(
                        FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_SAVED_PLACES
                    ),
                    activity)
                ScreenManager.presentSSOSignin(activity, DEPARTMENT_LOGIN_REQUEST)
            }
            R.id.backButton -> {
                activity?.onBackPressed()
            }
            R.id.enterNewAddress -> {


                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.SHOP_NEW_ADDRESS,
                    hashMapOf(
                        FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_NEW_ADDRESS
                    ),
                    activity)

                if (isComingFromCheckout && deliveryType == Delivery.STANDARD.toString()) {
                    navigateToAddAddress(savedAddressResponse)
                } else if (isComingFromCheckout && deliveryType == Delivery.CNC.name) {
                    //Navigate to map screen with delivery type or checkout type
                    navigateToConfirmAddressForStoreLocator(0.0, 0.0,true)

                } else {
                    val getMapData =
                        getDataForMapView(0.0, 0.0, true,false)
                    val directions =
                        ConfirmAddressFragmentDirections.actionToConfirmAddressMapFragment(
                            getMapData
                        )
                    findNavController().navigate(directions)
                }
            }
        }
    }

    private fun navigateToConfirmAddressForStoreLocator(latitude: Double?, longitude: Double?,isAddAddress: Boolean?) {
        val getMapData =
            getDataForMapView(latitude, longitude,
                isAddAddress = isAddAddress,
                isComingFromCheckout = true)
        val directions =
            ConfirmAddressFragmentDirections.actionToConfirmAddressMapFragment(
                getMapData
            )
        findNavController().navigate(directions)
    }

    private fun getDataForMapView(
        latitude: Double?,
        longitude: Double?,
        isAddAddress: Boolean?,
        isComingFromCheckout: Boolean,
    ): MapData {
        return MapData(
            latitude = latitude,
            longitude = longitude,
            isAddAddress = isAddAddress,
            isComingFromCheckout = isComingFromCheckout
        )
    }

    private fun navigateToUpdateAddress(savedAddressResponse: SavedAddressResponse) {
        val bundle = Bundle()

        bundle.putString(
            CheckoutAddressConfirmationListAdapter.EDIT_SAVED_ADDRESS_RESPONSE_KEY,
            Utils.toJson(savedAddressResponse))

        bundle.putInt(
            CheckoutAddressConfirmationListAdapter.EDIT_ADDRESS_POSITION_KEY,
            mPosition)

        findNavController().navigate(
            R.id.action_confirmAddressLocationFragment_to_checkoutAddAddressNewUserFragment,
            bundleOf(BUNDLE to bundle)
        )
    }


    private fun navigateToAddAddress(savedAddressResponse: SavedAddressResponse?) {
        val bundle = Bundle()
        bundle.putBoolean(CheckoutAddressConfirmationFragment.ADD_NEW_ADDRESS_KEY, true)

        bundle.putString(
            CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY,
            Utils.toJson(savedAddressResponse)
        )
        IS_COMING_FROM_SLOT_SELECTION
        bundle.putBoolean(
            IS_COMING_FROM_CHECKOUT,
            isComingFromCheckout
        )
        bundle.putBoolean(
            IS_COMING_FROM_SLOT_SELECTION,
            isComingFromSlotSelection
        )
        findNavController()?.navigate(
            R.id.action_confirmAddressLocationFragment_to_checkoutAddAddressNewUserFragment,
            bundleOf(BUNDLE to bundle)
        )
    }
}