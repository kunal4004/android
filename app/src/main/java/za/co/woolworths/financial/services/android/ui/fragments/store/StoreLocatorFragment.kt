package za.co.woolworths.financial.services.android.ui.fragments.store

import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.viewpager.widget.ViewPager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.StoreLocatorFragmentBinding
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.ui.adapters.CardsOnMapAdapter
import za.co.woolworths.financial.services.android.ui.fragments.npc.ParticipatingStoreFragment.Companion.STORE_CARD
import za.co.woolworths.financial.services.android.ui.fragments.store.StoresNearbyFragment1.Companion.CAMERA_ANIMATION_SPEED
import za.co.woolworths.financial.services.android.ui.fragments.vtc.SelectStoreDetailsFragment
import za.co.woolworths.financial.services.android.ui.views.maps.DynamicMapDelegate
import za.co.woolworths.financial.services.android.ui.views.maps.model.DynamicMapMarker
import za.co.woolworths.financial.services.android.util.Utils

class StoreLocatorFragment : Fragment(R.layout.store_locator_fragment), DynamicMapDelegate, ViewPager.OnPageChangeListener {

    private lateinit var binding: StoreLocatorFragmentBinding
    private var currentStorePostion: Int = 0
    private var mMarkers: HashMap<String, Int> = HashMap()
    private var markers: ArrayList<DynamicMapMarker>? = null
    private var previousMarker: DynamicMapMarker? = null
    private var storeDetailsList: MutableList<StoreDetails>? = ArrayList(0)
    @DrawableRes
    private var unSelectedIcon: Int? = null
    @DrawableRes
    private var selectedIcon: Int? = null
    private var showStoreSelect: Boolean = false

    companion object {
        fun newInstance(location: MutableList<StoreDetails>?, storeCardDetails: String?, showStoreSelect: Boolean): StoreLocatorFragment {
            val fragment = StoreLocatorFragment()
            fragment.arguments = bundleOf(
                    STORE_CARD to storeCardDetails
            )
            fragment.storeDetailsList = location ?: ArrayList(0)
            fragment.showStoreSelect = showStoreSelect
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = StoreLocatorFragmentBinding.bind(view)

        initViews(savedInstanceState)
    }

    private fun initViews(savedInstanceState: Bundle?) {
        initCardPager()
        initMap(savedInstanceState)
    }

    private fun initMap(savedInstanceState: Bundle?) {
        if (isAdded) {
            binding.dynamicMapView?.initializeMap(savedInstanceState, this)
            mMarkers = HashMap()
            markers = ArrayList()
        }
    }

    private fun initCardPager() {
        binding.cardPager?.addOnPageChangeListener(this)
        binding.cardPager?.setOnItemClickListener { position ->
            currentStorePostion = position
            showStoreDetails(currentStorePostion)
        }
    }

    private fun drawMarker(latitude: Double?, longitude: Double?, @DrawableRes icon: Int, pos: Int) {
        val marker: DynamicMapMarker? = binding.dynamicMapView?.addMarker(requireContext(), latitude, longitude, icon)
        marker?.let { mark ->
            mark.getId()?.let {
                mMarkers[it] = pos
            }
            markers?.add(marker)
            if (pos == 0) {
                binding.dynamicMapView?.animateCamera(
                    latitude, longitude,
                    zoom = 13f,
                    duration = CAMERA_ANIMATION_SPEED
                )
                previousMarker = marker
            }
        }
    }

    override fun onMapReady() {
        selectedIcon = getSelectedIcon()
        unSelectedIcon = getUnSelectedIcon()

        storeDetailsList?.let { stores -> bindDataWithUI(stores) }
    }

    override fun onMarkerClicked(marker: DynamicMapMarker) {
        marker?.getId()?.let { markerId ->
            val markerId = mMarkers[markerId]
            previousMarker?.setIcon(requireContext(), unSelectedIcon)
            marker?.setIcon(requireContext(), selectedIcon)
            binding.dynamicMapView?.animateCamera(
                marker?.getPositionLatitude(),
                marker?.getPositionLongitude(),
                zoom = 13f,
                duration = CAMERA_ANIMATION_SPEED
            )
            previousMarker = marker
            markerId?.let { id -> binding.cardPager?.currentItem = id }
        }
    }

    private fun getSelectedIcon() = R.drawable.selected_pin

    private fun getUnSelectedIcon() = R.drawable.unselected_pin

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        previousMarker?.apply {
            setIcon(requireContext(), unSelectedIcon)
            markers?.get(position)?.setIcon(requireContext(), selectedIcon)
            binding.dynamicMapView?.animateCamera(
                latitude = markers?.get(position)?.getPositionLatitude(),
                longitude = markers?.get(position)?.getPositionLongitude(),
                zoom = 13f,
                duration = CAMERA_ANIMATION_SPEED
            )
            previousMarker = markers?.get(position)
        }
    }

    private fun bindDataWithUI(storeDetailsList: MutableList<StoreDetails>) {
        if (storeDetailsList.size >= 0) {
            updateMyCurrentLocationOnMap(Utils.getLastSavedLocation())
            for (i in storeDetailsList.indices) {
                if (i == 0) {
                    selectedIcon?.let { selectedIcon -> drawMarker(storeDetailsList[i].latitude, storeDetailsList[i].longitude, selectedIcon, i) }
                } else {
                    unSelectedIcon?.let { unselectedIcon -> drawMarker(storeDetailsList[i].latitude, storeDetailsList[i].longitude, unselectedIcon, i) }
                }
            }
            activity?.let { activity -> binding.cardPager?.adapter = CardsOnMapAdapter(activity, storeDetailsList) }
        }
    }

    private fun updateMyCurrentLocationOnMap(location: Location?) {
        location?.apply {
            binding.dynamicMapView?.addMarker(requireContext(), latitude, longitude, R.drawable.mapcurrentlocation)
            binding.dynamicMapView?.animateCamera(
                latitude, longitude,
                zoom = 13f,
                duration = CAMERA_ANIMATION_SPEED
            )
        }
    }

    private fun showStoreDetails(position: Int) {

        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_SC_REPLACE_CARD_STORE, this) }

        view?.findNavController()?.navigate(R.id.action_participatingStoreFragment_to_selectStoreDetailsFragment, bundleOf(
                "store" to Gson().toJson(storeDetailsList?.get(position)),
                STORE_CARD to arguments?.getString(STORE_CARD),
                 SelectStoreDetailsFragment.SHOW_STORE_SELECT to showStoreSelect,
                "FromStockLocator" to false,
                "SHOULD_DISPLAY_BACK_ICON" to true
        ))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.dynamicMapView?.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        binding.dynamicMapView?.onResume()
    }

    override fun onPause() {
        binding.dynamicMapView?.onPause()
        super.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.dynamicMapView?.onLowMemory()
    }

    override fun onDestroyView() {
        binding.dynamicMapView?.onDestroy()
        super.onDestroyView()
    }
}