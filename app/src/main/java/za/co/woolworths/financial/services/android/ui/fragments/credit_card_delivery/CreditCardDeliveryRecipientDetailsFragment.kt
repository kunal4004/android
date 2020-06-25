package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.credit_card_delivery_recipient_details_layout.*
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.BookingAddress
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryRecipientDetailsFragment : Fragment(), View.OnClickListener {

    var navController: NavController? = null
    var bundle: Bundle? = null
    private var bookingAddress: BookingAddress = BookingAddress()
    private lateinit var listOfInputFields: List<EditText>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_recipient_details_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.apply {
            Utils.updateStatusBarBackground(this, R.color.white)
            findViewById<AppBarLayout>(R.id.appbar)?.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }

        bundle = arguments?.getBundle("bundle")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        listOfInputFields = listOf(recipientName, cellphoneNumber)
        recipientName?.apply {
            afterTextChanged { clearErrorInputField(this) }
        }
        cellphoneNumber?.apply {
            afterTextChanged { clearErrorInputField(this) }
        }
        confirm?.setOnClickListener(this)
        configureUI()
    }

    fun configureUI() {
        recipientName.setText(SessionUtilities.getInstance().jwt?.name?.get(0))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.confirm -> {
                if (recipientName?.text.toString().trim().isNotEmpty() && cellphoneNumber?.text.toString().trim().isNotEmpty()) {
                    bookingAddress.let {
                        it.nameSurname = recipientName.text.toString().trim()
                        it.telCell = cellphoneNumber.text.toString().trim()
                        it.telWork = alternativeNumber.text.toString().trim()
                    }
                    bundle?.putString("BookingAddress", Utils.toJson(bookingAddress))
                    navController?.navigate(R.id.action_to_creditCardDeliveryAddressDetailsFragment, bundleOf("bundle" to bundle))
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
        editText.setBackgroundResource(R.drawable.otp_box_error_background)
        when (editText.id) {
            R.id.recipientName -> {
                recipientNameErrorMsg.visibility = View.VISIBLE
            }
            R.id.cellphoneNumber -> {
                cellphoneNumberErrorMsg.visibility = View.VISIBLE
            }
        }
    }

    private fun clearErrorInputField(editText: EditText) {
        editText.setBackgroundResource(R.drawable.recipient_details_input_edittext_bg)
        when (editText.id) {
            R.id.recipientName -> {
                recipientNameErrorMsg.visibility = View.GONE
            }
            R.id.cellphoneNumber -> {
                cellphoneNumberErrorMsg.visibility = View.GONE
            }
        }
    }
}