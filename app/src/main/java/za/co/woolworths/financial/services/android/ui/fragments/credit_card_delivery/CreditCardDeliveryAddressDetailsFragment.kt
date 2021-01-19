package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_delivery_recipient_address_layout.*
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.*
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryAddressDetailsFragment : CreditCardDeliveryBaseFragment(), View.OnClickListener {

    var navController: NavController? = null
    private lateinit var listOfInputFields: List<EditText>
    private var isBusinessAddress: Boolean = false
    private var isEditAddressActivity: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_recipient_address_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolBar()
        navController = Navigation.findNavController(view)
        listOfInputFields = listOf(complexOrBuildingName, businessName, streetAddress, suburb, cityOrTown, province, postalCode)
        submitAddress?.setOnClickListener(this)
        clearDetails.setOnClickListener(this)
        complexOrBuildingName?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        businessName?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        streetAddress?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        suburb?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        cityOrTown?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        province?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        postalCode?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        configureUI()
        if (arguments?.containsKey("isEditAddressActivity") == true) {
            isEditAddressActivity = arguments?.get("isEditAddressActivity") as Boolean
        }
    }

    private fun setUpToolBar() {
        if (activity is CreditCardDeliveryActivity) {
            (activity as? CreditCardDeliveryActivity)?.apply {
                setToolbarTitle("")
                changeToolbarBackground(R.color.white)
            }
        }
    }

    fun configureUI() {
        statusResponse?.addressDetails?.let {
            complexOrBuildingName?.setText(it.complexName ?: "")
            businessName?.setText(it.businessName ?: "")
            streetAddress?.setText(it.street ?: "")
            suburb?.setText(it.suburb ?: "")
            cityOrTown?.setText(it.city ?: "")
            province?.setText(it.province ?: "")
            postalCode?.setText(it.postalCode ?: "")
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.submitAddress -> {
                onSubmit()
            }
            R.id.clearDetails -> {
                businessName.text.clear()
                complexOrBuildingName.text.clear()
                streetAddress.text.clear()
                suburb.text.clear()
                province.text.clear()
                cityOrTown.text.clear()
                postalCode.text.clear()
            }
        }
    }

    private fun onSubmit() {
        if (complexOrBuildingName?.text.toString().trim().isNotEmpty() && streetAddress?.text.toString().trim().isNotEmpty() && suburb?.text.toString().trim().isNotEmpty() && province?.text.toString().trim().isNotEmpty() && postalCode?.text.toString().trim().isNotEmpty() && if (isBusinessAddress) businessName?.text.toString().trim().isNotEmpty() else true) {
            val recipientDetails = RecipientDetails()
            val addressDetails = AddressDetails()
            val jsonRecipientDetails: String? = bundle?.getString("RecipientDetails")
            if (jsonRecipientDetails == null) {
                val recipDetails: RecipientDetails? = statusResponse?.recipientDetails
                if (recipDetails != null) {
                    recipientDetails?.telCell = recipDetails.telCell
                    recipientDetails?.telWork = recipDetails.telWork
                    recipientDetails?.isThirdPartyRecipient = recipDetails.isThirdPartyRecipient
                    recipientDetails?.deliverTo = recipDetails.deliverTo
                    recipientDetails?.idNumber = recipDetails.idNumber
                }
            } else {
                val recipient: RecipientDetails = Utils.jsonStringToObject(jsonRecipientDetails, RecipientDetails::class.java) as RecipientDetails
                recipientDetails?.telCell = recipient.telCell
                recipientDetails?.telWork = recipient.telWork
                recipientDetails?.isThirdPartyRecipient = recipient.isThirdPartyRecipient
                recipientDetails?.deliverTo = recipient.deliverTo
                recipientDetails?.idNumber = recipient.idNumber
            }

            val searchPhase = "${streetAddress?.text.toString().trim()} ${suburb?.text.toString().trim()} ${cityOrTown?.text.toString().trim()} ${postalCode?.text.toString().trim()}"
            addressDetails?.let {
                it.deliveryAddress = searchPhase
                it.searchPhrase = searchPhase
                it.x = ""
                it.y = ""
                it.complexName = complexOrBuildingName?.text.toString().trim()
                it.businessName = businessName?.text.toString().trim()
                it.buildingName = complexOrBuildingName?.text.toString().trim()
                it.street = streetAddress?.text.toString().trim()
                it.suburb = suburb?.text.toString().trim()
                it.city = cityOrTown?.text.toString().trim()
                it.province = province?.text.toString().trim()
                it.postalCode = postalCode?.text.toString().trim()
            }

            scheduleDeliveryRequest = ScheduleDeliveryRequest()
            scheduleDeliveryRequest.recipientDetails = recipientDetails
            scheduleDeliveryRequest.addressDetails = addressDetails
            statusResponse?.addressDetails = addressDetails
            statusResponse?.recipientDetails = recipientDetails

            bundle?.putString("ScheduleDeliveryRequest", Utils.toJson(scheduleDeliveryRequest))
            bundle?.putString("StatusResponse", Utils.toJson(statusResponse))
            bundle?.putBoolean("isEditAddressActivity", isEditAddressActivity)
            navController?.navigate(R.id.action_to_creditCardDeliveryValidateAddressRequestFragment, bundleOf("bundle" to bundle))

        } else {
            listOfInputFields.forEach {
                if (it.text.toString().trim().isEmpty())
                    showErrorInputField(it, View.VISIBLE)
            }
        }
    }

    private fun showErrorInputField(editText: EditText, visible: Int) {
        if (editText.id == R.id.businessName && !isBusinessAddress)
            return

        editText.setBackgroundResource(if (visible == View.VISIBLE) R.drawable.otp_box_error_background else R.drawable.recipient_details_input_edittext_bg)
        when (editText.id) {
            R.id.businessName -> {
                businessNameErrorMsg.visibility = visible
            }
            R.id.complexOrBuildingName -> {
                complexOrBuildingNameErrorMsg.visibility = visible
            }
            R.id.streetAddress -> {
                streetAddressErrorMsg.visibility = visible
            }
            R.id.suburb -> {
                suburbErrorMsg.visibility = visible
            }
            R.id.province -> {
                provinceErrorMsg.visibility = visible
            }
            R.id.postalCode -> {
                postalCodeErrorMsg.visibility = visible
            }
            R.id.cityOrTown -> {
                cityOrTownErrorMsg.visibility = visible
            }
        }
    }
}