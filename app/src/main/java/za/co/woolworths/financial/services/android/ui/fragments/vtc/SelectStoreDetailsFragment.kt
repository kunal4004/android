package za.co.woolworths.financial.services.android.ui.fragments.vtc

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentSelectStoreDetailsBinding
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.models.dto.StoreOfferings
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.network.StoreCardEmailConfirmBody
import za.co.woolworths.financial.services.android.ui.activities.card.SelectStoreActivity
import za.co.woolworths.financial.services.android.ui.fragments.npc.ParticipatingStoreFragment.Companion.STORE_CARD
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout.PanelState
import za.co.woolworths.financial.services.android.ui.views.maps.DynamicMapDelegate
import za.co.woolworths.financial.services.android.ui.views.maps.model.DynamicMapMarker
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage
import za.co.woolworths.financial.services.android.util.SpannableMenuOption
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension.Companion.animateViewPushDown
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class SelectStoreDetailsFragment :
    BaseFragmentBinding<FragmentSelectStoreDetailsBinding>(FragmentSelectStoreDetailsBinding::inflate),
    DynamicMapDelegate {

    private val REQUEST_CALL = 1

    var storeDetails: StoreDetails? = null
    var showStoreSelect: Boolean = false

    companion object {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            setupActionBar()
            dynamicMapView.initializeMap(savedInstanceState, this@SelectStoreDetailsFragment)

            mPopWindowValidationMessage = PopWindowValidationMessage(context)
            //getting height of device
            val displaymetrics = DisplayMetrics()
            activity?.apply {
                windowManager.defaultDisplay.getMetrics(displaymetrics)
            }
            val height = displaymetrics.heightPixels
            val width = displaymetrics.widthPixels
            //set height of map view to 3/10 of the screen height
            mapLayout.layoutParams = SlidingUpPanelLayout.LayoutParams(width, height * 3 / 10)
            //set height of store details view to 7/10 of the screen height
            selectStoreSlidingPane.panelHeight = height * 7 / 10
            animateViewPushDown(selectStoreTextViewBtn)
            selectStoreTextViewBtn.visibility = if (showStoreSelect) View.VISIBLE else View.GONE

            initStoreDetailsView(storeDetails)
            if (mShouldDisplayBackIcon) {
                closePage.setImageResource(R.drawable.back_button_circular_icon)
                closePage.rotation = 180f
            }
            closePage.setOnClickListener {
                onBackPressed()
            }
            selectStoreSlidingPane.setFadeOnClickListener {
                selectStoreSlidingPane.panelState = PanelState.COLLAPSED
            }
            selectStoreSlidingPane.addPanelSlideListener(object :
                SlidingUpPanelLayout.PanelSlideListener {
                override fun onPanelSlide(panel: View, slideOffset: Float) {
                    if (slideOffset.toDouble() == 0.0) {
                        selectStoreSlidingPane.anchorPoint = 1.0f
                    }
                }

                override fun onPanelStateChanged(
                    panel: View,
                    previousState: PanelState,
                    newState: PanelState
                ) {
                    if (newState != PanelState.COLLAPSED) {
                        /*
                     * Previous result: Application would exit completely when back button is pressed
                     * New result: Panel just returns to its previous position (Panel collapses)
                     */
                        selectStoreSlidingPane.isFocusableInTouchMode = true
                        selectStoreSlidingPane.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                selectStoreSlidingPane.panelState = PanelState.COLLAPSED
                                selectStoreSlidingPane.isFocusable = false
                                return@OnKeyListener true
                            }
                            true
                        })
                    }
                }
            })
        }
    }

    private fun setupActionBar() {
        (activity as? SelectStoreActivity)?.apply {
            supportActionBar?.hide()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.dynamicMapView.onResume()
        activity?.apply {
            Utils.setScreenName(
                this,
                FirebaseManagerAnalyticsProperties.ScreenNames.STORE_DETAILS
            )
        }
    }

    override fun onMapReady() {
        binding.dynamicMapView.setScrollGesturesEnabled(false)
        binding.dynamicMapView.setMyLocationEnabled(false)
        centerCamera()
    }

    fun centerCamera() {
        binding.dynamicMapView.addMarker(
            requireContext(),
            latitude = storeDetails?.latitude ?: 0.0,
            longitude = storeDetails?.longitude ?: 0.0,
            icon = R.drawable.selected_pin
        )
        binding.dynamicMapView.animateCamera(
            latitude = storeDetails?.latitude ?: 0.0,
            longitude = storeDetails?.longitude ?: 0.0,
            zoom = 13f
        )
    }

    fun onBackPressed() {
        findNavController()?.navigateUp()
    }

    fun initStoreDetailsView(storeDetail: StoreDetails?) {
        binding.storeDetailsLayoutCommon.apply {
            timeingsLayout.removeAllViews()
            brandsLayout.removeAllViews()
            storeNameTextView.text = storeDetail!!.name
            storeAddressTextView.text = storeDetail.address ?: ""
            if (storeDetail.phoneNumber != null) storeNumberTextView.text = storeDetail.phoneNumber
            val spannableMenuOption = SpannableMenuOption(context)
            distanceTextView.text =
                WFormatter.formatMeter(storeDetail.distance) + resources.getString(R.string.distance_in_km)
            val resources = resources
            if (isFromStockLocator) {
                offeringsTextView.let { Utils.setRagRating(context, it, storeDetails!!.status) }
            } else {
                if (storeDetail.offerings != null) {
                    offeringsTextView.text = WFormatter.formatOfferingString(storeDetail.offerings)
                }
            }
            if (storeDetail.offerings != null) {
                val brandslist = getOfferingByType(storeDetail.offerings, "Brand")
                if (brandslist != null) {
                    if (brandslist.isNotEmpty()) {
                        var textView: TextView
                        relBrandLayout.visibility = View.VISIBLE
                        for (i in brandslist.indices) {
                            val v = layoutInflater.inflate(R.layout.opening_hours_textview, null)
                            textView = v.findViewById(R.id.openingHoursTextView)
                            textView.text = brandslist[i].offering
                            brandsLayout.addView(textView)
                        }
                    } else {
                        relBrandLayout.visibility = View.GONE
                    }
                } else {
                    relBrandLayout.visibility = View.GONE
                }
            } else {
                relBrandLayout.visibility = View.GONE
            }
            var textView: TextView
            if (storeDetail.times != null && storeDetail.times.size != 0) {
                storeTimingView.visibility = View.VISIBLE
                for (i in storeDetail.times.indices) {
                    val v = layoutInflater.inflate(R.layout.opening_hours_textview, null)
                    textView = v.findViewById(R.id.openingHoursTextView)
                    textView.text = storeDetail.times[i].day + " " + storeDetail.times[i].hours
                    if (i == 0) {
                        context?.let {
                            textView.setTypeface(
                                Typeface.createFromAsset(
                                    it.assets,
                                    "fonts/OpenSans-SemiBold.ttf"
                                )
                            )
                        }
                    }
                    timeingsLayout.addView(textView)
                }
            } else {
                storeTimingView.visibility = View.GONE
            }
            call.setOnClickListener { v: View? ->
                if (storeDetail.phoneNumber != null) {
                    Utils.makeCall(storeDetail.phoneNumber)
                }
            }
            direction.setOnClickListener { v: View? ->
                if (TextUtils.isEmpty(storeDetail.address)) return@setOnClickListener
                mPopWindowValidationMessage!!.setmLatitude(storeDetail.latitude)
                mPopWindowValidationMessage!!.setmLongiude(storeDetail.longitude)
                mPopWindowValidationMessage!!.displayValidationMessage(
                    "",
                    PopWindowValidationMessage.OVERLAY_TYPE.STORE_LOCATOR_DIRECTION
                )
            }
            binding.selectStoreTextViewBtn.setOnClickListener { v: View? -> navigateToConfirmStore() }
        }
    }

    private fun navigateToConfirmStore() {
        var resp: StoreCardsResponse? = null
        arguments?.apply {
            val storeCardData = getString(STORE_CARD)
            resp = Gson().fromJson(storeCardData, StoreCardsResponse::class.java)
        }

        val storeCardEmailConfirmBody = StoreCardEmailConfirmBody(
            visionAccountNumber = resp?.storeCardsData?.visionAccountNumber.toString(),
            storeName = storeDetails?.name,
            storeAddress = storeDetails?.address,
            deliveryMethod = "store"
        )
        view?.findNavController()?.navigate(
            R.id.action_selectStoreDetailsFragment_to_storeConfirmationFragment, bundleOf(
                StoreConfirmationFragment.STORE_DETAILS to Gson().toJson(storeCardEmailConfirmBody)
            )
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
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

    override fun onMarkerClicked(marker: DynamicMapMarker) {}

    override fun onDestroyView() {
        binding.dynamicMapView.onDestroy()
        super.onDestroyView()
    }

    override fun onPause() {
        binding.dynamicMapView.onPause()
        super.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.dynamicMapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.dynamicMapView.onSaveInstanceState(outState)
    }
}