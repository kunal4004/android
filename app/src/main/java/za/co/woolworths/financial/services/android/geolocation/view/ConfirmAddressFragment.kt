package za.co.woolworths.financial.services.android.geolocation.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
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
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.model.MapData
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.view.adapter.SavedAddressAdapter
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CartFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.DepartmentsFragment.Companion.DEPARTMENT_LOGIN_REQUEST
import za.co.woolworths.financial.services.android.util.*
import java.util.*

class ConfirmAddressFragment : Fragment(), SavedAddressAdapter.OnAddressSelected,
    View.OnClickListener {
    private var savedAddressResponse: SavedAddressResponse? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mLastLocation: Location? = null
    private var selectedAddress = Address()

    companion object {
        fun newInstance() = ConfirmAddressFragment()
        var IS_COMING_FROM_CONFIRM_ADDRESS = "isComingFromConfirmAddress"
    }

    private lateinit var confirmAddressViewModel: ConfirmAddressViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            fetchAddress()
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
            fetchAddress()
        } else {
            inSavedAddress?.visibility = View.VISIBLE
            tvConfirmAddress?.visibility = View.GONE
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnCompleteListener(activity as Activity) { task ->
                if (task.isSuccessful) {
                    mLastLocation = task.result
                    if (mLastLocation != null) {
                        val addresses = Geocoder(
                            activity,
                            Locale.getDefault()
                        ).getFromLocation(mLastLocation!!.latitude, mLastLocation!!.longitude, 1)
                        tvCurrentLocation.text = addresses[0].getAddressLine(0)
                    } else {
                        hideCurrentLocation()
                    }
                } else {
                    Log.w("TAG", "getLastLocation:exception", task.exception)
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
                /*TODO:  Error sceanrio*/
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
                this
            )
        }
    }

    private fun setButtonUI(activated: Boolean) {
        if (activated) {
            tvConfirmAddress?.setBackgroundColor(resources.getColor(R.color.black))
        } else {
            tvConfirmAddress?.setBackgroundColor(resources.getColor(R.color.color_A9A9A9))
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

    override fun onAddressSelected(address: Address) {
        selectedAddress = address
        setButtonUI(true)
        if (address.verified) {
            tvConfirmAddress.text = getString(R.string.confirm)
        } else {
            tvConfirmAddress.text = getString(R.string.update_address)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.tvConfirmAddress -> {
                if (progressBar.visibility == View.GONE
                    && selectedAddress != null
                    && tvConfirmAddress.text == getString(R.string.update_address)){
                    savedAddressResponse?.let { navigateToCheckout(it) }
                    return
                }
                if (progressBar.visibility == View.GONE
                    && selectedAddress != null
                    && tvConfirmAddress.text == getString(R.string.confirm))
                {
                    selectedAddress.let {
                        if (it.latitude != null && it.longitude != null && it.placesId != null) {
//
//                              Navigation.findNavController(
//                                  requireActivity(),
//                                  R.id.nav_host_container).navigate(
//                                  ConfirmAddressFragmentDirections
//                                      .actionConfirmDeliveryLocationFragmentToConfirmAddressMapFragment2())


//                            (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(
//                                DeliveryAddressConfirmationFragment.newInstance(
//                                    selectedAddress.latitude.toString()!!,
//                                    selectedAddress.longitude.toString()!!,
//                                    selectedAddress.placesId.toString()!!))
                        }
                        else
                            return
                    }
                }
            }
            R.id.inCurrentLocation -> {

                val getMapData =
                    getDataForMapView(mLastLocation?.latitude, mLastLocation?.longitude, false)
                val directions =
                    ConfirmAddressFragmentDirections.actionToConfirmAddressMapFragment(
                        getMapData )
                findNavController().navigate(directions)

            }
            R.id.inSavedAddress -> {
                ScreenManager.presentSSOSignin(activity, DEPARTMENT_LOGIN_REQUEST)
            }
            R.id.backButton -> {
                activity?.onBackPressed()
            }
            R.id.enterNewAddress -> {
                val getMapData =
                    getDataForMapView(0.0, 0.0, true)
                val directions =
                    ConfirmAddressFragmentDirections.actionToConfirmAddressMapFragment(
                        getMapData)
                findNavController().navigate(directions)
            }
        }
    }

    private fun getDataForMapView(latitude: Double?, longitude: Double?, boolean: Boolean?): MapData {
        return MapData(
            latitude = latitude,
            longitude = longitude,
            isAddAddress = boolean
        )
    }

    private fun navigateToCheckout(response: SavedAddressResponse) {
        val activity: Activity? = activity
        if (activity != null) {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.CART_BEGIN_CHECKOUT,
                getActivity()
            )
            val checkoutActivityIntent = Intent(getActivity(), CheckoutActivity::class.java)
            checkoutActivityIntent.putExtra(
                CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY,
                response
            )

            checkoutActivityIntent.putExtra(
                IS_COMING_FROM_CONFIRM_ADDRESS,
                true
            )

            activity.startActivityForResult(
                checkoutActivityIntent,
                CartFragment.REQUEST_PAYMENT_STATUS
            )
            activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_out_to_left)
        }
    }
}