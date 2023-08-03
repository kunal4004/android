package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CreditCardDeliveryRecipientDetailsLayoutBinding
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.AddressDetails
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.RecipientDetails
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.ScheduleDeliveryRequest
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import android.graphics.Color

class CreditCardDeliveryRecipientDetailsFragment : CreditCardDeliveryBaseFragment(R.layout.credit_card_delivery_recipient_details_layout), View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private lateinit var binding: CreditCardDeliveryRecipientDetailsLayoutBinding
    var navController: NavController? = null
    private var recipientDetails = RecipientDetails()
    private lateinit var listOfInputFields: List<EditText>
    private var isRecipientIsThirdPerson: Boolean = false
    private var isEditRecipient: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CreditCardDeliveryRecipientDetailsLayoutBinding.bind(view)

        binding.apply {
            if (arguments?.containsKey("isEditRecipient") == true) {
                isEditRecipient = arguments?.get("isEditRecipient") as Boolean
            }
            if (isEditRecipient) {
                confirmProceedButton.visibility = View.VISIBLE
                val param = nestedScrollView.layoutParams as ViewGroup.MarginLayoutParams
                param.setMargins(0, 0, 0, 40)
                nestedScrollView.layoutParams = param

                confirm.visibility = View.GONE
                clearDetails.visibility = View.GONE
                recipientOption.visibility = View.GONE
            }

            navController = Navigation.findNavController(view)
            setUpToolBar()
            listOfInputFields = listOf(recipientName, cellphoneNumber, idNumber, alternativeNumber)
            recipientName?.apply {
                afterTextChanged { clearErrorInputField(this) }
            }
            cellphoneNumber?.apply {
                afterTextChanged { clearErrorInputField(this) }
            }

            idNumber?.apply {
                afterTextChanged { clearErrorInputField(this) }
            }

            alternativeNumber?.apply {
                afterTextChanged { clearErrorInputField(this) }
            }

            recipientOption?.setOnCheckedChangeListener(this@CreditCardDeliveryRecipientDetailsFragment)

            confirm?.setOnClickListener(this@CreditCardDeliveryRecipientDetailsFragment)
            confirmProceedButton?.setOnClickListener { view ->
                onConfirmButtonClicked()
            }
            clearDetails.setOnClickListener(this@CreditCardDeliveryRecipientDetailsFragment)
            configureUI()
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

    fun CreditCardDeliveryRecipientDetailsLayoutBinding.configureUI() {
        statusResponse?.recipientDetails?.let {
            idNumber?.setText(it.idNumber ?: "")
            if (it.idNumber?.isEmpty() == true || it.idNumber == null) {
                mySelf.isChecked = true
            } else {
                anotherPerson.isChecked = true
            }
            cellphoneNumber?.setText(it.telCell ?: "")
            alternativeNumber?.setText(it.telWork ?: "")
        }

        recipientName?.apply {
            isEnabled = statusResponse?.deliveryStatus?.isCardNew != true
            updatedRecipientTextField()
            setText(statusResponse?.recipientDetails?.deliverTo
                    ?: SessionUtilities.getInstance().jwt?.name?.get(0))
        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v?.id) {
                R.id.confirm -> {
                    onConfirmButtonClicked()
                }
                R.id.clearDetails -> {
                    if (recipientName.isEnabled) {
                        recipientName?.text?.clear()
                    }
                    cellphoneNumber?.text?.clear()
                    alternativeNumber?.text?.clear()
                    if (isRecipientIsThirdPerson) idNumber?.text?.clear()
                }
            }
        }
    }

    //This API should be Fire and forget
    private fun updateRecipientDetails() {
        val addressDetails: AddressDetails? = statusResponse?.let {
            AddressDetails(it.addressDetails?.deliveryAddress, it.addressDetails?.deliveryAddress, it.addressDetails?.x,
                    it.addressDetails?.y, it.addressDetails?.complexName, it.addressDetails?.businessName,
                    it.addressDetails?.buildingName, it.addressDetails?.street, it.addressDetails?.suburb, it.addressDetails?.city,
                    it.addressDetails?.province, it.addressDetails?.postalCode)
        }
        var scheduleDeliveryRequest = ScheduleDeliveryRequest()
        scheduleDeliveryRequest.let {
            it.recipientDetails = recipientDetails
            it.addressDetails = addressDetails
            it.slotDetails = statusResponse?.slotDetails
        }
        bundle?.putString("ScheduleDeliveryRequest", Utils.toJson(scheduleDeliveryRequest))
        bundle?.putBoolean("isEditRecipient", isEditRecipient)
        navController?.navigate(R.id.action_from_recipient_to_creditCardScheduleDelivery, bundleOf("bundle" to bundle))
    }

    private fun CreditCardDeliveryRecipientDetailsLayoutBinding.showErrorPhoneNumber(editText: EditText) {
        editText.setBackgroundResource(R.drawable.otp_box_error_background)
        when (editText.id) {
            R.id.cellphoneNumber -> {
                cellphoneNumberErrorMsg.visibility = View.VISIBLE
                cellphoneNumberErrorMsg.text = bindString(R.string.phone_number_invalid_error_msg)
            }
            R.id.alternativeNumber -> {
                alternativeNumberErrorMsg.visibility = View.VISIBLE
                alternativeNumberErrorMsg.text = bindString(R.string.phone_number_invalid_error_msg)
            }
        }
    }

    private fun CreditCardDeliveryRecipientDetailsLayoutBinding.showErrorInputField(editText: EditText) {
        if (editText.id == R.id.idNumber && !isRecipientIsThirdPerson)
            return

        editText.setBackgroundResource(R.drawable.otp_box_error_background)
        when (editText.id) {
            R.id.recipientName -> {
                recipientNameErrorMsg.visibility = View.VISIBLE
            }
            R.id.cellphoneNumber -> {
                cellphoneNumberErrorMsg.visibility = View.VISIBLE
                cellphoneNumberErrorMsg.text = bindString(R.string.cellphone_number_error_msg)
            }
            R.id.idNumber -> {
                idNumberErrorMsg.visibility = View.VISIBLE
            }
            R.id.alternativeNumber -> {
                alternativeNumberErrorMsg.visibility = View.VISIBLE
                alternativeNumberErrorMsg.text = bindString(R.string.alternate_number_error_msg)
            }
        }
    }

    private fun CreditCardDeliveryRecipientDetailsLayoutBinding.clearErrorInputField(editText: EditText) {
        editText.setBackgroundResource(R.drawable.recipient_details_input_edittext_bg)
        when (editText.id) {
            R.id.recipientName -> {
                recipientNameErrorMsg?.visibility = View.GONE
            }
            R.id.cellphoneNumber -> {
                cellphoneNumberErrorMsg?.visibility = View.GONE
            }
            R.id.idNumber -> {
                idNumberErrorMsg?.visibility = View.GONE
            }
            R.id.alternativeNumber -> {
                alternativeNumberErrorMsg.visibility = View.GONE
            }
        }
    }
    private fun CreditCardDeliveryRecipientDetailsLayoutBinding.onConfirmButtonClicked() {
        if (recipientName?.text.toString().trim().isNotEmpty() && cellphoneNumber?.text.toString().trim().isNotEmpty() && alternativeNumber?.text.toString().trim().isNotEmpty() && cellphoneNumber?.text?.length == 10 && alternativeNumber?.text?.length == 10 && if (isRecipientIsThirdPerson) idNumber?.text.toString().trim().isNotEmpty() else true) {
            recipientDetails.let {
                it?.deliverTo = recipientName?.text.toString().trim()
                it?.telCell = cellphoneNumber?.text.toString().trim()
                it?.telWork = alternativeNumber?.text.toString().trim()
                it?.isThirdPartyRecipient = isRecipientIsThirdPerson
                if (isRecipientIsThirdPerson)
                    it?.idNumber = idNumber?.text.toString().trim()
            }
            bundle?.putString("RecipientDetails", Utils.toJson(recipientDetails))
            statusResponse?.recipientDetails = recipientDetails
            bundle?.putParcelable(BundleKeysConstants.STATUS_RESPONSE, statusResponse)
            if (isEditRecipient) {
                updateRecipientDetails()
            } else {
                navController?.navigate(R.id.action_to_creditCardDeliveryAddressDetailsFragment, bundleOf("bundle" to bundle))
            }
        } else {
            listOfInputFields.forEach {
                if (it.text.toString().trim().isEmpty()) {
                    showErrorInputField(it)
                } else if (it.id == R.id.cellphoneNumber && it.text.length < 10) {
                    showErrorPhoneNumber(it)
                } else if (it.id == R.id.alternativeNumber && it.text.length < 10) {
                    showErrorPhoneNumber(it)
                }
            }
        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        binding.apply {
            isRecipientIsThirdPerson = checkedId == R.id.anotherPerson
            idNumberLayout.visibility = if (isRecipientIsThirdPerson) View.VISIBLE else View.GONE
            updatedRecipientTextField()
            if (checkedId == R.id.mySelf && idNumberErrorMsg.visibility == View.VISIBLE) {
                clearErrorInputField(idNumber)
            }
        }
    }

    fun updatedRecipientTextField() {
        binding.apply {
            recipientName.apply {
                if (mySelf.isChecked) {
                    isEnabled = false
                    recipientName.setTextColor(Color.parseColor("#50333333"))
                } else {
                    isEnabled = true
                    recipientName.setTextColor(Color.parseColor("#000000"))
                }
            }
        }
    }
}