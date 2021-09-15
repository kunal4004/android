package za.co.woolworths.financial.services.android.ui.fragments.vtc

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.*
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_select_store_details.*
import kotlinx.android.synthetic.main.layout_confirmation.*
import kotlinx.android.synthetic.main.select_store_activity.*
import kotlinx.android.synthetic.main.store_details_layout_common.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.models.dto.StoreOfferings
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.network.StoreCardEmailConfirmBody
import za.co.woolworths.financial.services.android.ui.activities.card.SelectStoreActivity
import za.co.woolworths.financial.services.android.ui.fragments.npc.ParticipatingStoreFragment.Companion.STORE_CARD
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout.PanelState
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage
import za.co.woolworths.financial.services.android.util.SpannableMenuOption
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension.Companion.animateViewPushDown
import java.util.*

class SelectStoreDetailsFragment : Fragment(), OnMapReadyCallback {

    private val REQUEST_CALL = 1
    var googleMap: GoogleMap? = null

    var storeDetails: StoreDetails? = null
    var showStoreSelect: Boolean = false

    companion object{
        const val SHOW_STORE_SELECT = "SHOW_STORE_SELECT"
    }

    var callIntent: Intent? = null
    private var mPopWindowValidationMessage: PopWindowValidationMessage? = null
    private var isFromStockLocator = false
    private var mShouldDisplayBackIcon = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            storeDetails = Gson().fromJson(getString("store"), StoreDetails::class.java)
            isFromStockLocator = getBoolean("FromStockLocator", false)
            mShouldDisplayBackIcon = getBoolean("SHOULD_DISPLAY_BACK_ICON", false)
            showStoreSelect = getBoolean(SHOW_STORE_SELECT, false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_store_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar()

        mPopWindowValidationMessage = PopWindowValidationMessage(context)
        //getting height of device
        val displaymetrics = DisplayMetrics()
        activity?.apply {
            windowManager.defaultDisplay.getMetrics(displaymetrics)
        }
        val height = displaymetrics.heightPixels
        val width = displaymetrics.widthPixels
        //set height of map view to 3/10 of the screen height
        mapLayout?.layoutParams = SlidingUpPanelLayout.LayoutParams(width, height * 3 / 10)
        //set height of store details view to 7/10 of the screen height
        selectStoreSlidingPane?.panelHeight = height * 7 / 10
        animateViewPushDown(selectStoreTextViewBtn)
        selectStoreTextViewBtn?.visibility = if(showStoreSelect) View.VISIBLE else View.GONE

        initStoreDetailsView(storeDetails)
        if (mShouldDisplayBackIcon) {
            closePage?.setImageResource(R.drawable.back_button_circular_icon)
            closePage?.rotation = 180f
        }
        closePage?.setOnClickListener {
            onBackPressed()
        }
        selectStoreSlidingPane?.setFadeOnClickListener { selectStoreSlidingPane?.panelState = PanelState.COLLAPSED }
        selectStoreSlidingPane?.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                if (slideOffset.toDouble() == 0.0) {
                    selectStoreSlidingPane?.anchorPoint = 1.0f
                }
            }

