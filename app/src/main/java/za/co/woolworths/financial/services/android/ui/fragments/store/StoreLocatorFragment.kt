package za.co.woolworths.financial.services.android.ui.fragments.store

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.awfs.coordination.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.store_locator_fragment.*
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.ui.activities.StoreLocatorActivity
import za.co.woolworths.financial.services.android.ui.adapters.MapWindowAdapter
import za.co.woolworths.financial.services.android.ui.adapters.StoreLocatorCardAdapter
import za.co.woolworths.financial.services.android.ui.fragments.store.StoresNearbyFragment1.CAMERA_ANIMATION_SPEED
import java.util.HashMap

class StoreLocatorFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mGoogleMap: GoogleMap
    private var mMarkers: HashMap<String, Int> = HashMap()

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 102
        fun newInstance() = StoreLocatorFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.store_locator_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val supportMapFragment = this.childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        supportMapFragment?.getMapAsync(this)

        val cardAdapter = StoreLocatorCardAdapter()
        (activity as? StoreLocatorActivity)?.storeLocatorViewModel?.getStoreLocationResult()?.observe(this, Observer<MutableList<StoreDetails>> { storeDetails ->
            // populate markers
            storeDetails?.forEachIndexed { index, storeDetail -> drawMarker(LatLng(storeDetail.latitude, storeDetail.longitude), if (index == 0) getSelectedIcon() else getUnSelectedIcon(), index) }
            cardViewPager?.adapter = cardAdapter
            cardAdapter.setItem(storeDetails)
        })
    }

    private fun drawMarker(point: LatLng, bitmapDescriptor: BitmapDescriptor, position: Int) {
        val markerOptions = MarkerOptions()
        markerOptions.position(point)
        markerOptions.icon(bitmapDescriptor)
        val marker = mGoogleMap.addMarker(markerOptions)
        mMarkers[marker.id] = position
        if (position == 0) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 13f), CAMERA_ANIMATION_SPEED, null)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.mGoogleMap = googleMap
        onMapReady()
    }

    private fun onMapReady() {
        activity?.apply {
            mGoogleMap.setInfoWindowAdapter(MapWindowAdapter(this))
            mGoogleMap.setOnMarkerClickListener(this@StoreLocatorFragment)
        }

    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        val markerId = mMarkers[marker?.id]


        return true
    }

    private fun getSelectedIcon() = BitmapDescriptorFactory.fromResource(R.drawable.selected_pin)

    private fun getUnSelectedIcon() = BitmapDescriptorFactory.fromResource(R.drawable.unselected_pin)

    private fun zoomToLocation(location: Location) {
        val mLocation = CameraPosition.Builder().target(LatLng(location.latitude, location
                .longitude))
                .zoom(13f)
                .bearing(0f)
                .tilt(25f)
                .build()

        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mLocation), 2000.coerceAtLeast(1), object : GoogleMap.CancelableCallback {
            override fun onFinish() {}

            override fun onCancel() {}
        })
    }
}