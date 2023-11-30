package za.co.woolworths.financial.services.android.checkout.view

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CheckoutAddAddressNewUserBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.checkout.service.network.AddAddressRequestBody
import za.co.woolworths.financial.services.android.checkout.service.network.AddAddressResponse
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.checkout.service.network.DeleteAddressResponse
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressNewUserFragment.ProvinceSuburbType.BOTH
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressNewUserFragment.ProvinceSuburbType.ONLY_PROVINCE
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressNewUserFragment.ProvinceSuburbType.ONLY_SUBURB
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.ADD_NEW_ADDRESS_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.DELETE_SAVED_ADDRESS_REQUEST_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.SAVED_ADDRESS_KEY
import za.co.woolworths.financial.services.android.checkout.view.ErrorHandlerBottomSheetDialog.Companion.ERROR_DESCRIPTION
import za.co.woolworths.financial.services.android.checkout.view.ErrorHandlerBottomSheetDialog.Companion.ERROR_TITLE
import za.co.woolworths.financial.services.android.checkout.view.ErrorHandlerBottomSheetDialog.Companion.ERROR_TYPE
import za.co.woolworths.financial.services.android.checkout.view.ErrorHandlerBottomSheetDialog.Companion.ERROR_TYPE_ADD_ADDRESS
import za.co.woolworths.financial.services.android.checkout.view.ErrorHandlerBottomSheetDialog.Companion.ERROR_TYPE_DELETE_ADDRESS
import za.co.woolworths.financial.services.android.checkout.view.ErrorHandlerBottomSheetDialog.Companion.RESULT_ERROR_CODE_RETRY
import za.co.woolworths.financial.services.android.checkout.view.SuburbNotDeliverableBottomsheetDialogFragment.Companion.ERROR_CODE
import za.co.woolworths.financial.services.android.checkout.view.SuburbNotDeliverableBottomsheetDialogFragment.Companion.ERROR_CODE_SUBURB_NOT_DELIVERABLE
import za.co.woolworths.financial.services.android.checkout.view.SuburbNotDeliverableBottomsheetDialogFragment.Companion.ERROR_CODE_SUBURB_NOT_FOUND
import za.co.woolworths.financial.services.android.checkout.view.SuburbNotDeliverableBottomsheetDialogFragment.Companion.RESULT_ERROR_CODE_SUBURB_NOT_FOUND
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutAddressConfirmationListAdapter.Companion.EDIT_ADDRESS_POSITION_KEY
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutAddressConfirmationListAdapter.Companion.EDIT_SAVED_ADDRESS_RESPONSE_KEY
import za.co.woolworths.financial.services.android.checkout.view.adapter.GooglePlacesAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.PlaceAutocomplete
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.ADMINISTRATIVE_AREA_LEVEL_1
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.LOCALITY
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.POSTAL_CODE
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.PREMISE
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.ROUTE
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.STREET_NUMBER
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.SUBLOCALITY_LEVEL_1
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.SUBLOCALITY_LEVEL_2
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.SelectedPlacesAddress
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.ADDRESS_APARTMENT
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.ADDRESS_COMPLEX_ESTATE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.ADDRESS_HOME
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.ADDRESS_OFFICE
import za.co.woolworths.financial.services.android.geolocation.GeoUtils.Companion.getSelectedDefaultName
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationParams
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidatePlace
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.UpdateScreenLiveData
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.shoptoggle.common.UnsellableAccess.Companion.resetUnsellableLiveData
import za.co.woolworths.financial.services.android.shoptoggle.common.UnsellableAccess.Companion.updateUnsellableLiveData
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.poi.PoiBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.views.UnsellableItemsBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELAY_100_MS
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELAY_500_MS
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.FIFTY
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK_201
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.TEN
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DELIVERY_TYPE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_CHECKOUT
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_SLOT_SELECTION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_PLACE_ID
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.SAVED_ADDRESS_RESPONSE
import za.co.woolworths.financial.services.android.util.Constant
import za.co.woolworths.financial.services.android.util.DeliveryType
import za.co.woolworths.financial.services.android.util.KeyboardUtils.Companion.hideKeyboardIfVisible
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.UnIndexedAddressIdentifiedListener
import za.co.woolworths.financial.services.android.util.UnsellableUtils
import za.co.woolworths.financial.services.android.util.UnsellableUtils.Companion.ADD_TO_LIST_SUCCESS_RESULT_CODE
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.isAValidSouthAfricanNumber
import za.co.woolworths.financial.services.android.util.location.DynamicGeocoder
import za.co.woolworths.financial.services.android.util.value
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import java.net.HttpURLConnection.HTTP_OK
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext

/**
 * Created by Kunal Uttarwar on 29/05/21.
 */
