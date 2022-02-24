package za.co.woolworths.financial.services.android.geolocation.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback

import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_click_and_collect_stores.*
import kotlinx.android.synthetic.main.fragment_click_and_collect_stores.progressBar
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.view.adapter.StoreListAdapter
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
import za.co.woolworths.financial.services.android.models.dto.Store
import java.util.ArrayList


class ClickAndCollectStoresFragment : Fragment(), OnMapReadyCallback, StoreListAdapter.OnStoreSelected {
    private lateinit var rvStoreList: RecyclerView
    private lateinit var  progressBar: ProgressBar
    private lateinit var geoLocationViewModel: ConfirmAddressViewModel
    var map: GoogleMap? = null

    companion object {
        var instance = ClickAndCollectStoresFragment()
        fun newInstance() = instance
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_click_and_collect_stores, container, false)
        rvStoreList = view.findViewById(R.id.rvStoreList)
        progressBar = view.findViewById(R.id.progressBar)
        setUpViewModel()
        val mapView = view.findViewById(R.id.mapView) as MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        fetchAddress()
        return view
    }

    private fun setUpViewModel() {
        geoLocationViewModel = ViewModelProvider(
            this,
            GeoLocationViewModelFactory(GeoLocationApiHelper())
        ).get(ConfirmAddressViewModel::class.java)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        map?.uiSettings?.isMyLocationButtonEnabled = false
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        map?.isMyLocationEnabled = true
        map?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(43.1, -87.9)))
    }

    private fun fetchAddress() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            try {
                val savedAddressResponse = geoLocationViewModel.getSavedAddress()
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
        rvStoreList.layoutManager =
            activity?.let { activity -> LinearLayoutManager(activity) }
        rvStoreList.adapter = activity?.let { activity ->
            StoreListAdapter(
                activity,
                address,
                this
            )
        }
    }

    private fun setButtonUI(activated: Boolean) {
        if (activated) {
            //tvConfirmAddress.setBackgroundColor(resources.getColor(R.color.black))
        } else {
            //tvConfirmAddress.setBackgroundColor(resources.getColor(R.color.color_A9A9A9))
        }

    }

    override fun onStoreSelected(store: Store) {
        TODO("Not yet implemented")
    }


}