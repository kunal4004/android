package za.co.woolworths.financial.services.android.checkout.view

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
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
import za.co.woolworths.financial.services.android.checkout.service.network.AddAddressRequestBody
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutAddAddressNewUserApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutMockApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.adapter.GooglePlacesAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.PlaceAutocomplete
import za.co.woolworths.financial.services.android.checkout.viewmodel.AddressComponentEnum.*
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.SelectedPlacesAddress
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.models.JWTDecodedModel
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.EditDeliveryLocationFragment
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.ProvinceSelectorFragment
import za.co.woolworths.financial.services.android.util.AuthenticateUtils
import za.co.woolworths.financial.services.android.util.DeliveryType
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import java.util.*


/**
 * Created by Kunal Uttarwar on 29/05/21.
 */
class CheckoutAddAddressNewUserFragment : Fragment(), View.OnClickListener {

    private var deliveringOptionsList: List<String>? = null
    private var provinceList: List<Province>? = null
    private var navController: NavController? = null
    private lateinit var listOfInputFields: List<View>
    var deliveryType: DeliveryType = DeliveryType.DELIVERY
    private var selectedDeliveryAddressType: String? = null
    var selectedAddress = SelectedPlacesAddress()
    private var savedAddressResponse: SavedAddressResponse? = null
    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private var isShimmerRequired = true