@AndroidEntryPoint
class CheckoutAddAddressNewUserFragment :
    CheckoutAddressManagementBaseFragment(R.layout.checkout_add_address_new_user),
    View.OnClickListener, CoroutineScope, ErrorHandlerBottomSheetDialog.ClickListener,
    PoiBottomSheetDialog.ClickListener,
    UnIndexedAddressIdentifiedListener {

    private var isLocationUpdateRequest: Boolean = false
    private var isComingFromNewToggleFulfilment: Boolean = false
    private lateinit var binding: CheckoutAddAddressNewUserBinding
    private var deliveringOptionsList: List<String>? = null
    private var navController: NavController? = null
    private lateinit var listOfInputFields: List<View>
    var deliveryType: DeliveryType = DeliveryType.DELIVERY
    private var selectedDeliveryAddressType: String? = null
    var selectedAddress = SelectedPlacesAddress()
    private var savedAddressResponse: SavedAddressResponse? = null
    private val checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel by activityViewModels()
    private var selectedAddressId = ""
    private var isAddNewAddress = false
    private var provinceSuburbEnableType: ProvinceSuburbType? = null
    private var bundle: Bundle? = null
    private var selectedAddressPosition: Int = -1
    private var isComingFromCheckout: Boolean = false
    private var isComingFromSlotSelection: Boolean = false
    private var isValidAddress: Boolean = false
    private var placeName: String? = null
    private var placeId: String = ""
    private var isPoiAddress: Boolean? = false
    private var address2: String? = ""
    private var oldNickName: String? = ""
    private var unIndexedAddressIdentified: Boolean = false
    private val unIndexedLiveData = MutableLiveData<Boolean>()
    private val confirmAddressViewModel: ConfirmAddressViewModel by activityViewModels()
    private var validateLocationResponse: ValidateLocationResponse? = null


    companion object {
        const val REGEX_NICK_NAME: String = "^$|^[a-zA-Z0-9\\s<!>@$&().+,-/\"']+$"
        const val ADDRESS_NICK_NAME_MAX_CHAR: Int = 40
    }

    enum class ProvinceSuburbType {
        ONLY_PROVINCE,
        ONLY_SUBURB,
        BOTH
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleBundleResponse()
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Main

    fun handleBundleResponse() {
        bundle = arguments?.getBundle(BUNDLE)
        bundle?.apply {
            isComingFromCheckout = getBoolean(IS_COMING_FROM_CHECKOUT, false)
            isComingFromSlotSelection = getBoolean(IS_COMING_FROM_SLOT_SELECTION, false)
            isComingFromNewToggleFulfilment = getBoolean(BundleKeysConstants.IS_COMING_FROM_NEW_TOGGLE_FULFILMENT_SCREEN, false)
            isLocationUpdateRequest = getBoolean(BundleKeysConstants.LOCATION_UPDATE_REQUEST, false)
            if (containsKey(EDIT_SAVED_ADDRESS_RESPONSE_KEY)) {
                //Edit new Address from delivery
                val editSavedAddress = getString(EDIT_SAVED_ADDRESS_RESPONSE_KEY)
                if (!editSavedAddress.isNullOrEmpty() && !editSavedAddress.equals("null", true)) {
                    savedAddressResponse = (Utils.jsonStringToObject(
                        editSavedAddress,
                        SavedAddressResponse::class.java
                    ) as? SavedAddressResponse)
                    baseFragBundle?.putString(SAVED_ADDRESS_KEY, Utils.toJson(savedAddressResponse))
                    selectedAddressPosition = getInt(EDIT_ADDRESS_POSITION_KEY, -1)
                    val savedAddress =
                        savedAddressResponse?.addresses?.get(getInt(EDIT_ADDRESS_POSITION_KEY))
                    selectedAddressId = savedAddress?.id.toString()
                    selectedDeliveryAddressType = savedAddress?.addressType
                    if (savedAddress != null) {
                        selectedAddress.savedAddress = savedAddress

                        if (!savedAddress.city.isNullOrEmpty()) {
                            selectedAddress?.provinceName = savedAddress.city!!
                        } else {
                            if (!savedAddress.region.isNullOrEmpty()) {
                                selectedAddress?.provinceName = savedAddress.region!!
                            }

                        }
                    }
                    setHasOptionsMenu(activity !is CheckoutActivity)
                }
            } else if (containsKey(ADD_NEW_ADDRESS_KEY)) {
                //Add new Address from delivery.
                isAddNewAddress = getBoolean(ADD_NEW_ADDRESS_KEY)
                savedAddressResponse = Utils.jsonStringToObject(
                    getString(SAVED_ADDRESS_KEY),
                    SavedAddressResponse::class.java
                ) as? SavedAddressResponse
                    ?: getSerializable(SAVED_ADDRESS_KEY) as? SavedAddressResponse
                setHasOptionsMenu(true)
            } else if (containsKey(SAVED_ADDRESS_KEY)) {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.CHANGE_FULFILLMENT_ADD_NEW_ADDRESS,
                    hashMapOf(
                        FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_ADD_NEW_ADDRESS
                    ),
                    activity
                )
                savedAddressResponse = Utils.jsonStringToObject(
                    getString(SAVED_ADDRESS_KEY),
                    SavedAddressResponse::class.java
                ) as? SavedAddressResponse
                    ?: getSerializable(SAVED_ADDRESS_KEY) as? SavedAddressResponse
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_item, menu)
        if (selectedAddressId.isNotEmpty() || isAddNewAddress) //show only if it is edit address screen
            return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CheckoutAddAddressNewUserBinding.bind(view)
        if (navController == null)
            navController = Navigation.findNavController(view)
        initView()
        listOfInputFields = listOf(
            binding.autoCompleteTextView,
            binding.recipientAddressLayout.addressNicknameEditText,
            binding.recipientAddressLayout.unitComplexFloorEditText,
            binding.recipientDetailsLayout.recipientNameEditText,
            binding.recipientDetailsLayout.cellphoneNumberEditText
        )
        init()
        addFragmentResultListener()
        // Show prepopulate fields on edit address
        if (selectedAddressId.isNotEmpty() || isAddNewAddress) {
            //selectedAddressId is not empty means it's a edit address call.
            if (selectedAddressId.isNotEmpty())
                setTextFields()
            if (activity is CheckoutActivity) {
                (activity as CheckoutActivity).hideBackArrow()
                if (!navController?.popBackStack()!!) {
                    binding.saveAddress.text = getString(R.string.save_address)
                }
            }
        }
        addUnIndexedIdentifiedListener()
    }


    private fun setTextFields() {
        oldNickName = selectedAddress?.savedAddress?.nickname
        binding.apply {
            enableDisableUserInputEditText(
                recipientAddressLayout.addressNicknameEditText,
                true,
                recipientAddressLayout.addressNicknameErrorMsg.isVisible
            )
            enableDisableUserInputEditText(
                recipientAddressLayout.unitComplexFloorEditText,
                isEnable = true,
                isErrorScreen = false
            )
            if (selectedAddress.savedAddress.placesId.isNullOrEmpty())
                autoCompleteTextView.text.clear() // This condition will only occur when address is added from web and is now opted for edit from app.
            else
                autoCompleteTextView?.setText(selectedAddress.savedAddress.address1)
            recipientAddressLayout.addressNicknameEditText.setText(selectedAddress.savedAddress.nickname)
            recipientAddressLayout.unitComplexFloorEditText.setText(selectedAddress.savedAddress.address2)
            suburbEditText.setText(selectedAddress.savedAddress.suburb)
            suburbEditText.isEnabled = false
            provinceAutocompleteEditText.setText(selectedAddress.provinceName)
            provinceAutocompleteEditText.isEnabled = false
            recipientDetailsLayout.cellphoneNumberEditText.setText(selectedAddress.savedAddress.primaryContactNo)
            recipientDetailsLayout.recipientNameEditText.setText(selectedAddress.savedAddress.recipientName)
            if (selectedAddress.savedAddress.postalCode.isNullOrEmpty()) {
                postalCode.text.clear()
            } else
                postalCode.setText(selectedAddress.savedAddress.postalCode)
            postalCode.isEnabled = false
            selectedDeliveryAddressType = selectedAddress.savedAddress.addressType
            isValidAddress = true
        }
    }

    private fun initView() {
        if (selectedAddressId.isNotEmpty()) {
            //it's not empty means it's a edit address call.
            var bundle = arguments?.getBundle(BUNDLE)
            if (savedAddressResponse?.defaultAddressNickname == bundle?.getInt(
                    EDIT_ADDRESS_POSITION_KEY
                )?.let {
                    savedAddressResponse?.addresses?.get(it)?.nickname
                }
            ) {

            } else if (savedAddressResponse?.addresses?.size!! > 1
                && (!getSelectedDefaultName(savedAddressResponse, selectedAddressPosition))
            ) {
                binding.deleteTextView?.visibility = View.VISIBLE
                binding.deleteTextView?.setOnClickListener(this)
            } else if (getSelectedDefaultName(savedAddressResponse, selectedAddressPosition)) {
                binding.deleteTextView?.visibility = View.GONE
            }
            binding.saveAddress?.text = bindString(R.string.change_details)
        }
        binding.saveAddress?.setOnClickListener(this)
        binding.backButton?.setOnClickListener(this)
        binding.autoCompleteTextView?.apply {
            afterTextChanged {
                if (it.isNotEmpty())
                    showErrorInputField(this, View.GONE)
            }
        }
        binding.recipientAddressLayout.addressNicknameEditText?.apply {
            afterTextChanged {
                val addressNickNameLength = it.length
                if (addressNickNameLength > 0 && !Pattern.matches(REGEX_NICK_NAME, it)) {
                    text?.delete(addressNickNameLength - 1, addressNickNameLength)
                }
                selectedAddress.savedAddress.nickname = it
                if (addressNickNameLength >= ADDRESS_NICK_NAME_MAX_CHAR) {
                    binding.recipientAddressLayout.addressNicknameErrorMsg?.visibility =
                        View.VISIBLE
                    binding.recipientAddressLayout.addressNicknameErrorMsg?.text =
                        getString(R.string.max_characters_allowed)
                } else if (it.isNotEmpty()) {
                    binding.recipientAddressLayout.addressNicknameErrorMsg?.text =
                        getString(R.string.address_nickname_error_msg)
                    showErrorInputField(this, View.GONE)
                }
            }
        }


        binding.recipientAddressLayout.unitComplexFloorEditText.apply {
            afterTextChanged {
                selectedAddress.savedAddress.address2 = it
                if (it.isNotEmpty())
                    showErrorInputField(this, View.GONE)
            }
        }
        binding.recipientDetailsLayout.recipientNameEditText?.apply {
            afterTextChanged {
                if (it.isNotEmpty())
                    showErrorInputField(this, View.GONE)
            }
        }
        binding.recipientDetailsLayout.cellphoneNumberEditText?.apply {
            afterTextChanged {
                if (it.isNotEmpty())
                    showErrorInputField(this, View.GONE)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.lifecycleScope?.launchWhenCreated {
            if (activity is CheckoutActivity) {
                (activity as? CheckoutActivity)?.apply {
                    showBackArrowWithoutTitle()
                }
            }
        }
    }



    private fun init() {
        deliveringOptionsList = AppConfigSingleton.nativeCheckout?.addressTypes
        showWhereAreWeDeliveringView()
        activity?.applicationContext?.let { context ->
            Places.initialize(context, getString(R.string.maps_google_api_key))
            val placesClient = Places.createClient(context)
            val placesAdapter =
                GooglePlacesAdapter(
                    requireActivity(),
                    placesClient,
                    this@CheckoutAddAddressNewUserFragment
                )
            binding.autoCompleteTextView?.apply {
                setAdapter(placesAdapter)

            }
            binding.autoCompleteTextView?.onItemClickListener =
                AdapterView.OnItemClickListener { parent, _, position, _ ->
                    val item = parent.getItemAtPosition(position) as? PlaceAutocomplete
                    placeId = item?.placeId.toString()
                    placeName = item?.primaryText.toString()
                    hideOrShowUnIndexedAddressErrorMessages(false)
                    val placeFields: MutableList<Place.Field> = mutableListOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.LAT_LNG,
                        Place.Field.ADDRESS,
                        Place.Field.ADDRESS_COMPONENTS,
                        Place.Field.TYPES
                    )
                    val request =
                        placeFields.let {
                            FetchPlaceRequest.builder(placeId, it).setSessionToken(item?.token)
                                .build()
                        }
                    request.let { placeRequest ->
                        placesClient.fetchPlace(placeRequest)
                            .addOnSuccessListener { response ->
                                val place = response.place
                                selectedAddress = SelectedPlacesAddress()
                                setAddress(place)
                            }.addOnFailureListener { exception ->
                                if (exception is ApiException) {
                                    Toast.makeText(
                                        requireContext(),
                                        exception.message + "",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }

                    }
                }
        }
    }

    private fun addFragmentResultListener() {
        setFragmentResultListener(RESULT_ERROR_CODE_SUBURB_NOT_FOUND) { _, _ ->
            if (selectedAddress.provinceName.isNullOrEmpty()) return@setFragmentResultListener
            provinceSuburbEnableType = ONLY_SUBURB
        }
        setFragmentResultListener(RESULT_ERROR_CODE_RETRY) { _, bundle ->
            when (bundle.getInt(BUNDLE)) {
                ERROR_TYPE_ADD_ADDRESS -> {
                    onSaveAddressClicked()
                }

                ERROR_TYPE_DELETE_ADDRESS -> {
                    deleteAddress()
                }
            }
        }
        setFragmentResultListener(ADD_TO_LIST_SUCCESS_RESULT_CODE) { _, _ ->
            relaunchCheckoutActivity()
        }

        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_DISMISS_RESULT) { requestKey, bundle ->
            val resultCode =
                bundle.getString(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT)
            if (resultCode == ADD_TO_LIST_SUCCESS_RESULT_CODE) {
                relaunchCheckoutActivity()
            }
        }

        UpdateScreenLiveData.observe(viewLifecycleOwner) {
            if (it == updateUnsellableLiveData) {
                UpdateScreenLiveData.value = resetUnsellableLiveData
                relaunchCheckoutActivity()
            }
        }
    }

    private fun relaunchCheckoutActivity() {
        savedAddressResponse?.defaultAddressNickname = validateLocationResponse?.validatePlace?.placeDetails?.nickname
        val checkoutActivityIntent =
            Intent(requireActivity(),
                CheckoutActivity::class.java
            ).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(
                    SAVED_ADDRESS_KEY,
                    savedAddressResponse
                )
                val result = when (Delivery.getType(KotlinUtils.getDeliveryType()?.deliveryType)) {
                    Delivery.STANDARD -> GEO_SLOT_SELECTION
                    else -> DASH_SLOT_SELECTION
                }
                putExtra(result, true)
                putExtra(Constant.LIQUOR_ORDER, getLiquorOrder())
                putExtra(
                    Constant.NO_LIQUOR_IMAGE_URL,
                    getLiquorImageUrl()
                )
            }
        requireActivity().apply {
            startActivityForResult(
                checkoutActivityIntent,
                BundleKeysConstants.FULLFILLMENT_REQUEST_CODE
            )
            overridePendingTransition(
                R.anim.slide_from_right,
                R.anim.slide_out_to_left
            )
            finish()
        }
    }

    private fun resetSuburbSelection() {
        selectedAddress.savedAddress.apply {
            suburb = ""
            suburbId = ""
        }
        selectedAddress.store = ""
        selectedAddress.storeId = ""
        binding.suburbEditText?.text?.clear()
    }


    private fun setAddress(place: Place) {
        enableDisableUserInputEditText(
            binding.recipientAddressLayout.addressNicknameEditText,
            true,
            binding.recipientAddressLayout.addressNicknameErrorMsg.isVisible
        )
        enableDisableUserInputEditText(
            binding.recipientAddressLayout.unitComplexFloorEditText,
            isEnable = true,
            isErrorScreen = false
        )
        provinceSuburbEnableType = null
        var streetNumber = ""
        var routeName = ""
        isPoiAddress = false
        for (address in place.addressComponents?.asList()!!) {
            when (address.types[0]) {
                STREET_NUMBER.value -> address?.name?.let { streetNumber = it }
                ROUTE.value -> address?.name?.let { routeName = it }
                ADMINISTRATIVE_AREA_LEVEL_1.value -> {
                    selectedAddress.provinceName = address.name
                }

                POSTAL_CODE.value -> selectedAddress.savedAddress.postalCode = address.name
                SUBLOCALITY_LEVEL_1.value -> {
                    if (address.name.isNotEmpty())
                        selectedAddress.savedAddress.suburb = address.name
                }

                SUBLOCALITY_LEVEL_2.value -> {
                    if (selectedAddress.savedAddress.suburb.isNullOrEmpty())
                        selectedAddress.savedAddress.suburb = address.name
                }

                LOCALITY.value -> selectedAddress.provinceName = address.name

                PREMISE.value -> {
                    if (routeName.isNullOrEmpty()) address?.name?.let { routeName = it }
                }
            }
        }

        var type: String? = ""
        address2 = ""
        val placeTypes: MutableList<Place.Type>? = place.types
        if (!placeTypes.isNullOrEmpty()) {
            for (placeType in placeTypes) {
                if (placeType == Place.Type.POINT_OF_INTEREST) {
                    isPoiAddress = true

                }
            }
        }

        if (isPoiAddress == true && streetNumber.isEmpty() && routeName.isEmpty()) {
            type = Constant.POI
        } else {
            isPoiAddress = false
        }
        if (isPoiAddress == true && type == Constant.POI) {
            isValidAddress = true
            enablePOIAddressTextFields()
        } else if (streetNumber.isEmpty() && routeName.isEmpty() && isPoiAddress == false) {
            isValidAddress = false
            launch(Main) {
                binding.autocompletePlaceErrorMsg?.text =
                    getString(R.string.geo_loc_error_msg_on_edit_address)
                binding.autocompletePlaceErrorMsg?.visibility = View.VISIBLE
            }
        } else {
            isValidAddress = true
        }
        if (!selectedAddress.provinceName.isNullOrEmpty() && !selectedAddress.savedAddress.suburb.isNullOrEmpty())
            selectedAddress.savedAddress.region = ""

        selectedAddress.savedAddress.apply {
            val tempAddress1 =
                if (streetNumber.isEmpty())
                    routeName
                else
                    streetNumber.plus(" ").plus(routeName)

            placeName?.let {

                address1 = if (it.isNotEmpty() && !it.equals("$streetNumber $routeName", true)) {
                    it
                } else {
                    tempAddress1
                }

            } ?: run {
                val googlePlacesName = place.name
                address1 =
                    if (googlePlacesName.isNullOrEmpty())
                        tempAddress1
                    else if (googlePlacesName.length > FIFTY) tempAddress1
                    else googlePlacesName
            }
            latitude = place.latLng?.latitude
            longitude = place.latLng?.longitude
            placesId = placeId
            unIndexedLiveData.value = true

        }

        val setTextAndCheckIfSelectedProvinceExist = {
            binding.autoCompleteTextView.apply {
                setText(selectedAddress.savedAddress.address1)

                if (selectedAddress.savedAddress.address1.isNullOrEmpty())
                    showErrorDialog()
                setSelection(binding.autoCompleteTextView.length())
                binding.autoCompleteTextView.dismissDropDown()
            }
            checkIfSelectedProvinceExist()

        }

        if (!selectedAddress.savedAddress.suburb.isNullOrEmpty())
            selectedAddress.savedAddress.suburbId = ""
        if (selectedAddress.savedAddress.postalCode.isNullOrEmpty()) {
            //If Google places failed to give postal code.
            DynamicGeocoder.getAddressFromLocation(
                context,
                selectedAddress.savedAddress.latitude,
                selectedAddress.savedAddress.longitude
            ) { address ->
                address?.postcode?.let {
                    selectedAddress.savedAddress.postalCode = it
                }
                setTextAndCheckIfSelectedProvinceExist.invoke()
            }
        } else {
            setTextAndCheckIfSelectedProvinceExist.invoke()
        }


    }

    fun checkIfSelectedProvinceExist() {
        val provinceName = selectedAddress.provinceName
        if (!provinceName.isNullOrEmpty()) {
            binding.provinceAutocompleteEditText?.setText(provinceName)
        } else {
            binding.provinceAutocompleteEditText.setText("")
            provinceSuburbEnableType = ONLY_PROVINCE
        }
        if (selectedAddress.savedAddress.suburb.isNullOrEmpty()) {
            resetSuburbSelection()
            provinceSuburbEnableType =
                if (selectedAddress.provinceName.isNullOrEmpty()) BOTH else ONLY_SUBURB
        } else {
            binding.suburbEditText?.setText(selectedAddress.savedAddress.suburb)
        }

        if (!selectedAddress.savedAddress.postalCode.isNullOrEmpty()) {
            binding.postalCode.setText(selectedAddress.savedAddress.postalCode)
        }
    }

    private fun enableDisableUserInputEditText(
        userInputField: EditText?,
        isEnable: Boolean,
        isErrorScreen: Boolean,
    ) {
        userInputField?.setBackgroundResource(if (isErrorScreen) R.drawable.input_error_background else R.drawable.recipient_details_input_edittext_bg)
        userInputField?.isClickable = isEnable
        userInputField?.isEnabled = isEnable
    }

    private fun showWhereAreWeDeliveringView() {
        if (!deliveringOptionsList.isNullOrEmpty()) {
            for ((index, options) in deliveringOptionsList!!.withIndex()) {
                val view = View.inflate(context, R.layout.where_are_we_delivering_items, null)
                val titleTextView: TextView? = view?.findViewById(R.id.titleTv)
                titleTextView?.tag = index
                titleTextView?.text = options
                if (!selectedDeliveryAddressType.isNullOrEmpty() && selectedDeliveryAddressType.equals(
                        options
                    )
                ) {
                    selectedAddress.savedAddress.addressType = selectedDeliveryAddressType
                    titleTextView?.background =
                        bindDrawable(R.drawable.checkout_delivering_title_round_button_pressed)
                    titleTextView?.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    // This to call the analytics when default address type selected first time
                    FirebaseAnalyticsEventHelper.setFirebaseEventForm(titleTextView?.text.toString(),
                      FirebaseManagerAnalyticsProperties.FORM_START, isComingFromCheckout)
                    binding.recipientAddressLayout.deliveringAddressTypesErrorMsg?.visibility =
                        View.GONE
                    changeUnitComplexPlaceHolderOnType(selectedDeliveryAddressType)
                    if (selectedDeliveryAddressType == ADDRESS_APARTMENT) {
                        binding.recipientAddressLayout.scrollView?.postDelayed(
                            {
                                binding.recipientAddressLayout.scrollView.fullScroll(
                                    HorizontalScrollView.FOCUS_RIGHT
                                )
                            },
                            DELAY_100_MS
                        )
                    }
                }
                titleTextView?.setOnClickListener {
                    setFirebaseEvents(titleTextView?.text.toString())
                    FirebaseAnalyticsEventHelper.setFirebaseEventForm(titleTextView?.text.toString(),
                         FirebaseManagerAnalyticsProperties.FORM_START, isComingFromCheckout)
                    resetOtherDeliveringTitle(it.tag as Int)
                    selectedDeliveryAddressType = (it as TextView).text as? String
                    selectedAddress.savedAddress.addressType = selectedDeliveryAddressType
                    binding.recipientAddressLayout.deliveringAddressTypesErrorMsg?.visibility =
                        View.GONE
                    // change background of selected textView
                    it.background =
                        bindDrawable(R.drawable.checkout_delivering_title_round_button_pressed)
                    it.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )

                    //if addressType is poi and Changed From Complex/Estate to other type then need to clear all fields
                    if (isPoiAddress == true && !selectedDeliveryAddressType?.equals(Constant.COMPLEX_ESTATE)!!) {
                        binding.apply {
                            autoCompleteTextView?.setText("")
                            suburbEditText?.setText("")
                            provinceAutocompleteEditText?.setText("")
                            postalCode?.setText("")
                        }
                        isPoiAddress = false
                    }
                    changeUnitComplexPlaceHolderOnType(selectedDeliveryAddressType)
                }
                binding.recipientAddressLayout.deliveringLayout?.addView(view)
            }
        }
    }

    private fun setFirebaseEvents(addressType: String) {


        val eventName = when (addressType) {
            ADDRESS_HOME -> {
                FirebaseManagerAnalyticsProperties.CHECKOUT_ADDRESS_DETAILS_HOME
            }

            ADDRESS_OFFICE -> {
                FirebaseManagerAnalyticsProperties.CHECKOUT_ADDRESS_DETAILS_OFFICE
            }

            ADDRESS_COMPLEX_ESTATE -> {
                FirebaseManagerAnalyticsProperties.CHECKOUT_ADDRESS_DETAILS_COMPLEX
            }

            ADDRESS_APARTMENT -> {
                FirebaseManagerAnalyticsProperties.CHECKOUT_ADDRESS_DETAILS_APARTMENT
            }

            else -> "default"
        }

        val eventProperty =
            when (addressType) {
                ADDRESS_HOME -> {
                    FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_CHECKOUT_ADDRESS_DETAILS_HOME
                }

                ADDRESS_OFFICE -> {
                    FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_CHECKOUT_ADDRESS_DETAILS_OFFICE
                }

                ADDRESS_COMPLEX_ESTATE -> {
                    FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_CHECKOUT_ADDRESS_DETAILS_COMPLEX
                }

                ADDRESS_APARTMENT -> {
                    FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_CHECKOUT_ADDRESS_DETAILS_APARTMENT
                }

                else -> "default"
            }

        Utils.triggerFireBaseEvents(
            eventName,
            hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        eventProperty
            ),
            activity
        )
    }
    private fun resetOtherDeliveringTitle(selectedTag: Int) {
        //change background of unselected textview
        for ((index) in deliveringOptionsList!!.withIndex()) {
            if (index != selectedTag) {
                val titleTextView: TextView? = view?.findViewWithTag(index)
                titleTextView?.background =
                    bindDrawable(R.drawable.checkout_delivering_title_round_button)
                titleTextView?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.saveAddress -> {
                onSaveAddressClicked()
            }

            R.id.backButton -> {
                // activity?.onBackPressed()
                activity?.let {
                    hideKeyboardIfVisible(it)
                    it.closeFragment(v)
                }
            }

            R.id.deleteTextView -> {
                if (savedAddressResponse?.addresses?.size!! > 1)
                    deleteAddress()
            }
        }
    }

    private fun FragmentActivity.closeFragment(view: View) {
        view.postDelayed({ onBackPressed() }, DELAY_500_MS)
    }

    private fun deleteAddress() {
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.CHANGE_FULFILLMENT_DELETE_ADDRESS, hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_DELETE_ADDRESS
            ), activity
        )
        binding.loadingProgressBar.visibility = View.VISIBLE
        checkoutAddAddressNewUserViewModel.deleteAddress(selectedAddressId)
            .observe(viewLifecycleOwner) { response ->
                binding.loadingProgressBar.visibility = View.GONE
                when (response) {
                    is DeleteAddressResponse -> {
                        when (response.httpCode) {
                            HTTP_OK, HTTP_OK_201 -> {
                                if (savedAddressResponse?.addresses != null) {
                                    val iterator =
                                        (savedAddressResponse?.addresses as? MutableList<Address>)?.iterator()
                                    while (iterator?.hasNext() == true) {
                                        val item = iterator.next()
                                        if (item.id.equals(selectedAddressId)) {
                                            iterator.remove()
                                            break
                                        }
                                    }
                                    baseFragBundle?.putString(
                                        SAVED_ADDRESS_KEY,
                                        Utils.toJson(savedAddressResponse)
                                    )
                                }
                                setFragmentResult(
                                    DELETE_SAVED_ADDRESS_REQUEST_KEY,
                                    bundleOf(SAVED_ADDRESS_KEY to savedAddressResponse)
                                )
                                navController?.navigateUp()
                                selectedAddressId = ""
                            }

                            else -> {
                                presentErrorDialog(
                                    getString(R.string.common_error_unfortunately_something_went_wrong),
                                    getString(R.string.delete_address_error),
                                    ERROR_TYPE_DELETE_ADDRESS
                                )
                            }
                        }
                    }

                    is Throwable -> {
                        presentErrorDialog(
                            getString(R.string.common_error_unfortunately_something_went_wrong),
                            getString(R.string.no_internet_subtitle),
                            ERROR_TYPE_DELETE_ADDRESS
                        )
                    }
                }
            }
    }

    fun onSaveAddressClicked() {
        if (selectedDeliveryAddressType.isNullOrEmpty()) {
            binding.recipientAddressLayout.deliveringAddressTypesErrorMsg.visibility = View.VISIBLE
            showAnimationErrorMessage(
                binding.recipientAddressLayout.deliveringAddressTypesErrorMsg,
                View.VISIBLE,
                0
            )
            listOfInputFields?.forEach {
                if (it is EditText) {
                    if (it.text.toString().trim().isEmpty())
                        showErrorInputField(it, View.VISIBLE)
                }
            }
            return
        }

        if (selectedAddress.savedAddress.address1.isNullOrEmpty()) {
            showErrorDialog()
            return
        }
        if(selectedAddress.savedAddress.address2.isNullOrEmpty() && binding.recipientAddressLayout.unitComplexFloorPlaceHolder?.text ==
                getString(R.string.additional_details)){
            showErrorInputField(binding.recipientAddressLayout.unitComplexFloorEditText, View.VISIBLE)
            return
        }
       if (!isValidAddress) {
            binding.autocompletePlaceErrorMsg.text =
                getString(R.string.geo_loc_error_msg_on_edit_address)
            showAnimationErrorMessage(binding.autocompletePlaceErrorMsg, View.VISIBLE, 0)
            return
        }
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.CHECKOUT_SAVE_ADDRESS, hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_SAVE_ADDRESS
            ), activity
        )


        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.CHECKOUT_ADDRESS_SAVE_ADDRESS, hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_CHECKOUT_ADDRESS_SAVE_ADDRESS
            ), activity
        )

        FirebaseAnalyticsEventHelper.setFirebaseEventForm(selectedDeliveryAddressType,
                FirebaseManagerAnalyticsProperties.FORM_COMPLETE, isComingFromCheckout)
        val isValidNumber = isAValidSouthAfricanNumber(
            binding.recipientDetailsLayout.cellphoneNumberEditText.text.toString().trim()
        )

        if (!isValidNumber) {
            showErrorPhoneNumber(R.string.enter_valid_sa_number)
        }
        if (binding.autoCompleteTextView?.text.toString().trim()
                .isNotEmpty() && binding.recipientAddressLayout.addressNicknameEditText?.text.toString()
                .trim()
                .isNotEmpty() && binding.recipientDetailsLayout.recipientNameEditText?.text.toString()
                .trim()
                .isNotEmpty() && binding.recipientDetailsLayout.cellphoneNumberEditText?.text.toString()
                .trim()
                .isNotEmpty() && selectedDeliveryAddressType != null
            && binding.recipientDetailsLayout.cellphoneNumberEditText?.text.toString()
                .trim().length == TEN
            && isValidNumber
        ) {
            if (isNickNameExist())
                return

            if (selectedAddressId.isNullOrEmpty()) {
                val body = getAddAddressRequestBody()
                binding.loadingProgressBar.visibility = View.VISIBLE
                checkoutAddAddressNewUserViewModel.addAddress(
                    body
                ).observe(viewLifecycleOwner) { response ->
                    binding.loadingProgressBar.visibility = View.GONE
                    when (response) {
                        is AddAddressResponse -> {
                            when (response.httpCode) {
                                HTTP_OK, HTTP_OK_201 -> {
                                    if (savedAddressResponse?.addresses != null) {
                                        savedAddressResponse?.addresses?.add(response.address)
                                    } else {
                                        if (savedAddressResponse == null)
                                            savedAddressResponse = SavedAddressResponse()
                                        val addressList: ArrayList<Address> = ArrayList()
                                        addressList.add(response.address)
                                        savedAddressResponse?.addresses = addressList
                                    }
                                    baseFragBundle?.putString(
                                        SAVED_ADDRESS_KEY,
                                        Utils.toJson(savedAddressResponse)
                                    )
                                    callValidatePlaceApi(response.address)
                                    hideKeyboardIfVisible(activity)
                                }

                                AppConstant.HTTP_SESSION_TIMEOUT_400, AppConstant.HTTP_EXPECTATION_FAILED_502 -> {
                                    addAddressErrorResponse(response, R.string.save_address_error)
                                }

                                else -> {
                                    presentErrorDialog(
                                        getString(R.string.common_error_unfortunately_something_went_wrong),
                                        getString(R.string.no_internet_subtitle),
                                        ERROR_TYPE_ADD_ADDRESS
                                    )
                                }
                            }
                        }

                        is Throwable -> {
                            presentErrorDialog(
                                getString(R.string.common_error_unfortunately_something_went_wrong),
                                getString(R.string.no_internet_subtitle),
                                ERROR_TYPE_ADD_ADDRESS
                            )
                        }
                    }
                }
            } else
                editAddress()

        } else {
            isNickNameExist()
            if (selectedDeliveryAddressType.isNullOrEmpty()) {
                binding.recipientAddressLayout.deliveringAddressTypesErrorMsg.visibility =
                    View.VISIBLE
                showAnimationErrorMessage(
                    binding.recipientAddressLayout.deliveringAddressTypesErrorMsg,
                    View.VISIBLE,
                    0
                )
            }
            listOfInputFields.forEach {
                if (it is EditText) {
                    if (it.text.toString().trim().isEmpty())
                        showErrorInputField(it, View.VISIBLE)
                }
            }
        }
    }

    fun addAddressErrorResponse(response: AddAddressResponse, errorMessage: Int) {
        if (response.response.code.toString() == ERROR_CODE_SUBURB_NOT_DELIVERABLE ||
            response.response.code.toString() == ERROR_CODE_SUBURB_NOT_FOUND
        ) {
            showSuburbNotDeliverableBottomSheetDialog(
                response.response.code.toString()
            )
        } else if (isNickNameAlreadyExist(response)) {
            showNickNameExist()
        } else {
            presentErrorDialog(
                getString(R.string.common_error_unfortunately_something_went_wrong),
                getString(errorMessage),
                ERROR_TYPE_ADD_ADDRESS
            )
        }
    }


    fun isNickNameAlreadyExist(response: AddAddressResponse): Boolean {
        if (!response.validationErrors.isNullOrEmpty()) {
            for (errorsFields in response.validationErrors) {
                if (errorsFields.getField() == "nickname") {
                    return true
                }
            }
        }
        return false
    }

    fun showSuburbNotDeliverableBottomSheetDialog(errorCode: String?) {
        view?.findNavController()?.navigate(
            R.id.action_checkoutAddAddressNewUserFragment_to_geoSuburbNotDeliverableBottomsheetDialogFragment,
            bundleOf(
                ERROR_CODE to errorCode
            )
        )
    }


    private fun getAddAddressRequestBody(): AddAddressRequestBody {
        return AddAddressRequestBody(
            binding.recipientAddressLayout.addressNicknameEditText?.text.toString().trim(),
            binding.recipientDetailsLayout.recipientNameEditText?.text.toString().trim(),
            (selectedAddress.savedAddress.address1 ?: "").toString().trim(),
            (selectedAddress.savedAddress.address2 ?: "").toString().trim(),
            binding.postalCode?.text.toString().trim(),
            binding.recipientDetailsLayout.cellphoneNumberEditText?.text.toString().trim(),
            "",
            binding.provinceAutocompleteEditText?.text?.toString() ?: "",
            selectedAddress.savedAddress.suburbId ?: "",
            selectedAddress.provinceName,
            binding.suburbEditText?.text.toString(),
            "",
            false,
            selectedAddress.savedAddress.latitude?.toString(),
            selectedAddress.savedAddress.longitude?.toString(),
            selectedAddress.savedAddress.placesId ?: "",
            selectedDeliveryAddressType.toString(),
            true
        )
    }

    private fun editAddress() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        checkoutAddAddressNewUserViewModel.editAddress(
            getAddAddressRequestBody(), selectedAddressId
        )
            .observe(viewLifecycleOwner) { response ->
                binding.loadingProgressBar.visibility = View.GONE
                when (response) {
                    is AddAddressResponse -> {
                        when (response.httpCode) {
                            HTTP_OK, HTTP_OK_201 -> {
                                if (savedAddressResponse != null && response != null) {
                                    arguments?.getInt(EDIT_ADDRESS_POSITION_KEY)
                                        ?.let { position ->
                                            (savedAddressResponse?.addresses as? MutableList<Address>)?.removeAt(
                                                position
                                            )
                                            response.address?.let { address ->
                                                (savedAddressResponse?.addresses as? MutableList<Address>)?.add(
                                                    position, address
                                                )
                                            }
                                            baseFragBundle?.putString(
                                                SAVED_ADDRESS_KEY,
                                                Utils.toJson(savedAddressResponse)
                                            )
                                        }

                                    KotlinUtils.isNickNameChanged =
                                        oldNickName?.equals(response?.address?.nickname) == false
                                    hideKeyboardIfVisible(activity)
                                    if (navController?.navigateUp() == false) {
                                        if (activity is CheckoutActivity) {
                                            (activity as CheckoutActivity).isEditAddressScreenNeeded =
                                                false
                                            navController?.navigate(
                                                (activity as CheckoutActivity).getStartDestinationGraph(),
                                                baseFragBundle
                                            )
                                        }
                                    }
                                }
                            }

                            AppConstant.HTTP_SESSION_TIMEOUT_400 -> {
                                addAddressErrorResponse(response, R.string.update_address_error)
                            }

                            AppConstant.HTTP_EXPECTATION_FAILED_502 -> {

                                validateNickNameWithServerError(response)
                            }

                            else -> {
                                validateNickNameWithServerError(response)
                            }
                        }
                    }

                    is Throwable -> {
                        presentErrorDialog(
                            getString(R.string.common_error_unfortunately_something_went_wrong),
                            getString(R.string.no_internet_subtitle),
                            ERROR_TYPE_ADD_ADDRESS
                        )
                    }
                }
            }
    }


    private fun isNickNameExist(): Boolean {
        var isExist = false
        if (!savedAddressResponse?.addresses.isNullOrEmpty() && selectedAddressId.isNullOrEmpty()) {
            for (address in savedAddressResponse?.addresses!!) {
                if (binding.recipientAddressLayout.addressNicknameEditText.text.toString()
                        .equals(address.nickname, true)
                ) {
                    showNickNameExist()
                    isExist = true
                }
            }
        }
        return isExist
    }

    fun showNickNameExist() {
        binding.recipientAddressLayout.addressNicknameEditText?.setBackgroundResource(R.drawable.input_error_background)
        binding.recipientAddressLayout.addressNicknameErrorMsg?.text =
            bindString(R.string.nick_name_exist_error_msg)
        showAnimationErrorMessage(
            binding.recipientAddressLayout.addressNicknameErrorMsg,
            View.VISIBLE,
            0
        )
    }

    private fun presentErrorDialog(title: String, subTitle: String, type: Int) {
        val bundle = Bundle()
        bundle.putString(
            ERROR_TITLE,
            title
        )
        bundle.putString(
            ERROR_DESCRIPTION,
            subTitle
        )

        bundle.putInt(ERROR_TYPE, type)

        val errorBottomSheetDialog = ErrorHandlerBottomSheetDialog.newInstance(bundle, this)
        requireActivity()?.supportFragmentManager?.let {
            errorBottomSheetDialog?.show(it, ErrorHandlerBottomSheetDialog::class.java.simpleName)
        }
    }

    fun showErrorDialog() {
        FirebaseManager.logException(AppConfigSingleton.nativeCheckout?.googlePlacesAddressErrorMessage)
        val dialog = ErrorDialogFragment.newInstance(
            AppConfigSingleton.nativeCheckout?.googlePlacesAddressErrorMessage
                ?: ""
        )
        (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()
            ?.let { fragmentTransaction ->
                dialog.show(
                    fragmentTransaction,
                    ErrorDialogFragment::class.java.simpleName
                )
            }
    }

    private fun callValidatePlaceApi(address: Address?){
        if (address?.placesId.isNullOrEmpty())
            return

        // Make Validate Location Call
        lifecycleScope.launch {
            binding.loadingProgressBar?.visibility = View.VISIBLE
            try {
                validateLocationResponse =
                    confirmAddressViewModel.getValidateLocation(address?.placesId!!)
                binding.loadingProgressBar?.visibility = View.GONE
                if (validateLocationResponse != null) {
                    when (validateLocationResponse?.httpCode) {
                        AppConstant.HTTP_OK -> {
                            validateLocationResponse?.validatePlace?.let { place ->
                                val delivery =
                                    Delivery.getType(KotlinUtils.getDeliveryType()?.deliveryType)
                                when (delivery) {
                                    Delivery.STANDARD -> {
                                        if (place.deliverable == true){
                                            validateUnsellable(
                                                address?.placesId,
                                                address,
                                                place
                                            )
                                        }
                                        else {
                                            showChangeLocationDialog()
                                        }
                                    }

                                    Delivery.DASH -> {
                                        if (place.onDemand != null && place.onDemand!!.deliverable) {
                                            validateUnsellable(
                                                address?.placesId,
                                                address,
                                                place
                                            )
                                        } else {
                                            // Show not deliverable Bottom Dialog.
                                            showChangeLocationDialog()
                                        }
                                    }

                                    else -> {

                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                FirebaseManager.logException(e)
               binding.loadingProgressBar?.visibility = View.GONE
                showErrorDialog()
            } catch (e: JsonSyntaxException) {
                FirebaseManager.logException(e)
                binding.loadingProgressBar?.visibility = View.GONE
                showErrorDialog()
            }
        }
    }

    private fun showChangeLocationDialog() {
        val customBottomSheetDialogFragment =
            CustomBottomSheetDialogFragment.newInstance(
                getString(R.string.no_location_title),
                getString(R.string.no_location_desc),
                getString(R.string.change_location),
                R.drawable.location_disabled, getString(R.string.dismiss)
            )
        customBottomSheetDialogFragment.show(
            requireFragmentManager(),
            CustomBottomSheetDialogFragment::class.java.simpleName
        )
    }

    private fun validateUnsellable(placesId: String?, address: Address, place: ValidatePlace) {
        baseFragBundle?.putString(KEY_PLACE_ID, placesId)
        baseFragBundle?.putBoolean(
            IS_COMING_FROM_CHECKOUT,
            isComingFromCheckout
        )
        baseFragBundle?.putBoolean(
            IS_COMING_FROM_SLOT_SELECTION,
            isComingFromSlotSelection
        )
        baseFragBundle?.putSerializable(
            SAVED_ADDRESS_RESPONSE,
            savedAddressResponse
        )
        baseFragBundle?.putSerializable(BundleKeysConstants.DEFAULT_ADDRESS, address)
        if (bundle?.containsKey(DELIVERY_TYPE) == true) {
            baseFragBundle?.putString(DELIVERY_TYPE, bundle?.getString(DELIVERY_TYPE))
        }
        if (isComingFromNewToggleFulfilment && isLocationUpdateRequest && isComingFromCheckout) {
            val delivery = Delivery.getType(KotlinUtils.getDeliveryType()?.deliveryType)
            if (isComingFromSlotSelection && (delivery == Delivery.STANDARD || delivery == Delivery.DASH)) {

                var unSellableCommerceItems = when (delivery) {
                    Delivery.STANDARD -> {
                            place?.unSellableCommerceItems
                    }

                    Delivery.DASH -> {
                        place?.onDemand?.unSellableCommerceItems
                    }
                    else -> {
                        place?.unSellableCommerceItems
                    }
                }

                if (unSellableCommerceItems?.isNullOrEmpty() == false) {
                    // show unsellable items
                    unSellableCommerceItems?.let {
                        navigateToUnsellableItemsFragment(
                            it as ArrayList<UnSellableCommerceItem>,
                            delivery,
                            getConfirmLocationRequest(placesId, address, delivery, place)
                        )
                    }

                } else {
                    val confirmLocationRequest = getConfirmLocationRequest(placesId, address, delivery, place)
                    UnsellableUtils.callConfirmPlace(
                        this@CheckoutAddAddressNewUserFragment,
                        ConfirmLocationParams(null, confirmLocationRequest),
                        binding.loadingProgressBar, confirmAddressViewModel,
                        delivery
                    )
                }
            }
        } else {
            findNavController().navigate(
                R.id.action_checkoutAddAddressNewUserFragment_to_deliveryAddressConfirmationFragment,
                bundleOf(BUNDLE to baseFragBundle)
            )
        }
    }

    private fun getConfirmLocationRequest(placesId: String?, address: Address, delivery: Delivery, place: ValidatePlace): ConfirmLocationRequest {
        val confirmLocationAddress =
            ConfirmLocationAddress(placesId, address.nickname, address.address2)
        return when (delivery) {
            Delivery.STANDARD -> {
                ConfirmLocationRequest(
                    BundleKeysConstants.STANDARD,
                    confirmLocationAddress,
                    ""
                )
            }

            Delivery.DASH -> {
                ConfirmLocationRequest(
                    BundleKeysConstants.DASH,
                    confirmLocationAddress,
                    place.onDemand?.storeId
                )
            }

            else -> {
                ConfirmLocationRequest(
                    BundleKeysConstants.STANDARD,
                    confirmLocationAddress,
                    ""
                )
            }
        }
    }
    private fun navigateToUnsellableItemsFragment(
        unSellableCommerceItems: ArrayList<UnSellableCommerceItem>,
        currentDeliveryType: Delivery,
        confirmLocationRequest: ConfirmLocationRequest,
    ) {
        val unsellableItemsBottomSheetDialog =
            UnsellableItemsBottomSheetDialog.newInstance(
                unSellableCommerceItems,
                currentDeliveryType,
                binding.loadingProgressBar,
                confirmAddressViewModel,
                this,
                confirmLocationRequest
            )
        unsellableItemsBottomSheetDialog.show(
            requireFragmentManager(),
            UnsellableItemsBottomSheetDialog::class.java.simpleName
        )
    }

    private fun getLiquorOrder(): Boolean {
        var liquorOrder = false
        arguments?.apply {
            liquorOrder = getBoolean(Constant.LIQUOR_ORDER)
        }
        return liquorOrder
    }

    private fun getLiquorImageUrl(): String {
        var liquorImageUrl = ""
        arguments?.apply {
            liquorImageUrl = getString(Constant.NO_LIQUOR_IMAGE_URL, "")
        }
        return liquorImageUrl
    }

    private fun showErrorPhoneNumber(resourceId:Int) {
        binding.recipientDetailsLayout.cellphoneNumberEditText?.setBackgroundResource(R.drawable.input_error_background)
        binding.recipientDetailsLayout.cellphoneNumberErrorMsg?.visibility = View.VISIBLE
        binding.recipientDetailsLayout.cellphoneNumberErrorMsg?.text =
            bindString(resourceId)
        showAnimationErrorMessage(
            binding.recipientDetailsLayout.cellphoneNumberErrorMsg,
            View.VISIBLE,
            binding.recipientAddressLayout.root.y.toInt()
        )
    }


    private fun showNickNameServerError(errorMsg: String?) {
        binding.recipientAddressLayout.addressNicknameEditText?.setBackgroundResource(R.drawable.input_error_background)
        binding.recipientAddressLayout.addressNicknameErrorMsg?.visibility = View.VISIBLE
        binding.recipientAddressLayout.addressNicknameErrorMsg?.text = errorMsg
        showAnimationErrorMessage(
            binding.recipientAddressLayout.addressNicknameErrorMsg,
            View.VISIBLE,
            binding.recipientAddressLayout.root.y.toInt()
        )
    }


    private fun showErrorInputField(editText: EditText?, visible: Int) {
        editText?.setBackgroundResource(if (visible == View.VISIBLE) R.drawable.input_error_background else R.drawable.recipient_details_input_edittext_bg)
        if (editText != null) {
            when (editText?.id) {
                R.id.autoCompleteTextView -> {
                    showAnimationErrorMessage(binding.autocompletePlaceErrorMsg, visible, 0)
                }

                R.id.addressNicknameEditText -> {
                    showAnimationErrorMessage(
                        binding.recipientAddressLayout.addressNicknameErrorMsg,
                        visible,
                        0
                    )
                }

                R.id.recipientNameEditText -> {
                    showAnimationErrorMessage(
                        binding.recipientDetailsLayout.recipientNameErrorMsg,
                        visible,
                        binding.recipientAddressLayout.root.y.toInt()
                    )
                }

                R.id.cellphoneNumberEditText -> {
                    binding.recipientDetailsLayout.cellphoneNumberErrorMsg.text =
                        bindString(R.string.enter_valid_sa_number)
                    showAnimationErrorMessage(
                        binding.recipientDetailsLayout.cellphoneNumberErrorMsg,
                        visible,
                        binding.recipientAddressLayout.root.y.toInt()
                    )
                }

                R.id.unitComplexFloorEditText -> {

                    //For other than House AddressType UnitComplex is  mandatory
                    if (selectedDeliveryAddressType != Constant.HOUSE) {
                        binding.recipientAddressLayout.unitComplexFloorEditTextErrorMsg?.text =
                            bindString(R.string.address_nickname_error_msg)
                        showAnimationErrorMessage(
                            binding.recipientAddressLayout.unitComplexFloorEditTextErrorMsg,
                            visible,
                            binding.recipientAddressLayout.root.y.toInt()
                        )
                    }

                }
            }
        }
    }

    private fun showAnimationErrorMessage(
        textView: TextView?,
        visible: Int,
        recipientLayoutValue: Int?,
    ) {
        textView?.visibility = visible
        if (View.VISIBLE == visible && textView != null) {
            val anim = ObjectAnimator.ofInt(
                binding.newUserNestedScrollView,
                "scrollY",
                recipientLayoutValue ?: (0 + (textView?.y?.toInt() ?: 0))
            )
            anim.setDuration(300).start()
        }
    }

    @VisibleForTesting
    fun testSetBundleArguments(bundle: Bundle) {
        arguments = bundle
    }

    @VisibleForTesting
    fun testGetSelectedAddressId(): String {
        return selectedAddressId
    }

    @VisibleForTesting
    fun testGetSelectedDeliveryAddressType(): String? {
        return selectedDeliveryAddressType
    }

    @VisibleForTesting
    fun testGetIsAddNewAddress(): Boolean {
        return isAddNewAddress
    }

    @VisibleForTesting
    fun testGetSavedAddress(): SavedAddressResponse? {
        return savedAddressResponse
    }

    private fun validateNickNameWithServerError(response: AddAddressResponse) {
        var nickNameErrorMessage: String? = ""
        response?.validationErrors?.let {
            nickNameErrorMessage = it.stream().filter { error ->
                error?.getField().equals(BundleKeysConstants.NICK_NAME)
                    .and(!error?.getField().isNullOrEmpty())
                    .and(!error?.getMessage().isNullOrEmpty())
            }.findFirst().orElse(null)?.getMessage()
        }

        if (!nickNameErrorMessage.isNullOrEmpty()) {
            showNickNameServerError(nickNameErrorMessage)
        } else {
            presentErrorDialog(
                getString(R.string.common_error_unfortunately_something_went_wrong),
                getString(R.string.no_internet_subtitle),
                ERROR_TYPE_ADD_ADDRESS
            )

        }
    }

    private fun enablePOIAddressTextFields() {
        binding.apply {
            //need to set Address Type as Complex/Estate
            selectedDeliveryAddressType = Constant.COMPLEX_ESTATE
            recipientAddressLayout.deliveringLayout.removeAllViews()
        }
        showWhereAreWeDeliveringView()

    }

    private fun changeUnitComplexPlaceHolderOnType(addressType: String?) {
        //based on the AddressType changing the PlaceHolder for UnitNo/Complex/Floor/Building

        when (addressType) {
            Constant.OFFICE, Constant.APARTMENT,
            Constant.COMPLEX_ESTATE,
            -> {
                binding.recipientAddressLayout.unitComplexFloorPlaceHolder?.text =
                    getString(R.string.additional_details)
            }

            else
            -> {
                binding.recipientAddressLayout.unitComplexFloorPlaceHolder?.text =
                    getString(R.string.house_details)
                binding.recipientAddressLayout.unitComplexFloorEditTextErrorMsg?.visibility =
                    View.GONE
            }
        }
        binding.recipientAddressLayout.unitComplexFloorEditText?.setBackgroundResource(R.drawable.recipient_details_input_edittext_bg)
    }

    override fun onRetryClick(errorType: Int) {
        when (errorType) {
            ERROR_TYPE_ADD_ADDRESS -> {
                onSaveAddressClicked()
            }

            ERROR_TYPE_DELETE_ADDRESS -> {
                deleteAddress()
            }
        }
    }


    override fun onConfirmClick(streetName: String) {
        address2 = streetName
        binding.recipientAddressLayout.unitComplexFloorEditText.value = streetName

    }


    override fun unIndexedAddressIdentified() {
        unIndexedAddressIdentified = true
        hideOrShowUnIndexedAddressErrorMessages(true)
    }


    private fun addUnIndexedIdentifiedListener() {
        unIndexedLiveData.value = false
        unIndexedLiveData.observe(viewLifecycleOwner) {
            if (it == true && unIndexedAddressIdentified == true && isPoiAddress == false) {

                isValidAddress = true
                enablePOIAddressTextFields()
                PoiBottomSheetDialog(this@CheckoutAddAddressNewUserFragment, false).show(
                    requireActivity().supportFragmentManager,
                    PoiBottomSheetDialog::class.java.simpleName


                )
            } else if (isPoiAddress == true) {
                PoiBottomSheetDialog(this@CheckoutAddAddressNewUserFragment, true).show(
                    requireActivity().supportFragmentManager,
                    PoiBottomSheetDialog::class.java.simpleName
                )

            }

        }
    }

    private fun hideOrShowUnIndexedAddressErrorMessages(isEnabled: Boolean?) {
        binding.apply {
            if (isEnabled == true) {
                errorMessageTitle.visibility = View.VISIBLE
                errorMessage.visibility = View.VISIBLE
            } else {
                errorMessageTitle.visibility = View.GONE
                errorMessage.visibility = View.GONE
            }
        }
    }
}