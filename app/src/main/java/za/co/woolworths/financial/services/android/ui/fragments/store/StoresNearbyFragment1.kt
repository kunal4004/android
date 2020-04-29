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
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.awfs.coordination.R
import com.crashlytics.android.Crashlytics
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_stores_nearby1.*
import kotlinx.android.synthetic.main.location_service_off_layout.*
import kotlinx.android.synthetic.main.store_details_layout_common.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.ILocationProvider
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.LocationResponse
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.models.dto.StoreOfferings
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService.queryServiceGetStore
import za.co.woolworths.financial.services.android.ui.activities.SearchStoresActivity
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.adapters.CardsOnMapAdapter
import za.co.woolworths.financial.services.android.ui.adapters.MapWindowAdapter
import za.co.woolworths.financial.services.android.ui.extension.cancelRetrofitRequest
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout.PanelState
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.util.*
import java.util.*

class StoresNearbyFragment1 : Fragment(), OnMapReadyCallback, ViewPager.OnPageChangeListener, OnMarkerClickListener, ILocationProvider {

    companion object {
        private const val TAG = "StoresNearbyFragment1"
        const val REQUEST_CALL = 1
        var CAMERA_ANIMATION_SPEED = 350
        const val LAYOUT_ANCHORED_RESULT_CODE = 8001
        const val REQUEST_CHECK_SETTINGS = 99
        const val REQUEST_CODE_FINE_GPS = 5123
    }

    private var mBottomNavigator: BottomNavigator? = null
    var googleMap: GoogleMap? = null
    var unSelectedIcon: BitmapDescriptor? = null
    var selectedIcon: BitmapDescriptor? = null
    var mMarkers: HashMap<String, Int>? = null
    var markers: ArrayList<Marker>? = null
    var previousmarker: Marker? = null
    var mapFragment: SupportMapFragment? = null
    var currentStorePosition = 0
    var storeDetailsList: List<StoreDetails>? = null
    private var updateMap = false
    var callIntent: Intent? = null
    var myLocation: Marker? = null
    private var navigateMenuState = false
    private var mPopWindowValidationMessage: PopWindowValidationMessage? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    private val mLocation: Location? = null
    var searchMenu: MenuItem? = null
    private var isSearchMenuEnabled = true
    var isLocationServiceButtonClicked = false
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

        val activity = activity ?: return
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
                Log.i(TAG, "onPanelSlide, offset $slideOffset")
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
                     */sliding_layout?.isFocusableInTouchMode = true
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
        initLocationCheck()

