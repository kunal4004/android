package za.co.woolworths.financial.services.android.ui.fragments.vtc

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_store_address.*
import kotlinx.android.synthetic.main.layout_address_residential_or_business.*
import kotlinx.android.synthetic.main.layout_link_device_validate_otp.*
import kotlinx.android.synthetic.main.select_store_activity.*
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.UserManager
import za.co.woolworths.financial.services.android.models.dto.LocationResponse
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.StoreCardEmailConfirmBody
import za.co.woolworths.financial.services.android.ui.activities.card.SelectStoreActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.npc.ParticipatingStoreFragment.Companion.MAP_LOCATION
import za.co.woolworths.financial.services.android.ui.fragments.npc.ParticipatingStoreFragment.Companion.PRODUCT_NAME
import za.co.woolworths.financial.services.android.ui.fragments.npc.ParticipatingStoreFragment.Companion.SHOW_BACK_BUTTON
import za.co.woolworths.financial.services.android.ui.fragments.npc.ParticipatingStoreFragment.Companion.SHOW_GEOFENCING
import za.co.woolworths.financial.services.android.ui.fragments.npc.ParticipatingStoreFragment.Companion.STORE_CARD
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.location.Event
import za.co.woolworths.financial.services.android.util.location.EventType
import za.co.woolworths.financial.services.android.util.location.Locator
import za.co.woolworths.financial.services.android.util.location.Logger

class StoreAddressFragment : Fragment() {

    companion object {
        const val DELIVERY_TYPE_F2F: String = "f2f"
        const val DELIVERY_TYPE_STORE: String = "store"
        const val ADDRESS_TYPE_RESIDENTIAL: String = "RESIDENTIAL"
        const val ADDRESS_TYPE_BUSINESS: String = "BUSINESS"
    }

