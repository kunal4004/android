package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_delivery_recipient_address_layout.*
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.BookingAddress
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryAddressDetailsFragment : Fragment(), View.OnClickListener {

    var navController: NavController? = null
    var bundle: Bundle? = null
    private var bookingAddress: BookingAddress? = null
    private lateinit var listOfInputFields: List<EditText>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_recipient_address_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            if (containsKey("BookingAddress")) {
                bookingAddress = Utils.jsonStringToObject(getString("BookingAddress"), BookingAddress::class.java) as BookingAddress
            }
        }
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
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.submitAddress -> {
                onSubmit()
            }
        }
    }

    private fun onSubmit() {
        if (complexOrBuildingName?.text.toString().trim().isNotEmpty() && businessName?.text.toString().trim().isNotEmpty() && streetAddress?.text.toString().trim().isNotEmpty() && suburb?.text.toString().trim().isNotEmpty() && province?.text.toString().trim().isNotEmpty() && postalCode?.text.toString().trim().isNotEmpty()) {
            bookingAddress?.let {
                it.businessName = businessName?.text.toString().trim()
                it.buildingName = complexOrBuildingName?.text.toString().trim()
                it.street = streetAddress?.text.toString().trim()
                it.suburb = suburb?.text.toString().trim()
                it.province = province?.text.toString().trim()
                it.city = cityOrTown?.text.toString().trim()
                it.postalCode = postalCode?.text.toString().trim()
            }
            bundle?.putString("BookingAddress", Utils.toJson(bookingAddress))
            navController?.navigate(R.id.action_to_creditCardDeliveryValidateAddressRequestFragment, bundleOf("bundle" to bundle))

        } else {
            listOfInputFields.forEach {
                if (it.text.toString().trim().isEmpty())
                    showErrorInputField(it, View.VISIBLE)
            }
        }
    }

    private fun showErrorInputField(editText: EditText, visible: Int) {
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