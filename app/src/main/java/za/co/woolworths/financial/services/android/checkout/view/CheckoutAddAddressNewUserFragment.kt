package za.co.woolworths.financial.services.android.checkout.view

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.facebook.shimmer.Shimmer
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
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.ADD_A_NEW_ADDRESS_REQUEST_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.ADD_NEW_ADDRESS_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.DELETE_SAVED_ADDRESS_REQUEST_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.SAVED_ADDRESS_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.SAVED_ADDRESS_RESPONSE_KEY
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
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.EditDeliveryLocationFragment
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.UnsellableItemsFragment.Companion.KEY_ARGS_BUNDLE
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.UnsellableItemsFragment.Companion.KEY_ARGS_PROVINCE
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.UnsellableItemsFragment.Companion.KEY_ARGS_SUBURB
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.UnsellableItemsFragment.Companion.KEY_ARGS_UNSELLABLE_COMMERCE_ITEMS
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK_201
import java.net.HttpURLConnection.HTTP_OK
import java.util.*


/**
 * Created by Kunal Uttarwar on 29/05/21.
 */
class CheckoutAddAddressNewUserFragment : Fragment(), View.OnClickListener {

    private var deliveringOptionsList: List<String>? = null
    private var navController: NavController? = null
    private lateinit var listOfInputFields: List<View>
    var deliveryType: DeliveryType = DeliveryType.DELIVERY
    private var selectedDeliveryAddressType: String? = null
    private var selectedAddress = SelectedPlacesAddress()
    private var savedAddressResponse: SavedAddressResponse? = null
    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private var isShimmerRequired = true
    private var selectedAddressId = ""
    private var isAddNewAddress = false
    private var provinceSuburbEnableType: ProvinceSuburbType? = null

