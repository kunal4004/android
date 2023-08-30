package za.co.woolworths.financial.services.android.ui.fragments.vtc

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentStoreAddressBinding
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
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
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import za.co.woolworths.financial.services.android.util.location.Event
import za.co.woolworths.financial.services.android.util.location.EventType
import za.co.woolworths.financial.services.android.util.location.Locator
import za.co.woolworths.financial.services.android.util.location.Logger

class StoreAddressFragment : BaseFragmentBinding<FragmentStoreAddressBinding>(
    FragmentStoreAddressBinding::inflate
) {

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
        binding.tomeLayout.nextButton.isEnabled = false
        binding.tomeLayout.nextButton.isClickable = false
        context?.let {
            binding.tomeLayout.nextButton.background =
                ContextCompat.getDrawable(it, R.drawable.next_button_inactive)
        }
    }

    private fun enableNextButton() {
        binding.tomeLayout.nextButton.isEnabled = true
        binding.tomeLayout.nextButton.isClickable = true
        context?.let {
            binding.tomeLayout.nextButton.background =
                ContextCompat.getDrawable(it, R.drawable.next_button_icon)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar()

        initView()

        binding.tomeLayout.complexOrBuildingNameEdtTV.addTextChangedListener(watcher)
        binding.tomeLayout.businessNameEdtTV.addTextChangedListener(watcher)
        binding.tomeLayout.streetAddressEdtTV.addTextChangedListener(watcher)
        binding.tomeLayout.suburbEdtTV.addTextChangedListener(watcher)
        binding.tomeLayout.cityTownEdtTV.addTextChangedListener(watcher)
        binding.tomeLayout.provinceEdtTV.addTextChangedListener(watcher)
        binding.tomeLayout.postalCodeEdtTV.addTextChangedListener(watcher)

        binding.tomeLayout.residentialTextView.setOnClickListener {
            Utils.hideSoftKeyboard(activity)
            deliveryType = DELIVERY_TYPE_F2F
            addressType = ADDRESS_TYPE_RESIDENTIAL

            binding.tomeLayout.tvBusinessName.visibility = View.GONE
            binding.tomeLayout.businessNameEdtTV.visibility = View.GONE
            if (validateTextViews()) {
                enableNextButton()
            } else {
                disableNextButton()
            }
            context?.let { context ->
                binding.tomeLayout.residentialTextView.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(context, R.drawable.checked_item),
                    null,
                    null,
                    null
                )
                binding.tomeLayout.businessTextView.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(context, R.drawable.uncheck_item),
                    null,
                    null,
                    null
                )
            }

        }

        binding.tomeLayout.businessTextView.setOnClickListener {
            deliveryType = DELIVERY_TYPE_F2F
            addressType = ADDRESS_TYPE_BUSINESS

            Utils.hideSoftKeyboard(activity)
            binding.tomeLayout.tvBusinessName.visibility = View.VISIBLE
            binding.tomeLayout.businessNameEdtTV.visibility = View.VISIBLE
            if (validateTextViews()) {
                enableNextButton()
            } else {
                disableNextButton()
            }

            context?.let { context ->
                binding.tomeLayout.businessTextView.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(context, R.drawable.checked_item),
                    null,
                    null,
                    null
                )
                binding.tomeLayout.residentialTextView.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(context, R.drawable.uncheck_item),
                    null,
                    null,
                    null
                )
            }
        }

        binding.storeAddressConstraintLayout.setOnClickListener {
            Utils.hideSoftKeyboard(activity)
        }

        binding.tabTome.setOnClickListener {
            Utils.hideSoftKeyboard(activity)
            if (binding.processingView.visibility == View.VISIBLE) {
                return@setOnClickListener
            }

            binding.tomeLayout.root.visibility = View.VISIBLE
            binding.tomeLayout.nextButton.visibility = View.VISIBLE
            context?.let {
                binding.viewFlipperTab1.background =
                    ContextCompat.getDrawable(it, R.drawable.onde_dp_black_border_bg)
                binding.viewFlipperTab2.background =
                    ContextCompat.getDrawable(it, R.drawable.border_quantity_dropdown)
                binding.tomeLayout.nextButton.background =
                    ContextCompat.getDrawable(it, R.drawable.next_button_inactive)
            }
        }

        binding.tabToWooliesStore.setOnClickListener {
            deliveryType = DELIVERY_TYPE_STORE
            Utils.hideSoftKeyboard(activity)
            binding.tomeLayout.root.visibility = View.GONE
            binding.processingView.visibility = View.VISIBLE
            binding.tomeLayout.nextButton.visibility = View.GONE
            context?.let {
                binding.viewFlipperTab1.background =
                    ContextCompat.getDrawable(it, R.drawable.border_quantity_dropdown)
                binding.viewFlipperTab2.background =
                    ContextCompat.getDrawable(it, R.drawable.onde_dp_black_border_bg)
                binding.tomeLayout.nextButton.background =
                    ContextCompat.getDrawable(it, R.drawable.next_button_icon)
            }
            callLocationStores()
        }

        binding.tomeLayout.nextButton.setOnClickListener {
            when (binding.tomeLayout.root.visibility) {
                View.VISIBLE -> {

                    Utils.hideSoftKeyboard(activity)

                    if (!validateTextViews()) {
                        return@setOnClickListener
                    }

                    activity?.apply {
                        Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.MYACCOUNTS_SC_REPLACE_CARD_F2F,
                            this
                        )
                    }

                    var resp: StoreCardsResponse? = null
                    arguments?.apply {
                        val storeCardData = getString(STORE_CARD)
                        resp = Gson().fromJson(storeCardData, StoreCardsResponse::class.java)
                    }

                    val storeCardEmailConfirmBody = StoreCardEmailConfirmBody(
                        visionAccountNumber = resp?.storeCardsData?.visionAccountNumber,
                        deliveryMethod = deliveryType,
                        province = binding.tomeLayout.provinceEdtTV.text.toString(),
                        city = binding.tomeLayout.cityTownEdtTV.text.toString(),
                        suburb = binding.tomeLayout.suburbEdtTV.text.toString(),
                        street = binding.tomeLayout.streetAddressEdtTV.text.toString(),
                        complexName = binding.tomeLayout.complexOrBuildingNameEdtTV.text.toString(),
                        businessName = binding.tomeLayout.businessNameEdtTV.text.toString(),
                        postalCode = binding.tomeLayout.postalCodeEdtTV.text.toString()
                    )

                    view.findNavController().navigate(
                        R.id.action_storeAddressFragment_to_storeConfirmationFragment, bundleOf(
                            StoreConfirmationFragment.STORE_DETAILS to Gson().toJson(
                                storeCardEmailConfirmBody
                            )
                        )
                    )
                }
                else -> callLocationStores()
            }
        }
    }

    private fun callLocationStores() {
        context?.apply {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                navigateToParticipatingStores(null)
                return
            }
            startLocationDiscoveryProcess()
        }

    }

    private fun validateTextViews(): Boolean {
        return (!TextUtils.isEmpty(binding.tomeLayout.provinceEdtTV.text.toString())
                && !TextUtils.isEmpty(binding.tomeLayout.cityTownEdtTV.text.toString())
                && !TextUtils.isEmpty(binding.tomeLayout.suburbEdtTV.text.toString())
                && !TextUtils.isEmpty(binding.tomeLayout.streetAddressEdtTV.text.toString())
                && !TextUtils.isEmpty(binding.tomeLayout.postalCodeEdtTV.text.toString())
                // Check if address type is residential or not if not check for business edit text empty
                && (ADDRESS_TYPE_RESIDENTIAL.equals(addressType, ignoreCase = true)
                || (ADDRESS_TYPE_BUSINESS.equals(
            addressType,
            ignoreCase = true
        ) && !TextUtils.isEmpty(binding.tomeLayout.businessNameEdtTV.text.toString()))
                ))
    }

    private fun initView() {

        locator = Locator(activity as AppCompatActivity)
        binding.tomeLayout.root.visibility = View.VISIBLE
        context?.let {
            binding.viewFlipperTab1.background =
                ContextCompat.getDrawable(it, R.drawable.onde_dp_black_border_bg)

            if (ADDRESS_TYPE_RESIDENTIAL.equals(addressType, ignoreCase = true)) {
                binding.tomeLayout.residentialTextView.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(it, R.drawable.checked_item),
                    null,
                    null,
                    null
                )

                binding.tomeLayout.tvBusinessName.visibility = View.GONE
                binding.tomeLayout.businessNameEdtTV.visibility = View.GONE
            } else {
                binding.tomeLayout.businessTextView.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(it, R.drawable.checked_item),
                    null,
                    null,
                    null
                )
                binding.tomeLayout.tvBusinessName.visibility = View.VISIBLE
                binding.tomeLayout.businessNameEdtTV.visibility = View.VISIBLE
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
            this.binding.vtcReplacementToolbarTextView.text = ""
            val mActionBar = supportActionBar
            mActionBar?.setDisplayHomeAsUpEnabled(true)
            mActionBar?.setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (binding.processingView.visibility == View.VISIBLE) {
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
            EventType.LOCATION_SERVICE_DISCONNECTED -> {
                // do nothing
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
            val locationRequestRequest = OneAppService().getStoresForNPC(
                location?.latitude
                    ?: 0.0, location?.longitude ?: 0.0, "", null
            )
            OneAppService.forceNetworkUpdate = true
            binding.processingView.visibility = View.VISIBLE
            locationRequestRequest.enqueue(CompletionHandler(object :
                IResponseListener<LocationResponse> {
                override fun onSuccess(locationResponse: LocationResponse?) {
                    if (!isAdded) return
                    binding.processingView.visibility = View.GONE

                    when (locationResponse?.httpCode) {
                        AppConstant.HTTP_OK -> {
                            val npcStores: List<StoreDetails>? = locationResponse.Locations
                                ?: mutableListOf()
                            if (npcStores?.size ?: 0 > 0) {

                                view?.findNavController()?.navigate(
                                    R.id.action_storeAddressFragment_to_participatingStoreFragment,
                                    bundleOf(
                                        PRODUCT_NAME to bindString(R.string.participating_stores),
                                        MAP_LOCATION to npcStores,
                                        STORE_CARD to arguments?.getString(STORE_CARD),
                                        SHOW_GEOFENCING to false,
                                        SHOW_BACK_BUTTON to true
                                    )
                                )
                            }
                        }
                        else -> return
                    }
                }

                override fun onFailure(error: Throwable?) {
                    binding.processingView.visibility = View.GONE
                }
            }, LocationResponse::class.java))
        }
    }
}