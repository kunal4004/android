package za.co.woolworths.financial.services.android.checkout.view

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.checkout_new_user_recipient_details.*
import kotlinx.android.synthetic.main.checkout_who_is_collecting_fragment.*
import kotlinx.android.synthetic.main.vehicle_details_layout.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.SAVED_ADDRESS_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutReturningUserCollectionFragment.Companion.KEY_COLLECTING_DETAILS
import za.co.woolworths.financial.services.android.checkout.viewmodel.WhoIsCollectingDetails
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CartFragment
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_CNC_SELETION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.SAVED_ADDRESS_RESPONSE
import za.co.woolworths.financial.services.android.util.Constant
import za.co.woolworths.financial.services.android.util.StoreUtils
import za.co.woolworths.financial.services.android.util.Utils
import java.util.regex.Pattern

/**
 * Created by Kunal Uttarwar on 26/10/21.
 */
class CheckoutWhoIsCollectingFragment : CheckoutAddressManagementBaseFragment(),
    View.OnClickListener {

    private lateinit var listOfVehicleInputFields: List<View>
    private lateinit var listOfTaxiInputFields: List<View>
    private var isMyVehicle = true
    private var navController: NavController? = null
    private var isComingFromCnc: Boolean? = false

    companion object {
        const val REGEX_VEHICLE_TEXT: String = "^\$|^[a-zA-Z0-9\\s<!>@\$&().+,-/\\\"']+\$"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.checkout_who_is_collecting_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (navController == null)
            navController = Navigation.findNavController(view)
        val bundle = arguments?.getBundle(BUNDLE)
        bundle?.apply {
            isComingFromCnc = getBoolean(IS_COMING_FROM_CNC_SELETION, false)
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
        }
    }

    private fun onVehicleSelected() {
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.CHECKOUT_COLLECTION_VECHILE_SELECT, hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_COLLECTION_VEHICLE_SELECT
            ), activity)
        isMyVehicle = true
        taxiDescription.visibility = View.GONE
        vehicleDetailsLayout.visibility = View.VISIBLE
        changeToggleTextLayout(myVehicleText)
    }

    private fun onTaxiSelected() {
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.CHECKOUT_COLLECTION_TAXI_SELECT, hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_COLLECTION_TAXI_SELECT
            ), activity)
        isMyVehicle = false
        taxiDescription.visibility = View.VISIBLE
        vehicleDetailsLayout.visibility = View.GONE
        changeToggleTextLayout(taxiText)
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
            taxiText -> {
                onTaxiTypeUnSelected(myVehicleText)
            }
            myVehicleText -> {
                onTaxiTypeUnSelected(taxiText)
            }
        }
    }

    private fun checkValidationsAndConfirm() {
        if (isMyVehicle) {
            if (!isErrorInputFields(listOfVehicleInputFields)) {
                onConfirmButtonClick()
            }
        } else {
            if (!isErrorInputFields(listOfTaxiInputFields)) {
                onConfirmButtonClick()
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
            recipientNameEditText.text.toString(),
            cellphoneNumberEditText.text.toString(),
            if (isMyVehicle) vehicleColourEditText.text.toString() else "",
            if (isMyVehicle) vehicleModelEditText.text.toString() else "",
            if (isMyVehicle) vehicleRegistrationEditText.text.toString() else "",
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
        if (cellphoneNumberEditText?.text.toString().trim()
                .isNotEmpty() && cellphoneNumberEditText?.text.toString().trim().length < 10
        ) {
            isEmptyError = true
            showErrorPhoneNumber()
        } else
            cellphoneNumberErrorMsg.text = bindString(R.string.mobile_number_error_msg)

        listOfInputFields.forEach {
            if (it is EditText) {
                if (it.text.toString().trim().isEmpty()) {
                    isEmptyError = true
                    if (it.id == R.id.recipientNameEditText) {
                        recipientNameErrorMsg.text = bindString(R.string.recipient_name_error_msg)
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

        recipientDetailsTitle?.text = bindString(R.string.who_is_collecting)
        confirmDetails?.setOnClickListener(this)
        myVehicleText?.setOnClickListener(this)
        taxiText?.setOnClickListener(this)
        showFBHView()

        recipientNameEditText?.apply {
            afterTextChanged {
                val length = it.length
                if (length > 0 && !Pattern.matches(REGEX_VEHICLE_TEXT, it)) {
                    recipientNameErrorMsg.text = bindString(R.string.special_char_name_error_text)
                    showErrorInputField(this, View.VISIBLE)
                } else
                    showErrorInputField(this, View.GONE)
            }
        }

        cellphoneNumberEditText?.apply {
            afterTextChanged {
                if (it.isNotEmpty())
                    showErrorInputField(this, View.GONE)
            }
        }

        vehicleColourEditText?.apply {
            afterTextChanged {
                val length = it.length
                if (length > 0 && !Pattern.matches(REGEX_VEHICLE_TEXT, it)) {
                    text?.delete(length - 1, length)
                } else
                    showErrorInputField(this, View.GONE)
            }
        }

        vehicleModelEditText?.apply {
            afterTextChanged {
                val length = it.length
                if (length > 0 && !Pattern.matches(REGEX_VEHICLE_TEXT, it)) {
                    text?.delete(length - 1, length)
                } else
                    showErrorInputField(this, View.GONE)
            }
        }

        vehicleRegistrationEditText?.apply {
            afterTextChanged {
                val length = it.length
                if (length > 0 && !Pattern.matches(REGEX_VEHICLE_TEXT, it)) {
                    text?.delete(length - 1, length)
                } else
                    showErrorInputField(this, View.GONE)
            }
        }

        listOfVehicleInputFields = listOf(
            recipientNameEditText,
            cellphoneNumberEditText,
            vehicleColourEditText,
            vehicleModelEditText
        )
        listOfTaxiInputFields = listOf(recipientNameEditText, cellphoneNumberEditText)
    }

    private fun showFBHView() {
        if(isComingFromCnc == true) {
            if(Utils.retrieveStoreId(StoreUtils.Companion.FulfillmentType.CRG_ITEMS?.type) !="" || Utils.retrieveStoreId(StoreUtils.Companion.FulfillmentType.CLOTHING_ITEMS?.type) !="") {
                recipientDetailsTitle?.visibility = View.GONE
                collectingPartition?.visibility = View.GONE
                vehiclesDetailsLayout?.visibility = View.GONE
                vehicleDetailsPartition?.visibility = View.GONE
                isMyVehicle = false

                whoIsCollectingDetailsInfoLayout?.visibility = View.VISIBLE
            }
        }
    }
    private fun setEditText(whoIsCollectingDetails: WhoIsCollectingDetails) {
        if (whoIsCollectingDetails != null) {
            if (whoIsCollectingDetails.isMyVehicle) {
                onVehicleSelected()
                vehicleColourEditText.setText(whoIsCollectingDetails.vehicleColor)
                vehicleModelEditText.setText(whoIsCollectingDetails.vehicleModel)
                vehicleRegistrationEditText.setText(whoIsCollectingDetails.vehicleRegistration)
            } else {
                onTaxiSelected()
            }
            recipientNameEditText?.setText(whoIsCollectingDetails.recipientName)
            cellphoneNumberEditText.setText(whoIsCollectingDetails.phoneNumber)
        }
    }

    private fun showErrorInputField(editText: EditText, visible: Int) {
        editText.setBackgroundResource(if (visible == View.VISIBLE) R.drawable.input_error_background else R.drawable.recipient_details_input_edittext_bg)
        when (editText.id) {
            R.id.recipientNameEditText -> {
                showAnimationErrorMessage(recipientNameErrorMsg, visible, 0)
            }
            R.id.cellphoneNumberEditText -> {
                showAnimationErrorMessage(cellphoneNumberErrorMsg, visible, 0)
            }
            R.id.vehicleColourEditText -> {
                showAnimationErrorMessage(vehicleColourErrorMsg, visible, 0)
            }
            R.id.vehicleModelEditText -> {
                showAnimationErrorMessage(vehicleModelErrorMsg, visible, 0)
            }
        }
    }

    private fun showErrorPhoneNumber() {
        cellphoneNumberEditText.setBackgroundResource(R.drawable.input_error_background)
        cellphoneNumberErrorMsg?.visibility = View.VISIBLE
        cellphoneNumberErrorMsg.text = bindString(R.string.phone_number_invalid_error_msg)
        showAnimationErrorMessage(
            cellphoneNumberErrorMsg,
            View.VISIBLE,
            whoIsCollectingDetailsLayout.y.toInt()
        )
    }

    private fun showAnimationErrorMessage(
        textView: TextView,
        visible: Int,
        recipientLayoutValue: Int,
    ) {
        if (View.VISIBLE == visible && textView.visibility == View.GONE) {
            val anim = ObjectAnimator.ofInt(
                collectionDetailsNestedScrollView,
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
}