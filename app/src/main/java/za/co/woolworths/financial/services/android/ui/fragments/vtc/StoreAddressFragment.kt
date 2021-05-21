package za.co.woolworths.financial.services.android.ui.fragments.vtc

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_store_address.*
import kotlinx.android.synthetic.main.fragment_store_address.view.*
import kotlinx.android.synthetic.main.layout_address_residential_or_business.*
import kotlinx.android.synthetic.main.select_store_activity.*
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.LocationResponse
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.StoreCardEmailConfirmBody
import za.co.woolworths.financial.services.android.ui.activities.card.SelectStoreActivity
import za.co.woolworths.financial.services.android.ui.activities.vtc.StoreLocatorActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.location.Event
import za.co.woolworths.financial.services.android.util.location.EventType
import za.co.woolworths.financial.services.android.util.location.Locator
import za.co.woolworths.financial.services.android.util.location.Logger

class StoreAddressFragment : Fragment() {

    private lateinit var locator: Locator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_store_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        setupActionBar()

        initView()

        residentialTextView?.setOnClickListener {
            context?.let { context ->
                residentialTextView?.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.checked_item), null, null, null)
                businessTextView?.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.uncheck_item), null, null, null)
            }
        }

        businessTextView?.setOnClickListener {
            startLocationDiscoveryProcess()
        }

        tabTome?.setOnClickListener {
            tomeLayout?.visibility = View.VISIBLE
            nextButton?.visibility = View.VISIBLE
            businessLayout?.visibility = View.GONE
            context?.let {
                viewFlipperTab1?.background = ContextCompat.getDrawable(it, R.drawable.onde_dp_black_border_bg)
                viewFlipperTab2?.background = ContextCompat.getDrawable(it, R.drawable.border_quantity_dropdown)
                nextButton?.background = ContextCompat.getDrawable(it, R.drawable.next_button_inactive)
            }
        }

        tabToWooliesStore?.setOnClickListener {
            tomeLayout?.visibility = View.GONE
            nextButton?.visibility = View.VISIBLE
            businessLayout?.visibility = View.VISIBLE
            context?.let {
                viewFlipperTab1?.background = ContextCompat.getDrawable(it, R.drawable.border_quantity_dropdown)
                viewFlipperTab2?.background = ContextCompat.getDrawable(it, R.drawable.onde_dp_black_border_bg)
                nextButton?.background = ContextCompat.getDrawable(it, R.drawable.next_button_icon)
            }
        }

        nextButton?.setOnClickListener {
            when (tomeLayout?.visibility) {
                View.VISIBLE -> {

//                    val storeCardEmailConfirmBody = StoreCardEmailConfirmBody(visionAccountNumber = , storeName = storeDetails?.name, storeAddress = storeDetails?.address)

                    view?.findNavController()?.navigate(R.id.action_storeAddressFragment_to_storeConfirmationFragment, bundleOf(

                    ))
                }
                else -> startLocationDiscoveryProcess()
            }
        }
    }

    private fun initView() {

        locator = Locator(activity as AppCompatActivity)
        tomeLayout?.visibility = View.VISIBLE
        context?.let {
            viewFlipperTab1?.background = ContextCompat.getDrawable(it, R.drawable.onde_dp_black_border_bg)
            residentialTextView?.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(it, R.drawable.checked_item), null, null, null)
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
//            progressVisibility(true)
            locationRequestRequest.enqueue(CompletionHandler(object : IResponseListener<LocationResponse> {
                override fun onSuccess(locationResponse: LocationResponse?) {
                    if (!isAdded) return
//                    progressVisibility(false)
                    when (locationResponse?.httpCode) {
                        AppConstant.HTTP_OK -> {
                            val npcStores: List<StoreDetails>? = locationResponse.Locations?.filter { stores -> stores.npcAvailable }
                                    ?: mutableListOf()
                            if (npcStores?.size ?: 0 > 0) {

                                findNavController()?.navigate(R.id.action_storeAddressFragment_to_participatingStoreFragment, bundleOf(
                                        StoreLocatorActivity.PRODUCT_NAME to bindString(R.string.participating_stores),
                                        StoreLocatorActivity.CONTACT_INFO to bindString(R.string.participating_store_desc),
                                        StoreLocatorActivity.MAP_LOCATION to Gson().toJson(npcStores),
                                        StoreLocatorActivity.SHOW_GEOFENCING to false
                                ))
                            }
                        }
                        else -> return
                    }
                }

                override fun onFailure(error: Throwable?) {
//                    progressVisibility(false)
                }
            }, LocationResponse::class.java))
        }
    }
}