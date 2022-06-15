package za.co.woolworths.financial.services.android.ui.fragments.store

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_stores_nearby1.*
import kotlinx.android.synthetic.main.location_service_off_layout.*
import kotlinx.android.synthetic.main.store_details_layout_common.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.LocationResponse
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.models.dto.StoreOfferings
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService.queryServiceGetStore
import za.co.woolworths.financial.services.android.ui.activities.SearchStoresActivity
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.adapters.CardsOnMapAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.cancelRetrofitRequest
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout.PanelState
import za.co.woolworths.financial.services.android.ui.views.WButton
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.ui.views.actionsheet.EnableLocationSettingsFragment.Companion.ACCESS_MY_LOCATION_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.views.maps.DynamicMapDelegate
import za.co.woolworths.financial.services.android.ui.views.maps.DynamicMapView
import za.co.woolworths.financial.services.android.ui.views.maps.model.DynamicMapMarker
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.location.Event
import za.co.woolworths.financial.services.android.util.location.EventType
import za.co.woolworths.financial.services.android.util.location.Locator

class StoresNearbyFragment1 : Fragment(), DynamicMapDelegate, ViewPager.OnPageChangeListener {

    companion object {
        private const val TAG = "StoresNearbyFragment1"
        const val REQUEST_CALL = 1
        var CAMERA_ANIMATION_SPEED = 350
        const val LAYOUT_ANCHORED_RESULT_CODE = 8001
        const val REQUEST_CHECK_SETTINGS = 99
        const val REQUEST_CODE_FINE_GPS = 5123
    }

    private lateinit var locator: Locator

    @DrawableRes
    var unSelectedIcon: Int? = null
    @DrawableRes
    var selectedIcon: Int? = null

