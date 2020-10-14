package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_delivery_recipient_address_layout.*
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.BookingAddress
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.RecipientDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.ScheduleDeliveryRequest
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryAddressDetailsFragment : CreditCardDeliveryBaseFragment(), View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    var navController: NavController? = null
    private lateinit var listOfInputFields: List<EditText>
    var recipientDetailsResponse: RecipientDetailsResponse? = null
    var isBusinessAddress: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_recipient_address_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        listOfInputFields = listOf(complexOrBuildingName, businessName, streetAddress, suburb, cityOrTown, province, postalCode)
        submitAddress?.setOnClickListener(this)
        complexOrBuildingName?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        businessName?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        streetAddress?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        suburb?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        cityOrTown?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        province?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        postalCode?.apply { afterTextChanged { showErrorInputField(this, View.GONE) } }
        addressOption?.setOnCheckedChangeListener(this)
        configureUI()
    }

    fun configureUI() {
        recipientDetailsResponse?.addressDetails?.let {
            complexOrBuildingName?.setText(it.buildingName ?: "")
            businessName?.setText(it.buildingName ?: "")
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
        }
    }

    private fun onSubmit() {
        if (complexOrBuildingName?.text.toString().trim().isNotEmpty() && streetAddress?.text.toString().trim().isNotEmpty() && suburb?.text.toString().trim().isNotEmpty() && province?.text.toString().trim().isNotEmpty() && postalCode?.text.toString().trim().isNotEmpty() && if (isBusinessAddress) businessName?.text.toString().trim().isNotEmpty() else true) {
            scheduleDeliveryRequest?.bookingAddress?.let {
                it.businessName = businessName?.text.toString().trim()
                it.complexName = complexOrBuildingName?.text.toString().trim()
                it.buildingName = complexOrBuildingName?.text.toString().trim()
                it.street = streetAddress?.text.toString().trim()
                it.suburb = suburb?.text.toString().trim()
                it.province = province?.text.toString().trim()
                it.city = cityOrTown?.text.toString().trim()
                it.postalCode = postalCode?.text.toString().trim()
            }
            bundle?.putString("ScheduleDeliveryRequest", Utils.toJson(scheduleDeliveryRequest))
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

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        businessNamePlaceHolder?.text = bindString(if (checkedId == R.id.business) R.string.business_name else R.string.business_name_optional)
        isBusinessAddress = checkedId == R.id.business
        if (checkedId == R.id.residential && businessNameErrorMsg.visibility == View.VISIBLE) {
            businessName.setBackgroundResource(R.drawable.recipient_details_input_edittext_bg)
            businessNameErrorMsg.visibility = View.GONE
        }
    }

}