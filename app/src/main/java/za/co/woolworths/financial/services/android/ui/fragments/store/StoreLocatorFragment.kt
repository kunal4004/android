package za.co.woolworths.financial.services.android.ui.fragments.store

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.awfs.coordination.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.store_locator_fragment.*
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.ui.activities.vtc.SelectStoreDetailsActivity
import za.co.woolworths.financial.services.android.ui.activities.vtc.StoreLocatorActivity
import za.co.woolworths.financial.services.android.ui.adapters.CardsOnMapAdapter
import za.co.woolworths.financial.services.android.ui.adapters.MapWindowAdapter
import za.co.woolworths.financial.services.android.ui.fragments.store.StoresNearbyFragment1.Companion.CAMERA_ANIMATION_SPEED
import za.co.woolworths.financial.services.android.util.Utils
import java.util.ArrayList
import java.util.HashMap

class StoreLocatorFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, ViewPager.OnPageChangeListener {

    private var currentStorePostion: Int = 0
    private var mGoogleMap: GoogleMap? = null
    private var mMarkers: HashMap<String, Int> = HashMap()
    private var markers: ArrayList<Marker>? = null
    private var mapFragment: SupportMapFragment? = null
    private var previousMarker: Marker? = null
    private var storeDetailsList: MutableList<StoreDetails>? = ArrayList(0)
    private var unSelectedIcon: BitmapDescriptor? = null
    private var selectedIcon: BitmapDescriptor? = null

    companion object {
        fun newInstance(location: MutableList<StoreDetails>?): StoreLocatorFragment {
            val fragment = StoreLocatorFragment()
            fragment.storeDetailsList = location ?: ArrayList(0)
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.store_locator_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCardPager()
        initMap()
    }

    private fun initCardPager() {
        cardPager?.addOnPageChangeListener(this)
        cardPager?.setOnItemClickListener { position ->
            currentStorePostion = position
            showStoreDetails(currentStorePostion)
        }
    }

    private fun drawMarker(point: LatLng, bitmapDescriptor: BitmapDescriptor, pos: Int) {
        val markerOptions = MarkerOptions()
        markerOptions.position(point)
        markerOptions.icon(bitmapDescriptor)
        val marker: Marker? = mGoogleMap?.addMarker(markerOptions)
        marker?.let { mark ->
            mMarkers[mark.id] = pos
            markers?.add(marker)
            if (pos == 0) {
                mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 13f), CAMERA_ANIMATION_SPEED, null)
                previousMarker = marker
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.mGoogleMap = googleMap
        onMapReady()
    }

    private fun onMapReady() {

        selectedIcon = getSelectedIcon()
        unSelectedIcon = getUnSelectedIcon()

        activity?.apply {
            mGoogleMap?.setInfoWindowAdapter(MapWindowAdapter(this))
            mGoogleMap?.setOnMarkerClickListener(this@StoreLocatorFragment)
        }

        storeDetailsList?.let { stores -> bindDataWithUI(stores) }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        val markerId = mMarkers[marker?.id]
        previousMarker?.setIcon(unSelectedIcon)
        marker?.setIcon(selectedIcon)
        mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker?.position, 13F), CAMERA_ANIMATION_SPEED, null)
        previousMarker = marker
        markerId?.let { id -> cardPager?.currentItem = id }
        return true
    }

    private fun getSelectedIcon() = BitmapDescriptorFactory.fromResource(R.drawable.selected_pin)

    private fun getUnSelectedIcon() = BitmapDescriptorFactory.fromResource(R.drawable.unselected_pin)

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        previousMarker?.apply {
            setIcon(unSelectedIcon)
            markers?.get(position)?.setIcon(selectedIcon)
            mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(markers?.get(position)?.position, 13f), CAMERA_ANIMATION_SPEED, null)
            previousMarker = markers?.get(position)
        }
    }

    private fun initMap() {
        if (isAdded) {
            mapFragment = this.childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment?
            mapFragment?.getMapAsync(this)
            mMarkers = HashMap()
            markers = ArrayList()
        }
    }

    private fun bindDataWithUI(storeDetailsList: MutableList<StoreDetails>) {
        mGoogleMap.let {
            if (storeDetailsList.size >= 0) {
                activity?.let { activity -> updateMyCurrentLocationOnMap(Utils.getLastSavedLocation()) }
                for (i in storeDetailsList.indices) {
                    if (i == 0) {
                        selectedIcon?.let { selectedIcon -> drawMarker(LatLng(storeDetailsList[i].latitude, storeDetailsList[i].longitude), selectedIcon, i) }
                    } else
                        unSelectedIcon?.let { unselectedIcon -> drawMarker(LatLng(storeDetailsList[i].latitude, storeDetailsList[i].longitude), unselectedIcon, i) }
                }
                activity?.let { activity -> cardPager?.adapter = CardsOnMapAdapter(activity, storeDetailsList) }
            }
        }
    }

    private fun updateMyCurrentLocationOnMap(location: Location?) {
        location?.apply {
            mGoogleMap?.addMarker(MarkerOptions().position(LatLng(latitude, longitude))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapcurrentlocation)))
            mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 13f), CAMERA_ANIMATION_SPEED, null)
        }
    }

    private fun showStoreDetails(position: Int) {
        /*activity?.apply {
            with(Intent(this, SelectStoreDetailsActivity::class.java)) {
                putExtra("store", Gson().toJson(storeDetailsList?.get(position)))
                putExtra("FromStockLocator", false)
                putExtra("SHOULD_DISPLAY_BACK_ICON", true)
                startActivity(this)
                overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }*/

        view?.findNavController()?.navigate(R.id.action_participatingStoreFragment_to_selectStoreDetailsFragment, bundleOf(
                "store" to Gson().toJson(storeDetailsList?.get(position)),
                "FromStockLocator" to false,
                "SHOULD_DISPLAY_BACK_ICON" to true
        ))
    }
}