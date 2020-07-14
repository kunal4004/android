package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.credit_card_delivery_recipient_details_layout.*
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.BookingAddress
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.StatusResponse
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryRecipientDetailsFragment : Fragment(), View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    var navController: NavController? = null
    var bundle: Bundle? = null
    private var bookingAddress: BookingAddress = BookingAddress()
    private lateinit var listOfInputFields: List<EditText>
    var statusResponse: StatusResponse? = null
    var isRecipientIsThirdPerson: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_recipient_details_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            statusResponse = Utils.jsonStringToObject(getString("delivery_status_response"), StatusResponse::class.java) as StatusResponse?
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        listOfInputFields = listOf(recipientName, cellphoneNumber, idNumber)
        recipientName?.apply {
            afterTextChanged { clearErrorInputField(this) }
        }
        cellphoneNumber?.apply {
            afterTextChanged { clearErrorInputField(this) }
        }

        idNumber?.apply {
            afterTextChanged { clearErrorInputField(this) }
        }

        recipientOption?.setOnCheckedChangeListener(this)

        if (isThirdPartyRecipientEligible())
            recipientOption?.visibility = View.VISIBLE

        confirm?.setOnClickListener(this)
        configureUI()
    }

    fun configureUI() {
        recipientName?.apply {
            isEnabled = statusResponse?.isCardNew == true
            setText(SessionUtilities.getInstance().jwt?.name?.get(0))
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.confirm -> {
                if (recipientName?.text.toString().trim().isNotEmpty() && cellphoneNumber?.text.toString().trim().isNotEmpty() && if (isRecipientIsThirdPerson) idNumber?.text.toString().trim().isNotEmpty() else true) {
                    bookingAddress.let {
                        it.deliverTo = recipientName?.text.toString().trim()
                        it.telCell = cellphoneNumber?.text.toString().trim()
                        it.telWork = alternativeNumber?.text.toString().trim()
                        it.isThirdPartyRecipient = isRecipientIsThirdPerson
                        if (isRecipientIsThirdPerson)
                            it.idNumber = idNumber?.text.toString().trim()
                    }
                    bundle?.putString("BookingAddress", Utils.toJson(bookingAddress))
                    navController?.navigate(R.id.action_to_updateRecipientDetailsProcessingFragment, bundleOf("bundle" to bundle))
                } else {
                    listOfInputFields.forEach {
                        if (it.text.toString().trim().isEmpty())
                            showErrorInputField(it)
                    }
                }
            }
        }
    }

    private fun showErrorInputField(editText: EditText) {
        if (editText.id == R.id.idNumber && !isRecipientIsThirdPerson)
            return

        editText.setBackgroundResource(R.drawable.otp_box_error_background)
        when (editText.id) {
            R.id.recipientName -> {
                recipientNameErrorMsg.visibility = View.VISIBLE
            }
            R.id.cellphoneNumber -> {
                cellphoneNumberErrorMsg.visibility = View.VISIBLE
            }
            R.id.idNumber -> {
                idNumberErrorMsg.visibility = View.VISIBLE
            }
        }
    }

    private fun clearErrorInputField(editText: EditText) {
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
        }
    }

    private fun isThirdPartyRecipientEligible(): Boolean {
        return !(statusResponse?.isThirdPartyRecipient == true)
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        isRecipientIsThirdPerson = checkedId == R.id.anotherPerson
        idNumberLayout.visibility = if (isRecipientIsThirdPerson) View.VISIBLE else View.GONE
        if (checkedId == R.id.mySelf && idNumberErrorMsg.visibility == View.VISIBLE) {
            clearErrorInputField(idNumber)
        }
    }
}