    private var mBottomNavigator: BottomNavigator? = null
    var mMarkers: HashMap<String, Int>? = null
    var markers: ArrayList<DynamicMapMarker>? = null
    var previousMarker: DynamicMapMarker? = null
    var currentStorePosition = 0
    var storeDetailsList: List<StoreDetails>? = null
    private var updateMap = false
    var callIntent: Intent? = null
    var myLocation: DynamicMapMarker? = null
    private var navigateMenuState = false
    private var mPopWindowValidationMessage: PopWindowValidationMessage? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    var searchMenu: MenuItem? = null
    private var isSearchMenuEnabled = true
    private var mLocationAPIRequest: Call<LocationResponse>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity) {
            mBottomNavigator = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_stores_nearby1, container, false)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        locator = Locator(activity as AppCompatActivity)
        dynamicMapView?.initializeMap(savedInstanceState, this)
        mMarkers = HashMap()
        markers = ArrayList()

        mPopWindowValidationMessage = PopWindowValidationMessage(activity)
        storesProgressBar?.indeterminateDrawable?.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
        val relNoConnectionLayout =
                v.findViewById<View>(R.id.no_connection_layout) as RelativeLayout
        mErrorHandlerView = ErrorHandlerView(activity, relNoConnectionLayout)
        mErrorHandlerView?.setMargin(relNoConnectionLayout, 0, 0, 0, 0)

        setupToolbar()

        selectUnSelectMarkerDrawable()

        cardPager?.addOnPageChangeListener(this)
        cardPager?.setOnItemClickListener { position ->
            currentStorePosition = position
            showStoreDetails(currentStorePosition)
        }

        close?.setOnClickListener { backToAllStoresPage(currentStorePosition) }
        sliding_layout?.setFadeOnClickListener { sliding_layout?.panelState = PanelState.COLLAPSED }
        sliding_layout?.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                if (slideOffset.toDouble() == 0.0) {
                    sliding_layout?.anchorPoint = 1.0f
                    backToAllStoresPage(currentStorePosition)
                }
            }

            override fun onPanelStateChanged(panel: View, previousState: PanelState, newState: PanelState) {
                when (newState) {
                    PanelState.COLLAPSED -> {
                        /*
                         * Previous result: Application would exit completely when back button is pressed
                         * New result: Panel just returns to its previous position (Panel collapses)
                         */
                        sliding_layout?.isFocusableInTouchMode = true
                        sliding_layout?.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                sliding_layout?.panelState = PanelState.COLLAPSED
                                sliding_layout?.isFocusable = false
                                return@OnKeyListener false // set to false to prevent user double tap hardware back button to navigate back
                            }
                            true
                        })
                        mBottomNavigator?.showBottomNavigationMenu()
                        (activity as? MyAccountActivity)?.supportActionBar?.show()
                    }
                    PanelState.DRAGGING -> {
                        mBottomNavigator?.hideBottomNavigationMenu()
                        (activity as? MyAccountActivity)?.supportActionBar?.hide()

                    }
                    else -> {
                    }
                }
            }
        })

        /*
		 init();
         */
        buttonLocationOn?.setOnClickListener {
            updateMap = true
            KotlinUtils.openAccessMyLocationDeviceSettings(ACCESS_MY_LOCATION_REQUEST_CODE, activity)
        }
        v.findViewById<WButton>(R.id.btnRetry)?.setOnClickListener {
            if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                mErrorHandlerView?.hideErrorHandlerLayout()
                initLocationCheck()
            }
        }

        try {
            activity?.registerReceiver(broadcastCall, IntentFilter("broadcastCall"))
        } catch (ex: Exception) {
            FirebaseManager.logException(ex)
        }

        if (activity is MyAccountActivity) {
            (activity as? MyAccountActivity?)?.supportActionBar?.show()
        }
    }

    private fun selectUnSelectMarkerDrawable() {
        try {
            unSelectedIcon = R.drawable.unselected_pin
            selectedIcon = R.drawable.selected_pin
        } catch (ex: NullPointerException) {
            FirebaseManager.logException(ex)
        }
    }

    private fun initLocationCheck() {
        val locationServiceIsEnabled = Utils.isLocationServiceEnabled(activity)
        val lastKnownLocation = Utils.getLastSavedLocation()
        when {
            lastKnownLocation != null -> {
                updateMap(Utils.getLastSavedLocation())
            }
            locationServiceIsEnabled -> {
                checkLocationServiceAndSetLayout(true)
                startLocationDiscoveryProcess()
            }
            else -> {
                checkLocationServiceAndSetLayout(false)
            }
        }
    }

    private fun startLocationDiscoveryProcess() {
        locator.getCurrentLocation { locationEvent ->
            when (locationEvent) {
                is Event.Location -> handleLocationEvent(locationEvent)
                is Event.Permission -> handlePermissionEvent(locationEvent)
            }
        }
    }

    private fun handlePermissionEvent(permissionEvent: Event.Permission) {
        if (permissionEvent.event == EventType.LOCATION_PERMISSION_NOT_GRANTED) {
            Utils.saveLastLocation(null, activity)
            checkLocationServiceAndSetLayout(false)
        }
    }

    private fun handleLocationEvent(locationEvent: Event.Location) {
        updateMap(locationEvent.locationData)
    }

    override fun onMapReady() {
        unSelectedIcon = R.drawable.unselected_pin
        selectedIcon = R.drawable.selected_pin
        initLocationCheck()
    }

    private fun drawMarker(latitude: Double, longitude: Double, @DrawableRes icon: Int?, pos: Int) {
        val marker = dynamicMapView?.addMarker(requireContext(), latitude, longitude, icon)
        marker?.apply {
            getId()?.let { id ->
                mMarkers?.set(id, pos)
            }
            markers?.add(this)
        }
        if (pos == 0) {
            dynamicMapView?.animateCamera(marker)
            previousMarker = marker
        }
    }

    override fun onPageSelected(position: Int) {
        if (dynamicMapView?.isMapInstantiated() == false) return

        previousMarker?.setIcon(requireContext(), unSelectedIcon)
        markers?.get(position)?.setIcon(requireContext(), selectedIcon)
        dynamicMapView?.animateCamera(markers?.get(position))
        previousMarker = markers?.get(position)
        /*
		 *InfoWindow shows description above a marker.
         *Make info window invisible to make selected marker come in front of unselected marker.
         */
        previousMarker?.showInfoWindow()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageScrollStateChanged(state: Int) {}

    override fun onMarkerClicked(marker: DynamicMapMarker) {
        try {
            val id = mMarkers?.get(marker.getId())
            previousMarker?.setIcon(requireContext(), unSelectedIcon)
            marker.setIcon(requireContext(), selectedIcon)
            previousMarker = marker
            cardPager?.currentItem = id ?: 0
        } catch (ex: NullPointerException) {
            FirebaseManager.logException(ex)
        }
    }

    private fun locationAPIRequest(location: Location) {
        activity?.apply {
            Utils.saveLastLocation(location, this)
            mLocationAPIRequest = init(location)
            invalidateOptionsMenu()
        }
    }

    fun backToAllStoresPage(position: Int) {
        if (dynamicMapView?.isMapInstantiated() == true) {
            dynamicMapView?.setScrollGesturesEnabled(isEnabled = true)
            dynamicMapView?.animateCamera(
                markers?.get(position),
                DynamicMapView.CAMERA_ANIMATION_DURATION_SLOW
            )
        }

        val toolbar = mBottomNavigator?.toolbar()
        toolbar?.animate()?.translationY(toolbar.top.toFloat())?.setInterpolator(AccelerateInterpolator())?.start()
        showAllMarkers(markers)
    }

    private fun showStoreDetails(position: Int) {
        storeDetailsList?.get(position)?.let { storeDetails -> initStoreDetailsView(storeDetails) }

        if (dynamicMapView?.isMapInstantiated() == true) {
            hideMarkers(markers, position)
            val center = dynamicMapView?.getCameraPositionTargetLatitude()
            val northMap = dynamicMapView?.getVisibleRegionNortheastLatitude()
            val diff = northMap?.let { center?.minus(it) }
            val newLat = markers?.get(position)?.getPositionLatitude()?.plus(diff?.div(1.5)!!)
            val newLng = markers?.get(position)?.getPositionLongitude()
            dynamicMapView?.animateCamera(newLat, newLng)
            dynamicMapView?.setScrollGesturesEnabled(isEnabled = false)
        }

        if (sliding_layout?.anchorPoint == 1.0f) {
            val toolbar = mBottomNavigator?.toolbar()
            toolbar?.animate()?.translationY(-toolbar.bottom.toFloat())?.setInterpolator(AccelerateInterpolator())?.start()
            sliding_layout?.anchorPoint = 0.7f
            sliding_layout?.panelState = PanelState.ANCHORED
        }
    }

    private fun hideMarkers(markers: ArrayList<DynamicMapMarker>?, pos: Int) {
        markers?.indices?.let { indices ->
            for (i in indices) {
                if (i != pos) markers[i].setVisibility(isVisible = false)
            }
        }
    }

    private fun showAllMarkers(markers: ArrayList<DynamicMapMarker>?) {
        markers?.indices?.let { indices ->
            for (i in indices) {
                markers[i].setVisibility(isVisible = true)
            }
        }
    }

    fun bindDataWithUI(storeDetailsList: List<StoreDetails>, currentLocation: Location?) {
        if (dynamicMapView?.isMapInstantiated() == true && storeDetailsList.size >= 0) {
            updateMyCurrentLocationOnMap(currentLocation)
            for (i in storeDetailsList.indices) {
                if (i == 0) {
                    drawMarker(storeDetailsList[i].latitude, storeDetailsList[i].longitude, selectedIcon, i)
                } else drawMarker(storeDetailsList[i].latitude, storeDetailsList[i].longitude, unSelectedIcon, i)
            }
        }
        cardPager?.adapter = CardsOnMapAdapter(activity, storeDetailsList)
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun initStoreDetailsView(storeDetail: StoreDetails) {
        val activity = activity ?: return
        timeingsLayout?.removeAllViews()
        brandsLayout?.removeAllViews()
        storeNameTextView?.text = storeDetail.name
        storeAddressTextView?.text =
                if (TextUtils.isEmpty(storeDetail.address)) "" else storeDetail.address
        storeNumberTextView?.text =
                if (TextUtils.isEmpty(storeDetail.phoneNumber)) "" else storeDetail.phoneNumber
        distanceTextView?.text =
                activity.resources?.getString(R.string.distance_per_km, WFormatter.formatMeter(storeDetail.distance))
        if (storeDetail.offerings != null) {
            offeringsTextView?.text =
                    WFormatter.formatOfferingString(getOfferingByType(storeDetail.offerings, "Department"))
            val brandsList = getOfferingByType(storeDetail.offerings, "Brand")
            if (brandsList.isNotEmpty()) {
                var textView: WTextView
                relBrandLayout?.visibility = View.VISIBLE
                for (i in brandsList.indices) {
                    val v = activity.layoutInflater.inflate(R.layout.opening_hours_textview, null)
                    textView = v?.findViewById(R.id.openingHoursTextView)!!
                    textView.setText(brandsList[i].offering)
                    brandsLayout?.addView(textView)
                }
            } else {
                relBrandLayout?.visibility = View.GONE
            }
        } else {
            relBrandLayout?.visibility = View.GONE
        }
        if (storeDetail.times != null && storeDetail.times.size != 0) {
            storeTimingView?.visibility = View.VISIBLE
            var textView: TextView
            val typeface: Typeface? =
                    ResourcesCompat.getFont(activity, R.font.myriad_pro_semi_bold_otf)
            for (i in storeDetail.times.indices) {
                val v = activity.layoutInflater.inflate(R.layout.opening_hours_textview, null)
                textView = v?.findViewById<View>(R.id.openingHoursTextView) as TextView
                textView.text = "${storeDetail.times[i].day} ${storeDetail.times[i].hours}"

                if (i == 0) textView.typeface = typeface
                timeingsLayout?.addView(textView)
            }
        } else {
            storeTimingView?.visibility = View.GONE
        }
        call?.setOnClickListener {
            storeDetail.phoneNumber?.let { phoneNumber -> Utils.makeCall(phoneNumber) }
        }

        direction?.setOnClickListener(View.OnClickListener {
            if (TextUtils.isEmpty(storeDetail.address)) return@OnClickListener
            mPopWindowValidationMessage?.apply {
                setmName(storeDetail.name)
                setmLatitude(storeDetail.latitude)
                setmLongiude(storeDetail.longitude)
                displayValidationMessage("", PopWindowValidationMessage.OVERLAY_TYPE.STORE_LOCATOR_DIRECTION)
            }
        })
    }

    fun init(location: Location?): Call<LocationResponse> {
        onLocationLoadStart()
        val latitude = location?.latitude ?: 0.0
        val longitude = location?.longitude ?: 0.0
        val locationResponseCall = queryServiceGetStore(latitude, longitude, "")
        locationResponseCall.enqueue(CompletionHandler(object : IResponseListener<LocationResponse> {
            override fun onSuccess(locationResponse: LocationResponse?) {
                enableSearchMenu()
                hideProgressBar()
                storeDetailsList = ArrayList()
                storeDetailsList = locationResponse?.Locations
                storeDetailsList?.let { listDetail -> bindDataWithUI(listDetail, location) }
            }

            override fun onFailure(error: Throwable?) {
                val activity = activity ?: return
                activity.runOnUiThread {
                    enableSearchMenu()
                    hideProgressBar()
                    error?.message?.let{ mErrorHandlerView?.networkFailureHandler(it)}
                }
            }
        }, LocationResponse::class.java))
        return locationResponseCall
    }

    private fun getOfferingByType(offerings: List<StoreOfferings>, type: String?): List<StoreOfferings> {
        val list: MutableList<StoreOfferings> = ArrayList()
        list.clear()
        for (d in offerings) {
            if (d.type != null && d.type.contains(type!!)) list.add(d)
        }
        return list
    }

    private fun checkLocationServiceAndSetLayout(locationServiceStatus: Boolean) {
        //Check for location service and Last location
        if (!locationServiceStatus) {
            layoutLocationServiceOn?.visibility = View.GONE
            layoutLocationServiceOff?.visibility = View.VISIBLE
            navigateMenuState = false
            activity?.invalidateOptionsMenu()
        } else {
            layoutLocationServiceOff?.visibility = View.GONE
            layoutLocationServiceOn?.visibility = View.VISIBLE
            navigateMenuState = true
            if (isAdded) activity?.invalidateOptionsMenu()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("RequestSETTINGRESULT", requestCode.toString())
        when (requestCode) {
            FuseLocationAPISingleton.REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> initLocationCheck()
                else -> mBottomNavigator?.popFragment()
            }
            REQUEST_CHECK_SETTINGS -> initLocationCheck()
            LAYOUT_ANCHORED_RESULT_CODE -> {
                sliding_layout?.panelState = PanelState.COLLAPSED
                sliding_layout?.isFocusable = false
            }
            ACCESS_MY_LOCATION_REQUEST_CODE -> startLocationDiscoveryProcess()
        }
    }

    private fun updateMyCurrentLocationOnMap(location: Location?) {
        try {
            val latitude = location?.latitude ?: 0.0
            val longitude = location?.longitude ?: 0.0

            if (myLocation == null) {
                myLocation =
                    dynamicMapView?.addMarker(requireContext(), latitude, longitude, R.drawable.mapcurrentlocation)
            } else {
                myLocation?.setPosition(latitude, longitude)
            }
            dynamicMapView?.animateCamera(myLocation)
        } catch (ex: Exception) {
            FirebaseManager.logException(ex)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.w_store_locator_menu, menu)
        searchMenu = menu.findItem(R.id.action_search).setVisible(true)
        //Disable until finding location
        menu.findItem(R.id.action_locate)?.isVisible = navigateMenuState
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> if (isSearchMenuEnabled) activity?.startActivity(Intent(activity, SearchStoresActivity::class.java))
            R.id.action_locate -> Utils.getLastSavedLocation()?.let { location -> zoomToLocation(location) }
        }
        return super.onOptionsItemSelected(item)
    }

    fun showProgressBar() {
        storesProgressBar?.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        storesProgressBar?.visibility = View.GONE
    }

    private fun updateMap(location: Location?) {
        if (location != null) {
            Utils.saveLastLocation(location, activity)
            checkLocationServiceAndSetLayout(true)
            updateMyCurrentLocationOnMap(location)
            locationAPIRequest(location)
        }
    }

    @SuppressLint("NewApi")
    private fun checkLocationPermission(): Boolean {
        activity?.apply {
            val perms =
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            return if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(this, perms, REQUEST_CODE_FINE_GPS)
                } else {
                    //we can request the permission.
                    ActivityCompat.requestPermissions(this, perms, REQUEST_CODE_FINE_GPS)
                }
                false
            } else {
                true
            }
        }
        return false
    }

    private fun zoomToLocation(location: Location) {
        dynamicMapView?.animateCamera(
            location.latitude,
            location.longitude,
            zoom = 13f,
            bearing = 0f,
            tilt = 25f,
            duration = 2000.coerceAtLeast(1) // The duration must be strictly positive so we make it at least 1.
        )
    }

    var broadcastCall: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            startActivity(callIntent)
        }
    }

    fun unregisterReceiver() {
        try {
            activity?.unregisterReceiver(broadcastCall)
        } catch (ex: Exception) {
            FirebaseManager.logException(ex)
        }
    }

    override fun onDetach() {
        super.onDetach()
        mBottomNavigator?.removeToolbar()
    }

    override fun onDestroyView() {
        dynamicMapView?.onDestroy()
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver()
        cancelRetrofitRequest(mLocationAPIRequest)
    }

    override fun onResume() {
        super.onResume()
        dynamicMapView?.onResume()
        activity?.let { Utils.setScreenName(it, FirebaseManagerAnalyticsProperties.ScreenNames.STORES_NEARBY) }
        if (updateMap) {
            checkLocationServiceAndSetLayout(true)
            initLocationCheck()
            updateMap = false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CALL -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) startActivity(callIntent)
        }
    }

    fun enableSearchMenu() {
        if (searchMenu != null) {
            isSearchMenuEnabled = true
            searchMenu?.icon?.alpha = 255
            activity?.invalidateOptionsMenu()
        }
    }

    private fun disableSearchMenu() {
        if (searchMenu != null) {
            isSearchMenuEnabled = false
            searchMenu?.icon?.alpha = 130
            activity?.invalidateOptionsMenu()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            setupToolbar()
    }

    private fun setupToolbar() {
        val toolbarTitle = bindString(R.string.stores_nearby)
        mBottomNavigator?.apply {
            setTitle(toolbarTitle)
            showBackNavigationIcon(true)
            displayToolbar()
        }
        if (activity is MyAccountActivity) {
            (activity as? MyAccountActivity?)?.setToolbarTitle(toolbarTitle)
        }

        if (activity is PayMyAccountActivity){
            (activity as? PayMyAccountActivity)?.apply {
                configureToolbar(toolbarTitle)
                displayToolbarDivider(true)
            }
        }
    }

    private fun onLocationLoadStart() {
        disableSearchMenu()
        showProgressBar()
        mErrorHandlerView?.hideErrorHandlerLayout()
    }

    fun layoutIsAnchored(): Boolean {
        return if (sliding_layout == null) false else sliding_layout?.panelState == PanelState.ANCHORED
    }

    fun collapseSlidingPanel() { sliding_layout.panelState = PanelState.COLLAPSED }

    fun getSlidingPanelState() = sliding_layout?.panelState

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
}