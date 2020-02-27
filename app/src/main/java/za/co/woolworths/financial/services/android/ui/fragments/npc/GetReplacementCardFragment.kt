package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.NonNull
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.replace_card_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.ILocationProvider
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.LocationResponse
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.StoreLocatorActivity
import za.co.woolworths.financial.services.android.ui.activities.StoreLocatorActivity.Companion.CONTACT_INFO
import za.co.woolworths.financial.services.android.ui.activities.StoreLocatorActivity.Companion.MAP_LOCATION
import za.co.woolworths.financial.services.android.ui.activities.StoreLocatorActivity.Companion.PRODUCT_NAME
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductListingFindInStoreNoQuantityFragment
import za.co.woolworths.financial.services.android.util.FuseLocationAPISingleton
import za.co.woolworths.financial.services.android.util.Utils

class GetReplacementCardFragment : MyCardExtension() {

    companion object {
        fun newInstance() = GetReplacementCardFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.replace_card_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { Utils.updateStatusBarBackground(it) }
        updateToolbarBg()
        tvAlreadyHaveCard?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        pbParticipatingStore?.indeterminateDrawable?.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
        requestGPSLocation()
        val storeCardResponse = (activity as? MyCardDetailActivity)?.getStoreCardDetail()
        tvAlreadyHaveCard?.setOnClickListener {
            (activity as? MyCardDetailActivity)?.apply {
               navigateToLinkNewCardActivity(this,storeCardResponse)
            }
        }
        btnParticipatingStores?.setOnClickListener { checkForLocationPermission() }

        uniqueIdsForReplacementCard()
    }

    private fun uniqueIdsForReplacementCard() {
        activity?.resources?.apply {
            imReplacementCard?.contentDescription = getString(R.string.image_card)
            tvReplacementCardTitle?.contentDescription = getString(R.string.label_getICR)
            tvPermanentBlockDescPart1?.contentDescription = getString(R.string.label_getICRCardDescription)
            btnParticipatingStores?.contentDescription = getString(R.string.button_getParticipantsStores)
            tvAlreadyHaveCard?.contentDescription = getString(R.string.link_alreadyHaveCard)
        }
    }

    private fun requestGPSLocation() {
        FuseLocationAPISingleton.addLocationChangeListener(object : ILocationProvider {
            override fun onLocationChange(location: Location?) {
                activity?.let { activity -> Utils.saveLastLocation(location, activity) }
                FuseLocationAPISingleton.stopLocationUpdate()
                navigateToParticipatingStores(location)
            }

            override fun onPopUpLocationDialogMethod() {
            }
        })
    }

    private fun updateToolbarBg() {
        (activity as? MyCardDetailActivity)?.apply {
            hideToolbarTitle()
            changeToolbarBackground(R.color.white)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun checkForLocationPermission() {
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_ICR_STORES)
        activity?.apply {
            //Check if user has location services enabled. If not, notify user as per current store locator functionality.
            if (!Utils.isLocationEnabled(this)) {
                Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.LOCATION_OFF, "")
                return@apply
            }

            // If location services enabled, extract latitude and longitude request v4/user/locations
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ProductListingFindInStoreNoQuantityFragment.REQUEST_PERMISSION_LOCATION)
        }
    }

    private fun navigateToParticipatingStores(location: Location?) {
        activity?.runOnUiThread {
            enableAlreadyHaveALink(false)
            val locationRequestRequest = OneAppService.getStoresForNPC(location?.latitude
                    ?: 0.0, location?.longitude ?: 0.0, "", true)
            OneAppService.forceNetworkUpdate = true
            progressVisibility(true)
            locationRequestRequest.enqueue(CompletionHandler(object : RequestListener<LocationResponse> {
                override fun onSuccess(locationResponse: LocationResponse?) {
                    if (!isAdded) return
                    activity?.apply {
                        progressVisibility(false)
                        when (locationResponse?.httpCode) {
                                200 -> {
                                val npcStores: List<StoreDetails>? = locationResponse.Locations?.filter { stores -> stores.npcAvailable }
                                        ?: mutableListOf()
                                if (npcStores?.size ?: 0 > 0) {
                                    val intentInStoreFinder = Intent(this, StoreLocatorActivity::class.java)
                                    intentInStoreFinder.putExtra(PRODUCT_NAME, getString(R.string.participating_stores))
                                    intentInStoreFinder.putExtra(CONTACT_INFO, getString(R.string.participating_store_desc))
                                    intentInStoreFinder.putExtra(MAP_LOCATION, Gson().toJson(npcStores))
                                    startActivity(intentInStoreFinder)
                                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
                                }

                                }
                            else -> return
                        }
                        enableAlreadyHaveALink(true)
                    }
                }

                override fun onFailure(error: Throwable?) {
                    activity?.runOnUiThread {
                        enableAlreadyHaveALink(true)
                    }
                    progressVisibility(false)
                }

            }, LocationResponse::class.java))
        }
    }

    private fun enableAlreadyHaveALink(enableLink: Boolean) {
        tvAlreadyHaveCard?.isEnabled = enableLink
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        when (requestCode) {
            ProductListingFindInStoreNoQuantityFragment.REQUEST_PERMISSION_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdate()
            }
            else -> return
        }
    }

    private fun startLocationUpdate() = FuseLocationAPISingleton.startLocationUpdate()

    private fun progressVisibility(state: Boolean) = activity?.runOnUiThread {
        pbParticipatingStore?.visibility = if (state) VISIBLE else GONE
        btnParticipatingStores?.setTextColor(if (state) Color.BLACK else Color.WHITE)
        btnParticipatingStores?.isClickable = !state
    }
}