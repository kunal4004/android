package za.co.woolworths.financial.services.android.geolocation.view

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ConfirmAddressBottomSheetDialogBinding
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutAddressConfirmationListAdapter
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.geolocation.LocationProviderBroadcastReceiver
import za.co.woolworths.financial.services.android.geolocation.model.MapData
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.geolocation.network.model.ConfirmAddressStoreLocator
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.view.adapter.SavedAddressAdapter
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.shop.StandardDeliveryFragment.Companion.DEPARTMENT_LOGIN_REQUEST
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.EnableLocationSettingsFragment
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.ADDRESS
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DEFAULT_ADDRESS
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DELIVERY_TYPE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_CONFIRM_ADD
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_CHECKOUT
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_SLOT_SELECTION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_FROM_DASH_TAB
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_FROM_STORE_LOCATOR
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_LATITUDE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_LONGITUDE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_PLACE_ID
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getDeliveryType
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.location.DynamicGeocoder
import za.co.woolworths.financial.services.android.util.location.Event
import za.co.woolworths.financial.services.android.util.location.EventType
import za.co.woolworths.financial.services.android.util.location.Locator
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmAddressFragment : Fragment(R.layout.confirm_address_bottom_sheet_dialog),
    SavedAddressAdapter.OnAddressSelected,
    PermissionResultCallback,
    LocationProviderBroadcastReceiver.LocationProviderInterface,
    View.OnClickListener, VtoTryAgainListener {

    private lateinit var binding: ConfirmAddressBottomSheetDialogBinding
    private lateinit var locator: Locator
    private var mPosition: Int = 0
    private var savedAddressResponse: SavedAddressResponse? = null
    private var mLastLocation: Location? = null
    private var selectedAddress = Address()
    private var bundle: Bundle? = null
    private var isComingFromCheckout: Boolean = false
    private var isComingFromSlotSelection: Boolean = false
    private var isFromDashTab: Boolean = false
    private var deliveryType: String? = null
    private var isAddressAvailable: Boolean = false
    private var permissionUtils: PermissionUtils? = null
    var permissions: ArrayList<String> = arrayListOf()
    private lateinit var locationBroadcastReceiver : LocationProviderBroadcastReceiver

    @Inject
    lateinit var vtoErrorBottomSheetDialog: VtoErrorBottomSheetDialog
    private var address:Address? = null

    companion object {
        fun newInstance() = ConfirmAddressFragment()
    }

    private val confirmAddressViewModel: ConfirmAddressViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle(BUNDLE)
        bundle?.apply {
            isComingFromCheckout = this.getBoolean(IS_COMING_FROM_CHECKOUT, false)
            isComingFromSlotSelection = this.getBoolean(IS_COMING_FROM_SLOT_SELECTION, false)
            isFromDashTab = this.getBoolean(IS_FROM_DASH_TAB, false)
            deliveryType = this.getString(DELIVERY_TYPE, "")
        }
        hideBottomNav()
    }

    private fun hideBottomNav() {
        (activity as? BottomNavigationActivity)?.apply {
            hideBottomNavigationMenu()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ConfirmAddressBottomSheetDialogBinding.bind(view)
        locator = Locator(activity as AppCompatActivity)
        locationBroadcastReceiver = LocationProviderBroadcastReceiver()
        locationBroadcastReceiver.registerCallback(this)
        activity?.apply {
            permissionUtils = PermissionUtils(this, this@ConfirmAddressFragment)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        binding.initViews()
        addFragmentListener()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
        checkForLocationPermissionAndSetLocationAddress()
        binding.updateInitialStateOnResume()
    }

    private fun ConfirmAddressBottomSheetDialogBinding.updateInitialStateOnResume() {
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            inSavedAddress?.root?.visibility = View.GONE
            tvSignIn?.visibility = View.GONE
            tvConfirmAddress?.visibility = View.VISIBLE
            if (confirmAddressViewModel.isConnectedToInternet(requireActivity()))
                fetchAddress()
            else {
                noAddressConnectionLayout?.root?.visibility = View.VISIBLE
                noAddressConnectionLayout?.noConnectionLayout?.visibility = View.VISIBLE
            }
            rvSavedAddressList?.visibility = View.VISIBLE
        } else {
            inSavedAddress?.root?.visibility = View.GONE
            tvConfirmAddress?.visibility = View.GONE
            rvSavedAddressList?.visibility = View.GONE
            tvSignIn?.visibility = View.VISIBLE
        }
    }

    private fun ConfirmAddressBottomSheetDialogBinding.initViews() {
        tvConfirmAddress?.setOnClickListener(this@ConfirmAddressFragment)
        inCurrentLocation?.root?.setOnClickListener(this@ConfirmAddressFragment)
        inSavedAddress?.root?.setOnClickListener(this@ConfirmAddressFragment)
        backButton?.setOnClickListener(this@ConfirmAddressFragment)
        enterNewAddress?.setOnClickListener(this@ConfirmAddressFragment)
        tvSignIn?.setOnClickListener(this@ConfirmAddressFragment)

        if (SessionUtilities.getInstance().isUserAuthenticated) {
            inSavedAddress?.root?.visibility = View.GONE
            tvConfirmAddress?.visibility = View.VISIBLE
            if (confirmAddressViewModel.isConnectedToInternet(requireActivity()))
                binding.fetchAddress()
            else {
                noAddressConnectionLayout?.root?.visibility = View.VISIBLE
                noAddressConnectionLayout?.noConnectionLayout?.visibility = View.VISIBLE
            }
        } else {
            inSavedAddress?.root?.visibility = View.GONE
            tvConfirmAddress?.visibility = View.GONE
        }
        binding.setButtonUI(false)
        noAddressConnectionLayout?.btnRetry?.setOnClickListener {
            binding.initViews()
        }

        inCurrentLocation?.swEnableLocation?.setOnClickListener {
            if (inCurrentLocation?.swEnableLocation?.isChecked == true) {
                if (!Utils.isLocationEnabled(requireContext())) {
                    KotlinUtils.openAccessMyLocationDeviceSettings(
                            EnableLocationSettingsFragment.ACCESS_MY_LOCATION_REQUEST_CODE, activity)
                } else if(!PermissionUtils.hasPermissions(
                                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    checkLocationPermission()
                } else {
                    inCurrentLocation?.swEnableLocation?.isChecked = true
                    startLocationDiscoveryProcess()
                }
            }
        }
    }

    private fun addFragmentListener() {
        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT) { _, _ ->
            // Do nothing. Only want to close this listener.
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun checkForLocationPermissionAndSetLocationAddress() {
        activity?.apply {
            //Check if user has location services enabled. If not, notify user as per current store locator functionality.
           /* if (!Utils.isLocationEnabled(this)) {
                val enableLocationSettingsFragment = EnableLocationSettingsFragment()
                enableLocationSettingsFragment?.show(
                    supportFragmentManager,
                    EnableLocationSettingsFragment::class.java.simpleName
                )
                return@apply
            }*/
            val isLocEnabled = Utils.isLocationEnabled(this)

            // If location services enabled, extract latitude and longitude
            if (isLocEnabled && PermissionUtils.hasPermissions(
                            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                startLocationDiscoveryProcess()
            } else {
                isAddressAvailable = false
                binding.disableCurrentLocation()
                binding.inCurrentLocation?.swEnableLocation?.isChecked = false
            }
        }
    }

    private fun startLocationDiscoveryProcess() {
        locator?.getCurrentLocationSilently { locationEvent ->
            when (locationEvent) {
                is Event.Location -> binding?.handleLocationEvent(locationEvent)
                is Event.Permission -> handlePermissionEvent(locationEvent)
            }
        }
    }

    private fun handlePermissionEvent(permissionEvent: Event.Permission) {
        when (permissionEvent.event) {
            EventType.LOCATION_PERMISSION_GRANTED -> {
                // do nothing
            }
            EventType.LOCATION_PERMISSION_NOT_GRANTED -> {
                Utils.saveLastLocation(null, activity)
                binding.handleLocationEvent(null)
            }
            EventType.LOCATION_DISABLED_ON_DEVICE -> {
                // do nothing
            }
            EventType.LOCATION_SERVICE_DISCONNECTED -> {
                Utils.getLastSavedLocation()?.let {
                    binding.handleLocationEvent(Event.Location(it))
                }
            }
        }
    }

    private fun ConfirmAddressBottomSheetDialogBinding.handleLocationEvent(locationEvent: Event.Location?) {
        Utils.saveLastLocation(locationEvent?.locationData, context)
        mLastLocation = locationEvent?.locationData
        mLastLocation?.let {
            DynamicGeocoder.getAddressFromLocation(activity, it.latitude, it.longitude) { address ->
                address?.let { address ->
                    isAddressAvailable = true
                    inCurrentLocation?.tvCurrentLocation?.text = address.addressLine
                    inCurrentLocation?.ivArrow?.visibility = View.VISIBLE
                    inCurrentLocation?.swEnableLocation?.visibility = View.GONE
                } ?: kotlin.run {
                    binding.disableCurrentLocation()
                    isAddressAvailable = false
                }
            }
        } ?: kotlin.run {
            binding.disableCurrentLocation()
            isAddressAvailable = false
        }
    }

    private fun ConfirmAddressBottomSheetDialogBinding.fetchAddress() {
        lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE
            try {
                savedAddressResponse = confirmAddressViewModel.getSavedAddress()
                savedAddressResponse?.addresses?.let { addressList ->
                    val nickName = savedAddressResponse?.defaultAddressNickname
                    //To Show the DefaultAddress on Top of the Adapter
                    if (!nickName.isNullOrEmpty()) {
                        val defaultAddressOptional: Optional<Address> =
                            addressList.stream()
                                .filter { address -> address.nickname.equals(nickName) }
                                .findFirst()
                        if (defaultAddressOptional.isPresent) {
                            val defaultAddress = defaultAddressOptional.get()
                            addressList.remove(defaultAddress)
                            addressList.add(0, defaultAddress)
                            binding.setAddressUI(addressList)
                        } else {
                            binding.setAddressUI(addressList)
                        }
                    } else {
                        binding.setAddressUI(addressList)
                    }
                }
                savedAddressResponse?.defaultAddressNickname?.let {
                    binding.setButtonUI(it.length > 1)
                }
                progressBar?.visibility = View.GONE
            } catch (e: Exception) {
                FirebaseManager.logException(e)
                progressBar?.visibility = View.GONE
            }
        }
    }

    private fun ConfirmAddressBottomSheetDialogBinding.setAddressUI(address: ArrayList<Address>) {
        rvSavedAddressList?.layoutManager =
            activity?.let { activity -> LinearLayoutManager(activity) }
        rvSavedAddressList?.adapter = activity?.let { activity ->
            SavedAddressAdapter(
                activity,
                address,
                savedAddressResponse?.defaultAddressNickname,
                this@ConfirmAddressFragment,
            )
        }
    }

    private fun ConfirmAddressBottomSheetDialogBinding.setButtonUI(activated: Boolean) {
        if (activated) {
            tvConfirmAddress?.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.black
                )
            )
        } else {
            tvConfirmAddress?.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.color_A9A9A9
                )
            )
        }
    }

    private fun ConfirmAddressBottomSheetDialogBinding.disableCurrentLocation() {
        inCurrentLocation?.ivArrow?.visibility = View.GONE
        inCurrentLocation?.swEnableLocation?.visibility = View.VISIBLE
        inCurrentLocation?.swEnableLocation?.isChecked = false
        inCurrentLocation?.tvCurrentLocation?.text = getString(R.string.enable_location_services)
    }

    override fun onAddressSelected(address: Address, position: Int) {
        selectedAddress = address
        mPosition = position
        binding.currentLocDiv.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        binding.setButtonUI(true)
        if (address.verified) {
            selectedAddress?.apply {
                    binding.tvConfirmAddress?.text=getString(R.string.use)+nickname
            }
        } else {
            binding.tvConfirmAddress?.text = getString(R.string.update_address)
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
                    activity
                )

                if (binding.progressBar.visibility == View.GONE
                    && selectedAddress != null
                    && binding.tvConfirmAddress?.text == getString(R.string.update_address)
                ) {
                    savedAddressResponse?.let {

                        Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.SHOP_UPDATE_ADDRESS,
                            hashMapOf(
                                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_UPDATE_ADDRESS
                            ),
                            activity
                        )

                        navigateToUpdateAddress(it)
                    }
                    return
                }
                if (binding.progressBar.visibility == View.GONE
                    && selectedAddress != null
                    && binding.tvConfirmAddress?.text == getString(R.string.confirm) || binding.tvConfirmAddress?.text?.take(
                        4
                    )
                    == (getString(R.string.use))
                ) {
                    selectedAddress.let {
                        if (it.placesId != null) {
                            address = it
                            binding.validateLocation(it)
                        } else
                            return
                    }
                }
            }
            R.id.inCurrentLocation -> {
                if(isAddressAvailable) {
                if (isComingFromCheckout && deliveryType == Delivery.STANDARD.name) {
                    navigateToAddAddress(savedAddressResponse)
                } else if (isComingFromCheckout && deliveryType == Delivery.CNC.name) {
                    //Navigate to map screen with delivery type or checkout type
                    val confirmAddressStoreLocator =
                        ConfirmAddressStoreLocator(
                            mLastLocation?.latitude,
                            mLastLocation?.longitude,
                            false, deliveryType
                        )
                    navigateToConfirmAddressForStoreLocator(confirmAddressStoreLocator)
                } else if (!isComingFromCheckout && deliveryType == Delivery.DASH.name) {
                    // Navigate to Map screen
                    val getMapData =
                        MapData(
                            mLastLocation?.latitude,
                            mLastLocation?.longitude,
                            false,
                            isComingFromCheckout = false,
                            isFromDashTab = isFromDashTab,
                            deliveryType = deliveryType
                        )
                    val directions =
                        ConfirmAddressFragmentDirections.actionToConfirmAddressMapFragment(
                            getMapData
                        )
                    if (findNavController().currentDestination?.id == R.id.confirmAddressLocationFragment) {
                        findNavController().navigate(directions)
                    }
                } else {
                    val getMapData =
                        MapData(
                            mLastLocation?.latitude,
                            mLastLocation?.longitude,
                            false,
                            isComingFromCheckout = false,
                            isFromDashTab = false,
                            deliveryType = deliveryType
                        )
                    val directions =
                        ConfirmAddressFragmentDirections.actionToConfirmAddressMapFragment(
                            getMapData
                        )
                    if (findNavController().currentDestination?.id == R.id.confirmAddressLocationFragment) {
                        findNavController().navigate(directions)
                    }
                }
            }

        }
            R.id.inSavedAddress -> {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.SHOP_SAVED_PLACES,
                    hashMapOf(
                        FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_SAVED_PLACES
                    ),
                    activity
                )
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
                    activity
                )

                if (isComingFromCheckout && (deliveryType == Delivery.STANDARD.name || deliveryType == Delivery.DASH.name)) {
                    navigateToAddAddress(savedAddressResponse)
                } else if (isComingFromCheckout && deliveryType == Delivery.CNC.name) {
                    //Navigate to map screen with delivery type or checkout type
                    val confirmAddressStoreLocator =
                        ConfirmAddressStoreLocator(0.0, 0.0, true, deliveryType)
                    navigateToConfirmAddressForStoreLocator(confirmAddressStoreLocator)

                } else if (!isComingFromCheckout && deliveryType == Delivery.DASH.name) {
                    // Navigate to Map screen
                    val getMapData =
                        MapData(
                            0.0, 0.0, true,
                            isComingFromCheckout = false,
                            isFromDashTab = isFromDashTab,
                            deliveryType = deliveryType
                        )
                    val directions =
                        ConfirmAddressFragmentDirections.actionToConfirmAddressMapFragment(
                            getMapData
                        )
                    if (findNavController().currentDestination?.id == R.id.confirmAddressLocationFragment) {
                        findNavController().navigate(directions)
                    }
                } else {
                    val getMapData =
                        MapData(
                            0.0, 0.0, true,
                            isComingFromCheckout = false,
                            isFromDashTab = false,
                            deliveryType = deliveryType
                        )
                    val directions =
                        ConfirmAddressFragmentDirections.actionToConfirmAddressMapFragment(
                            getMapData
                        )
                    if (findNavController().currentDestination?.id == R.id.confirmAddressLocationFragment) {
                        findNavController().navigate(directions)
                    }
                }
            }
            R.id.tvSignIn -> {
                ScreenManager.presentSSOSignin(activity, DEPARTMENT_LOGIN_REQUEST)
            }
        }
    }

    private fun ConfirmAddressBottomSheetDialogBinding.validateLocation(address: Address) {
        if (address.placesId.isNullOrEmpty())
            return

        // Make Validate Location Call
        lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE
            try {
                val validateLocationResponse =
                    confirmAddressViewModel.getValidateLocation(address.placesId!!)
                progressBar?.visibility = View.GONE
                if (validateLocationResponse != null) {
                    when (validateLocationResponse?.httpCode) {
                        AppConstant.HTTP_OK -> {
                            validateLocationResponse.validatePlace?.let { place ->

                                if (isFromDashTab == true) {
                                    if (place.onDemand != null && place.onDemand!!.deliverable == true) {
                                        KotlinUtils.isDashTabCrossClicked =
                                            address.placesId?.equals(getDeliveryType()?.address?.placeId) // changing black tooltip flag as user changes in his location.
                                        if (getDeliveryType() == null) {
                                            // User don't have any location (sign in or sign out both) that's why we are setting new location.
                                            binding.confirmSetAddress(
                                                validateLocationResponse,
                                                address.placesId!!, BundleKeysConstants.DASH
                                            )
                                        } else {
                                            // User has location. Means only changing browsing location.
                                            // directly go back to Dash landing screen. Don't call confirm location API as user only wants to browse Dash.
                                            val intent = Intent()
                                            intent.putExtra(
                                                BundleKeysConstants.VALIDATE_RESPONSE,
                                                validateLocationResponse
                                            )
                                            activity?.setResult(Activity.RESULT_OK, intent)
                                            activity?.finish()
                                        }
                                    } else {
                                        // Show not deliverable Bottom Dialog.
                                        val customBottomSheetDialogFragment =
                                            CustomBottomSheetDialogFragment.newInstance(
                                                getString(R.string.no_location_title),
                                                getString(R.string.no_location_desc),
                                                getString(R.string.change_location),
                                                R.drawable.location_disabled,
                                                getString(R.string.dismiss)
                                            )
                                        customBottomSheetDialogFragment.show(
                                            requireFragmentManager(),
                                            CustomBottomSheetDialogFragment::class.java.simpleName
                                        )
                                    }
                                } else if (KotlinUtils.isComingFromCncTab == true) {
                                    KotlinUtils.isComingFromCncTab = false
                                    /* set cnc browsing data */
                                    WoolworthsApplication.setCncBrowsingValidatePlaceDetails(
                                        validateLocationResponse?.validatePlace
                                    )
                                    /*user is coming from CNC i.e. set Location flow or change button flow  */
                                    // navigate to CNC home tab.
                                    activity?.finish()
                                } else {
                                    if (getDeliveryType() == null) {
                                        // User don't have any location (signin or signout both) then move user to change fulfillment screen.
                                        navigateToLastScreen(address)
                                        return@let
                                    }
                                    when (deliveryType) {
                                        // As per delivery type first we will verify if it is deliverable for that or not.
                                        Delivery.STANDARD.name -> {
                                            if (place.deliverable == true) {
                                                navigateToLastScreen(address)
                                            } else
                                                showChangeLocationDialog()
                                        }
                                        Delivery.CNC.name -> {
                                            if (place.stores?.getOrNull(0)?.deliverable == true) {
                                                navigateToLastScreen(address)
                                            } else
                                                showNoCollectionStores()
                                        }
                                        Delivery.DASH.name -> {
                                            if (place.onDemand?.deliverable == true) {
                                                navigateToLastScreen(address)
                                            } else
                                                showChangeLocationDialog()
                                        }
                                        else -> {
                                            // This happens when there is no location. So delivery type might be null.
                                            if (place.deliverable == true) {
                                                navigateToLastScreen(address)
                                            } else
                                                showChangeLocationDialog()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                FirebaseManager.logException(e)
                progressBar?.visibility = View.GONE
                showErrorDialog()
            } catch (e: JsonSyntaxException) {
                FirebaseManager.logException(e)
                progressBar?.visibility = View.GONE
                showErrorDialog()
            }
        }
    }

    private fun ConfirmAddressFragment.showErrorDialog() {
        if(!isAdded && !isVisible) return
        requireActivity().resources?.apply {
            vtoErrorBottomSheetDialog?.showErrorBottomSheetDialog(
                this@ConfirmAddressFragment,
                requireActivity(),
                getString(R.string.something_went_wrong),
                getString(R.string.location_error_msg),
                getString(R.string.retry_label)
            )
        }
    }


    private fun ConfirmAddressBottomSheetDialogBinding.confirmSetAddress(
        validateLocationResponse: ValidateLocationResponse,
        placeId: String,
        currentDeliveryType: String,
    ) {
        if (placeId.isNullOrEmpty())
            return

        //make confirm Location call
        val confirmLocationAddress = ConfirmLocationAddress(placeId)
        val confirmLocationRequest =
            ConfirmLocationRequest(
                currentDeliveryType,
                confirmLocationAddress,
                validateLocationResponse.validatePlace?.onDemand?.storeId
            )

        lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE
            try {
                val confirmLocationResponse =
                    confirmAddressViewModel.postConfirmAddress(confirmLocationRequest)
                progressBar?.visibility = View.GONE
                if (confirmLocationResponse != null) {
                    when (confirmLocationResponse.httpCode) {
                        AppConstant.HTTP_OK -> {

                            /*reset browsing data for cnc and dash both once fulfillment location is confirmed*/
                            WoolworthsApplication.setCncBrowsingValidatePlaceDetails(
                                validateLocationResponse?.validatePlace
                            )
                            WoolworthsApplication.setDashBrowsingValidatePlaceDetails(
                                validateLocationResponse?.validatePlace
                            )

                            KotlinUtils.placeId = placeId
                            KotlinUtils.isLocationPlaceIdSame =
                                placeId?.equals(Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId)

                            WoolworthsApplication.setValidatedSuburbProducts(
                                validateLocationResponse.validatePlace
                            )

                            // save details in cache
                            if (SessionUtilities.getInstance().isUserAuthenticated) {
                                Utils.savePreferredDeliveryLocation(
                                    ShoppingDeliveryLocation(
                                        confirmLocationResponse.orderSummary?.fulfillmentDetails
                                    )
                                )
                                if (KotlinUtils.getAnonymousUserLocationDetails() != null)
                                    KotlinUtils.clearAnonymousUserLocationDetails()
                            } else {
                                KotlinUtils.saveAnonymousUserLocationDetails(
                                    ShoppingDeliveryLocation(
                                        confirmLocationResponse.orderSummary?.fulfillmentDetails
                                    )
                                )
                            }

                            // navigate to Dash home tab.
                            activity?.setResult(Activity.RESULT_OK)
                            activity?.finish()
                        }
                    }
                }
            } catch (e: Exception) {
                progressBar?.visibility = View.GONE
                FirebaseManager.logException(e)
            }
        }
    }

    private fun navigateToLastScreen(address: Address) {
        bundle?.apply {
            putString(KEY_LATITUDE, address.latitude?.toString())
            putString(KEY_LONGITUDE, address.longitude?.toString())
            putString(KEY_PLACE_ID, address.placesId)
            putString(ADDRESS, address.address1)
            putString(CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY, Utils.toJson(address))
            putSerializable(DEFAULT_ADDRESS, address)
            putBoolean(IS_COMING_CONFIRM_ADD, true)
        }
        if (IS_FROM_STORE_LOCATOR) {
            view?.let {
                GeoUtils.navigateSafe(
                    it, R.id.actionClickAndCollectStoresFragment,
                    bundleOf(BUNDLE to bundle)
                )
            }
        } else {
            if (findNavController().navigateUp()) {
                setFragmentResult(
                    DeliveryAddressConfirmationFragment.MAP_LOCATION_RESULT,
                    bundleOf(BUNDLE to bundle)
                )
            } else {
                view?.let {
                    GeoUtils.navigateSafe(
                        it, R.id.actionToDeliveryAddressConfirmationFragment,
                        bundleOf(BUNDLE to bundle)
                    )
                }
            }
        }
    }

    private fun showChangeLocationDialog() {
        val customBottomSheetDialogFragment =
            CustomBottomSheetDialogFragment.newInstance(
                getString(R.string.no_location_title),
                getString(R.string.no_location_desc),
                getString(R.string.change_location),
                R.drawable.location_disabled, getString(R.string.dismiss)
            )
        customBottomSheetDialogFragment.show(
            requireFragmentManager(),
            CustomBottomSheetDialogFragment::class.java.simpleName
        )
    }

    private fun showNoCollectionStores() {
        // Show no store available Bottom Dialog.
        val customBottomSheetDialogFragment =
            CustomBottomSheetDialogFragment.newInstance(
                getString(R.string.no_location_collection),
                getString(R.string.no_location_desc),
                getString(R.string.change_location),
                R.drawable.img_collection_bag,
                resources.getString(R.string.cancel_underline_html)
            )
        customBottomSheetDialogFragment.show(
            requireFragmentManager(),
            CustomBottomSheetDialogFragment::class.java.simpleName
        )
    }

    private fun navigateToConfirmAddressForStoreLocator(confirmAddressStoreLocator: ConfirmAddressStoreLocator) {
        val getMapData = MapData(
            confirmAddressStoreLocator.latitude,
            confirmAddressStoreLocator.longitude,
            confirmAddressStoreLocator.isAddAddress,
            isComingFromCheckout = true,
            isFromDashTab = false,
            deliveryType = confirmAddressStoreLocator.deliveryType
        )
        val directions =
            ConfirmAddressFragmentDirections.actionToConfirmAddressMapFragment(
                getMapData
            )
        if (findNavController().currentDestination?.id == R.id.confirmAddressLocationFragment) {
            findNavController().navigate(directions)
        }
    }

    private fun navigateToUpdateAddress(savedAddressResponse: SavedAddressResponse) {
        val bundle = Bundle()

        bundle.putString(
            CheckoutAddressConfirmationListAdapter.EDIT_SAVED_ADDRESS_RESPONSE_KEY,
            Utils.toJson(savedAddressResponse)
        )

        bundle.putInt(
            CheckoutAddressConfirmationListAdapter.EDIT_ADDRESS_POSITION_KEY,
            mPosition
        )

        view?.let {
            GeoUtils.navigateSafe(
                it, R.id.action_confirmAddressLocationFragment_to_checkoutAddAddressNewUserFragment,
                bundleOf(BUNDLE to bundle)
            )
        }
    }


    private fun navigateToAddAddress(savedAddressResponse: SavedAddressResponse?) {
        val bundle = Bundle()
        bundle.putBoolean(CheckoutAddressConfirmationFragment.ADD_NEW_ADDRESS_KEY, true)

        bundle.putString(
            CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY,
            Utils.toJson(savedAddressResponse)
        )
        bundle.putString(DELIVERY_TYPE, deliveryType)
        IS_COMING_FROM_SLOT_SELECTION
        bundle.putBoolean(
            IS_COMING_FROM_CHECKOUT,
            isComingFromCheckout
        )
        bundle.putBoolean(
            IS_COMING_FROM_SLOT_SELECTION,
            isComingFromSlotSelection
        )
        view?.let {
            GeoUtils.navigateSafe(
                it, R.id.action_confirmAddressLocationFragment_to_checkoutAddAddressNewUserFragment,
                bundleOf(BUNDLE to bundle)
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EnableLocationSettingsFragment.ACCESS_MY_LOCATION_REQUEST_CODE) {
            startLocationDiscoveryProcess()
        }
    }

    private fun checkLocationPermission() {
        permissionUtils?.checkAndRequestPermissions(
                permissions,
                AppConstant.LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun permissionGranted(requestCode: Int) {
        if (requestCode == AppConstant.LOCATION_PERMISSION_REQUEST_CODE) {
            if (!Utils.isLocationEnabled(requireContext())) {
                KotlinUtils.openAccessMyLocationDeviceSettings(
                        EnableLocationSettingsFragment.ACCESS_MY_LOCATION_REQUEST_CODE, activity)
            }
        }
    }

    private fun unregisterReceiver() {
        try {
            if(::locationBroadcastReceiver.isInitialized) {
                locationBroadcastReceiver.let {
                    requireContext().unregisterReceiver(locationBroadcastReceiver)
                }
            }
        } catch (ex: IllegalArgumentException) {
            FirebaseManager.logException("unregisterReceiver locationBroadcastReceiver $ex")
        }
    }

    private fun registerReceiver() {
        requireContext().registerReceiver(locationBroadcastReceiver, IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        })
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver()
    }

    override fun onDetach() {
        super.onDetach()
        unregisterReceiver()
    }

    override fun onLocationProviderChange(context: Context?, intent: Intent?) {
        if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
            Handler(Looper.getMainLooper()).postDelayed({
                checkForLocationPermissionAndSetLocationAddress()
            }, AppConstant.DELAY_2000_MS)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun tryAgain() {
        address?.let {
            binding.validateLocation(it)
        }
    }
}