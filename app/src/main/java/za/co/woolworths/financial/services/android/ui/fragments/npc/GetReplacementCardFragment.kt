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
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.LocationResponse
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.StoreLocatorActivity
import za.co.woolworths.financial.services.android.ui.activities.StoreLocatorActivity.Companion.CONTACT_INFO
import za.co.woolworths.financial.services.android.ui.activities.StoreLocatorActivity.Companion.MAP_LOCATION
import za.co.woolworths.financial.services.android.ui.activities.StoreLocatorActivity.Companion.PRODUCT_NAME
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.views.actionsheet.EnableLocationSettingsFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.EnableLocationSettingsFragment.Companion.ACCESS_MY_LOCATION_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductListingFindInStoreNoQuantityFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.FuseLocationAPISingleton
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

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

        AnimationUtilExtension.animateViewPushDown(btnParticipatingStores)
        AnimationUtilExtension.animateViewPushDown(tvAlreadyHaveCard)

        val storeCardResponse = (activity as? MyCardDetailActivity)?.getStoreCardDetail()
        tvAlreadyHaveCard?.setOnClickListener {
            (activity as? MyCardDetailActivity)?.apply {
                navigateToLinkNewCardActivity(this, storeCardResponse)
            }
        }
        btnParticipatingStores?.setOnClickListener { checkForLocationPermission() }

        uniqueIdsForReplacementCard()
    }

    private fun uniqueIdsForReplacementCard() {
        imReplacementCard?.contentDescription = bindString(R.string.image_card)
        tvReplacementCardTitle?.contentDescription = bindString(R.string.label_getICR)
        tvPermanentBlockDescPart1?.contentDescription = bindString(R.string.label_getICRCardDescription)
        btnParticipatingStores?.contentDescription = bindString(R.string.button_getParticipantsStores)
        tvAlreadyHaveCard?.contentDescription = bindString(R.string.link_alreadyHaveCard)
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
        activity?.apply {
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_ICR_STORES, this)
            //Check if user has location services enabled. If not, notify user as per current store locator functionality.
            if (!Utils.isLocationEnabled(this)) {
                val enableLocationSettingsFragment: EnableLocationSettingsFragment? = EnableLocationSettingsFragment()
                enableLocationSettingsFragment?.show(supportFragmentManager, EnableLocationSettingsFragment::class.java.simpleName)
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
            locationRequestRequest.enqueue(CompletionHandler(object : IResponseListener<LocationResponse> {
                override fun onSuccess(locationResponse: LocationResponse?) {
                    if (!isAdded) return
                    activity?.apply {
                        progressVisibility(false)
                        when (locationResponse?.httpCode) {
                            AppConstant.HTTP_OK -> {
                                val npcStores: List<StoreDetails>? = locationResponse.Locations?.filter { stores -> stores.npcAvailable }
                                        ?: mutableListOf()
                                if (npcStores?.size ?: 0 > 0) {
                                    val intentInStoreFinder = Intent(this, StoreLocatorActivity::class.java)
                                    intentInStoreFinder.putExtra(PRODUCT_NAME, bindString(R.string.participating_stores))
                                    intentInStoreFinder.putExtra(CONTACT_INFO, bindString(R.string.participating_store_desc))
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
            } else {
               KotlinUtils.openApplicationSettings(ACCESS_MY_LOCATION_REQUEST_CODE, activity)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACCESS_MY_LOCATION_REQUEST_CODE) {
            activity?.runOnUiThread {
                checkForLocationPermission()
            }
        }
    }
}