        /*
		 init();
         */
        buttonLocationOn?.setOnClickListener {
            updateMap = true
            if (checkLocationPermission()) {
                val locIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(locIntent)
                activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            } else {
                isLocationServiceButtonClicked = true
                checkLocationPermission()
            }
        }
        v.findViewById<View>(R.id.btnRetry).setOnClickListener {
            if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                mErrorHandlerView?.hideErrorHandlerLayout()
                initLocationCheck()
            }
        }
        activity.registerReceiver(broadcastCall, IntentFilter("broadcastCall"))
        if (activity is MyAccountActivity) {
            (activity as? MyAccountActivity?)?.supportActionBar?.show()
        }
    }

    private fun selectUnSelectMarkerDrawable() {
        try {
            unSelectedIcon = BitmapDescriptorFactory.fromResource(R.drawable.unselected_pin)
            selectedIcon = BitmapDescriptorFactory.fromResource(R.drawable.selected_pin)
        } catch (ex: NullPointerException) {
            Crashlytics.logException(ex)
        }
    }

    private fun initLocationCheck() {
        val locationServiceIsEnabled = Utils.isLocationServiceEnabled(activity)
        val lastKnownLocationIsNull = Utils.getLastSavedLocation() == null
        if (!locationServiceIsEnabled and lastKnownLocationIsNull) {
            checkLocationServiceAndSetLayout(false)
        } else if (locationServiceIsEnabled && lastKnownLocationIsNull) {
            checkLocationServiceAndSetLayout(true)
            startLocationUpdates()
        } else if (!locationServiceIsEnabled) {
            updateMap(Utils.getLastSavedLocation())
        } else {
            startLocationUpdates()
        }
    }

    private fun initMap() {
        if (googleMap == null && isAdded) {
            mapFragment =
                    this.childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(this)
            mMarkers = HashMap<String, Int>()
            markers = ArrayList<Marker>()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        onMapReady()
    }

    private fun onMapReady() {
        //If permission is not granted, request permission.
        googleMap?.setInfoWindowAdapter(MapWindowAdapter(context))
        googleMap?.setOnMarkerClickListener(this@StoresNearbyFragment1)
        unSelectedIcon = BitmapDescriptorFactory.fromResource(R.drawable.unselected_pin)
        selectedIcon = BitmapDescriptorFactory.fromResource(R.drawable.selected_pin)
    }

    private fun drawMarker(point: LatLng, bitmapDescriptor: BitmapDescriptor?, pos: Int) {
        val markerOptions = MarkerOptions()
        markerOptions.position(point)
        markerOptions.icon(bitmapDescriptor)
        val marker = googleMap?.addMarker(markerOptions)
        mMarkers?.set(marker!!.id, pos)
        marker?.let { markers?.add(it) }
        if (pos == 0) {
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker?.position, 13f), CAMERA_ANIMATION_SPEED, null)
            previousmarker = marker
        }
    }

    override fun onPageSelected(position: Int) {
        previousmarker?.setIcon(unSelectedIcon)
        markers?.get(position)?.setIcon(selectedIcon)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(markers?.get(position)?.position, 13f), CAMERA_ANIMATION_SPEED, null)
        previousmarker = markers?.get(position)
        /*
		 *InfoWindow shows description above a marker.
         *Make info window invisible to make selected marker come in front of unselected marker.
         */previousmarker?.showInfoWindow()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageScrollStateChanged(state: Int) {}
    override fun onMarkerClick(marker: Marker): Boolean {
        onMarkerClicked(marker)
        return true
    }

    private fun onMarkerClicked(marker: Marker) {
        try {
            val id = mMarkers?.get(marker.id)
            previousmarker?.setIcon(unSelectedIcon)
            marker.setIcon(selectedIcon)
            //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 13), CAMERA_ANIMATION_SPEED, null);
            previousmarker = marker
            cardPager?.currentItem = id ?: 0
        } catch (ex: NullPointerException) {
            Crashlytics.logException(ex)
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
        googleMap?.uiSettings?.isScrollGesturesEnabled = true
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(markers?.get(position)?.position, 13f), 500, null)
        val toolbar = mBottomNavigator?.toolbar()
        toolbar?.animate()?.translationY(toolbar.top.toFloat())?.setInterpolator(AccelerateInterpolator())?.start()
        showAllMarkers(markers)
    }

    private fun showStoreDetails(position: Int) {
        storeDetailsList?.get(position)?.let { storeDetails -> initStoreDetailsView(storeDetails) }
        hideMarkers(markers, position)
        val center = googleMap?.cameraPosition?.target?.latitude
        val northMap = googleMap?.projection?.visibleRegion?.latLngBounds?.northeast?.latitude
        val diff = northMap?.let { center?.minus(it) }
        val newLat = markers?.get(position)?.position?.latitude?.plus(diff?.div(1.5)!!)
        val centerCam =
                CameraUpdateFactory.newLatLng(markers?.get(position)?.position?.longitude?.let { newLat?.let { latitude -> LatLng(latitude, it) } })
        googleMap?.animateCamera(centerCam, CAMERA_ANIMATION_SPEED, null)
        googleMap?.uiSettings?.isScrollGesturesEnabled = false
        if (sliding_layout?.anchorPoint == 1.0f) {
            val toolbar = mBottomNavigator?.toolbar()
            toolbar?.animate()?.translationY(-toolbar.bottom.toFloat())?.setInterpolator(AccelerateInterpolator())?.start()
            sliding_layout?.anchorPoint = 0.7f
            sliding_layout?.panelState = PanelState.ANCHORED
        }
    }

    private fun hideMarkers(markers: ArrayList<Marker>?, pos: Int) {
        val indices = markers?.indices
        for (i in indices!!) {
            if (i != pos) markers[i].isVisible = false
        }
    }

    private fun showAllMarkers(markers: ArrayList<Marker>?) {
        val indices = markers?.indices
        for (i in indices!!) {
            markers[i].isVisible = true
        }
    }

    fun bindDataWithUI(storeDetailsList: List<StoreDetails>) {
        if (googleMap != null && storeDetailsList.size >= 0) {
            updateMyCurrentLocationOnMap(mLocation)
            for (i in storeDetailsList.indices) {
                if (i == 0) {
                    drawMarker(LatLng(storeDetailsList[i].latitude, storeDetailsList[i].longitude), selectedIcon, i)
                } else drawMarker(LatLng(storeDetailsList[i].latitude, storeDetailsList[i].longitude), unSelectedIcon, i)
            }
            cardPager?.adapter = CardsOnMapAdapter(activity, storeDetailsList)
        }
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
            override fun onSuccess(locationResponse: LocationResponse) {
                enableSearchMenu()
                hideProgressBar()
                storeDetailsList = ArrayList()
                storeDetailsList = locationResponse.Locations
                storeDetailsList?.let { listDetail -> bindDataWithUI(listDetail) }
            }

            override fun onFailure(error: Throwable) {
                val activity = activity ?: return
                activity.runOnUiThread {
                    enableSearchMenu()
                    hideProgressBar()
                    mErrorHandlerView?.networkFailureHandler(error.message)
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
        }
    }

    private fun updateMyCurrentLocationOnMap(location: Location?) {
        try {
            val latitude = location?.latitude ?: 0.0
            val longitude = location?.longitude ?: 0.0

            if (myLocation == null) {
                myLocation =
                        googleMap?.addMarker(MarkerOptions().position(LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.mapcurrentlocation)))
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 13f), CAMERA_ANIMATION_SPEED, null)
            } else {
                myLocation?.position = LatLng(latitude, longitude)
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 13f), CAMERA_ANIMATION_SPEED, null)
            }
        } catch (ex: Exception) {
            Crashlytics.logException(ex)
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

    private fun goToUser(mLocation: CameraPosition?) {
        changeCamera(CameraUpdateFactory.newCameraPosition(mLocation), object : CancelableCallback {
            override fun onFinish() {}
            override fun onCancel() {}
        })
    }

    /**
     * Change the camera position by moving or animating the camera depending on the state of the
     * animate toggle button.
     */
    private fun changeCamera(update: CameraUpdate, callback: CancelableCallback) {
        // The duration must be strictly positive so we make it at least 1.
        googleMap?.animateCamera(update, 2000.coerceAtLeast(1), callback)
    }

    fun showProgressBar() {
        storesProgressBar?.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        storesProgressBar?.visibility = View.GONE
    }

    private fun startLocationUpdates() {
        val activity = activity ?: return
        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                onLocationLoadStart()
                FuseLocationAPISingleton.addLocationChangeListener(this)
                FuseLocationAPISingleton.startLocationUpdate()
            }
        } else {
            checkLocationPermission()
        }
    }

    private fun updateMap(location: Location?) {
        if (location != null) {
            Utils.saveLastLocation(location, activity)
            checkLocationServiceAndSetLayout(true)
            initMap()
            updateMyCurrentLocationOnMap(location)
            locationAPIRequest(location)
        }
        stopLocationUpdate()
    }

    private fun stopLocationUpdate() {
        FuseLocationAPISingleton.stopLocationUpdate()
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
        val mLocation = CameraPosition.Builder().target(LatLng(location.latitude, location
                .longitude))
                .zoom(13f)
                .bearing(0f)
                .tilt(25f)
                .build()
        goToUser(mLocation)
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
            Crashlytics.logException(ex)
        }
    }

    override fun onDetach() {
        super.onDetach()
        mBottomNavigator?.removeToolbar()
        unregisterReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver()
        cancelRetrofitRequest(mLocationAPIRequest)
    }

    override fun onResume() {
        super.onResume()
        activity?.let { Utils.setScreenName(it, FirebaseManagerAnalyticsProperties.ScreenNames.STORES_NEARBY) }
        if (updateMap) {
            checkLocationServiceAndSetLayout(true)
            initLocationCheck()
            updateMap = false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_FINE_GPS -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    val activity = activity ?: return
                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (isLocationServiceButtonClicked) {
                            val locIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            activity.startActivityForResult(locIntent, REQUEST_CHECK_SETTINGS)
                            activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
                        } else {
                            startLocationUpdates()
                            googleMap?.isMyLocationEnabled = false
                        }
                    }
                }
                return
            }
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
        val toolbarTitle = activity?.resources?.getString(R.string.stores_nearby) ?: ""
        mBottomNavigator?.apply {
            setTitle(toolbarTitle)
            showBackNavigationIcon(true)
            displayToolbar()
        }
        if (activity is MyAccountActivity) {
            (activity as? MyAccountActivity?)?.setToolbarTitle(toolbarTitle)
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

    override fun onLocationChange(location: Location?) {
        updateMap(location)
    }

    override fun onPopUpLocationDialogMethod() {
        hideProgressBar()
    }

    fun collapseSlidingPanel() { sliding_layout.panelState = PanelState.COLLAPSED }

    fun getSlidingPanelState() = sliding_layout?.panelState
}