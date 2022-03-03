package za.co.woolworths.financial.services.android.geolocation.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.confirm_address_bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.current_location_row_layout.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.view.adapter.SavedAddressAdapter
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.shop.DepartmentsFragment.Companion.DEPARTMENT_LOGIN_REQUEST
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import java.util.*

class ConfirmAddressFragment : Fragment(), SavedAddressAdapter.OnAddressSelected,
    View.OnClickListener {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mLastLocation: Location? = null
    private var rvSavedAddress: RecyclerView? = null
    private var selectedAddress = Address()

    companion object {
        var dialogInstance = ConfirmAddressFragment()
        fun newInstance() = dialogInstance
    }

    private lateinit var confirmAddressViewModel: ConfirmAddressViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        if (checkPermissions()) {
            getLastLocation()
        } else {
            hideCurrentLocation()
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
        rvSavedAddress = view.findViewById(R.id.rvSavedAddressList)
        setUpViewModel()
        initViews()
    }

    override fun onResume() {
        super.onResume()
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            inSavedAddress.visibility = View.GONE
            tvConfirmAddress.visibility = View.VISIBLE
            fetchAddress()
            rvSavedAddressList.visibility = View.VISIBLE
        } else {
            inSavedAddress.visibility = View.VISIBLE
            tvConfirmAddress.visibility = View.GONE
            rvSavedAddressList.visibility = View.GONE
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
            progressBar.visibility = View.VISIBLE
            try {
                val savedAddressResponse = confirmAddressViewModel.getSavedAddress()
                savedAddressResponse.addresses?.let { setAddressUI(it) }
                savedAddressResponse.defaultAddressNickname?.let {
                    setButtonUI(it.length > 1)
                }
                progressBar.visibility = View.GONE
            } catch (e: HttpException) {
                e.printStackTrace()
                progressBar.visibility = View.GONE
            }

        }
    }

    private fun setAddressUI(address: ArrayList<Address>) {
        rvSavedAddressList.layoutManager =
            activity?.let { activity -> LinearLayoutManager(activity) }
        rvSavedAddressList.adapter = activity?.let { activity ->
            SavedAddressAdapter(
                activity,
                address,
                this
            )
        }
    }

    private fun setButtonUI(activated: Boolean) {
        if (activated) {
            tvConfirmAddress.setBackgroundColor(resources.getColor(R.color.black))
        } else {
            tvConfirmAddress.setBackgroundColor(resources.getColor(R.color.color_A9A9A9))
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
                if (progressBar.visibility == View.GONE && selectedAddress != null && tvConfirmAddress.text == getString(R.string.confirm))
                {
                    selectedAddress.let {
                        if (it.latitude != null && it.longitude != null && it.placesId != null) {
                            (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(
                                GeolocationDeliveryAddressConfirmationFragment.newInstance(
                                    selectedAddress.latitude!!,
                                    selectedAddress.longitude!!,
                                    selectedAddress.placesId!!))
                        }
                        else
                            return
                    }
                }
            }
            R.id.inCurrentLocation -> {
                (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(
                    ConfirmAddressMapFragment(mLastLocation?.latitude, mLastLocation?.longitude,false))
            }
            R.id.inSavedAddress -> {
                ScreenManager.presentSSOSignin(activity, DEPARTMENT_LOGIN_REQUEST)
            }
            R.id.backButton -> {
                activity?.onBackPressed()
            }
            R.id.enterNewAddress ->{
                (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(
                    ConfirmAddressMapFragment(null, null,true))

            }
        }
    }
}