package za.co.woolworths.financial.services.android.checkout.view

import android.animation.ObjectAnimator
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import kotlinx.android.synthetic.main.checkout_add_address_new_user.*
import kotlinx.android.synthetic.main.checkout_new_user_address_details.*
import kotlinx.android.synthetic.main.checkout_new_user_recipient_details.*
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressNewUserFragment.ProvinceSuburbType.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.ADD_NEW_ADDRESS_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.DELETE_SAVED_ADDRESS_REQUEST_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.SAVED_ADDRESS_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.UNSELLABLE_CHANGE_STORE_REQUEST_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.UPDATE_SAVED_ADDRESS_REQUEST_KEY
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
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.*
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.SelectedPlacesAddress
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.ADDRESS_APARTMENT
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.ADDRESS_COMPLEX_ESTATE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.ADDRESS_HOME
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.ADDRESS_OFFICE
import za.co.woolworths.financial.services.android.geolocation.GeoUtils.Companion.getSelectedDefaultName
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.UnsellableItemsFragment.Companion.KEY_ARGS_BUNDLE
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.UnsellableItemsFragment.Companion.KEY_ARGS_PROVINCE
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.UnsellableItemsFragment.Companion.KEY_ARGS_SCREEN_NAME
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.UnsellableItemsFragment.Companion.KEY_ARGS_SUBURB
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.UnsellableItemsFragment.Companion.KEY_ARGS_UNSELLABLE_COMMERCE_ITEMS
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK_201
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DELIVERY_TYPE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_CHECKOUT
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_SLOT_SELECTION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_PLACE_ID
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.SAVED_ADDRESS_RESPONSE
import java.net.HttpURLConnection.HTTP_OK
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList


/**
 * Created by Kunal Uttarwar on 29/05/21.
 */
