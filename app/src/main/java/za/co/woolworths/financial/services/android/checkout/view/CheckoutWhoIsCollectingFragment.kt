package za.co.woolworths.financial.services.android.checkout.view

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CheckoutWhoIsCollectingFragmentBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.cart.view.CartFragment
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.SAVED_ADDRESS_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutReturningUserCollectionFragment.Companion.KEY_COLLECTING_DETAILS
import za.co.woolworths.financial.services.android.checkout.viewmodel.WhoIsCollectingDetails
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_CNC_SELETION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_FBH_ONLY
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_MIXED_BASKET
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.PLACE_ID
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.SAVED_ADDRESS_RESPONSE
import za.co.woolworths.financial.services.android.util.Constant
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper
import java.util.regex.Pattern

/**
 * Created by Kunal Uttarwar on 26/10/21.
 */
class CheckoutWhoIsCollectingFragment : CheckoutAddressManagementBaseFragment(R.layout.checkout_who_is_collecting_fragment),
    View.OnClickListener {

    lateinit var binding: CheckoutWhoIsCollectingFragmentBinding
    private lateinit var listOfVehicleInputFields: List<View>
    private lateinit var listOfTaxiInputFields: List<View>
    private var isMyVehicle = true
    private var navController: NavController? = null
    private var isComingFromCnc: Boolean? = false
    private var isMixBasket: Boolean? = false
    private var isFBHOnly: Boolean? = false
    private var placeId : String? = null
    private var savedAddressResponse :SavedAddressResponse? = null


    companion object {
        const val REGEX_VEHICLE_TEXT: String = "^\$|^[a-zA-Z0-9\\s<!>@\$&().+,-/\\\"']+\$"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CheckoutWhoIsCollectingFragmentBinding.bind(view)
        if (navController == null)
            navController = Navigation.findNavController(view)
        val bundle = arguments?.getBundle(BUNDLE)
        bundle?.apply {
            isComingFromCnc = getBoolean(IS_COMING_FROM_CNC_SELETION, false)
            isMixBasket = getBoolean(IS_MIXED_BASKET, false)
            isFBHOnly = getBoolean(IS_FBH_ONLY, false)
            placeId = getString(PLACE_ID,"")
            savedAddressResponse = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                getSerializable(SAVED_ADDRESS_RESPONSE, SavedAddressResponse::class.java)
            else
                getSerializable(SAVED_ADDRESS_RESPONSE) as? SavedAddressResponse
        }

        initView()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.confirmDetails -> {
                checkValidationsAndConfirm()
            }
            R.id.taxiText -> {
                onTaxiSelected()
            }
            R.id.myVehicleText -> {
                onVehicleSelected()
            }

            R.id.backArrow -> {
                activity ?.apply{
                    setResult(CheckOutFragment.RESULT_RELOAD_CART)
                    view?.let{
                        closeFragment(it)
                    }
                    overridePendingTransition(
                        R.anim.slide_in_from_left,
                        R.anim.slide_out_to_right
                    )
                }

            }
        }
    }

    private fun onVehicleSelected() {
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.CHECKOUT_COLLECTION_VECHILE_SELECT, hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_COLLECTION_VEHICLE_SELECT
            ), activity)

        FirebaseAnalyticsEventHelper.setFirebaseEventForm(FirebaseManagerAnalyticsProperties.PropertyValues.MY_VEHICLE,
                FirebaseManagerAnalyticsProperties.FORM_START, true)

        isMyVehicle = true
        binding.vehiclesDetailsLayout.taxiDescription.visibility = View.GONE
        binding.vehiclesDetailsLayout.vehicleDetailsLayout.visibility = View.VISIBLE
        changeToggleTextLayout(binding.vehiclesDetailsLayout.myVehicleText)
    }

    private fun onTaxiSelected() {
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.CHECKOUT_COLLECTION_TAXI_SELECT, hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_COLLECTION_TAXI_SELECT
            ), activity)

        FirebaseAnalyticsEventHelper.setFirebaseEventForm(FirebaseManagerAnalyticsProperties.PropertyValues.TAXI,
                FirebaseManagerAnalyticsProperties.FORM_START, true)

        isMyVehicle = false
        binding.vehiclesDetailsLayout.taxiDescription.visibility = View.VISIBLE
        binding.vehiclesDetailsLayout.vehicleDetailsLayout.visibility = View.GONE
        changeToggleTextLayout(binding.vehiclesDetailsLayout.taxiText)
    }


    private fun changeToggleTextLayout(taxiType: TextView) {
        taxiType.background =
            bindDrawable(R.drawable.checkout_delivering_title_round_button_pressed)
        taxiType.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        unselectOtherTaxiType(taxiType)
    }

    private fun onTaxiTypeUnSelected(taxiType: TextView) {
        taxiType?.background =
            bindDrawable(R.drawable.checkout_delivering_title_round_button)
        taxiType?.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
    }

    private fun unselectOtherTaxiType(taxiType: TextView) {
        when (taxiType) {
            binding.vehiclesDetailsLayout.taxiText -> {
                onTaxiTypeUnSelected(binding.vehiclesDetailsLayout.myVehicleText)
            }
            binding.vehiclesDetailsLayout.myVehicleText -> {
                onTaxiTypeUnSelected(binding.vehiclesDetailsLayout.taxiText)
            }
        }
    }

    private fun checkValidationsAndConfirm() {
        if (isMyVehicle) {
            if (!isErrorInputFields(listOfVehicleInputFields)) {
                onConfirmButtonClick()
                FirebaseAnalyticsEventHelper.setFirebaseEventForm(FirebaseManagerAnalyticsProperties.PropertyValues.MY_VEHICLE,
                        FirebaseManagerAnalyticsProperties.FORM_COMPLETE, true)
            }
        } else {
            if (!isErrorInputFields(listOfTaxiInputFields)) {
                onConfirmButtonClick()
                FirebaseAnalyticsEventHelper.setFirebaseEventForm(FirebaseManagerAnalyticsProperties.PropertyValues.TAXI,
                        FirebaseManagerAnalyticsProperties.FORM_COMPLETE, true)
            }
        }
    }

    private fun onConfirmButtonClick() {
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.CHECKOUT_COLLECTION_CONFIRM_DETAILS, hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_COLLECTION_CONFIRM_DETAILS
            ), activity)
        val whoIsCollectingDetails = WhoIsCollectingDetails(
            binding.whoIsCollectingDetailsLayout.recipientNameEditText.text.toString(),
            binding.whoIsCollectingDetailsLayout.cellphoneNumberEditText.text.toString(),
            if (isMyVehicle) binding.vehiclesDetailsLayout.vehicleColourEditText.text.toString() else "",
            if (isMyVehicle) binding.vehiclesDetailsLayout.vehicleModelEditText.text.toString() else "",
            if (isMyVehicle) binding.vehiclesDetailsLayout.vehicleRegistrationEditText.text.toString() else "",
            isMyVehicle
        )
        startCheckoutActivity(Utils.toJson(whoIsCollectingDetails))
    }

    private fun startCheckoutActivity(toJson: String) {
        val checkoutActivityIntent = Intent(activity, CheckoutActivity::class.java)
        val bundle = arguments?.getBundle(BUNDLE)
        bundle?.apply {
            if (containsKey(Constant.LIQUOR_ORDER) && containsKey(Constant.NO_LIQUOR_IMAGE_URL)) {
                checkoutActivityIntent.putExtra(Constant.LIQUOR_ORDER,
                    getBoolean(Constant.LIQUOR_ORDER))
                checkoutActivityIntent.putExtra(Constant.NO_LIQUOR_IMAGE_URL,
                    getString((Constant.NO_LIQUOR_IMAGE_URL)))
            }
            checkoutActivityIntent.putExtra(CART_ITEM_LIST,
                    bundle.getSerializable(CART_ITEM_LIST) as ArrayList<CommerceItem>?)
        }
        checkoutActivityIntent.putExtra(
            KEY_COLLECTING_DETAILS,
            toJson
        )
        checkoutActivityIntent.putExtra(
            IS_COMING_FROM_CNC_SELETION,
            isComingFromCnc
        )
        if (bundle?.containsKey(SAVED_ADDRESS_RESPONSE) == true) {
            checkoutActivityIntent.putExtra(SAVED_ADDRESS_KEY,
                bundle?.getSerializable(SAVED_ADDRESS_RESPONSE))
        }
        activity?.let {
            startActivityForResult(
                checkoutActivityIntent,
                CartFragment.REQUEST_PAYMENT_STATUS
            )

            it.overridePendingTransition(
                R.anim.slide_from_right,
                R.anim.slide_out_to_left
            )
            it.finish()
        }

    }

    private fun isErrorInputFields(listOfInputFields: List<View>): Boolean {
        var isEmptyError = false
        if (binding.whoIsCollectingDetailsLayout.cellphoneNumberEditText?.text.toString().trim()
                .isNotEmpty() && binding.whoIsCollectingDetailsLayout.cellphoneNumberEditText?.text.toString().trim().length < 10
        ) {
            isEmptyError = true
            showErrorPhoneNumber()
        } else
            binding.whoIsCollectingDetailsLayout.cellphoneNumberErrorMsg.text = bindString(R.string.mobile_number_error_msg)

        listOfInputFields.forEach {
            if (it is EditText) {
                if (it.text.toString().trim().isEmpty()) {
                    isEmptyError = true
                    if (it.id == R.id.recipientNameEditText) {
                        binding.whoIsCollectingDetailsLayout.recipientNameErrorMsg.text = bindString(R.string.recipient_name_error_msg)
                    }
                    showErrorInputField(it, View.VISIBLE)
                }
            }
        }
        return isEmptyError
    }

    fun initView() {
        arguments?.apply {
            if (containsKey(KEY_COLLECTING_DETAILS)) {
                getString(KEY_COLLECTING_DETAILS)?.let {
                    val whoIsCollectingDetails: WhoIsCollectingDetails =
                        Gson().fromJson(it, object : TypeToken<WhoIsCollectingDetails>() {}.type)
                    setEditText(whoIsCollectingDetails)
                }
            }
        }

        // When first time visit this page default event is "My Vehicle"
        FirebaseAnalyticsEventHelper.setFirebaseEventForm(FirebaseManagerAnalyticsProperties.PropertyValues.MY_VEHICLE,
                FirebaseManagerAnalyticsProperties.FORM_START, true)

        binding.whoIsCollectingDetailsLayout.recipientDetailsTitle?.text = bindString(R.string.who_is_collecting)
        binding.confirmDetails?.setOnClickListener(this)
        binding.vehiclesDetailsLayout.myVehicleText?.setOnClickListener(this)
        binding.vehiclesDetailsLayout.taxiText?.setOnClickListener(this)
        if(isComingFromCnc == true && savedAddressResponse != null){
            val address : Address? = savedAddressResponse?.addresses?.firstOrNull { it.placesId == placeId }
            binding.whoIsCollectingDetailsLayout.recipientNameEditText?.setText(address?.recipientName)
            binding.whoIsCollectingDetailsLayout.cellphoneNumberEditText.setText(address?.primaryContactNo)
        }
        showFBHView()
        binding.backArrow?.setOnClickListener(this)

        binding.whoIsCollectingDetailsLayout.recipientNameEditText?.apply {
            afterTextChanged {
                val length = it.length
                if (length > 0 && !Pattern.matches(REGEX_VEHICLE_TEXT, it)) {
                    binding.whoIsCollectingDetailsLayout.recipientNameErrorMsg.text = bindString(R.string.special_char_name_error_text)
                    showErrorInputField(this, View.VISIBLE)
                } else
                    showErrorInputField(this, View.GONE)
            }
        }

        binding.whoIsCollectingDetailsLayout.cellphoneNumberEditText?.apply {
            afterTextChanged {
                if (it.isNotEmpty())
                    showErrorInputField(this, View.GONE)
            }
        }

        binding.vehiclesDetailsLayout.vehicleColourEditText?.apply {
            afterTextChanged {
                val length = it.length
                if (length > 0 && !Pattern.matches(REGEX_VEHICLE_TEXT, it)) {
                    text?.delete(length - 1, length)
                } else
                    showErrorInputField(this, View.GONE)
            }
        }

        binding.vehiclesDetailsLayout.vehicleModelEditText?.apply {
            afterTextChanged {
                val length = it.length
                if (length > 0 && !Pattern.matches(REGEX_VEHICLE_TEXT, it)) {
                    text?.delete(length - 1, length)
                } else
                    showErrorInputField(this, View.GONE)
            }
        }

        binding.vehiclesDetailsLayout.vehicleRegistrationEditText?.apply {
            afterTextChanged {
                val length = it.length
                if (length > 0 && !Pattern.matches(REGEX_VEHICLE_TEXT, it)) {
                    text?.delete(length - 1, length)
                } else
                    showErrorInputField(this, View.GONE)
            }
        }

        listOfVehicleInputFields = listOf(
            binding.whoIsCollectingDetailsLayout.recipientNameEditText,
            binding.whoIsCollectingDetailsLayout.cellphoneNumberEditText,
            binding.vehiclesDetailsLayout.vehicleColourEditText,
            binding.vehiclesDetailsLayout.vehicleModelEditText
        )
        listOfTaxiInputFields = listOf(binding.whoIsCollectingDetailsLayout.recipientNameEditText, binding.whoIsCollectingDetailsLayout.cellphoneNumberEditText)
    }

    private fun showFBHView() {
        if (isComingFromCnc == true) {
            if (isFBHOnly == true) {
                binding.collectingPartition?.visibility = View.GONE
                binding.vehiclesDetailsLayout?.root?.visibility = View.GONE
                binding.vehicleDetailsPartition?.visibility = View.GONE
                isMyVehicle = false

                binding.whoIsCollectingDetailsLayout?.recipientDetailsTitle?.visibility = View.GONE
                binding.whoIsCollectingDetailsInfoLayout?.root?.visibility = View.VISIBLE
                binding.whoIsCollectingDetailsLayout.recipientNamePlaceHolder.text = context?.getString(R.string.recipient_name_fbh)
            } else if (isMixBasket == true) {
                binding.whoIsCollectingDetailsLayout?.recipientDetailsTitle?.visibility = View.GONE
                binding.whoIsCollectingDetailsInfoLayout?.root?.visibility = View.VISIBLE
                binding.whoIsCollectingDetailsLayout.recipientNamePlaceHolder.text = context?.getString(R.string.recipient_name_fbh)
            }
        }
    }
    private fun setEditText(whoIsCollectingDetails: WhoIsCollectingDetails) {
        if (whoIsCollectingDetails != null) {
            if (whoIsCollectingDetails.isMyVehicle) {
                onVehicleSelected()
                binding.vehiclesDetailsLayout.vehicleColourEditText.setText(whoIsCollectingDetails.vehicleColor)
                binding.vehiclesDetailsLayout.vehicleModelEditText.setText(whoIsCollectingDetails.vehicleModel)
                binding.vehiclesDetailsLayout.vehicleRegistrationEditText.setText(whoIsCollectingDetails.vehicleRegistration)
            } else {
                onTaxiSelected()
            }
            binding.whoIsCollectingDetailsLayout.recipientNameEditText?.setText(whoIsCollectingDetails.recipientName)
            binding.whoIsCollectingDetailsLayout.cellphoneNumberEditText.setText(whoIsCollectingDetails.phoneNumber)
        }
    }

    private fun showErrorInputField(editText: EditText, visible: Int) {
        editText.setBackgroundResource(if (visible == View.VISIBLE) R.drawable.input_error_background else R.drawable.recipient_details_input_edittext_bg)
        when (editText.id) {
            R.id.recipientNameEditText -> {
                showAnimationErrorMessage(binding.whoIsCollectingDetailsLayout.recipientNameErrorMsg, visible, 0)
            }
            R.id.cellphoneNumberEditText -> {
                showAnimationErrorMessage(binding.whoIsCollectingDetailsLayout.cellphoneNumberErrorMsg, visible, 0)
            }
            R.id.vehicleColourEditText -> {
                showAnimationErrorMessage(binding.vehiclesDetailsLayout.vehicleColourErrorMsg, visible, 0)
            }
            R.id.vehicleModelEditText -> {
                showAnimationErrorMessage(binding.vehiclesDetailsLayout.vehicleModelErrorMsg, visible, 0)
            }
        }
    }

    private fun showErrorPhoneNumber() {
        binding.whoIsCollectingDetailsLayout.cellphoneNumberEditText.setBackgroundResource(R.drawable.input_error_background)
        binding.whoIsCollectingDetailsLayout.cellphoneNumberErrorMsg?.visibility = View.VISIBLE
        binding.whoIsCollectingDetailsLayout.cellphoneNumberErrorMsg.text = bindString(R.string.phone_number_invalid_error_msg)
        showAnimationErrorMessage(
            binding.whoIsCollectingDetailsLayout.cellphoneNumberErrorMsg,
            View.VISIBLE,
            binding.whoIsCollectingDetailsLayout.root.y.toInt()
        )
    }

    private fun showAnimationErrorMessage(
        textView: TextView,
        visible: Int,
        recipientLayoutValue: Int,
    ) {
        if (View.VISIBLE == visible && textView.visibility == View.GONE) {
            val anim = ObjectAnimator.ofInt(
                binding.collectionDetailsNestedScrollView,
                "scrollY",
                recipientLayoutValue + textView.y.toInt()
            )
            anim.setDuration(300).start()
        }
        textView?.visibility = visible
    }

    @VisibleForTesting
    fun testGetMyVehicleList(): List<View> {
        return listOfVehicleInputFields
    }

    @VisibleForTesting
    fun testGetTaxiList(): List<View> {
        return listOfTaxiInputFields
    }

    private fun FragmentActivity.closeFragment(view: View) {
        view.postDelayed({ onBackPressed() }, AppConstant.DELAY_500_MS)
    }
}