    private lateinit var locator: Locator
    private var deliveryType: String = DELIVERY_TYPE_F2F
    private var addressType: String = ADDRESS_TYPE_RESIDENTIAL
    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            if (validateTextViews()) {
                enableNextButton()
            } else {
                disableNextButton()
            }
        }
    }

    private fun disableNextButton() {
        nextButton?.isEnabled = false
        nextButton?.isClickable = false
        context?.let { nextButton?.background = ContextCompat.getDrawable(it, R.drawable.next_button_inactive) }
    }

    private fun enableNextButton() {
        nextButton?.isEnabled = true
        nextButton?.isClickable = true
        context?.let { nextButton?.background = ContextCompat.getDrawable(it, R.drawable.next_button_icon) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_store_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar()

        initView()

        complexOrBuildingNameEdtTV?.addTextChangedListener(watcher)
        businessNameEdtTV?.addTextChangedListener(watcher)
        streetAddressEdtTV?.addTextChangedListener(watcher)
        suburbEdtTV?.addTextChangedListener(watcher)
        cityTownEdtTV?.addTextChangedListener(watcher)
        provinceEdtTV?.addTextChangedListener(watcher)
        postalCodeEdtTV?.addTextChangedListener(watcher)

        residentialTextView?.setOnClickListener {
            Utils.hideSoftKeyboard(activity)
            deliveryType = DELIVERY_TYPE_F2F
            addressType = ADDRESS_TYPE_RESIDENTIAL

            tvBusinessName?.visibility = View.GONE
            businessNameEdtTV?.visibility = View.GONE
            if (validateTextViews()) {
                enableNextButton()
            } else {
                disableNextButton()
            }
            context?.let { context ->
                residentialTextView?.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.checked_item), null, null, null)
                businessTextView?.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.uncheck_item), null, null, null)
            }

        }

        businessTextView?.setOnClickListener {
            deliveryType = DELIVERY_TYPE_F2F
            addressType = ADDRESS_TYPE_BUSINESS

            Utils.hideSoftKeyboard(activity)
            tvBusinessName?.visibility = View.VISIBLE
            businessNameEdtTV?.visibility = View.VISIBLE
            if (validateTextViews()) {
                enableNextButton()
            } else {
                disableNextButton()
            }

            context?.let { context ->
                businessTextView?.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.checked_item), null, null, null)
                residentialTextView?.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.uncheck_item), null, null, null)
            }
        }

        storeAddressConstraintLayout?.setOnClickListener {
            Utils.hideSoftKeyboard(activity)
        }

        tabTome?.setOnClickListener {
            Utils.hideSoftKeyboard(activity)
            if (processingView?.visibility == View.VISIBLE) {
                return@setOnClickListener
            }

            tomeLayout?.visibility = View.VISIBLE
            nextButton?.visibility = View.VISIBLE
            context?.let {
                viewFlipperTab1?.background = ContextCompat.getDrawable(it, R.drawable.onde_dp_black_border_bg)
                viewFlipperTab2?.background = ContextCompat.getDrawable(it, R.drawable.border_quantity_dropdown)
                nextButton?.background = ContextCompat.getDrawable(it, R.drawable.next_button_inactive)
            }
        }

        tabToWooliesStore?.setOnClickListener {
            deliveryType = DELIVERY_TYPE_STORE
            Utils.hideSoftKeyboard(activity)
            tomeLayout?.visibility = View.GONE
            processingView?.visibility = View.VISIBLE
            nextButton?.visibility = View.GONE
            context?.let {
                viewFlipperTab1?.background = ContextCompat.getDrawable(it, R.drawable.border_quantity_dropdown)
                viewFlipperTab2?.background = ContextCompat.getDrawable(it, R.drawable.onde_dp_black_border_bg)
                nextButton?.background = ContextCompat.getDrawable(it, R.drawable.next_button_icon)
            }
            callLocationStores()
            startLocationDiscoveryProcess()
        }

        nextButton?.setOnClickListener {
            when (tomeLayout?.visibility) {
                View.VISIBLE -> {

                    Utils.hideSoftKeyboard(activity)

                    if (!validateTextViews()) {
                        return@setOnClickListener
                    }

                    var resp: StoreCardsResponse? = null
                    arguments?.apply {
                        val storeCardData = getString(STORE_CARD)
                        resp = Gson().fromJson(storeCardData, StoreCardsResponse::class.java)
                    }

                    val storeCardEmailConfirmBody = StoreCardEmailConfirmBody(visionAccountNumber = resp?.storeCardsData?.visionAccountNumber,
                            deliveryMethod = deliveryType,
                            province = provinceEdtTV?.text.toString(),
                            city = cityTownEdtTV?.text.toString(),
                            suburb = suburbEdtTV?.text.toString(),
                            street = streetAddressEdtTV?.text.toString(),
                            complexName = complexOrBuildingNameEdtTV?.text.toString(),
                            businessName = businessNameEdtTV?.text.toString(),
                            postalCode = postalCodeEdtTV?.text.toString())

                    view?.findNavController()?.navigate(R.id.action_storeAddressFragment_to_storeConfirmationFragment, bundleOf(
                            StoreConfirmationFragment.STORE_DETAILS to Gson().toJson(storeCardEmailConfirmBody)
                    ))
                }
                else -> callLocationStores()
            }
        }
    }

    private fun callLocationStores() {
        context?.apply {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                navigateToParticipatingStores(null)
                return
            }
            startLocationDiscoveryProcess()
        }

    }

    private fun validateTextViews(): Boolean {
        return (!TextUtils.isEmpty(provinceEdtTV?.text.toString())
                && !TextUtils.isEmpty(cityTownEdtTV?.text.toString())
                && !TextUtils.isEmpty(suburbEdtTV?.text.toString())
                && !TextUtils.isEmpty(streetAddressEdtTV?.text.toString())
                && !TextUtils.isEmpty(complexOrBuildingNameEdtTV?.text.toString())
                && !TextUtils.isEmpty(postalCodeEdtTV?.text.toString())
                // Check if address type is residential or not if not check for business edit text empty
                && (ADDRESS_TYPE_RESIDENTIAL.equals(addressType, ignoreCase = true)
                || (ADDRESS_TYPE_BUSINESS.equals(addressType, ignoreCase = true) && !TextUtils.isEmpty(businessNameEdtTV?.text.toString()))
                ))
    }

    private fun initView() {

        locator = Locator(activity as AppCompatActivity)
        tomeLayout?.visibility = View.VISIBLE
        context?.let {
            viewFlipperTab1?.background = ContextCompat.getDrawable(it, R.drawable.onde_dp_black_border_bg)

            if (ADDRESS_TYPE_RESIDENTIAL.equals(addressType, ignoreCase = true)) {
                residentialTextView?.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(it, R.drawable.checked_item), null, null, null)

                tvBusinessName?.visibility = View.GONE
                businessNameEdtTV?.visibility = View.GONE
            } else {
                businessTextView?.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(it, R.drawable.checked_item), null, null, null)
                tvBusinessName?.visibility = View.VISIBLE
                businessNameEdtTV?.visibility = View.VISIBLE
            }
        }

        if (validateTextViews()) {
            enableNextButton()
        } else {
            disableNextButton()
        }
    }

    private fun setupActionBar() {
        (activity as? SelectStoreActivity)?.apply {
            vtcReplacementToolbarTextView?.text = ""
            val mActionBar = supportActionBar
            mActionBar?.setDisplayHomeAsUpEnabled(true)
            mActionBar?.setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                if (processingView?.visibility == View.VISIBLE) {
                    return true
                }
                view?.findNavController()?.navigateUp()
            }
        }
        return super.onOptionsItemSelected(item)
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
        when (permissionEvent.event) {
            EventType.LOCATION_PERMISSION_GRANTED -> {
                Logger.logDebug("Permission granted")
            }
            EventType.LOCATION_PERMISSION_NOT_GRANTED -> {
                Logger.logDebug("Permission NOT granted")
                Utils.saveLastLocation(null, activity)
            }
            EventType.LOCATION_DISABLED_ON_DEVICE -> {
                Logger.logDebug("Permission NOT granted permanently")
            }
        }
    }

    private fun handleLocationEvent(locationEvent: Event.Location) {
        locationEvent.locationData?.apply {
            Utils.saveLastLocation(this, context)
            navigateToParticipatingStores(this)
        }
    }

    private fun navigateToParticipatingStores(location: Location?) {
        activity?.runOnUiThread {
            val locationRequestRequest = OneAppService.getStoresForNPC(location?.latitude
                    ?: 0.0, location?.longitude ?: 0.0, "", null)
            OneAppService.forceNetworkUpdate = true
            processingView?.visibility = View.VISIBLE
            locationRequestRequest.enqueue(CompletionHandler(object : IResponseListener<LocationResponse> {
                override fun onSuccess(locationResponse: LocationResponse?) {
                    if (!isAdded) return
                    processingView?.visibility = View.GONE

                    when (locationResponse?.httpCode) {
                        AppConstant.HTTP_OK -> {
                            val npcStores: List<StoreDetails>? = locationResponse.Locations
                                    ?: mutableListOf()
                            if (npcStores?.size ?: 0 > 0) {
                                val stores = Gson().toJson(npcStores)
                                view?.findNavController()?.navigate(R.id.action_storeAddressFragment_to_participatingStoreFragment, bundleOf(
                                        PRODUCT_NAME to bindString(R.string.participating_stores),
                                        MAP_LOCATION to stores,
                                        STORE_CARD to arguments?.getString(STORE_CARD),
                                        SHOW_GEOFENCING to false,
                                        SHOW_BACK_BUTTON to true
                                ))
                            }
                        }
                        else -> return
                    }
                }

                override fun onFailure(error: Throwable?) {
                    processingView?.visibility = View.GONE
                }
            }, LocationResponse::class.java))
        }
    }
}