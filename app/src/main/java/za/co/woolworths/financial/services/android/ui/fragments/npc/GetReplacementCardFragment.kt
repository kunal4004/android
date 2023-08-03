package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ReplaceCardFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.LocationResponse
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.card.InstantStoreCardReplacementActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.card.SelectStoreActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.npc.ParticipatingStoreFragment.Companion.CONTACT_INFO
import za.co.woolworths.financial.services.android.ui.fragments.npc.ParticipatingStoreFragment.Companion.GEOFENCE_ENABLED
import za.co.woolworths.financial.services.android.ui.fragments.npc.ParticipatingStoreFragment.Companion.MAP_LOCATION
import za.co.woolworths.financial.services.android.ui.fragments.npc.ParticipatingStoreFragment.Companion.PRODUCT_NAME
import za.co.woolworths.financial.services.android.ui.fragments.npc.ParticipatingStoreFragment.Companion.STORE_CARD
import za.co.woolworths.financial.services.android.ui.views.actionsheet.EnableLocationSettingsFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.EnableLocationSettingsFragment.Companion.ACCESS_MY_LOCATION_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductListingFindInStoreNoQuantityFragment.Companion.REQUEST_PERMISSION_LOCATION
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.location.Event
import za.co.woolworths.financial.services.android.util.location.EventType
import za.co.woolworths.financial.services.android.util.location.Locator
import za.co.woolworths.financial.services.android.util.location.Logger

class GetReplacementCardFragment : MyCardExtension(R.layout.replace_card_fragment) {

    private lateinit var binding: ReplaceCardFragmentBinding
    private lateinit var locator: Locator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ReplaceCardFragmentBinding.bind(view)
        activity?.let { Utils.updateStatusBarBackground(it) }

        binding.apply {
            setActionBar()
            tvAlreadyHaveCard.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            pbParticipatingStore.indeterminateDrawable?.setColorFilter(
                Color.WHITE,
                PorterDuff.Mode.MULTIPLY
            )
            locator = Locator(activity as AppCompatActivity)

            AnimationUtilExtension.animateViewPushDown(btnParticipatingStores)
            AnimationUtilExtension.animateViewPushDown(tvAlreadyHaveCard)

            val storeCardResponse =
                arguments?.getString(SelectStoreActivity.STORE_DETAILS) /*(activity as? MyCardDetailActivity)?.getStoreCardDetail()*/
            tvAlreadyHaveCard?.setOnClickListener {
                navigateToLinkNewCardActivity(activity, storeCardResponse)
            }
            btnParticipatingStores?.setOnClickListener {

                context?.let {
                    if (ContextCompat.checkSelfPermission(
                            it,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        navigateToParticipatingStores(null)
                        return@setOnClickListener
                    }
                }

                startLocationDiscoveryProcess()
            }

            uniqueIdsForReplacementCard()
        }
    }

    private fun ReplaceCardFragmentBinding.uniqueIdsForReplacementCard() {
        imReplacementCard?.contentDescription = bindString(R.string.image_card)
        tvReplacementCardTitle?.contentDescription = bindString(R.string.label_getICR)
        tvPermanentBlockDescPart1?.contentDescription = bindString(R.string.label_getICRCardDescription)
        btnParticipatingStores?.contentDescription = bindString(R.string.button_getParticipantsStores)
        tvAlreadyHaveCard?.contentDescription = bindString(R.string.link_alreadyHaveCard)
    }

    private fun setActionBar() {
        (activity as? SelectStoreActivity)?.apply {
            (activity as? SelectStoreActivity)?.binding?.vtcReplacementToolbarTextView?.text = ""
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowTitleEnabled(false)
                setDisplayUseLogoEnabled(false)
                setHomeAsUpIndicator(R.drawable.back24)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onBackPressed() {
        activity?.apply {
            finish()
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
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
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION)
        }
    }

    fun navigateToLinkNewCardActivity(activity: Activity?, storeCard: String?) {
        activity?.apply {
            val openLinkNewCardActivity = Intent(this, InstantStoreCardReplacementActivity::class.java)
            openLinkNewCardActivity.putExtra(MyCardDetailActivity.STORE_CARD_DETAIL, storeCard)
            startActivityForResult(openLinkNewCardActivity, MyCardExtension.INSTANT_STORE_CARD_REPLACEMENT_REQUEST_CODE)
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    private fun navigateToParticipatingStores(location: Location?) {
        activity?.runOnUiThread {
            enableAlreadyHaveALink(false)
            val locationRequestRequest = OneAppService().getStoresForNPC(location?.latitude
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

                                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_ICR_STORES, this)

                                    val storeCardResponse = arguments?.getString(SelectStoreActivity.STORE_DETAILS)
                                    view?.findNavController()?.navigate(R.id.action_getReplacementCardFragment_to_participatingStoreFragment, bundleOf(
                                            PRODUCT_NAME to bindString(R.string.participating_stores),
                                            CONTACT_INFO to bindString(R.string.participating_store_desc),
                                            MAP_LOCATION to npcStores,
                                            STORE_CARD to storeCardResponse,
                                            GEOFENCE_ENABLED to locationResponse.inGeofence
                                    ))
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
        binding.tvAlreadyHaveCard?.isEnabled = enableLink
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }

    private fun startLocationDiscoveryProcess() {
        locator.getCurrentLocation { locationEvent ->
            when (locationEvent) {
                is Event.Location -> handleLocationEvent(locationEvent)
                is Event.Permission -> handlePermissionEvent(locationEvent)
            }
        }
    }

    private fun handleLocationEvent(locationEvent: Event.Location?) {
        locationEvent?.locationData?.apply {
            Utils.saveLastLocation(this, context)
            navigateToParticipatingStores(this)
        }
    }


    private fun handlePermissionEvent(permissionEvent: Event.Permission) {
        when (permissionEvent.event) {
            EventType.LOCATION_PERMISSION_GRANTED -> {
                Logger.logDebug("Permission granted")
            }
            EventType.LOCATION_PERMISSION_NOT_GRANTED -> {
                Logger.logDebug("Permission NOT granted")
            }
            EventType.LOCATION_DISABLED_ON_DEVICE -> {
                Logger.logDebug("Permission NOT granted permanently")
            }
            EventType.LOCATION_SERVICE_DISCONNECTED -> {
                // do nothing
            }
        }
    }

    private fun progressVisibility(state: Boolean) = activity?.runOnUiThread {
        binding.apply {
            pbParticipatingStore?.visibility = if (state) VISIBLE else GONE
            btnParticipatingStores?.setTextColor(if (state) Color.BLACK else Color.WHITE)
            btnParticipatingStores?.isClickable = !state
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACCESS_MY_LOCATION_REQUEST_CODE) {
            activity?.runOnUiThread {
                checkForLocationPermission()
            }
        } else if ((requestCode == MyCardExtension.INSTANT_STORE_CARD_REPLACEMENT_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) ||
                (requestCode == MyCardExtension.INSTANT_STORE_CARD_REPLACEMENT_REQUEST_CODE && resultCode == ProcessBlockCardFragment.RESULT_CODE_BLOCK_CODE_SUCCESS)) { // close previous cart detail
            activity?.apply {
                setResult(MyCardDetailActivity.TEMPORARY_FREEZE_STORE_CARD_RESULT_CODE)
                finish() // will close previous activity in stack
            }
        }
    }
}