    companion object {
        const val PROVINCE_SELECTION_BACK_PRESSED = "5645"
        const val SUBURB_SELECTION_BACK_PRESSED = "5465"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.checkout_add_address_new_user, container, false)
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
            recipientName,
            cellphoneNumber
        )
        setupViewModel()
        init()
        addFragmentResultListener()
    }

    private fun initView() {
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
        addressNicknameEditText?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        suburbEditText?.apply { afterTextChanged { suburbNameErrorMsg?.visibility = View.GONE } }
        provinceAutocompleteEditText?.apply {
            afterTextChanged {
                provinceNameErrorMsg?.visibility = View.GONE
            }
        }
        postalCode?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        recipientName?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        cellphoneNumber?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
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
        if (recipientName?.text.toString().isEmpty()) {
            val jwtDecoded: JWTDecodedModel? = SessionUtilities.getInstance().jwt
            val name = jwtDecoded?.name?.get(0) ?: ""
            recipientName.setText(name)
        }
        if (savedAddressResponse?.addresses.isNullOrEmpty()) {
            getSavedAddresses()
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

    private fun getSavedAddresses() {
        checkoutAddAddressNewUserViewModel.getSavedAddresses().observe(viewLifecycleOwner, {
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    savedAddressResponse = it.data
                    if (cellphoneNumber?.text.toString().isEmpty())
                        cellphoneNumber.setText(savedAddressResponse?.primaryContactNo)
                }
                ResponseStatus.LOADING -> {

                }
                ResponseStatus.ERROR -> {

                }
            }
        })
    }

    private fun addFragmentResultListener() {
        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResultListener(EditDeliveryLocationFragment.SUBURB_SELECTOR_REQUEST_CODE) { requestKey, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            val result = bundle.getString("Suburb")
            val suburb: Suburb? = Utils.strToJson(result, Suburb::class.java) as? Suburb
            onSuburbSelected(suburb)
        }
        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResultListener(EditDeliveryLocationFragment.PROVINCE_SELECTOR_REQUEST_CODE) { requestKey, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            val result = bundle.getString("Province")
            val province: Province? = Utils.strToJson(result, Province::class.java) as? Province
            onProvinceSelected(province)
        }

        setFragmentResultListener(PROVINCE_SELECTION_BACK_PRESSED) { requestKey, bundle ->
            enableSuburbSelection()
            enableProvinceSelection()
        }
        setFragmentResultListener(SUBURB_SELECTION_BACK_PRESSED) { requestKey, bundle ->
            enableSuburbSelection()
            enableProvinceSelection()
        }
    }

    private fun onProvinceSelected(province: Province?) {
        selectedAddress.province = province?.name.toString()
        selectedAddress.region = province?.id.toString()
        enableProvinceSelection()
        enableSuburbSelection()
        resetSuburbSelection()
        provinceAutocompleteEditText?.setText(province?.name)
    }

    private fun resetSuburbSelection() {
        selectedAddress.suburb = ""
        selectedAddress.suburbId = ""
        selectedAddress.store = ""
        selectedAddress.storeId = ""
        provinceAutocompleteEditText.text.clear()
        suburbEditText.text.clear()
    }

    private fun onSuburbSelected(suburb: Suburb?) {
        if (deliveryType == DeliveryType.DELIVERY) {
            selectedAddress.suburb = suburb?.name.toString()
            selectedAddress.suburbId = suburb?.id.toString()
        } else {
            selectedAddress.store = suburb?.name.toString()
            selectedAddress.storeId = suburb?.id.toString()
        }
        selectedAddress.postalCode = suburb?.postalCode.toString()
        enableSuburbSelection()
        enableProvinceSelection()
        suburbEditText?.setText(suburb?.name)
        postalCode?.setText(suburb?.postalCode)
    }

    private fun setAddress(place: Place) {
        var addressText1 = ""
        var addressText2 = ""
        for (address in place.addressComponents?.asList()!!) {
            when (address.types[0]) {
                STREET_NUMBER.value -> addressText1 = address.name
                ROUTE.value -> addressText2 = address.name
                ADMINISTRATIVE_AREA_LEVEL_1.value -> {
                    selectedAddress.province = address.name
                    if (selectedAddress.province.isNotEmpty())
                        selectedAddress.region = ""
                }
                POSTAL_CODE.value -> selectedAddress.postalCode = address.name
                SUBLOCALITY_LEVEL_1.value -> {
                    if (address.name.isNotEmpty())
                        selectedAddress.suburb = address.name
                }
                SUBLOCALITY_LEVEL_2.value -> {
                    if (selectedAddress.suburb.isEmpty())
                        selectedAddress.suburb = address.name
                }

                LOCALITY.value -> selectedAddress.city = address.name

            }
        }
        selectedAddress.address1 = addressText1.plus(" ").plus(addressText2)
        selectedAddress.latitude = place.latLng?.latitude
        selectedAddress.longitude = place.latLng?.longitude

        if (selectedAddress.suburb.isNotEmpty())
            selectedAddress.suburbId = ""
        if (selectedAddress.postalCode.isEmpty()) {
            //If Google places failed to give postal code.
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses =
                geocoder.getFromLocation(selectedAddress.latitude!!, selectedAddress.longitude!!, 1)
            selectedAddress.postalCode = addresses[0].postalCode
        }

        autoCompleteTextView.apply {
            setText(selectedAddress.address1)
            setSelection(autoCompleteTextView.length())
            autoCompleteTextView.dismissDropDown()
        }
        getProvince()
    }

    private fun getProvince() {
        checkoutAddAddressNewUserViewModel.initGetProvince().observe(viewLifecycleOwner, {
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    if (it?.data?.regions.isNullOrEmpty()) {
                        //showNoStoresError()
                    } else {
                        it?.data?.regions?.let { it1 ->
                            provinceList = it1
                            checkIfSelectedProvinceExist(it1)
                        }
                    }
                }
                ResponseStatus.LOADING -> {
                }
                ResponseStatus.ERROR -> {
                }
            }
        })
    }

    private fun checkIfSelectedProvinceExist(provinceList: MutableList<Province>) {
        val province = Province()
        val provinceName = selectedAddress.province
        if (provinceName.isNotEmpty()) {
            for (provinces in provinceList) {
                if (provinceName.equals(provinces.name)) {
                    province.id = ""
                    province.name = provinces.name
                }
            }
            selectProvinceLayout.setBackgroundResource(R.drawable.input_non_editable_edit_text)
            provinceAutocompleteEditText.setBackgroundResource(R.drawable.input_non_editable_half_edit_text)
            selectedAddress.province = province.name
            selectedAddress.region = province.id

            if (selectedAddress.suburb.isEmpty())
                enableSuburbSelection()
            else {
                selectSuburbLayout.setBackgroundResource(R.drawable.input_non_editable_edit_text)
                suburbEditText.setBackgroundResource(R.drawable.input_non_editable_half_edit_text)
            }
        } else {
            enableProvinceSelection()
            enableSuburbSelection()
        }
        suburbEditText.setText(if (provinceName.isEmpty()) "" else selectedAddress.suburb)
        provinceAutocompleteEditText.setText(provinceName)
        postalCode.setText(selectedAddress.postalCode)
    }

    private fun enableProvinceSelection() {
        selectProvinceLayout?.setOnClickListener(this)
        provinceAutocompleteEditText?.setOnClickListener(this)
        dropdownGetProvincesImg.visibility = View.VISIBLE
        selectProvinceLayout.setBackgroundResource(if (provinceNameErrorMsg.visibility == View.VISIBLE) R.drawable.input_error_background else R.drawable.input_box_inactive_bg)
        provinceAutocompleteEditText.setBackgroundResource(if (provinceNameErrorMsg.visibility == View.VISIBLE) R.drawable.input_box_half_error_bg else R.drawable.input_box_autocomplete_edit_text)
    }

    private fun enableSuburbSelection() {
        selectSuburbLayout?.setOnClickListener(this)
        suburbEditText?.setOnClickListener(this)
        dropdownGetSuburbImg.visibility = View.VISIBLE
        selectSuburbLayout.setBackgroundResource(if (suburbNameErrorMsg.visibility == View.VISIBLE) R.drawable.input_error_background else R.drawable.input_box_inactive_bg)
        suburbEditText.setBackgroundResource(if (suburbNameErrorMsg.visibility == View.VISIBLE) R.drawable.input_box_half_error_bg else R.drawable.input_box_autocomplete_edit_text)
    }

    private fun navigateToProvinceSelection() {
        showGetProvincesProgress()
        val bundle = Bundle()
        bundle.apply {
            putString("ProvinceList", Utils.toJson(provinceList))
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
                selectedDeliveryAddressType = (it as TextView).text as String
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

    fun resetOtherDeliveringTitle(selectedTag: Int) {
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
                if (selectedAddress.province.isEmpty()) return
                getSuburbs()
            }
            R.id.selectProvinceLayout, R.id.provinceAutocompleteEditText -> {
                navigateToProvinceSelection()
            }
        }
    }

    private fun getSuburbs() {
        if (progressbarGetProvinces?.visibility == View.VISIBLE) return
        checkoutAddAddressNewUserViewModel.initGetSuburbs(selectedAddress.region).observe(this, {
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    hideSetSuburbProgressBar()
                    if (it?.data?.suburbs.isNullOrEmpty()) {
                        //showNoStoresError()
                    } else {
                        it?.data?.suburbs?.let { it1 -> navigateToSuburbSelection(it1) }
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

    fun showGetSuburbProgress() {
        dropdownGetSuburbImg?.visibility = View.INVISIBLE
        progressbarGetSuburb?.visibility = View.VISIBLE
    }

    fun hideSetSuburbProgressBar() {
        progressbarGetSuburb?.visibility = View.INVISIBLE
        dropdownGetSuburbImg?.visibility = View.VISIBLE
    }

    fun hideGetProvincesProgress() {
        progressbarGetProvinces?.visibility = View.INVISIBLE
        dropdownGetProvincesImg?.visibility = View.VISIBLE
    }

    fun showGetProvincesProgress() {
        dropdownGetProvincesImg?.visibility = View.INVISIBLE
        progressbarGetProvinces?.visibility = View.VISIBLE
    }

    private fun onSaveAddressClicked() {
        if (cellphoneNumber?.text.toString().trim().isNotEmpty() && cellphoneNumber?.text.toString()
                .trim().length < 10
        ) {
            showErrorPhoneNumber()
        }
        if (autoCompleteTextView?.text.toString().trim()
                .isNotEmpty() && addressNicknameEditText?.text.toString().trim()
                .isNotEmpty() && suburbEditText?.text.toString().trim()
                .isNotEmpty() && provinceAutocompleteEditText?.text.toString().trim()
                .isNotEmpty() && postalCode?.text.toString().trim()
                .isNotEmpty() && recipientName?.text.toString().trim()
                .isNotEmpty() && cellphoneNumber?.text.toString().trim()
                .isNotEmpty() && selectedDeliveryAddressType != null
        ) {
            if (isNickNameExist())
                return

            checkoutAddAddressNewUserViewModel.addAddress(
                AddAddressRequestBody(
                    addressNicknameEditText?.text.toString(),
                    recipientName?.text.toString(),
                    autoCompleteTextView?.text.toString(),
                    unitComplexFloorEditText?.text.toString(),
                    postalCode?.text.toString(),
                    cellphoneNumber?.text.toString(),
                    "",
                    selectedAddress.region,
                    selectedAddress.suburbId,
                    selectedAddress.city,
                    suburbEditText?.text.toString(),
                    selectedDeliveryAddressType.toString(),
                    "",
                    false,
                    selectedAddress.latitude!!,
                    selectedAddress.longitude!!
                )
            ).observe(this, {
                when (it.responseStatus) {
                    ResponseStatus.SUCCESS -> {
                        savedAddressResponse?.addresses?.add(it.data?.address)
                    }
                    ResponseStatus.LOADING -> {

                    }
                    ResponseStatus.ERROR -> {

                    }
                }
            })

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

    private fun isNickNameExist(): Boolean {
        var isExist = false
        for (address in savedAddressResponse?.addresses!!) {
            if (addressNicknameEditText.text.toString().equals(address.nickname, true)) {
                addressNicknameEditText.setBackgroundResource(R.drawable.input_error_background)
                addressNicknameErrorMsg?.visibility = View.VISIBLE
                addressNicknameErrorMsg.text = bindString(R.string.nick_name_exist_error_msg)
                isExist = true
            }
        }
        return isExist
    }

    private fun showErrorPhoneNumber() {
        cellphoneNumberErrorMsg?.visibility = View.VISIBLE
        cellphoneNumberErrorMsg.text = bindString(R.string.phone_number_invalid_error_msg)
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

    private fun showErrorInputField(editText: EditText, visible: Int) {
        editText.setBackgroundResource(if (visible == View.VISIBLE) R.drawable.input_error_background else R.drawable.recipient_details_input_edittext_bg)
        when (editText.id) {
            R.id.autoCompleteTextView -> {
                autocompletePlaceErrorMsg?.visibility = visible
            }
            R.id.addressNicknameEditText -> {
                addressNicknameErrorMsg?.visibility = visible
            }
            R.id.suburbEditText -> {
                suburbNameErrorMsg?.visibility = visible
            }
            R.id.provinceAutocompleteEditText -> {
                provinceNameErrorMsg?.visibility = visible
            }
            R.id.postalCode -> {
                postalCodeTextErrorMsg?.visibility = visible
                editText.setBackgroundResource(if (visible == View.VISIBLE) R.drawable.input_error_non_editable_background else R.drawable.input_non_editable_edit_text)
            }
            R.id.recipientName -> {
                recipientNameErrorMsg?.visibility = visible
            }
            R.id.cellphoneNumber -> {
                cellphoneNumberErrorMsg?.visibility = visible
                cellphoneNumberErrorMsg.text = bindString(R.string.mobile_number_error_msg)
            }
        }
    }
}