    companion object {
        const val PROVINCE_SELECTION_BACK_PRESSED = "5645"
        const val SUBURB_SELECTION_BACK_PRESSED = "5465"
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

        val bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            if (containsKey(EDIT_SAVED_ADDRESS_RESPONSE_KEY)) {
                //Edit new Address from delivery
                val editSavedAddress = getString(EDIT_SAVED_ADDRESS_RESPONSE_KEY)
                if (!editSavedAddress.isNullOrEmpty() && !editSavedAddress.equals("null", true)) {
                    savedAddressResponse = (Utils.jsonStringToObject(
                        editSavedAddress,
                        SavedAddressResponse::class.java
                    ) as? SavedAddressResponse)
                    val savedAddress =
                        savedAddressResponse?.addresses?.get(getInt(EDIT_ADDRESS_POSITION_KEY))
                    selectedAddressId = savedAddress?.id.toString()
                    selectedDeliveryAddressType = savedAddress?.addressType
                    if (savedAddress != null) {
                        selectedAddress.savedAddress = savedAddress
                    }
                    setHasOptionsMenu(true)
                    isShimmerRequired = false
                }
            } else if (containsKey(ADD_NEW_ADDRESS_KEY)) {
                //Add new Address from delivery.
                isAddNewAddress = getBoolean(ADD_NEW_ADDRESS_KEY)
                savedAddressResponse = (Utils.jsonStringToObject(
                    getString(SAVED_ADDRESS_RESPONSE_KEY),
                    SavedAddressResponse::class.java
                ) as? SavedAddressResponse)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_item, menu)
        if (selectedAddressId.isNotEmpty()) //show only if it is edit address screen
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
            selectSuburbLayout,
            selectProvinceLayout,
            postalCode,
            recipientNameEditText,
            cellphoneNumberEditText
        )
        setupViewModel()
        init()
        addFragmentResultListener()
        // Show prepopulate fields on edit address
        if (selectedAddressId.isNotEmpty()) {
            //it's not empty means it's a edit address call.
            if (activity is CheckoutActivity)
                (activity as CheckoutActivity).hideBackArrow()
            setTextFields()
        }
    }

    private fun setTextFields() {
        autoCompleteTextView?.setText(selectedAddress.savedAddress.address1)
        addressNicknameEditText.setText(selectedAddress.savedAddress.nickname)
        unitComplexFloorEditText.setText(selectedAddress.savedAddress.address2)
        suburbEditText.setText(selectedAddress.savedAddress.suburb)
        provinceAutocompleteEditText.setText(selectedAddress.savedAddress.city)
        cellphoneNumberEditText.setText(selectedAddress.savedAddress.primaryContactNo)
        recipientNameEditText.setText(selectedAddress.savedAddress.recipientName)
        if (selectedAddress.savedAddress.postalCode.isNullOrEmpty()) {
            enablePostalCode()
            postalCode.text.clear()
        } else
            postalCode.setText(selectedAddress.savedAddress.postalCode)
        selectedDeliveryAddressType = selectedAddress.savedAddress.addressType
    }

    private fun initView() {
        if (selectedAddressId.isNotEmpty()) {
            //it's not empty means it's a edit address call.
            if (savedAddressResponse?.defaultAddressNickname == arguments?.getBundle("bundle")
                    ?.getInt(EDIT_ADDRESS_POSITION_KEY)?.let {
                        savedAddressResponse?.addresses?.get(it)?.nickname
                    }
            ) {
                // Do Nothing
            } else if (savedAddressResponse?.addresses?.size!! > 1) {
                deleteTextView.visibility = View.VISIBLE
                deleteTextView.setOnClickListener(this)
            }
            saveAddress.text = bindString(R.string.change_details)
        }
        if (isShimmerRequired) {
            setUpShimmer()
            isShimmerRequired = false
        } else
            stopShimmer()
        if (activity is CheckoutActivity) {
            (activity as? CheckoutActivity)?.apply {
                showBackArrowWithoutTitle()
            }
        }
        saveAddress?.setOnClickListener(this)
        autoCompleteTextView?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        addressNicknameEditText?.apply {
            afterTextChanged {
                selectedAddress.savedAddress.nickname = it
                showErrorInputField(this, View.GONE)
            }
        }
        suburbEditText?.apply { afterTextChanged { suburbNameErrorMsg?.visibility = View.GONE } }
        provinceAutocompleteEditText?.apply {
            afterTextChanged {
                provinceNameErrorMsg?.visibility = View.GONE
            }
        }
        unitComplexFloorEditText?.apply {
            afterTextChanged {
                selectedAddress.savedAddress.address2 = it
            }
        }
        postalCode?.apply {
            afterTextChanged {
                if (it.isNotEmpty())
                    showErrorInputField(this, View.GONE)
            }
        }
        recipientNameEditText?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        cellphoneNumberEditText?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
    }

    private fun setUpShimmer() {
        val shimmer = Shimmer.AlphaHighlightBuilder().build()
        addressNicknameShimmerFrameLayout?.setShimmer(shimmer)
        unitComplexPlaceHolderShimmerFrameLayout.setShimmer(shimmer)
        suburbPlaceHolderShimmerFrameLayout.setShimmer(shimmer)
        provincePlaceHolderShimmerFrameLayout.setShimmer(shimmer)
        postalCodePlaceHolderShimmerFrameLayout.setShimmer(shimmer)
        addressNicknameEditTextShimmerFrameLayout.setShimmer(shimmer)
        unitComplexEditTextShimmerFrameLayout.setShimmer(shimmer)
        selectSuburbLayoutShimmerFrameLayout.setShimmer(shimmer)
        selectProvinceLayoutShimmerFrameLayout.setShimmer(shimmer)
        postalCodeShimmerFrameLayout.setShimmer(shimmer)
        startShimmer()
    }

    private fun startShimmer() {
        addressNicknameShimmerFrameLayout?.startShimmer()
        unitComplexPlaceHolderShimmerFrameLayout?.startShimmer()
        suburbPlaceHolderShimmerFrameLayout?.startShimmer()
        provincePlaceHolderShimmerFrameLayout?.startShimmer()
        postalCodePlaceHolderShimmerFrameLayout?.startShimmer()
        addressNicknameEditTextShimmerFrameLayout?.startShimmer()
        unitComplexEditTextShimmerFrameLayout?.startShimmer()
        selectSuburbLayoutShimmerFrameLayout?.startShimmer()
        selectProvinceLayoutShimmerFrameLayout?.startShimmer()
        postalCodeShimmerFrameLayout?.startShimmer()

        addressNicknamePlaceHolder.visibility = View.INVISIBLE
        unitComplexFloorPlaceHolder.visibility = View.INVISIBLE
        suburbPlaceHolder.visibility = View.INVISIBLE
        provincePlaceHolder.visibility = View.INVISIBLE
        postalCodePlaceHolder.visibility = View.INVISIBLE
        addressNicknameEditText.visibility = View.INVISIBLE
        unitComplexFloorEditText.visibility = View.INVISIBLE
        selectSuburbLayout.visibility = View.INVISIBLE
        selectProvinceLayout.visibility = View.INVISIBLE
        postalCode.visibility = View.INVISIBLE
    }

    private fun stopShimmer() {
        addressNicknameShimmerFrameLayout?.stopShimmer()
        unitComplexPlaceHolderShimmerFrameLayout?.stopShimmer()
        suburbPlaceHolderShimmerFrameLayout?.stopShimmer()
        provincePlaceHolderShimmerFrameLayout?.stopShimmer()
        postalCodePlaceHolderShimmerFrameLayout?.stopShimmer()
        addressNicknameEditTextShimmerFrameLayout?.stopShimmer()
        unitComplexEditTextShimmerFrameLayout?.stopShimmer()
        selectSuburbLayoutShimmerFrameLayout?.stopShimmer()
        selectProvinceLayoutShimmerFrameLayout?.stopShimmer()
        postalCodeShimmerFrameLayout?.stopShimmer()

        addressNicknameShimmerFrameLayout.setShimmer(null)
        unitComplexPlaceHolderShimmerFrameLayout.setShimmer(null)
        suburbPlaceHolderShimmerFrameLayout.setShimmer(null)
        provincePlaceHolderShimmerFrameLayout.setShimmer(null)
        postalCodePlaceHolderShimmerFrameLayout.setShimmer(null)
        addressNicknameEditTextShimmerFrameLayout.setShimmer(null)
        unitComplexEditTextShimmerFrameLayout.setShimmer(null)
        selectSuburbLayoutShimmerFrameLayout.setShimmer(null)
        selectProvinceLayoutShimmerFrameLayout.setShimmer(null)
        postalCodeShimmerFrameLayout.setShimmer(null)

        addressNicknamePlaceHolder.visibility = View.VISIBLE
        unitComplexFloorPlaceHolder.visibility = View.VISIBLE
        suburbPlaceHolder.visibility = View.VISIBLE
        provincePlaceHolder.visibility = View.VISIBLE
        postalCodePlaceHolder.visibility = View.VISIBLE
        addressNicknameEditText.visibility = View.VISIBLE
        unitComplexFloorEditText.visibility = View.VISIBLE
        selectSuburbLayout.visibility = View.VISIBLE
        selectProvinceLayout.visibility = View.VISIBLE
        postalCode.visibility = View.VISIBLE
    }

    private fun setupViewModel() {
        checkoutAddAddressNewUserViewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(
                CheckoutAddAddressNewUserInteractor(
                    CheckoutAddAddressNewUserApiHelper(),
                    CheckoutMockApiHelper()
                )
            )
        ).get(CheckoutAddAddressNewUserViewModel::class.java)
    }

    private fun init() {
        if (selectedAddress.savedAddress.postalCode.isNullOrEmpty()) {
            enablePostalCode()
        }
        deliveringOptionsList = WoolworthsApplication.getNativeCheckout()?.addressTypes
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
                    stopShimmer()
                    val request =
                        placeFields.let { FetchPlaceRequest.builder(placeId, it).build() }
                    request.let { placeRequest ->
                        placesClient.fetchPlace(placeRequest)
                            .addOnSuccessListener { response ->
                                val place = response!!.place
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
        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResultListener(EditDeliveryLocationFragment.SUBURB_SELECTOR_REQUEST_CODE) { _, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            val result = bundle.getString("Suburb")
            val suburb: Suburb? = Utils.strToJson(result, Suburb::class.java) as? Suburb
            onSuburbSelected(suburb)
        }
        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResultListener(EditDeliveryLocationFragment.PROVINCE_SELECTOR_REQUEST_CODE) { _, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            val result = bundle.getString("Province")
            val province: Province? = Utils.strToJson(result, Province::class.java) as? Province
            onProvinceSelected(province)
        }

        setFragmentResultListener(PROVINCE_SELECTION_BACK_PRESSED) { _, _ ->
            enableDisableEditText()
        }
        setFragmentResultListener(SUBURB_SELECTION_BACK_PRESSED) { _, _ ->
            enableDisableEditText()
        }

        setFragmentResultListener(RESULT_ERROR_CODE_SUBURB_NOT_FOUND) { _, _ ->
            if (selectedAddress.savedAddress.city.isNullOrEmpty()) return@setFragmentResultListener
            provinceSuburbEnableType = ONLY_SUBURB
            enableDisableEditText()
            getSuburbs()
        }
        setFragmentResultListener(RESULT_ERROR_CODE_RETRY) { _, bundle ->
            when (bundle.getInt("bundle")) {
                ERROR_TYPE_ADD_ADDRESS -> {
                    onSaveAddressClicked()
                }
                ERROR_TYPE_DELETE_ADDRESS -> {
                    deleteAddress()
                }
            }

        }

        setFragmentResultListener(UNSELLABLE_CHANGE_STORE_REQUEST_KEY) { _, _ ->
            savedAddressResponse?.defaultAddressNickname = selectedAddress.savedAddress.nickname
            view?.findNavController()?.navigate(
                R.id.action_CheckoutAddAddressNewUserFragment_to_CheckoutAddAddressReturningUserFragment,
                bundleOf(
                    SAVED_ADDRESS_KEY to savedAddressResponse
                )
            )
        }
    }

    private fun onProvinceSelected(province: Province?) {
        selectedAddress.savedAddress.apply {
            city = province?.name.toString()
            region = province?.id.toString()
        }
        enableDisableEditText()
        provinceAutocompleteEditText?.setText(province?.name)
    }

    private fun resetSuburbSelection() {
        selectedAddress.savedAddress.apply {
            suburb = ""
            suburbId = ""
        }
        selectedAddress.store = ""
        selectedAddress.storeId = ""
        suburbEditText.text.clear()
    }

    private fun onSuburbSelected(onSelectedSuburb: Suburb?) {
        if (deliveryType == DeliveryType.DELIVERY) {
            selectedAddress.savedAddress.apply {
                suburb = onSelectedSuburb?.name.toString()
                suburbId = onSelectedSuburb?.id.toString()
            }
        } else {
            selectedAddress.store = onSelectedSuburb?.name.toString()
            selectedAddress.storeId = onSelectedSuburb?.id.toString()
        }
        selectedAddress.savedAddress.postalCode = onSelectedSuburb?.postalCode.toString()
        enableDisableEditText()
        suburbEditText?.setText(onSelectedSuburb?.name)
        if (onSelectedSuburb?.postalCode.isNullOrEmpty()) {
            enablePostalCode()
            postalCode.text.clear()
        } else {
            postalCode?.setText(onSelectedSuburb?.postalCode)
            if (postalCode.text.isNotEmpty()) {
                disablePostalCode()
            } else
                enablePostalCode()
        }
    }

    private fun setAddress(place: Place) {
        provinceSuburbEnableType = null
        var addressText1 = ""
        var addressText2 = ""
        for (address in place.addressComponents?.asList()!!) {
            when (address.types[0]) {
                STREET_NUMBER.value -> addressText1 = address.name
                ROUTE.value -> addressText2 = address.name
                ADMINISTRATIVE_AREA_LEVEL_1.value -> {
                    selectedAddress.savedAddress.city = address.name
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

                LOCALITY.value -> selectedAddress.savedAddress.city = address.name

            }
        }
        if (!selectedAddress.savedAddress.city.isNullOrEmpty() && !selectedAddress.savedAddress.suburb.isNullOrEmpty())
            selectedAddress.savedAddress.region = ""
        selectedAddress.savedAddress.apply {
            address1 = addressText1.plus(" ").plus(addressText2)
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
            setText(selectedAddress.savedAddress.address1)
            setSelection(autoCompleteTextView.length())
            autoCompleteTextView.dismissDropDown()
        }
        checkIfSelectedProvinceExist(WoolworthsApplication.getNativeCheckout()?.regions as MutableList<Province>)
    }

    private fun checkIfSelectedProvinceExist(provinceList: MutableList<Province>) {
        val localProvince = Province()
        val provinceName = selectedAddress.savedAddress.city
        if (!provinceName.isNullOrEmpty()) {
            for (provinces in provinceList) {
                if (provinceName.equals(provinces.name)) {
                    // province name is matching with the province list from config.
                    localProvince.apply {
                        id = provinces.id
                        name = provinces.name
                    }
                    disableProvinceSelection()
                    provinceAutocompleteEditText.setText(provinceName)
                    selectedAddress.savedAddress.apply {
                        city = localProvince.name
                        region = localProvince.id
                    }
                }
            }
            if (localProvince.name.isNullOrEmpty()) {
                // province name is not matching with the province list from config.
                provinceAutocompleteEditText.setText("")
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
                if (selectedAddress.savedAddress.city.isNullOrEmpty()) BOTH else ONLY_SUBURB
        } else {
            suburbEditText.setText(selectedAddress.savedAddress.suburb)
            disableSuburbSelection()
        }
        enableDisableEditText()
        when (selectedAddress.savedAddress.postalCode.isNullOrEmpty()) {
            true -> {
                enablePostalCode()
                postalCode.text.clear()
            }
            false -> {
                postalCode.setText(selectedAddress.savedAddress.postalCode)
                if (postalCode.text.isNotEmpty()) {
                    disablePostalCode()
                } else
                    enablePostalCode()
            }
        }
    }

    private fun enableProvinceSelection() {
        selectProvinceLayout?.isClickable = true
        provinceAutocompleteEditText?.isClickable = true
        selectProvinceLayout?.isEnabled = true
        provinceAutocompleteEditText?.isEnabled = true
        selectProvinceLayout?.setOnClickListener(this)
        provinceAutocompleteEditText?.setOnClickListener(this)
        dropdownGetProvincesImg.visibility = View.VISIBLE
        selectProvinceLayout.setBackgroundResource(if (provinceNameErrorMsg.visibility == View.VISIBLE) R.drawable.input_error_background else R.drawable.input_box_active_bg)
        provinceAutocompleteEditText.setBackgroundResource(if (provinceNameErrorMsg.visibility == View.VISIBLE) R.drawable.input_box_half_error_bg else R.drawable.input_box_autocomplete_edit_text)
    }

    private fun enableSuburbSelection() {
        selectSuburbLayout?.isClickable = true
        suburbEditText?.isClickable = true
        selectSuburbLayout?.isEnabled = true
        suburbEditText?.isEnabled = true
        selectSuburbLayout?.setOnClickListener(this)
        suburbEditText?.setOnClickListener(this)
        dropdownGetSuburbImg.visibility = View.VISIBLE
        selectSuburbLayout.setBackgroundResource(if (suburbNameErrorMsg.visibility == View.VISIBLE) R.drawable.input_error_background else R.drawable.input_box_active_bg)
        suburbEditText.setBackgroundResource(if (suburbNameErrorMsg.visibility == View.VISIBLE) R.drawable.input_box_half_error_bg else R.drawable.input_box_autocomplete_edit_text)
    }

    private fun disableProvinceSelection() {
        selectProvinceLayout?.isClickable = false
        provinceAutocompleteEditText?.isClickable = false
        selectProvinceLayout?.isEnabled = false
        provinceAutocompleteEditText?.isEnabled = false
        dropdownGetProvincesImg.visibility = View.GONE
        selectProvinceLayout.setBackgroundResource(if (provinceNameErrorMsg.visibility == View.VISIBLE) R.drawable.input_error_background else R.drawable.input_non_editable_edit_text)
        provinceAutocompleteEditText.setBackgroundResource(if (provinceNameErrorMsg.visibility == View.VISIBLE) R.drawable.input_box_half_error_bg else R.drawable.input_non_editable_half_edit_text)
    }

    private fun disableSuburbSelection() {
        selectSuburbLayout?.isClickable = false
        suburbEditText?.isClickable = false
        selectSuburbLayout?.isEnabled = false
        suburbEditText?.isEnabled = false
        dropdownGetSuburbImg.visibility = View.GONE
        selectSuburbLayout.setBackgroundResource(if (provinceNameErrorMsg.visibility == View.VISIBLE) R.drawable.input_error_background else R.drawable.input_non_editable_edit_text)
        suburbEditText.setBackgroundResource(if (provinceNameErrorMsg.visibility == View.VISIBLE) R.drawable.input_box_half_error_bg else R.drawable.input_non_editable_half_edit_text)
    }

    private fun enablePostalCode() {
        postalCode.setBackgroundResource(if (postalCodeTextErrorMsg.visibility == View.VISIBLE) R.drawable.input_error_background else R.drawable.recipient_details_input_edittext_bg)
        postalCode.isClickable = true
        postalCode.isEnabled = true
    }

    private fun disablePostalCode() {
        postalCode.setBackgroundResource(R.drawable.input_box_inactive_bg)
        postalCode.isClickable = false
        postalCode.isEnabled = false
    }

    private fun navigateToProvinceSelection() {
        showGetProvincesProgress()
        val bundle = Bundle()
        bundle.apply {
            putString(
                "ProvinceList",
                Utils.toJson(WoolworthsApplication.getNativeCheckout()?.regions as? MutableList<Province>)
            )
        }
        navController?.navigate(
            R.id.action_to_provinceSelectorFragment,
            bundleOf("bundle" to bundle)
        )
        hideGetProvincesProgress()
    }

    private fun showWhereAreWeDeliveringView() {
        for ((index, options) in deliveringOptionsList!!.withIndex()) {
            val view = View.inflate(context, R.layout.where_are_we_delivering_items, null)
            val titleTextView: TextView? = view?.findViewById(R.id.titleTv)
            titleTextView?.tag = index
            titleTextView?.text = options
            if (!selectedDeliveryAddressType.isNullOrEmpty() && selectedDeliveryAddressType.equals(
                    options
                )
            ) {
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
                resetOtherDeliveringTitle(it.tag as Int)
                selectedDeliveryAddressType = (it as TextView).text as? String
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
        if (progressbarGetProvinces.visibility == View.VISIBLE || progressbarGetSuburb.visibility == View.VISIBLE) return
        when (v?.id) {
            R.id.saveAddress -> {
                onSaveAddressClicked()
            }
            R.id.selectSuburbLayout, R.id.suburbEditText -> {
                if (selectedAddress.savedAddress.city.isNullOrEmpty()) return
                getSuburbs()
            }
            R.id.selectProvinceLayout, R.id.provinceAutocompleteEditText -> {
                navigateToProvinceSelection()
            }
            R.id.deleteTextView -> {
                if (savedAddressResponse?.addresses?.size!! > 1)
                    deleteAddress()
            }
        }
    }

    private fun deleteAddress() {
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

    private fun getSuburbs() {
        if (progressbarGetProvinces?.visibility == View.VISIBLE) return
        selectedAddress.savedAddress.region?.let { provinceId ->
            checkoutAddAddressNewUserViewModel.initGetSuburbs(provinceId)
                .observe(viewLifecycleOwner, {
                    when (it.responseStatus) {
                        ResponseStatus.SUCCESS -> {
                            hideSetSuburbProgressBar()
                            if ((it?.data as? SuburbsResponse)?.suburbs.isNullOrEmpty()) {
                                //showNoStoresError()
                            } else {
                                (it?.data as? SuburbsResponse)?.suburbs?.let { it1 ->
                                    navigateToSuburbSelection(
                                        it1
                                    )
                                }
                            }
                        }
                        ResponseStatus.LOADING -> {
                            showGetSuburbProgress()
                        }
                        ResponseStatus.ERROR -> {
                            hideSetSuburbProgressBar()
                        }
                    }
                })
        }
    }

    private fun navigateToSuburbSelection(suburbs: List<Suburb>) {
        activity?.let {
            // TODO:: WOP-9342 - Handle Transaction too large exception android nougat
            //  and remove share preference temp fix
            val sharedPreferences = it.getSharedPreferences(
                EditDeliveryLocationFragment.SHARED_PREFS,
                Context.MODE_PRIVATE
            )
            val editor = sharedPreferences?.edit()
            editor?.putString(EditDeliveryLocationFragment.SUBURB_LIST, Utils.toJson(suburbs))
            editor?.apply()
            val bundle = Bundle()
            bundle.apply {
                putString("SuburbList", Utils.toJson(suburbs))
                putSerializable("deliveryType", deliveryType)
            }
            navController?.navigate(
                R.id.action_to_suburbSelectorFragment,
                bundleOf("bundle" to bundle)
            )
        }
    }

    private fun showGetSuburbProgress() {
        dropdownGetSuburbImg?.visibility = View.INVISIBLE
        progressbarGetSuburb?.visibility = View.VISIBLE
    }

    private fun hideSetSuburbProgressBar() {
        progressbarGetSuburb?.visibility = View.INVISIBLE
        dropdownGetSuburbImg?.visibility = View.VISIBLE
    }

    private fun hideGetProvincesProgress() {
        progressbarGetProvinces?.visibility = View.INVISIBLE
        dropdownGetProvincesImg?.visibility = View.VISIBLE
    }

    private fun showGetProvincesProgress() {
        dropdownGetProvincesImg?.visibility = View.INVISIBLE
        progressbarGetProvinces?.visibility = View.VISIBLE
    }

    private fun onSaveAddressClicked() {

        if (cellphoneNumberEditText?.text.toString().trim().isNotEmpty()
            && cellphoneNumberEditText?.text.toString().trim().length < 10
        ) {
            showErrorPhoneNumber()
        }
        if (autoCompleteTextView?.text.toString().trim()
                .isNotEmpty() && addressNicknameEditText?.text.toString().trim()
                .isNotEmpty() && suburbEditText?.text.toString().trim()
                .isNotEmpty() && provinceAutocompleteEditText?.text.toString().trim()
                .isNotEmpty() && postalCode?.text.toString().trim()
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
                                    if (savedAddressResponse != null && response != null)
                                        savedAddressResponse?.addresses?.add(response.address)
                                    response.address.nickname?.let { nickName ->
                                        onAddNewAddress(
                                            nickName
                                        )
                                    }
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
            if (selectedDeliveryAddressType == null) {
                deliveringAddressTypesErrorMsg.visibility = View.VISIBLE
            }
            listOfInputFields.forEach {
                if (it is RelativeLayout) {
                    if (it.id == R.id.selectSuburbLayout && suburbEditText?.text.toString().trim()
                            .isEmpty()
                    ) {
                        showErrorSuburbOrProvince(it)
                    }
                    if (it.id == R.id.selectProvinceLayout && provinceAutocompleteEditText?.text.toString()
                            .trim().isEmpty()
                    ) {
                        showErrorSuburbOrProvince(it)
                    }
                }
                if (it is EditText) {
                    if (it.text.toString().trim().isEmpty())
                        showErrorInputField(it, View.VISIBLE)
                }
            }
        }
    }

    private fun addAddressErrorResponse(response: AddAddressResponse, errorMessage: Int) {
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

    /**
     * This function should perform following tasks:
     * On successful add address,
     * - request the API  GET [{base_url}/changeAddress/{nickname}]. Use [@param nickName] of newly added address
     * - If Change Address comes back with deliverable: [false] then Display an info dialog
     *  with message “we don't deliver to this suburb, please add a different address “ as per design.
     * - Don't allow user to navigate to Checkout page when deliverable : [false].
     * - Check if any unSellableCommerceItems[ ] > 0 display the items in modal as per the design
     * ( Reference :  We use  similar kind of functionality on edit delivery location when user switches
     * from delivery to CNC or vice versa).
     * - User selects REMOVE AND CONTINUE navigate to Checkout page
     *
     * @param nickName  unique address name used to identify individual address
     */
    private fun onAddNewAddress(@NonNull nickName: String) {
        loadingProgressBar.visibility = View.VISIBLE
        checkoutAddAddressNewUserViewModel.changeAddress(
            nickName
        ).observe(viewLifecycleOwner, { response ->
            loadingProgressBar.visibility = View.GONE
            when (response) {
                is ChangeAddressResponse -> {
                    when (response.httpCode) {
                        HTTP_OK, HTTP_OK_201 -> {
                            // If deliverable false then show cant deliver popup
                            // Don't allow user to navigate to Checkout page when deliverable : [false].
                            if (!response.deliverable) {
                                showSuburbNotDeliverableBottomSheetDialog(
                                    ERROR_CODE_SUBURB_NOT_DELIVERABLE
                                )
                                return@observe
                            }

                            // Check if any unSellableCommerceItems[ ] > 0 display the items in modal as per the design
                            if (!response.unSellableCommerceItems.isNullOrEmpty()) {
                                navigateToUnsellableItemsFragment(
                                    response.unSellableCommerceItems,
                                    response.deliverable
                                )
                                return@observe
                            }

                            // else functionality complete.
                            if (isAddNewAddress) {
                                setFragmentResult(
                                    ADD_A_NEW_ADDRESS_REQUEST_KEY, bundleOf(
                                        SAVED_ADDRESS_KEY to savedAddressResponse
                                    )
                                )
                                navController?.navigateUp()
                            } else
                                navigateToAddressConfirmation()
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

    private fun showErrorScreen(errorType: Int, errorMessage: String?) {
        activity?.apply {
            val intent = Intent(this, ErrorHandlerActivity::class.java)
            intent.putExtra("errorType", errorType)
            intent.putExtra("errorMessage", errorMessage)
            startActivityForResult(intent, ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE)
        }
    }

    private fun isNickNameAlreadyExist(response: AddAddressResponse): Boolean {
        if (!response.validationErrors.isNullOrEmpty()) {
            for (errorsFields in response.validationErrors) {
                if (errorsFields.getField() == "nickname") {
                    return true
                }
            }
        }
        return false
    }

    private fun showSuburbNotDeliverableBottomSheetDialog(errorCode: String?) {
        view?.findNavController()?.navigate(
            R.id.action_CheckoutAddAddressNewUserFragment_to_suburbNotDeliverableBottomsheetDialogFragment,
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
        deliverable: Boolean
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
            name = selectedAddress.savedAddress.city
            id = selectedAddress.savedAddress.region
        }

        navController?.navigate(
            R.id.action_to_unsellableItemsFragment,
            bundleOf(
                KEY_ARGS_BUNDLE to bundleOf(
                    SAVED_ADDRESS_KEY to savedAddressResponse,
                    EditDeliveryLocationActivity.DELIVERY_TYPE to DeliveryType.DELIVERY.name,
                    KEY_ARGS_SUBURB to Utils.toJson(suburb),
                    KEY_ARGS_PROVINCE to Utils.toJson(province),
                    KEY_ARGS_UNSELLABLE_COMMERCE_ITEMS to Utils.toJson(unSellableCommerceItems)
                )
            )
        )
    }

    private fun getAddAddressRequestBody(): AddAddressRequestBody {
        return AddAddressRequestBody(
            addressNicknameEditText?.text.toString().trim(),
            recipientNameEditText?.text.toString().trim(),
            autoCompleteTextView?.text.toString().trim(),
            unitComplexFloorEditText?.text.toString().trim(),
            postalCode?.text.toString().trim(),
            cellphoneNumberEditText?.text.toString().trim(),
            "",
            selectedAddress.savedAddress.region ?: "",
            selectedAddress.savedAddress.suburbId ?: "",
            selectedAddress.savedAddress.city ?: "",
            suburbEditText?.text.toString(),
            "",
            false,
            selectedAddress.savedAddress.latitude,
            selectedAddress.savedAddress.longitude,
            selectedAddress.savedAddress.placesId ?: "",
            selectedDeliveryAddressType.toString()
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
                                    arguments?.getBundle("bundle")
                                        ?.getInt(EDIT_ADDRESS_POSITION_KEY)
                                        ?.let { position ->
                                            (savedAddressResponse?.addresses as? MutableList<Address>)?.removeAt(
                                                position
                                            )
                                            response.address?.let { address ->
                                                (savedAddressResponse?.addresses as? MutableList<Address>)?.add(
                                                    position, address
                                                )
                                            }
                                        }
                                    setFragmentResult(
                                        UPDATE_SAVED_ADDRESS_REQUEST_KEY, bundleOf(
                                            SAVED_ADDRESS_KEY to savedAddressResponse
                                        )
                                    )
                                    navController?.navigateUp()
                                    selectedAddressId = ""
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

    private fun showNickNameExist() {
        addressNicknameEditText.setBackgroundResource(R.drawable.input_error_background)
        addressNicknameErrorMsg?.visibility = View.VISIBLE
        addressNicknameErrorMsg.text = bindString(R.string.nick_name_exist_error_msg)
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
            R.id.action_CheckoutAddAddressNewUserFragment_to_ErrorHandlerBottomSheetDialog,
            bundle
        )
    }

    private fun navigateToAddressConfirmation() {
        navController?.navigate(
            R.id.action_CheckoutAddAddressNewUserFragment_to_checkoutAddressConfirmationFragment,
            bundleOf(SAVED_ADDRESS_KEY to savedAddressResponse)
        )
    }

    private fun showErrorPhoneNumber() {
        cellphoneNumberErrorMsg?.visibility = View.VISIBLE
        cellphoneNumberErrorMsg.text = bindString(R.string.phone_number_invalid_error_msg)
        showAnimationErrorMessage(
            cellphoneNumberErrorMsg,
            View.VISIBLE,
            recipientAddressLayout.y.toInt()
        )
    }

    private fun showErrorSuburbOrProvince(relativeLayout: RelativeLayout) {
        relativeLayout.setBackgroundResource(R.drawable.input_error_background)
        when (relativeLayout.id) {
            R.id.selectSuburbLayout -> {
                suburbNameErrorMsg.visibility = View.VISIBLE
                suburbEditText.setBackgroundResource(R.drawable.input_box_half_error_bg)
                selectSuburbLayout.setBackgroundResource(R.drawable.input_error_background)
            }
            R.id.selectProvinceLayout -> {
                provinceNameErrorMsg.visibility = View.VISIBLE
                provinceAutocompleteEditText.setBackgroundResource(R.drawable.input_box_half_error_bg)
                selectProvinceLayout.setBackgroundResource(R.drawable.input_error_background)
            }
        }
    }

    private fun enableDisableEditText() {
        when (provinceSuburbEnableType) {
            ONLY_PROVINCE -> enableProvinceSelection()
            ONLY_SUBURB -> enableSuburbSelection()
            BOTH -> {
                enableProvinceSelection()
                enableSuburbSelection()
            }
        }
    }

    private fun showErrorInputField(editText: EditText, visible: Int) {
        editText.setBackgroundResource(if (visible == View.VISIBLE) R.drawable.input_error_background else R.drawable.recipient_details_input_edittext_bg)
        when (editText.id) {
            R.id.autoCompleteTextView -> {
                showAnimationErrorMessage(autocompletePlaceErrorMsg, visible, 0)
            }
            R.id.addressNicknameEditText -> {
                showAnimationErrorMessage(addressNicknameErrorMsg, visible, 0)
            }
            R.id.suburbEditText -> {
                showAnimationErrorMessage(suburbNameErrorMsg, visible, 0)
            }
            R.id.provinceAutocompleteEditText -> {
                showAnimationErrorMessage(provinceNameErrorMsg, visible, 0)
            }
            R.id.postalCode -> {
                showAnimationErrorMessage(postalCodeTextErrorMsg, visible, 0)
                editText.setBackgroundResource(if (visible == View.VISIBLE) R.drawable.input_error_non_editable_background else R.drawable.input_non_editable_edit_text)
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
    fun testSetViewModelInstance(viewModel: CheckoutAddAddressNewUserViewModel) {
        checkoutAddAddressNewUserViewModel = viewModel
    }

    @VisibleForTesting
    fun testSetBundleArguments(bundle: Bundle) {
        arguments?.putBundle("bundle", bundle)
    }
}