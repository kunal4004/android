package za.co.woolworths.financial.services.android.checkout.view

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_new_user_recipient_details.*
import kotlinx.android.synthetic.main.checkout_who_is_collecting_fragment.*
import kotlinx.android.synthetic.main.vehicle_details_layout.*
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import java.util.regex.Pattern

/**
 * Created by Kunal Uttarwar on 26/10/21.
 */
class CheckoutWhoIsCollectingFragment : CheckoutAddressManagementBaseFragment(),
    View.OnClickListener {

    private lateinit var listOfInputFields: List<View>

    companion object {
        const val REGEX_VEHICLE_TEXT: String = "^\$|^[a-zA-Z0-9\\s<!>@#\$&().+,-/\\\"']+\$"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.checkout_who_is_collecting_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.confirmDetails -> {
                checkValidationsAndConfirm()
            }
            R.id.taxiText -> {
                onTaxiTypeSelected(taxiText)
            }
            R.id.myVehicleText -> {
                onTaxiTypeSelected(myVehicleText)
            }
        }
    }

    private fun onTaxiTypeSelected(taxiType: TextView) {
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
        if (cellphoneNumberEditText?.text.toString().trim().isNotEmpty()
            && cellphoneNumberEditText?.text.toString().trim().length < 10
        ) {
            showErrorPhoneNumber()
        }

        if (recipientNameEditText?.text.toString().trim()
                .isNotEmpty() && vehicleColourEditText?.text.toString().trim()
                .isNotEmpty() && vehicleModelEditText?.text.toString().trim().isNotEmpty()
        ) {

        } else {
            listOfInputFields.forEach {
                if (it is EditText) {
                    if (it.text.toString().trim().isEmpty())
                        showErrorInputField(it, View.VISIBLE)
                }
            }
        }
    }

    private fun initView() {
        recipientDetailsTitle.text = bindString(R.string.who_is_collecting)
        confirmDetails?.setOnClickListener(this)
        myVehicleText?.setOnClickListener(this)
        taxiText?.setOnClickListener(this)

        recipientNameEditText?.apply {
            afterTextChanged {
                val length = it.length
                if (length > 0 && !Pattern.matches(REGEX_VEHICLE_TEXT, it)) {
                    recipientNameErrorMsg.text = bindString(R.string.special_char_name_error_text)
                    showErrorInputField(this, View.VISIBLE)
                } else if (length == 0) {
                    recipientNameErrorMsg.text = bindString(R.string.recipient_name_error_msg)
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

        listOfInputFields = listOf(
            recipientNameEditText,
            cellphoneNumberEditText,
            vehicleColourEditText,
            vehicleModelEditText
        )
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
        recipientLayoutValue: Int
    ) {
        textView?.visibility = visible
        if (View.VISIBLE == visible) {
            val anim = ObjectAnimator.ofInt(
                collectionDetailsNestedScrollView,
                "scrollY",
                recipientLayoutValue + textView.y.toInt()
            )
            anim.setDuration(300).start()
        }
    }
}