class CheckoutAddAddressNewUserFragment : CheckoutAddressManagementBaseFragment(),
    View.OnClickListener {

    private var deliveringOptionsList: List<String>? = null
    private var navController: NavController? = null
    private lateinit var listOfInputFields: List<View>
    var deliveryType: DeliveryType = DeliveryType.DELIVERY
    private var selectedDeliveryAddressType: String? = null
    var selectedAddress = SelectedPlacesAddress()
    private var savedAddressResponse: SavedAddressResponse? = null
    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private var selectedAddressId = ""
    private var isAddNewAddress = false
    private var provinceSuburbEnableType: ProvinceSuburbType? = null
    private var bundle: Bundle? = null
    private var selectedAddressPosition: Int = -1
    private var isComingFromCheckout: Boolean = false
    private var isComingFromSlotSelection: Boolean = false


    companion object {
        const val PROVINCE_SELECTION_BACK_PRESSED = "5645"
        const val SUBURB_SELECTION_BACK_PRESSED = "5465"
        const val SCREEN_NAME_EDIT_ADDRESS: String = "SCREEN_NAME_EDIT_ADDRESS"
        const val SCREEN_NAME_ADD_NEW_ADDRESS: String = "SCREEN_NAME_ADD_NEW_ADDRESS"
        const val REGEX_NICK_NAME: String = "^$|^[a-zA-Z0-9\\s<!>@$&().+,-/\"']+$"
        const val ADDRESS_NICK_NAME_MAX_CHAR: Int = 40
    }

    enum class ProvinceSuburbType {
        ONLY_PROVINCE,
        ONLY_SUBURB,
        BOTH
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.checkout_add_address_new_user, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleBundleResponse()
    }

    fun handleBundleResponse() {
        bundle = arguments?.getBundle(BUNDLE)
        bundle?.apply {
            isComingFromCheckout = getBoolean(IS_COMING_FROM_CHECKOUT, false)
            isComingFromSlotSelection = getBoolean(IS_COMING_FROM_SLOT_SELECTION, false)
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
                        var provinceName: String? = ""
                        provinceName = getProvinceName(savedAddress.region)
                        if (!provinceName.isNullOrEmpty()) {
                            selectedAddress?.provinceName = provinceName
                        } else {
                            savedAddress?.region?.let {
                                selectedAddress?.provinceName = it
                            }
                        }
                    }
                    setHasOptionsMenu(true)
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

    private fun getProvinceName(provinceId: String?): String {
        val provinceList =
            AppConfigSingleton.nativeCheckout?.regions as MutableList<Province>
        if (!provinceId.isNullOrEmpty()) {
            for (provinces in provinceList) {
                if (provinceId == provinces.id) {
                    // province id is matching with the province list from config.
                    return provinces.name ?: ""
                }
            }
        }
        return ""
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_item, menu)
        if (selectedAddressId.isNotEmpty() || isAddNewAddress) //show only if it is edit address screen
            return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (navController == null)
            navController = Navigation.findNavController(view)
        initView()
        listOfInputFields = listOf(
            autoCompleteTextView,
            addressNicknameEditText,
            recipientNameEditText,
            cellphoneNumberEditText
        )
        setupViewModel()
        init()
        addFragmentResultListener()
        // Show prepopulate fields on edit address
        if (selectedAddressId.isNotEmpty() || isAddNewAddress) {
            //selectedAddressId is not empty means it's a edit address call.
            if (activity is CheckoutActivity)
                (activity as CheckoutActivity).hideBackArrow()
            if (selectedAddressId.isNotEmpty())
                setTextFields()
        }
    }

    private fun setTextFields() {
        enableDisableUserInputEditText(
            addressNicknameEditText,
            true,
            addressNicknameErrorMsg.isVisible
        )
        enableDisableUserInputEditText(
            unitComplexFloorEditText,
            isEnable = true,
            isErrorScreen = false
        )
        if (selectedAddress.savedAddress.placesId.isNullOrEmpty())
            autoCompleteTextView.text.clear() // This condition will only occur when address is added from web and is now opted for edit from app.
        else
            autoCompleteTextView?.setText(selectedAddress.savedAddress.address1)
        addressNicknameEditText.setText(selectedAddress.savedAddress.nickname)
        unitComplexFloorEditText.setText(selectedAddress.savedAddress.address2)
        suburbEditText.setText(selectedAddress.savedAddress.suburb)
        provinceAutocompleteEditText.setText(selectedAddress.provinceName)
        cellphoneNumberEditText.setText(selectedAddress.savedAddress.primaryContactNo)
        recipientNameEditText.setText(selectedAddress.savedAddress.recipientName)
        if (selectedAddress.savedAddress.postalCode.isNullOrEmpty()) {
            postalCode.text.clear()
        } else
            postalCode.setText(selectedAddress.savedAddress.postalCode)
        selectedDeliveryAddressType = selectedAddress.savedAddress.addressType
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
                deleteTextView?.visibility = View.VISIBLE
                deleteTextView?.setOnClickListener(this)
            } else if (getSelectedDefaultName(savedAddressResponse, selectedAddressPosition)) {
                deleteTextView?.visibility = View.GONE
            }
            saveAddress?.text = bindString(R.string.change_details)
        }
        if (activity is CheckoutActivity) {
            (activity as? CheckoutActivity)?.apply {
                showBackArrowWithoutTitle()
            }
        }
        saveAddress?.setOnClickListener(this)
        autoCompleteTextView?.apply {
            afterTextChanged {
                if (it.isNotEmpty())
                    showErrorInputField(this, View.GONE)
            }
        }
        addressNicknameEditText?.apply {
            afterTextChanged {
                val addressNickNameLength = it.length
                if (addressNickNameLength > 0 && !Pattern.matches(REGEX_NICK_NAME, it)) {
                    text?.delete(addressNickNameLength - 1, addressNickNameLength)
                }
                selectedAddress.savedAddress.nickname = it
                if (addressNickNameLength >= ADDRESS_NICK_NAME_MAX_CHAR) {
                    addressNicknameErrorMsg?.visibility = View.VISIBLE
                    addressNicknameErrorMsg?.text = getString(R.string.max_characters_allowed)
                } else if (it.isNotEmpty()) {
                    addressNicknameErrorMsg?.text = getString(R.string.address_nickname_error_msg)
                    showErrorInputField(this, View.GONE)
                }
            }
        }

        unitComplexFloorEditText?.apply {
            afterTextChanged {
                selectedAddress.savedAddress.address2 = it
            }
        }
        recipientNameEditText?.apply {
            afterTextChanged {
                if (it.isNotEmpty())
                    showErrorInputField(this, View.GONE)
            }
        }
        cellphoneNumberEditText?.apply {
            afterTextChanged {
                if (it.isNotEmpty())
                    showErrorInputField(this, View.GONE)
            }
        }
    }

    private fun setupViewModel() {
        checkoutAddAddressNewUserViewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(
                CheckoutAddAddressNewUserInteractor(
                    CheckoutAddAddressNewUserApiHelper()
                )
            )
        ).get(CheckoutAddAddressNewUserViewModel::class.java)
    }

    private fun init() {
        deliveringOptionsList = AppConfigSingleton.nativeCheckout?.addressTypes
        showWhereAreWeDeliveringView()
        activity?.applicationContext?.let { context ->
            Places.initialize(context, getString(R.string.maps_api_key))
            val placesClient = Places.createClient(context)
            val placesAdapter =
                GooglePlacesAdapter(requireActivity(), placesClient)
            autoCompleteTextView?.apply {
                setAdapter(placesAdapter)
            }
            autoCompleteTextView?.onItemClickListener =
                AdapterView.OnItemClickListener { parent, _, position, _ ->
                    val item = parent.getItemAtPosition(position) as? PlaceAutocomplete
                    val placeId = item?.placeId.toString()
                    val placeFields: MutableList<Place.Field> = mutableListOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.LAT_LNG,
                        Place.Field.ADDRESS,
                        Place.Field.ADDRESS_COMPONENTS
                    )
                    val request =
                        placeFields.let { FetchPlaceRequest.builder(placeId, it).build() }
                    request.let { placeRequest ->
                        placesClient.fetchPlace(placeRequest)
                            .addOnSuccessListener { response ->
                                val place = response.place
                                selectedAddress = SelectedPlacesAddress()
                                setAddress(place)
                            }.addOnFailureListener { exception ->
                                if (exception is ApiException) {
                                    Toast.makeText(
                                        AuthenticateUtils.mContext,
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

        setFragmentResultListener(UNSELLABLE_CHANGE_STORE_REQUEST_KEY) { _, bundle ->
            var screenName: String
            bundle.apply {
                screenName = getString(KEY_ARGS_SCREEN_NAME, "")
            }

            when (screenName) {
                SCREEN_NAME_ADD_NEW_ADDRESS -> {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.CHECKOUT_REMOVE_UNSELLABLE_ITEMS,
                        hashMapOf(
                            FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                    FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_REMOVE_ITEMS
                        ),
                        activity
                    )
                    savedAddressResponse?.defaultAddressNickname =
                        selectedAddress.savedAddress.nickname
                    view?.findNavController()?.navigate(
                        R.id.action_CheckoutAddAddressNewUserFragment_to_CheckoutAddAddressReturningUserFragment,
                        bundleOf(
                            SAVED_ADDRESS_KEY to savedAddressResponse
                        )
                    )
                }
                SCREEN_NAME_EDIT_ADDRESS -> {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.CHECKOUT_REMOVE_UNSELLABLE_ITEMS,
                        hashMapOf(
                            FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                    FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_REMOVE_ITEMS
                        ),
                        activity
                    )
                    setFragmentResult(
                        UPDATE_SAVED_ADDRESS_REQUEST_KEY, bundleOf(
                            SAVED_ADDRESS_KEY to savedAddressResponse
                        )
                    )
                    navController?.navigateUp()
                    selectedAddressId = ""
                }
            }
        }
    }

    fun resetSuburbSelection() {
        selectedAddress.savedAddress.apply {
            suburb = ""
            suburbId = ""
        }
        selectedAddress.store = ""
        selectedAddress.storeId = ""
        suburbEditText?.text?.clear()
    }


    private fun setAddress(place: Place) {
        enableDisableUserInputEditText(
            addressNicknameEditText,
            true,
            addressNicknameErrorMsg.isVisible
        )
        enableDisableUserInputEditText(
            unitComplexFloorEditText,
            isEnable = true,
            isErrorScreen = false
        )
        provinceSuburbEnableType = null
        var addressText1 = ""
        var addressText2 = ""
        for (address in place.addressComponents?.asList()!!) {
            when (address.types[0]) {
                STREET_NUMBER.value -> addressText1 = address.name
                ROUTE.value -> addressText2 =
                    if (!address.name.isNullOrEmpty()) address.name else addressText2
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
                    if (addressText2.isNullOrEmpty()) addressText2 = address.name
                }
            }
        }
        if (!selectedAddress.provinceName.isNullOrEmpty() && !selectedAddress.savedAddress.suburb.isNullOrEmpty())
            selectedAddress.savedAddress.region = ""
        selectedAddress.savedAddress.apply {
            val tempAddress1 =
                if (addressText1.isNullOrEmpty()) addressText2 else addressText1.plus(" ")
                    .plus(addressText2)
            val googlePlacesName = place.name
            address1 =
                if (googlePlacesName.isNullOrEmpty()) tempAddress1 else if (googlePlacesName.length > 50) tempAddress1 else googlePlacesName
            latitude = place.latLng?.latitude
            longitude = place.latLng?.longitude
            placesId = place.id
        }

        if (!selectedAddress.savedAddress.suburb.isNullOrEmpty())
            selectedAddress.savedAddress.suburbId = ""
        if (selectedAddress.savedAddress.postalCode.isNullOrEmpty()) {
            //If Google places failed to give postal code.
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses =
                geocoder.getFromLocation(
                    selectedAddress.savedAddress.latitude!!,
                    selectedAddress.savedAddress.longitude!!,
                    1
                )
            if (!addresses[0]?.postalCode.isNullOrEmpty())
                selectedAddress.savedAddress.postalCode = addresses[0]?.postalCode.toString()
        }

        autoCompleteTextView.apply {
            setText(
                if (place.name.isNullOrEmpty()) selectedAddress.savedAddress.address1 else place.name
            )
            if (selectedAddress.savedAddress.address1.isNullOrEmpty())
                showErrorDialog()
            setSelection(autoCompleteTextView.length())
            autoCompleteTextView.dismissDropDown()
        }
        checkIfSelectedProvinceExist(AppConfigSingleton.nativeCheckout?.regions as MutableList<Province>)
    }

    fun checkIfSelectedProvinceExist(provinceList: MutableList<Province>) {
        val localProvince = Province()
        val provinceName = selectedAddress.provinceName
        if (!provinceName.isNullOrEmpty()) {
            for (provinces in provinceList) {
                if (provinceName.equals(provinces.name)) {
                    // province name is matching with the province list from config.
                    localProvince.apply {
                        id = provinces.id
                        name = provinces.name
                    }
                    provinceAutocompleteEditText?.setText(provinceName)
                    selectedAddress.apply {
                        this.provinceName = localProvince.name ?: ""
                        savedAddress.region = localProvince.id
                    }
                }
            }
            if (localProvince.name.isNullOrEmpty()) {
                // province name is not matching with the province list from config.
                provinceAutocompleteEditText?.setText("")
                provinceSuburbEnableType =
                    ONLY_PROVINCE
            }
        } else {
            provinceAutocompleteEditText.setText("")
            provinceSuburbEnableType = ONLY_PROVINCE
        }
        if (selectedAddress.savedAddress.suburb.isNullOrEmpty()) {
            resetSuburbSelection()
            provinceSuburbEnableType =
                if (selectedAddress.provinceName.isNullOrEmpty()) BOTH else ONLY_SUBURB
        } else {
            suburbEditText?.setText(selectedAddress.savedAddress.suburb)
        }

        when (selectedAddress.savedAddress.postalCode.isNullOrEmpty()) {

            false -> {
                postalCode.setText(selectedAddress.savedAddress.postalCode)
            }
        }
    }

    private fun enableDisableUserInputEditText(
        userInputField: EditText?,
        isEnable: Boolean,
        isErrorScreen: Boolean
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
                }
                titleTextView?.setOnClickListener {
                    setFirebaseEvents(titleTextView?.text.toString())
                    resetOtherDeliveringTitle(it.tag as Int)
                    selectedDeliveryAddressType = (it as TextView).text as? String
                    selectedAddress.savedAddress.addressType = selectedDeliveryAddressType
                    deliveringAddressTypesErrorMsg.visibility = View.GONE
                    // change background of selected textView
                    it.background =
                        bindDrawable(R.drawable.checkout_delivering_title_round_button_pressed)
                    it.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }
                delivering_layout?.addView(view)
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

            R.id.deleteTextView -> {
                if (savedAddressResponse?.addresses?.size!! > 1)
                    deleteAddress()
            }
        }
    }

    private fun deleteAddress() {
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.CHANGE_FULFILLMENT_DELETE_ADDRESS, hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_DELETE_ADDRESS
            ), activity
        )
        loadingProgressBar.visibility = View.VISIBLE
        checkoutAddAddressNewUserViewModel.deleteAddress(selectedAddressId)
            .observe(viewLifecycleOwner, { response ->
                loadingProgressBar.visibility = View.GONE
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
            })
    }

    fun onSaveAddressClicked() {
        if (selectedDeliveryAddressType.isNullOrEmpty()) {
            deliveringAddressTypesErrorMsg.visibility = View.VISIBLE
            showAnimationErrorMessage(deliveringAddressTypesErrorMsg, View.VISIBLE, 0)
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
        if (cellphoneNumberEditText?.text.toString().trim().isNotEmpty()
            && cellphoneNumberEditText?.text.toString().trim().length < 10
        ) {
            showErrorPhoneNumber()
        }
        if (autoCompleteTextView?.text.toString().trim()
                .isNotEmpty() && addressNicknameEditText?.text.toString().trim()
                .isNotEmpty() && recipientNameEditText?.text.toString().trim()
                .isNotEmpty() && cellphoneNumberEditText?.text.toString().trim()
                .isNotEmpty() && selectedDeliveryAddressType != null
            && cellphoneNumberEditText?.text.toString().trim().length == 10
        ) {
            if (isNickNameExist())
                return

            if (selectedAddressId.isNullOrEmpty()) {
                val body = getAddAddressRequestBody()
                loadingProgressBar.visibility = View.VISIBLE
                checkoutAddAddressNewUserViewModel.addAddress(
                    body
                ).observe(viewLifecycleOwner, { response ->
                    loadingProgressBar.visibility = View.GONE
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
                                    response.address.nickname?.let { nickName ->
                                        navigateToAddressConfirmation(response.address.placesId)

                                    }
                                    KeyboardUtils.hideKeyboardIfVisible(activity)
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
                })
            } else
                editAddress()

        } else {
            isNickNameExist()
            if (selectedDeliveryAddressType.isNullOrEmpty()) {
                deliveringAddressTypesErrorMsg.visibility = View.VISIBLE
                showAnimationErrorMessage(deliveringAddressTypesErrorMsg, View.VISIBLE, 0)
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


    private fun showErrorScreen(errorType: Int, errorMessage: String?) {
        activity?.apply {
            val intent = Intent(this, ErrorHandlerActivity::class.java)
            intent.putExtra("errorType", errorType)
            intent.putExtra("errorMessage", errorMessage)
            startActivityForResult(intent, ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE)
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

    /**
     * This function is to navigate to Unsellable Items screen.
     * @param [unSellableCommerceItems] list of items that are not deliverable in the selected location
     * @param [deliverable] boolean flag to determine if provided list of items are deliverable
     *
     * @see [Suburb]
     * @see [Province]
     * @see [UnSellableCommerceItem]
     */
    private fun navigateToUnsellableItemsFragment(
        unSellableCommerceItems: MutableList<UnSellableCommerceItem>,
        deliverable: Boolean,
        screenName: String
    ) {
        val suburb = Suburb()
        val province = Province()
        suburb.apply {
            id = selectedAddress.savedAddress.suburbId
            name = selectedAddress.savedAddress.suburb
            postalCode = selectedAddress.savedAddress.postalCode
            suburbDeliverable = deliverable
        }
        province.apply {
            name = selectedAddress.provinceName
            id = selectedAddress.savedAddress.region
        }

        navController?.navigate(
            R.id.action_checkoutAddAddressNewUserFragment_to_geoUnsellableItemsFragment,
            bundleOf(
                KEY_ARGS_BUNDLE to bundleOf(
                    SAVED_ADDRESS_KEY to savedAddressResponse,
                    DELIVERY_TYPE to DeliveryType.DELIVERY.name,
                    KEY_ARGS_SUBURB to Utils.toJson(suburb),
                    KEY_ARGS_PROVINCE to Utils.toJson(province),
                    KEY_ARGS_UNSELLABLE_COMMERCE_ITEMS to Utils.toJson(unSellableCommerceItems),
                    KEY_ARGS_SCREEN_NAME to screenName
                )
            )
        )
    }

    private fun getAddAddressRequestBody(): AddAddressRequestBody {
        return AddAddressRequestBody(
            addressNicknameEditText?.text.toString().trim(),
            recipientNameEditText?.text.toString().trim(),
            (selectedAddress.savedAddress.address1 ?: "").toString().trim(),
            unitComplexFloorEditText?.text.toString().trim(),
            postalCode?.text.toString().trim(),
            cellphoneNumberEditText?.text.toString().trim(),
            "",
            provinceAutocompleteEditText?.text?.toString() ?: "",
            selectedAddress.savedAddress.suburbId ?: "",
            selectedAddress.provinceName,
            suburbEditText?.text.toString(),
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
        loadingProgressBar.visibility = View.VISIBLE
        checkoutAddAddressNewUserViewModel.editAddress(
            getAddAddressRequestBody(), selectedAddressId
        )
            .observe(viewLifecycleOwner, { response ->
                loadingProgressBar.visibility = View.GONE
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
                                    KeyboardUtils.hideKeyboardIfVisible(activity)
                                    navController?.navigateUp()
                                }
                            }
                            AppConstant.HTTP_SESSION_TIMEOUT_400, AppConstant.HTTP_EXPECTATION_FAILED_502 -> {
                                addAddressErrorResponse(response, R.string.update_address_error)
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
            })
    }

    private fun callChangeAddressApi(nickName: String) {
        loadingProgressBar.visibility = View.VISIBLE
        checkoutAddAddressNewUserViewModel.changeAddress(
            nickName
        ).observe(viewLifecycleOwner, { response ->
            loadingProgressBar.visibility = View.GONE
            when (response) {
                is ChangeAddressResponse -> {
                    when (response.httpCode) {
                        HTTP_OK, HTTP_OK_201 -> {

                            if (response.deliverable == null) {
                                showErrorScreen(
                                    ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON,
                                    getString(R.string.common_error_message_without_contact_info)
                                )
                                return@observe
                            }

                            // If deliverable false then show cant deliver popup
                            // Don't allow user to navigate to Checkout page when deliverable : [false].
                            if (response.deliverable == false) {
                                showSuburbNotDeliverableBottomSheetDialog(
                                    ERROR_CODE_SUBURB_NOT_DELIVERABLE
                                )
                                return@observe
                            }

                            // Check if any unSellableCommerceItems[ ] > 0 display the items in modal as per the design
                            if (!response.unSellableCommerceItems.isNullOrEmpty()) {
                                navigateToUnsellableItemsFragment(
                                    response.unSellableCommerceItems,
                                    response.deliverable ?: false,
                                    SCREEN_NAME_EDIT_ADDRESS
                                )
                                return@observe
                            }

                            // else functionality complete.
                            setFragmentResult(
                                UPDATE_SAVED_ADDRESS_REQUEST_KEY, bundleOf(
                                    SAVED_ADDRESS_KEY to savedAddressResponse
                                )
                            )
                            navController?.navigateUp()
                            selectedAddressId = ""
                        }
                        else -> {
                            showErrorScreen(
                                ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON,
                                getString(R.string.common_error_message_without_contact_info)
                            )
                        }
                    }
                }
                is Throwable -> {
                    showErrorScreen(
                        ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON,
                        getString(R.string.common_error_message_without_contact_info)
                    )
                }
            }
        })
    }

    private fun isNickNameExist(): Boolean {
        var isExist = false
        if (!savedAddressResponse?.addresses.isNullOrEmpty() && selectedAddressId.isNullOrEmpty()) {
            for (address in savedAddressResponse?.addresses!!) {
                if (addressNicknameEditText.text.toString().equals(address.nickname, true)) {
                    showNickNameExist()
                    isExist = true
                }
            }
        }
        return isExist
    }

    fun showNickNameExist() {
        addressNicknameEditText?.setBackgroundResource(R.drawable.input_error_background)
        addressNicknameErrorMsg?.text = bindString(R.string.nick_name_exist_error_msg)
        showAnimationErrorMessage(addressNicknameErrorMsg, View.VISIBLE, 0)
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
        view?.findNavController()?.navigate(
            R.id.actionOpenErrorHandlerBottomSheetDialog,
            bundle
        )
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

    private fun navigateToAddressConfirmation(placesId: String?) {
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
        findNavController().navigate(
            R.id.action_checkoutAddAddressNewUserFragment_to_deliveryAddressConfirmationFragment,
            bundleOf(BUNDLE to baseFragBundle)
        )
    }

    private fun showErrorPhoneNumber() {
        cellphoneNumberEditText?.setBackgroundResource(R.drawable.input_error_background)
        cellphoneNumberErrorMsg?.visibility = View.VISIBLE
        cellphoneNumberErrorMsg?.text = bindString(R.string.phone_number_invalid_error_msg)
        showAnimationErrorMessage(
            cellphoneNumberErrorMsg,
            View.VISIBLE,
            recipientAddressLayout.y.toInt()
        )
    }


    private fun showErrorInputField(editText: EditText?, visible: Int) {
        editText?.setBackgroundResource(if (visible == View.VISIBLE) R.drawable.input_error_background else R.drawable.recipient_details_input_edittext_bg)
        if (editText != null) {
            when (editText?.id) {
                R.id.autoCompleteTextView -> {
                    showAnimationErrorMessage(autocompletePlaceErrorMsg, visible, 0)
                }
                R.id.addressNicknameEditText -> {
                    showAnimationErrorMessage(addressNicknameErrorMsg, visible, 0)
                }

                R.id.recipientNameEditText -> {
                    showAnimationErrorMessage(
                        recipientNameErrorMsg,
                        visible,
                        recipientAddressLayout.y.toInt()
                    )
                }
                R.id.cellphoneNumberEditText -> {
                    cellphoneNumberErrorMsg.text = bindString(R.string.mobile_number_error_msg)
                    showAnimationErrorMessage(
                        cellphoneNumberErrorMsg,
                        visible,
                        recipientAddressLayout.y.toInt()
                    )
                }
            }
        }
    }

    private fun showAnimationErrorMessage(
        textView: TextView,
        visible: Int,
        recipientLayoutValue: Int
    ) {
        textView?.visibility = visible
        if (View.VISIBLE == visible) {
            val anim = ObjectAnimator.ofInt(
                newUserNestedScrollView,
                "scrollY",
                recipientLayoutValue + textView.y.toInt()
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
}