            override fun onPanelStateChanged(panel: View, previousState: PanelState, newState: PanelState) {
                if (newState != PanelState.COLLAPSED) {
                    /*
                     * Previous result: Application would exit completely when back button is pressed
                     * New result: Panel just returns to its previous position (Panel collapses)
                     */
                    selectStoreSlidingPane?.isFocusableInTouchMode = true
                    selectStoreSlidingPane?.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            selectStoreSlidingPane?.panelState = PanelState.COLLAPSED
                            selectStoreSlidingPane?.isFocusable = false
                            return@OnKeyListener true
                        }
                        true
                    })
                }
            }
        })
        initMap()
    }

    private fun setupActionBar() {
        (activity as? SelectStoreActivity)?.apply {
            supportActionBar?.hide()
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.apply { Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.STORE_DETAILS) }
    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map
        googleMap?.uiSettings?.isScrollGesturesEnabled = false
        googleMap?.isMyLocationEnabled = false
        centerCamera()
    }

    fun centerCamera() {
        googleMap?.addMarker(MarkerOptions().position(LatLng(storeDetails?.latitude ?: 0.0, storeDetails?.longitude ?: 0.0))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.selected_pin)))
        val cameraPosition = CameraPosition.Builder().target(
                LatLng(storeDetails?.latitude ?: 0.0, storeDetails?.longitude ?: 0.0)).zoom(13f).build()
        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    fun initMap() {
        activity?.apply {
            if (googleMap == null) {
                val fm: FragmentManager = supportFragmentManager /// getChildFragmentManager();
                var mapFragment: SupportMapFragment? = null
                mapFragment = fm.findFragmentById(R.id.map) as? SupportMapFragment
                if (mapFragment == null) {
                    mapFragment = SupportMapFragment.newInstance()
                    fm.beginTransaction().replace(R.id.map, mapFragment).commit()
                }
                mapFragment?.getMapAsync(this@SelectStoreDetailsFragment)
            }
        }
    }

    fun onBackPressed() {
        findNavController()?.navigateUp()
    }

    fun initStoreDetailsView(storeDetail: StoreDetails?) {
        timeingsLayout?.removeAllViews()
        brandsLayout?.removeAllViews()
        storeNameTextView?.text = storeDetail!!.name
        storeAddressTextView?.text = storeDetail.address ?: ""
        if (storeDetail.phoneNumber != null) storeNumberTextView?.text = storeDetail.phoneNumber
        val spannableMenuOption = SpannableMenuOption(context)
        distanceTextView?.text = WFormatter.formatMeter(storeDetail.distance) + resources.getString(R.string.distance_in_km)
        val resources = resources
        if (isFromStockLocator) {
            offeringsTextView?.let { Utils.setRagRating(context, it, storeDetails!!.status) }
        } else {
            if (storeDetail.offerings != null) {
                offeringsTextView?.text = WFormatter.formatOfferingString(storeDetail.offerings)
            }
        }
        if (storeDetail.offerings != null) {
            val brandslist = getOfferingByType(storeDetail.offerings, "Brand")
            if (brandslist != null) {
                if (brandslist.isNotEmpty()) {
                    var textView: TextView
                    relBrandLayout?.visibility = View.VISIBLE
                    for (i in brandslist.indices) {
                        val v = layoutInflater.inflate(R.layout.opening_hours_textview, null)
                        textView = v.findViewById(R.id.openingHoursTextView)
                        textView.text = brandslist[i].offering
                        brandsLayout?.addView(textView)
                    }
                } else {
                    relBrandLayout?.visibility = View.GONE
                }
            } else {
                relBrandLayout?.visibility = View.GONE
            }
        } else {
            relBrandLayout?.visibility = View.GONE
        }
        var textView: TextView
        if (storeDetail.times != null && storeDetail.times.size != 0) {
            storeTimingView?.visibility = View.VISIBLE
            for (i in storeDetail.times.indices) {
                val v = layoutInflater.inflate(R.layout.opening_hours_textview, null)
                textView = v.findViewById(R.id.openingHoursTextView)
                textView.text = storeDetail.times[i].day + " " + storeDetail.times[i].hours
                if (i == 0) {
                    context?.let { textView.setTypeface(Typeface.createFromAsset(it.assets, "fonts/MyriadPro-Semibold.otf")) }
                }
                timeingsLayout?.addView(textView)
            }
        } else {
            storeTimingView?.visibility = View.GONE
        }
        call?.setOnClickListener { v: View? ->
            if (storeDetail.phoneNumber != null) {
                Utils.makeCall(storeDetail.phoneNumber)
            }
        }
        direction?.setOnClickListener { v: View? ->
            if (TextUtils.isEmpty(storeDetail.address)) return@setOnClickListener
            mPopWindowValidationMessage!!.setmLatitude(storeDetail.latitude)
            mPopWindowValidationMessage!!.setmLongiude(storeDetail.longitude)
            mPopWindowValidationMessage!!.displayValidationMessage("",
                    PopWindowValidationMessage.OVERLAY_TYPE.STORE_LOCATOR_DIRECTION)
        }
        selectStoreTextViewBtn?.setOnClickListener { v: View? -> navigateToConfirmStore() }
    }

    private fun navigateToConfirmStore() {
        var resp: StoreCardsResponse? = null
        arguments?.apply {
            val storeCardData = getString(STORE_CARD)
            resp = Gson().fromJson(storeCardData, StoreCardsResponse::class.java)
        }

        val storeCardEmailConfirmBody = StoreCardEmailConfirmBody(visionAccountNumber = resp?.storeCardsData?.visionAccountNumber.toString(), storeName = storeDetails?.name, storeAddress = storeDetails?.address, deliveryMethod = "store")
        view?.findNavController()?.navigate(R.id.action_selectStoreDetailsFragment_to_storeConfirmationFragment, bundleOf(
                StoreConfirmationFragment.STORE_DETAILS to Gson().toJson(storeCardEmailConfirmBody)
        ))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CALL -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(callIntent)
            }
        }
    }

    fun getOfferingByType(offerings: List<StoreOfferings>, type: String?): List<StoreOfferings>? {
        val list: MutableList<StoreOfferings> = ArrayList()
        list.clear()
        for (d in offerings) {
            if (d.type != null && d.type.contains(type!!)) list.add(d)
        }
        return list
    }
}